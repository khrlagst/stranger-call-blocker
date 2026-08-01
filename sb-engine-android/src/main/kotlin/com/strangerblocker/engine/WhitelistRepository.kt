// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import com.strangerblocker.engine.data.AppDatabase
import com.strangerblocker.engine.data.WhitelistedNumber
import kotlinx.coroutines.flow.Flow

/** Shared calls/SMS whitelist access. */
class WhitelistRepository(private val db: AppDatabase) {

    fun observeAll(): Flow<List<WhitelistedNumber>> = db.whitelistedNumberDao().observeAll()

    suspend fun isWhitelisted(number: String): Boolean =
        db.whitelistedNumberDao().isWhitelisted(number)

    suspend fun add(number: String, label: String?) =
        db.whitelistedNumberDao().insert(
            WhitelistedNumber(number, label, System.currentTimeMillis()),
        )

    suspend fun remove(number: String) = db.whitelistedNumberDao().delete(number)
}
