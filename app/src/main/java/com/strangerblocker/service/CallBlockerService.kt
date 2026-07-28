package com.strangerblocker.service

import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.data.BlockedCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CallBlockerService : CallScreeningService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(details: Call.Details) {
        val phoneNumber = details.handle?.schemeSpecificPart

        // No caller ID (private/unknown number) → block silently
        if (phoneNumber == null) {
            respondToCall(
                details,
                CallScreeningService.CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .build(),
            )
            return
        }

        if (!isBlockingEnabled()) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
            return
        }

        // Whitelist check first (fastest path)
        if (isWhitelisted(phoneNumber)) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
            return
        }

        val isContact = isNumberInContacts(phoneNumber)

        if (isContact) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
        } else {
            respondToCall(
                details,
                CallScreeningService.CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build(),
            )

            scope.launch {
                val db = (applicationContext as StrangerBlockerApp).db
                db.blockedCallDao().insert(
                    BlockedCall(
                        phoneNumber = phoneNumber,
                        blockedAtMillis = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    private fun isBlockingEnabled(): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)
        return prefs.getBoolean("blocking_enabled", true)
    }

    private fun isWhitelisted(number: String): Boolean {
        return try {
            val db = (applicationContext as StrangerBlockerApp).db
            db.whitelistedNumberDao().isWhitelisted(number)
        } catch (_: Exception) {
            false
        }
    }

    private fun isNumberInContacts(number: String): Boolean {
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number),
            )
            val projection = arrayOf(ContactsContract.PhoneLookup._ID)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                cursor.count > 0
            } ?: false
        } catch (_: SecurityException) {
            false
        }
    }
}
