package com.strangerblocker.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.strangerblocker.MainActivity
import com.strangerblocker.R
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.data.BlockedCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                postBlockedNotification(app)
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
                // Notification is posted after DB save - uses the shared preference
                // checked inside postBlockedNotification()
                postBlockedNotification(app)
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

    private fun isManuallyBlocked(number: String): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)
        return prefs.getStringSet("manual_blocks", emptySet())?.contains(number) == true
    }

    private fun isNotificationsEnabled(): Boolean {
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)
        return prefs.getBoolean("notifications_enabled", true)
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

    private fun postBlockedNotification(app: StrangerBlockerApp) {
        if (!isNotificationsEnabled()) return

        val todayKey = "blocked_today_${todayDate()}"
        val prefs = getSharedPreferences("stranger_blocker", MODE_PRIVATE)

        val count: Int
        if (!prefs.contains(todayKey)) {
            // First notification today: init from DB. The call was already
            // inserted, so dbCount already includes it — no extra increment.
            val todayStart = todayStartMillis()
            val dbCount = runBlocking(Dispatchers.IO) {
                app.db.blockedCallDao().countSince(todayStart)
            }
            prefs.edit().putInt(todayKey, dbCount).apply()
            count = dbCount
        } else {
            // Subsequent calls: increment the running counter.
            count = prefs.getInt(todayKey, 0) + 1
            prefs.edit().putInt(todayKey, count).apply()
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val iconStyle = prefs.getString("notification_icon_style", "shield") ?: "shield"
        val text = count.toString()

        val notification: Notification
        if (iconStyle == "circle_count") {
            val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#10B981")
                style = Paint.Style.FILL
            }.let { canvas.drawCircle(24f, 24f, 24f, it) }
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = 26f
                typeface = Typeface.DEFAULT_BOLD
            }.let { canvas.drawText(text, 24f, 24f + 26f / 3f, it) }
            val icon = Icon.createWithBitmap(bitmap)
            notification = Notification.Builder(this, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle("$text blocked today")
                .setContentText("Stranger Blocker is active")
                .setContentIntent(pendingIntent)
                .build()
        } else {
            notification = NotificationCompat.Builder(this, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("$text blocked today")
                .setContentText("Stranger Blocker is active")
                .setNumber(count)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setSilent(true)
                .build()
        }

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun todayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun todayStartMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
