package com.strangerblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistedNumber(
    @PrimaryKey val phoneNumber: String,
    val label: String?,
    val addedAtMillis: Long,
)
