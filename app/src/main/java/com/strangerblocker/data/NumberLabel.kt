package com.strangerblocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "number_labels")
data class NumberLabel(
    @PrimaryKey val phoneNumber: String,
    val label: String,
    val updatedAtMillis: Long,
)
