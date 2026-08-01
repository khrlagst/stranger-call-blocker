// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "number_labels")
data class NumberLabel(
    @PrimaryKey val phoneNumber: String,
    val label: String,
    val updatedAtMillis: Long,
)
