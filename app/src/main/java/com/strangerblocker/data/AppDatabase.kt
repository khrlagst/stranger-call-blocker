package com.strangerblocker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BlockedCall::class, WhitelistedNumber::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun whitelistedNumberDao(): WhitelistedNumberDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS whitelist (
                        phoneNumber TEXT PRIMARY KEY,
                        label TEXT,
                        addedAtMillis INTEGER NOT NULL
                    )"""
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "stranger_blocker.db",
                    )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
