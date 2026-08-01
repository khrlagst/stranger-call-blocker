// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.strangerblocker.engine.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Host-provided settings for [SbEngine]. */
class EngineConfig(
    val prefsName: String,
    /** Blocked-call and blocked-SMS history older than this many days is pruned. */
    val retentionDays: Int = 30,
)

/**
 * The on-device spam-blocking engine. Hosts wire their framework classes
 * (CallScreeningService, SMS receiver, notification listener) to these pure
 * decision/record methods; all logic, storage and rules live here — no network.
 */
class SbEngine(
    context: Context,
    config: EngineConfig,
    private val notificationConfig: NotificationConfig,
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(config.prefsName, Context.MODE_PRIVATE)
    private val prefsName = config.prefsName

    // The database filename derives from the prefs name so multiple engines in
    // one process keep fully isolated data (multi-tenant / white-label hosts).
    val db: AppDatabase = AppDatabase.getInstance(appContext, "${config.prefsName}.db")

    // Public data API — the raw DAOs stay accessible via [db].
    val history = HistoryRepository(db)
    val whitelist = WhitelistRepository(db)
    val labels = LabelRepository(db)

    private val registry = BlockedSenderRegistry(PrefsSenderStore(prefs))
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        if (config.retentionDays > 0) {
            scope.launch {
                val cutoff = System.currentTimeMillis() - config.retentionDays * 86_400_000L
                db.blockedCallDao().deleteOlderThan(cutoff)
                db.blockedSmsDao().deleteOlderThan(cutoff)
            }
        }
    }

    // ── Call screening ──

    fun shouldBlockCall(number: String?): CallDecision {
        // The enabled/paused gate comes first: disabled means EVERYTHING
        // rings through, including private/unknown-number calls.
        if (!isBlockingEnabled() || isPaused()) return CallDecision.ALLOW
        if (number == null) return CallDecision.BLOCK_PRIVATE
        if (!NumberRules.isPhoneNumberShape(number)) {
            return if (blockVoipCalls()) CallDecision.BLOCK_VOIP else CallDecision.ALLOW
        }
        if (isWhitelisted(number)) return CallDecision.ALLOW
        if (isManuallyBlocked(number)) return CallDecision.BLOCK
        if (isContact(number)) return CallDecision.ALLOW
        return CallDecision.BLOCK
    }

    /** Persists a blocked call and refreshes the daily count notification. A null number
     *  (private/unknown call) is recorded under [NumberRules.PRIVATE_NUMBER_LABEL]. */
    fun recordBlockedCall(number: String?) {
        scope.launch {
            history.recordCall(number ?: NumberRules.PRIVATE_NUMBER_LABEL)
            postBlockedCountNotification()
        }
    }

    // ── SMS screening ──

    /**
     * Returns the block reason (persistable) when the SMS should be blocked,
     * or null when it should be delivered. Order matters: manual block and
     * keyword matches override the whitelist, exactly like the broadcast path.
     */
    fun smsBlockReason(sender: String, body: String): String? {
        if (!smsBlockingEnabled()) return null
        val keywords = prefs.getStringSet("sms_keywords", emptySet())?.toList() ?: emptyList()
        val matchedKeyword = keywords.firstOrNull { body.contains(it, ignoreCase = true) }
        val reason = if (matchedKeyword != null) "KEYWORD:$matchedKeyword" else "SENDER"

        if (isManuallyBlocked(sender)) return reason
        if (matchedKeyword != null) return reason
        if (isWhitelisted(sender)) return null
        return if (isContact(sender)) null else reason
    }

    /** Records the blocked sender for notification dismissal, persists, refreshes the count. */
    fun recordBlockedSms(sender: String, body: String, reason: String) {
        registry.record(sender)
        scope.launch {
            history.recordSms(sender, body, reason)
            postBlockedCountNotification()
        }
    }

    // ── Notification dismissal (Android 11+ fallback) ──

    fun shouldDismissNotification(title: String?, fromDefaultSmsApp: Boolean): Boolean {
        val sender = title?.trim() ?: return false
        if (fromDefaultSmsApp) {
            return smsBlockingEnabled() && registry.isRecentlyBlocked(sender)
        }
        return silenceMessagingApps() && isUnknownSender(sender)
    }

    // ── Notification ──

    fun postBlockedCountNotification() {
        BlockedNotification.post(appContext, db, notificationConfig, prefsName)
    }

    // ── Rules ──

    private fun isBlockingEnabled(): Boolean = prefs.getBoolean("blocking_enabled", true)

    private fun isPaused(): Boolean =
        prefs.getLong("blocking_paused_until", 0L) > System.currentTimeMillis()

    private fun blockVoipCalls(): Boolean = prefs.getBoolean("block_voip_calls", false)

    private fun smsBlockingEnabled(): Boolean = prefs.getBoolean("sms_blocking_enabled", false)

    private fun silenceMessagingApps(): Boolean = prefs.getBoolean("silence_messaging_apps", false)

    private fun isManuallyBlocked(number: String): Boolean =
        prefs.getStringSet("manual_blocks", emptySet())?.contains(number) == true

    // Fail open: a transient DB error must never wrongfully block a
    // whitelisted number. If the lookup fails, treat the number as
    // whitelisted so the call/message rings through.
    private fun isWhitelisted(number: String): Boolean = try {
        runBlocking(Dispatchers.IO) {
            whitelist.isWhitelisted(number)
        }
    } catch (_: Exception) {
        true
    }

    // Fail open: same reasoning for the contacts lookup.
    private fun isContact(number: String): Boolean = try {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number),
        )
        appContext.contentResolver.query(uri, null, null, null, null)?.use { it.count > 0 } ?: false
    } catch (_: Exception) {
        true
    }

    private fun isUnknownSender(sender: String): Boolean {
        if (isManuallyBlocked(sender)) return true
        // Only apply number rules when the title actually carries a phone
        // number; contact-name titles are left alone.
        val digits = sender.count { it.isDigit() }
        if (digits !in 7..15) return false
        if (isWhitelisted(sender)) return false
        return !isContact(sender)
    }
}
