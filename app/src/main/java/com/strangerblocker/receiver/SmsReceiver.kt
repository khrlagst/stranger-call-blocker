package com.strangerblocker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.strangerblocker.StrangerBlockerApp

/**
 * Thin adapter: all SMS screening logic lives in [com.strangerblocker.engine.SbEngine].
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages: List<SmsMessage> = Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()
        val sender = messages.firstOrNull()?.originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody ?: "" }

        val engine = (context.applicationContext as StrangerBlockerApp).engine
        val reason = engine.smsBlockReason(sender, body) ?: return

        // Abort the broadcast NOW — synchronously inside onReceive(), or it
        // is silently ignored.
        abortBroadcast()
        engine.recordBlockedSms(sender, body, reason)
    }
}
