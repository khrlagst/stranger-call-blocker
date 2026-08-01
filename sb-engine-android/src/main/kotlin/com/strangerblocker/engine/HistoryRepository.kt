// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import com.strangerblocker.engine.data.AppDatabase
import com.strangerblocker.engine.data.BlockedCall
import com.strangerblocker.engine.data.BlockedSms
import java.util.Calendar
import kotlinx.coroutines.flow.Flow

/** Blocked-call and blocked-SMS history access. */
class HistoryRepository(private val db: AppDatabase) {

    fun observeCalls(): Flow<List<BlockedCall>> = db.blockedCallDao().observeAll()

    fun observeSms(): Flow<List<BlockedSms>> = db.blockedSmsDao().observeAll()

    suspend fun recordCall(number: String) =
        db.blockedCallDao().insert(BlockedCall(number, System.currentTimeMillis()))

    suspend fun recordSms(sender: String, body: String, reason: String) =
        db.blockedSmsDao().insert(
            BlockedSms(sender, body, System.currentTimeMillis(), reason),
        )

    suspend fun deleteCalls(ids: List<Long>) = db.blockedCallDao().deleteByIds(ids)

    suspend fun deleteSms(ids: List<Long>) = db.blockedSmsDao().deleteByIds(ids)

    suspend fun clearCalls() = db.blockedCallDao().clearAll()

    suspend fun clearSms() = db.blockedSmsDao().clearAll()

    suspend fun allCalls(): List<BlockedCall> = db.blockedCallDao().getAll()

    /** (calls, sms) blocked since local midnight. */
    suspend fun todayCounts(): Pair<Int, Int> {
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return db.blockedCallDao().countSince(midnight) to db.blockedSmsDao().countSince(midnight)
    }
}
