// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BlockedCall::class, WhitelistedNumber::class, BlockedSms::class, NumberLabel::class],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun whitelistedNumberDao(): WhitelistedNumberDao
    abstract fun blockedSmsDao(): BlockedSmsDao
    abstract fun numberLabelDao(): NumberLabelDao

    companion object {
        // One instance per database name so multiple engines in a single
        // process keep isolated data (multi-tenant / white-label hosts).
        private val INSTANCES = mutableMapOf<String, AppDatabase>()

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

        fun getInstance(context: Context, name: String = "stranger_blocker.db"): AppDatabase {
            val appContext = context.applicationContext
            return synchronized(INSTANCES) {
                INSTANCES.getOrPut(name) {
                    Room
                        .databaseBuilder(
                            appContext,
                            AppDatabase::class.java,
                            name,
                        )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                        .build()
                }
            }
        }
    }
}
