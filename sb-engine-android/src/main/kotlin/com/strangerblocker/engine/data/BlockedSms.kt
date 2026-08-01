// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_sms")
data class BlockedSms(
    val senderNumber: String,
    val messageBody: String,
    val blockedAtMillis: Long,
    val blockReason: String = "SENDER",
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
