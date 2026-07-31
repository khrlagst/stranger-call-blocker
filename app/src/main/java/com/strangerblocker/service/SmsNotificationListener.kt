package com.strangerblocker.service

import android.app.Notification
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.strangerblocker.StrangerBlockerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Fallback SMS blocker for Android 11+ where abortBroadcast() is ignored
 * for non-default SMS apps. Detects SMS notifications from messaging apps
 * and dismisses them when the sender is not whitelisted or in contacts.
 */
class SmsNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val notif = sbn.notification ?: return
        if (notif.category != Notification.CATEGORY_MESSAGE) return

        val prefs = getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("sms_blocking_enabled", true)) return

        val sender = extractSender(notif) ?: return
        val db = (applicationContext as StrangerBlockerApp).db

        val isWhitelisted = try {
            runBlocking(Dispatchers.IO) {
                db.whitelistedNumberDao().isWhitelisted(sender)
            }
        } catch (_: Exception) {
            false
        }

        if (!isWhitelisted && !isContact(sender)) {
            cancelNotification(sbn.key)
        }
    }

    private fun extractSender(notif: Notification): String? {
        // SMS notifications typically have the sender as title
        return notif.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
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
}
