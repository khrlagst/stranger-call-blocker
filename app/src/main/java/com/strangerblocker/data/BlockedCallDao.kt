package com.strangerblocker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedCallDao {

    /** Observe all blocked calls newest-first. */
    @Query("SELECT * FROM blocked_calls ORDER BY blockedAtMillis DESC")
    fun observeAll(): Flow<List<BlockedCall>>

    /** Persist a newly blocked call. */
    @Insert
    suspend fun insert(call: BlockedCall)

    /** Delete all history. */
    @Query("DELETE FROM blocked_calls")
    suspend fun clearAll()
}
