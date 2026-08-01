package com.strangerblocker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Telephony
import android.telephony.SmsMessage
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.data.BlockedSms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("sms_blocking_enabled", false)) return

        val messages: List<SmsMessage> = Telephony.Sms.Intents.getMessagesFromIntent(intent).toList()

        val firstMsg = messages.firstOrNull() ?: return
        val sender = firstMsg.originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody ?: "" }

        val db = (context.applicationContext as StrangerBlockerApp).db

        // ── SYNCHRONOUS check + abort ──
        // abortBroadcast() must be called before onReceive() returns, or it
        // is silently ignored. The whitelist query is fast (single row lookup).
        val isWhitelisted = try {
            runBlocking(Dispatchers.IO) {
                db.whitelistedNumberDao().isWhitelisted(sender)
            }
        } catch (_: Exception) {
            false
        }

        val isManuallyBlocked = prefs.getStringSet("manual_blocks", emptySet())?.contains(sender) == true
        val keywords = prefs.getStringSet("sms_keywords", emptySet())?.toList() ?: emptyList()
        val matchedKeyword = keywords.firstOrNull { body.contains(it, ignoreCase = true) }
        val reason = when {
            matchedKeyword != null -> "KEYWORD:$matchedKeyword"
            else -> "SENDER"
        }

        if (isManuallyBlocked || (!isWhitelisted && !isContact(context, sender)) || matchedKeyword != null) {
            // Abort the broadcast NOW — synchronously inside onReceive().
            abortBroadcast()

            // Record the blocked sender so the notification listener can
            // dismiss this message's notification (Android 11+ fallback).
            recordBlockedSender(prefs, sender)

            // Persist to DB in the background AFTER abort.
            scope.launch {
                try {
                    db.blockedSmsDao().insert(
                        BlockedSms(
                            senderNumber = sender,
                            messageBody = body,
                            blockedAtMillis = System.currentTimeMillis(),
                            blockReason = reason,
                        )
                    )
                } catch (_: Exception) {
                    // silent
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

    /** Remember a blocked sender (with timestamp) so the notification listener can dismiss its notification. */
    private fun recordBlockedSender(prefs: SharedPreferences, sender: String) {
        val now = System.currentTimeMillis()
        val cutoff = now - 10 * 60_000
        val updated = (prefs.getStringSet("recent_blocked_sms_senders", emptySet()) ?: emptySet())
            .filterTo(mutableSetOf()) {
                (it.substringAfterLast('|').toLongOrNull() ?: 0L) > cutoff
            }
        updated.add("$sender|$now")
        prefs.edit().putStringSet("recent_blocked_sms_senders", updated).apply()
    }
}
