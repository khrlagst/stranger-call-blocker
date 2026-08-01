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
import kotlinx.coroutines.runBlocking

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

        if (!isBlockingEnabled() || isPaused()) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
            return
        }

        // VoIP/unknown handle (e.g. "WhatsApp Call" without a number) — number
        // rules can't apply, so the user decides via the "messaging apps" toggle.
        if (!isPhoneNumberShape(phoneNumber)) {
            if (blockVoipCalls()) {
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
                    val app = applicationContext as StrangerBlockerApp
                    app.db.blockedCallDao().insert(
                        BlockedCall(
                            phoneNumber = phoneNumber,
                            blockedAtMillis = System.currentTimeMillis(),
                        )
                    )
                    BlockedNotification.post(app, app.db)
                }
            } else {
                respondToCall(details, CallScreeningService.CallResponse.Builder().build())
            }
            return
        }

        // Whitelist check first (fastest path)
        if (isWhitelisted(phoneNumber)) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
            return
        }

        // Manual block overrides contacts — user explicitly blocked this number
        if (isManuallyBlocked(phoneNumber)) {
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
                val app = applicationContext as StrangerBlockerApp
                app.db.blockedCallDao().insert(
                    BlockedCall(
                        phoneNumber = phoneNumber,
                        blockedAtMillis = System.currentTimeMillis(),
                    )
                )
                BlockedNotification.post(app, app.db)
            }
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
                val app = applicationContext as StrangerBlockerApp
                app.db.blockedCallDao().insert(
                    BlockedCall(
                        phoneNumber = phoneNumber,
                        blockedAtMillis = System.currentTimeMillis(),
                    )
                )
                BlockedNotification.post(app, app.db)
            }
        }
    }

    private fun isBlockingEnabled(): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)
        return prefs.getBoolean("blocking_enabled", true)
    }

    private fun isPaused(): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)
        return prefs.getLong("blocking_paused_until", 0L) > System.currentTimeMillis()
    }

    /** True when the handle carries an actual phone number (7–15 digits). */
    private fun isPhoneNumberShape(number: String): Boolean {
        val digits = number.count { it.isDigit() }
        return digits in 7..15
    }

    private fun blockVoipCalls(): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)
        return prefs.getBoolean("block_voip_calls", false)
    }

    private fun isManuallyBlocked(number: String): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)
        return prefs.getStringSet("manual_blocks", emptySet())?.contains(number) == true
    }

    private fun isWhitelisted(number: String): Boolean {
        return try {
            val db = (applicationContext as StrangerBlockerApp).db
            runBlocking(Dispatchers.IO) {
                db.whitelistedNumberDao().isWhitelisted(number)
            }
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
