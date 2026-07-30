package com.strangerblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_sms")
data class BlockedSms(
    val senderNumber: String,
    val messageBody: String,
    val blockedAtMillis: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
