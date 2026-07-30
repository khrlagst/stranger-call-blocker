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

    /** Purge entries older than [cutoff] millis (e.g. 30 days). */
    @Query("DELETE FROM blocked_calls WHERE blockedAtMillis < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    /** Snapshot of all entries for export (non-Flow). */
    @Query("SELECT * FROM blocked_calls ORDER BY blockedAtMillis DESC")
    suspend fun getAll(): List<BlockedCall>

    /** Count of calls blocked since [since] millis (used for daily notification). */
    @Query("SELECT COUNT(*) FROM blocked_calls WHERE blockedAtMillis >= :since")
    suspend fun countSince(since: Long): Int

    /** Count of calls blocked on each of the last 7 days. */
    @Query("""
        SELECT CAST(blockedAtMillis / 86400000 AS INTEGER) AS dayBucket, COUNT(*) AS cnt
        FROM blocked_calls WHERE blockedAtMillis >= :sevenDaysAgo
        GROUP BY dayBucket ORDER BY dayBucket ASC
    """)
    suspend fun dailyCountsLast7(sevenDaysAgo: Long): List<DayCount>
}

data class DayCount(val dayBucket: Long, val cnt: Int)
