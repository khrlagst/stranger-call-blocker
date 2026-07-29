package com.strangerblocker.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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

        if (!isBlockingEnabled()) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
            return
        }

        // Whitelist check first (fastest path)
        if (isWhitelisted(phoneNumber)) {
            respondToCall(details, CallScreeningService.CallResponse.Builder().build())
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

        // If this is the first notification today, init counter from DB so
        // the count reflects the entire day's blocking, not just since the
        // notification became active.
        if (!prefs.contains(todayKey)) {
            val todayStart = todayStartMillis()
            val dbCount = runBlocking(Dispatchers.IO) {
                app.db.blockedCallDao().countSince(todayStart)
            }
            prefs.edit().putInt(todayKey, dbCount).apply()
        }

        val count = prefs.getInt(todayKey, 0)
        prefs.edit().putInt(todayKey, count + 1).apply()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val iconStyle = prefs.getString("notification_icon_style", "shield") ?: "shield"

        val builder = NotificationCompat.Builder(this, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${count + 1} blocked today")
            .setContentText("Stranger Blocker is active")
            .setNumber(count + 1)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setSilent(true)

        if (iconStyle == "circle_count") {
            val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#10B981")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(48f, 48f, 48f, circlePaint)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = 56f
                typeface = Typeface.DEFAULT_BOLD
            }
            val text = (count + 1).toString()
            canvas.drawText(text, 48f, 48f + textPaint.textSize / 3f, textPaint)
            builder.setLargeIcon(bitmap)
        }

        val notification = builder.build()

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
