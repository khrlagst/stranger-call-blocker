package com.strangerblocker.service

import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Fallback SMS blocker for Android 11+ where abortBroadcast() is ignored
 * for non-default SMS apps. Dismisses the notification for messages whose
 * sender was just blocked by [com.strangerblocker.receiver.SmsReceiver].
 */
class SmsNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notif = sbn.notification ?: return

        val prefs = getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("sms_blocking_enabled", false)) return

        // Only consider SMS-ish notifications: category MESSAGE, or any
        // notification from the default SMS app (covers OEMs that skip the category).
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(this)
        val fromDefaultSmsApp = defaultSms != null && sbn.packageName == defaultSms
        if (notif.category != Notification.CATEGORY_MESSAGE && !fromDefaultSmsApp) return

        val sender = extractSender(notif) ?: return
        if (isRecentlyBlocked(prefs, sender)) {
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

    private fun normalizeSender(s: String): String = s.filter { it.isLetterOrDigit() }

    companion object {
        private const val RECENT_WINDOW_MS = 10 * 60 * 1000L
    }
}
