// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockedSenderRegistryTest {

    private class FakeStore : SenderStore {
        val map = mutableMapOf<String, Set<String>>()
        override fun getStringSet(key: String, default: Set<String>): Set<String> =
            map[key] ?: default
        override fun putStringSet(key: String, value: Set<String>) {
            map[key] = value
        }
    }

    @Test
    fun `records a sender and matches it`() {
        val store = FakeStore()
        val registry = BlockedSenderRegistry(store)
        val now = 1_000_000L
        registry.record("+6285592679948", now)
        assertTrue(registry.isRecentlyBlocked("+62 855 9267 9948", now + 1_000)) // normalized match
    }

    @Test
    fun `expires entries after the window`() {
        val store = FakeStore()
        val registry = BlockedSenderRegistry(store, windowMillis = 10_000)
        val now = 1_000_000L
        registry.record("+6285592679948", now)
        assertFalse(registry.isRecentlyBlocked("+6285592679948", now + 10_001))
    }

    @Test
    fun `prunes stale entries when recording`() {
        val store = FakeStore()
        val registry = BlockedSenderRegistry(store, windowMillis = 10_000)
        val now = 1_000_000L
        registry.record("+6285592679948", now)
        registry.record("+622135523726", now + 15_000) // older entry should be pruned
        assertFalse(registry.isRecentlyBlocked("+6285592679948", now + 15_000))
        assertTrue(registry.isRecentlyBlocked("+622135523726", now + 15_000))
    }

    @Test
    fun `ignores empty or non-sender titles`() {
        val store = FakeStore()
        val registry = BlockedSenderRegistry(store)
        registry.record("TELKOMSEL", 1_000_000L)
        assertTrue(registry.isRecentlyBlocked("TELKOMSEL", 1_000_001L))
        assertFalse(registry.isRecentlyBlocked("", 1_000_001L))
        assertFalse(registry.isRecentlyBlocked("...", 1_000_001L)) // punctuation only → normalized empty
    }
}
