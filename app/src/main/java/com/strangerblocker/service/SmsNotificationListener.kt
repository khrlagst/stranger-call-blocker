package com.strangerblocker.service

import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.strangerblocker.StrangerBlockerApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Fallback blocker for Android 11+ where abortBroadcast() is ignored for
 * non-default SMS apps:
 * - SMS: dismisses notifications whose sender was just blocked by SmsReceiver.
 * - Other messaging apps (WhatsApp etc.): dismisses unknown-sender notifications
 *   when the "silence messaging apps" setting is on.
 */
class SmsNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notif = sbn.notification ?: return

        val prefs = getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(this)
        val fromDefaultSmsApp = defaultSms != null && sbn.packageName == defaultSms

        // Only consider SMS-ish notifications: category MESSAGE, or anything
        // from the default SMS app (covers OEMs that skip the category).
        if (notif.category != Notification.CATEGORY_MESSAGE && !fromDefaultSmsApp) return

        val sender = extractSender(notif) ?: return

        if (fromDefaultSmsApp) {
            // SMS fallback — dismiss only messages this app actually blocked.
            if (prefs.getBoolean("sms_blocking_enabled", false) && isRecentlyBlocked(prefs, sender)) {
                cancelNotification(sbn.key)
            }
        } else if (prefs.getBoolean("silence_messaging_apps", false) && isUnknownSender(sender)) {
            // Messaging apps (WhatsApp etc.) — cancel unknown senders.
            cancelNotification(sbn.key)
        }
    }

    private fun extractSender(notif: Notification): String? {
        val title = notif.extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: notif.extras.getCharSequence(Notification.EXTRA_TITLE_BIG)
            ?: return null
        return title.toString().trim()
    }

    private fun isRecentlyBlocked(prefs: SharedPreferences, sender: String): Boolean {
        val norm = normalizeSender(sender)
        if (norm.isEmpty()) return false
        val now = System.currentTimeMillis()
        val set = prefs.getStringSet("recent_blocked_sms_senders", emptySet()) ?: return false
        return set.any {
            val ts = it.substringAfterLast('|').toLongOrNull() ?: 0L
            now - ts < RECENT_WINDOW_MS && normalizeSender(it.substringBeforeLast('|')) == norm
        }
    }

    private fun isUnknownSender(sender: String): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)
        if (prefs.getStringSet("manual_blocks", emptySet())?.contains(sender) == true) return true
        // Only apply number rules when the title actually carries a phone number;
        // contact-name titles are left alone.
        val digits = sender.count { it.isDigit() }
        if (digits !in 7..15) return false

        val db = (applicationContext as StrangerBlockerApp).db
        val isWhitelisted = try {
            runBlocking(Dispatchers.IO) {
                db.whitelistedNumberDao().isWhitelisted(sender)
            }
        } catch (_: Exception) {
            false
        }
        if (isWhitelisted) return false
        return !isContact(sender)
    }

    private fun isContact(number: String): Boolean {
        return try {
            val uri = android.net.Uri.withAppendedPath(
                android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(number),
            )
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use { it.count > 0 } ?: false
        } catch (_: Exception) {
            false
        }
    }

    private fun normalizeSender(s: String): String = s.filter { it.isLetterOrDigit() }

    companion object {
        private const val RECENT_WINDOW_MS = 10 * 60 * 1000L
    }
}
