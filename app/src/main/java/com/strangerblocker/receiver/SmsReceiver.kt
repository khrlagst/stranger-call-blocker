package com.strangerblocker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.strangerblocker.StrangerBlockerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Thin adapter: all SMS screening logic lives in [com.strangerblocker.engine.SbEngine].
 * The decision runs asynchronously via [goAsync] so the main thread is never
 * blocked by the whitelist/contacts lookups (ANR safety); abortBroadcast still
 * happens before the pending result is finished.
 */
class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages: List<SmsMessage> = Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()
        val sender = messages.firstOrNull()?.originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody ?: "" }

        val engine = (context.applicationContext as StrangerBlockerApp).engine
        val pendingResult = goAsync()
        scope.launch {
            try {
                val reason = engine.smsBlockReason(sender, body)
                if (reason != null) {
                    pendingResult.abortBroadcast()
                    engine.recordBlockedSms(sender, body, reason)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
