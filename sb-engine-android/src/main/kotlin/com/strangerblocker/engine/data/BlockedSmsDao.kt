// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedSmsDao {

    @Query("SELECT * FROM blocked_sms ORDER BY blockedAtMillis DESC")
    fun observeAll(): Flow<List<BlockedSms>>

    @Insert
    suspend fun insert(sms: BlockedSms)

    @Query("DELETE FROM blocked_sms")
    suspend fun clearAll()

    @Query("DELETE FROM blocked_sms WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM blocked_sms WHERE blockedAtMillis < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("SELECT COUNT(*) FROM blocked_sms WHERE blockedAtMillis >= :since")
    suspend fun countSince(since: Long): Int
}
