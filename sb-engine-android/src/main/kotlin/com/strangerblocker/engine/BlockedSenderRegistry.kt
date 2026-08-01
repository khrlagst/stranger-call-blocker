// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

/**
 * Records which senders were actually blocked so the notification listener can
 * dismiss only matching notifications (the Android 11+ SMS fallback). Entries
 * expire after [windowMillis].
 */
interface SenderStore {
    fun getStringSet(key: String, default: Set<String>): Set<String>
    fun putStringSet(key: String, value: Set<String>)
}

class BlockedSenderRegistry(
    private val store: SenderStore,
    private val windowMillis: Long = DEFAULT_WINDOW_MS,
) {

    fun record(sender: String, now: Long = System.currentTimeMillis()) {
        val cutoff = now - windowMillis
        val updated = store.getStringSet(KEY, emptySet())
            .filterTo(mutableSetOf()) {
                (it.substringAfterLast('|').toLongOrNull() ?: 0L) > cutoff
            }
        updated.add("$sender|$now")
        store.putStringSet(KEY, updated)
    }

    fun isRecentlyBlocked(sender: String, now: Long = System.currentTimeMillis()): Boolean {
        val norm = NumberRules.normalizeSender(sender)
        if (norm.isEmpty()) return false
        return store.getStringSet(KEY, emptySet()).any {
            val ts = it.substringAfterLast('|').toLongOrNull() ?: 0L
            now - ts < windowMillis && NumberRules.normalizeSender(it.substringBeforeLast('|')) == norm
        }
    }

    private companion object {
        const val KEY = "recent_blocked_sms_senders"
        const val DEFAULT_WINDOW_MS = 10 * 60 * 1000L
    }
}
