package com.strangerblocker.service

import android.app.Notification
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.strangerblocker.StrangerBlockerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Thin adapter: notification-dismissal decisions live in
 * [com.strangerblocker.engine.SbEngine]. The decision runs on the IO dispatcher
 * (the listener callback runs on the main thread; the engine's whitelist and
 * contacts lookups must not block it).
 */
class SmsNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notif = sbn.notification ?: return

        val defaultSms = Telephony.Sms.getDefaultSmsPackage(this)
        val fromDefaultSmsApp = defaultSms != null && sbn.packageName == defaultSms

        // Only consider SMS-ish notifications: category MESSAGE, or anything
        // from the default SMS app (covers OEMs that skip the category).
        if (notif.category != Notification.CATEGORY_MESSAGE && !fromDefaultSmsApp) return

        val title = notif.extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: notif.extras.getCharSequence(Notification.EXTRA_TITLE_BIG)

        val engine = (applicationContext as StrangerBlockerApp).engine
        scope.launch {
            if (engine.shouldDismissNotification(title?.toString(), fromDefaultSmsApp)) {
                cancelNotification(sbn.key)
            }
        }
    }
}
