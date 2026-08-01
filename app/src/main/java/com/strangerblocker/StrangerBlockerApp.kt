package com.strangerblocker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.strangerblocker.data.AppDatabase

class StrangerBlockerApp : Application() {

    val db: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Block alerts",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows how many calls and SMS were blocked today"
            setShowBadge(true)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "stranger_blocker_alerts"
    }
}
