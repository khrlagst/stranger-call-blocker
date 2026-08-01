package com.strangerblocker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NumberLabelDao {

    @Query("SELECT * FROM number_labels")
    fun observeAll(): Flow<List<NumberLabel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(label: NumberLabel)
}
