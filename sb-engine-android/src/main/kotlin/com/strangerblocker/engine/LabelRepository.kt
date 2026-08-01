// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import com.strangerblocker.engine.data.AppDatabase
import com.strangerblocker.engine.data.NumberLabel
import kotlinx.coroutines.flow.Flow

/** Local spam-label access (reports never leave the device). */
class LabelRepository(private val db: AppDatabase) {

    fun observeAll(): Flow<List<NumberLabel>> = db.numberLabelDao().observeAll()

    suspend fun set(number: String, label: String) =
        db.numberLabelDao().upsert(NumberLabel(number, label, System.currentTimeMillis()))
}
