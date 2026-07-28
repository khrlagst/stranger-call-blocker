package com.strangerblocker.service

import android.content.ContentResolver
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

/**
 * Telecom CallScreeningService that silently rejects incoming calls
 * from numbers NOT in the device's contacts list.
 *
 * Must be declared in AndroidManifest.xml with
 * `android.permission.BIND_SCREENING_SERVICE`.
 */
class CallBlockerService : CallScreeningService() {

    /** App-scoped coroutine scope for DB writes. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(details: Call.Details) {
        val phoneNumber = details.handle?.schemeSpecificPart ?: return

        if (!isBlockingEnabled()) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
            return
        }

        val isContact = isNumberInContacts(phoneNumber)

        if (isContact) {
            // Let contact calls ring through
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
        } else {
            // Silently reject the call
            respondToCall(
                details,
                CallScreeningService.CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build(),
            )

            // Persist to block history (number + timestamp only — no contact names)
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

    /** Check SharedPreferences for the blocking toggle state. */
    private fun isBlockingEnabled(): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)
        return prefs.getBoolean("blocking_enabled", true) // default ON
    }

    /**
     * Query ContactsContract to determine if [number] belongs to a
     * known contact. Returns true if at least one matching contact row
     * exists.
     */
    private fun isNumberInContacts(number: String): Boolean {
        val resolvedNumber = Uri.encode(number)
        val uri = Uri.parse(
            "content://com.android.contacts/data/phones/filter/$resolvedNumber"
        )
        return queryHasRows(contentResolver, uri)
    }

    /** Simple check if a content URI query returns at least one row. */
    private fun queryHasRows(cr: ContentResolver, uri: Uri): Boolean {
        // Projection with one column for minimal overhead
        val projection = arrayOf(ContactsContract.Data._ID)
        cr.query(uri, projection, null, null, null)?.use { cursor ->
            return cursor.count > 0
        }
        return false
    }
}
