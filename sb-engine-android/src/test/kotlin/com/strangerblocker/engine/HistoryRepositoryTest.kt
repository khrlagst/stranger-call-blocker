// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.strangerblocker.engine.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HistoryRepositoryTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `records calls and sms and counts both for today`() = runBlocking {
        val repo = HistoryRepository(db)
        repo.recordCall("+6285592679948")
        repo.recordSms("+622130179723", "promo", "SENDER")

        val (calls, sms) = repo.todayCounts()
        assertEquals(1, calls)
        assertEquals(1, sms)

        val (callsAfter, smsAfter) = repo.todayCounts()
        assertEquals(1, callsAfter)
        assertEquals(1, smsAfter)
    }

    @Test
    fun `deletes and clears history by channel`() = runBlocking {
        val repo = HistoryRepository(db)
        repo.recordCall("+6285592679948")
        repo.recordSms("+622130179723", "hi", "SENDER")

        val callId = repo.observeCalls().first().first().id
        repo.deleteCalls(listOf(callId))
        assertEquals(0, repo.observeCalls().first().size)

        repo.clearSms()
        assertEquals(0, repo.observeSms().first().size)
    }

    @Test
    fun `whitelist add check and remove`() = runBlocking {
        val wl = WhitelistRepository(db)
        assertFalse(wl.isWhitelisted("+6281234567890"))
        wl.add("+6281234567890", "Bank")
        assertTrue(wl.isWhitelisted("+6281234567890"))
        wl.remove("+6281234567890")
        assertFalse(wl.isWhitelisted("+6281234567890"))
    }

    @Test
    fun `labels upsert by number`() = runBlocking {
        val lr = LabelRepository(db)
        lr.set("+6281234567890", "SCAM")
        lr.set("+6281234567890", "SPAM")
        val labels = lr.observeAll().first()
        assertEquals(1, labels.size)
        assertEquals("SPAM", labels[0].label)
    }
}
