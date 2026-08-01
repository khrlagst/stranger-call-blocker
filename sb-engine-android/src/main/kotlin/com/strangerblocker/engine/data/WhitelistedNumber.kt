// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistedNumber(
    @PrimaryKey val phoneNumber: String,
    val label: String?,
    val addedAtMillis: Long,
)
