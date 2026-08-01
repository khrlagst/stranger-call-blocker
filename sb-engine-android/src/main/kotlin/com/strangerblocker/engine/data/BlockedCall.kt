// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_calls")
data class BlockedCall(
    /** Phone number of the blocked caller (raw from telecom). */
    val phoneNumber: String,
    /** Epoch millis when the call was blocked. */
    val blockedAtMillis: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
