package com.strangerblocker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistedNumberDao {

    @Query("SELECT * FROM whitelist ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<WhitelistedNumber>>

    @Query("SELECT EXISTS(SELECT 1 FROM whitelist WHERE phoneNumber = :number)")
    suspend fun isWhitelisted(number: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WhitelistedNumber)

    @Query("DELETE FROM whitelist WHERE phoneNumber = :number")
    suspend fun delete(number: String)
}
