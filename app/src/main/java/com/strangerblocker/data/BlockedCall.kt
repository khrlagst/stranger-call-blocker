package com.strangerblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_calls")
data class BlockedCall(
    /** Phone number of the blocked caller (raw from telecom). */
    val phoneNumber: String,
    /** Display name resolved from Contacts, or null if unknown. */
    val callerName: String?,
    /** Epoch millis when the call was blocked. */
    val blockedAtMillis: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
