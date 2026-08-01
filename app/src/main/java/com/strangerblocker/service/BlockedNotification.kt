package com.strangerblocker.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.strangerblocker.MainActivity
import com.strangerblocker.R
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/**
 * Posts the daily blocked-count notification. Count is always derived from
 * the DB (calls + SMS) so the badge can never drift from reality.
 */
object BlockedNotification {

    private const val NOTIFICATION_ID = 1001

    fun post(context: Context, db: AppDatabase) {
        val prefs = context.getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return

        val todayStart = run {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        val count = runBlocking(Dispatchers.IO) {
            db.blockedCallDao().countSince(todayStart) + db.blockedSmsDao().countSince(todayStart)
        }
        val text = count.toString()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val iconStyle = prefs.getString("notification_icon_style", "shield") ?: "shield"
        val notification: Notification =
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
                Notification.Builder(context, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(Icon.createWithBitmap(bitmap))
                    .setContentTitle("$text blocked today")
                    .setContentText("Stranger Blocker is active")
                    .setContentIntent(pendingIntent)
                    .build()
            } else {
                NotificationCompat.Builder(context, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("$text blocked today")
                    .setContentText("Stranger Blocker is active")
                    .setNumber(count)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setSilent(true)
                    .build()
            }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
