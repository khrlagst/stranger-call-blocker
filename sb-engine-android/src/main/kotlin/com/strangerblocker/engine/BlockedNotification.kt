// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

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
import com.strangerblocker.engine.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/** Host-provided pieces for the daily blocked-count notification. */
class NotificationConfig(
    val smallIconRes: Int,
    val channelId: String,
)

/**
 * Posts the daily blocked-count notification. Count is always derived from
 * the DB (calls + SMS) so the badge can never drift from reality. The launch
 * intent is resolved from the host package so the engine has no app coupling.
 */
object BlockedNotification {

    private const val NOTIFICATION_ID = 1001

    fun post(context: Context, db: AppDatabase, config: NotificationConfig, prefsName: String) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
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

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        } else null

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
                Notification.Builder(context, config.channelId)
                    .setSmallIcon(Icon.createWithBitmap(bitmap))
                    .setContentTitle("$text blocked today")
                    .setContentText("Stranger Blocker is active")
                    .setContentIntent(pendingIntent)
                    .build()
            } else {
                NotificationCompat.Builder(context, config.channelId)
                    .setSmallIcon(config.smallIconRes)
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
