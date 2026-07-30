package com.strangerblocker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.data.BlockedSms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("sms_blocking_enabled", true)) return

        val messages: List<SmsMessage> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } else {
            @Suppress("DEPRECATION")
            val pdus = intent.extras?.get("pdus") as? Array<*> ?: return
            pdus.mapNotNull { pdu ->
                @Suppress("DEPRECATION")
                SmsMessage.createFromPdu(pdu as? ByteArray ?: return@mapNotNull null)
            }
        }

        val firstMsg = messages.firstOrNull() ?: return
        val sender = firstMsg.originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody ?: "" }

        val db = (context.applicationContext as StrangerBlockerApp).db
        scope.launch {
            val isWhitelisted = try {
                db.whitelistedNumberDao().isWhitelisted(sender)
            } catch (_: Exception) { false }

            if (!isWhitelisted && !isContact(context, sender)) {
                db.blockedSmsDao().insert(
                    BlockedSms(
                        senderNumber = sender,
                        messageBody = body,
                        blockedAtMillis = System.currentTimeMillis(),
                    )
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    abortBroadcast()
                }
            }
        }
    }

    private fun isContact(context: Context, number: String): Boolean {
        return try {
            val uri = android.net.Uri.withAppendedPath(
                android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(number),
            )
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { it.count > 0 } ?: false
        } catch (_: Exception) {
            false
        }
    }
}
