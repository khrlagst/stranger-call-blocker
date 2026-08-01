package com.strangerblocker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BlockedCall::class, WhitelistedNumber::class, BlockedSms::class, NumberLabel::class],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun whitelistedNumberDao(): WhitelistedNumberDao
    abstract fun blockedSmsDao(): BlockedSmsDao
    abstract fun numberLabelDao(): NumberLabelDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS blocked_sms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        senderNumber TEXT NOT NULL,
                        messageBody TEXT NOT NULL,
                        blockedAtMillis INTEGER NOT NULL
                    )"""
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE blocked_sms ADD COLUMN blockReason TEXT NOT NULL DEFAULT 'SENDER'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS number_labels (
                        phoneNumber TEXT PRIMARY KEY NOT NULL,
                        label TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
