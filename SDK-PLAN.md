# Stranger Blocker Engine — SDK Extraction Plan

**Pitch:** *The only Android spam-blocking engine that works fully offline — no account, no cloud, no data collection.*

**License model:** Dual — Apache-2.0 core (goodwill: free for OSS/non-commercial), commercial license for proprietary embedding (credit: companies pursuing profit pay). The **free version is a complete, working product** — never a crippled demo.

---

## 1. Goal

Extract the reusable spam-intelligence core out of the Stranger Blocker app into a standalone, embeddable Android SDK (`stranger-blocker-engine`) that any host app can integrate in an afternoon — while the app itself becomes the reference implementation.

**Non-goals for v1:** no UI components, no network/cloud backend, no analytics. The engine does everything on-device.

---

## 2. What moves where

The golden rule: **framework classes stay thin adapters in the host; ALL logic lives in the engine.**

### Engine (extracted)

| Module | Contents | Origin |
|---|---|---|
| `sb-engine-core` (pure Kotlin, no Android) | `SpamLabel` enum, `BlockPattern` + `detectPatterns` (LCP clustering, min-prefix/min-support constants), `NumberRules` (phone-shape check, whitelist/manual/contact/VoIP decision), `inRange` date logic, version compare, sender normalization | `MainViewModel.kt` helpers, `CallBlockerService` decision branches |
| `sb-engine-android` (Android lib) | Room entities + DAOs (`BlockedCall`, `BlockedSms`, `WhitelistedNumber`, `NumberLabel` + `AppDatabase` v5), repository wrappers (history/whitelist/labels/patterns), `BlockedSenderRegistry` (receiver→listener handoff, 10-min window), `BlockedNotification` poster (DB-authoritative count) | `data/`, `service/BlockedNotification`, the registry in `SmsReceiver` |

### App (stays, becomes thin adapters)

| Piece | Becomes |
|---|---|
| `CallBlockerService` | Thin `CallScreeningService` adapter → `engine.shouldBlockCall(...)` |
| `SmsReceiver` | Thin `BroadcastReceiver` → `engine.handleBlockedSms(...)` |
| `SmsNotificationListener` | Thin `NotificationListenerService` → `engine.shouldDismissNotification(title, fromDefaultSmsApp)` |
| All Compose UI, OTA updater, theme, Quick Settings tile, manifest | Unchanged — the app is the living integration example |

---

## 3. Public API surface (v1)

```kotlin
// ── sb-engine-core ──
enum class SpamLabel { SPAM, SCAM, TELEMARKETER, PROMO }
enum class CallDecision { ALLOW, BLOCK, BLOCK_PRIVATE, BLOCK_VOIP }
data class BlockPattern(val prefix: String, val count: Int, val example: String)

object NumberRules {
    fun isPhoneNumberShape(s: String): Boolean          // 7–15 digits
    fun classifyCall(
        number: String?,
        roleHeld: Boolean, blockingEnabled: Boolean, paused: Boolean,
        blockVoip: Boolean,
    ): CallDecision
    fun normalizeSender(s: String): String              // letterOrDigit filter
    fun inRange(millis: Long, from: Long?, to: Long?): Boolean
}

class PatternLearner(private val numbers: List<String>) {
    fun learn(): List<BlockPattern>                     // LCP clustering
    fun matches(number: String): BlockPattern?
}

// ── sb-engine-android ──
class SbEngine(context: Context, private val config: EngineConfig) {
    val history: HistoryRepository                      // blocked calls + SMS, grouped, filterable
    val whitelist: WhitelistRepository                  // shared across channels
    val labels: LabelRepository                         // NumberLabel upsert/observe
    val patterns: PatternLearner                        // fed from history
    val registry: BlockedSenderRegistry                 // record + match, 10-min window

    fun shouldBlockCall(...): CallDecision
    fun handleBlockedSms(sender: String, body: String, reason: String)
    fun shouldDismissNotification(title: String?, fromDefaultSmsApp: Boolean): Boolean
    fun postBlockedCountNotification()
}
```

`EngineConfig`: flags (blockVoip, silenceMessagingApps, notificationsEnabled) + the app's `SharedPreferences` name — everything else is derived from the DB.

---

## 4. Licensing file structure

```
stranger-blocker-engine/
  LICENSE                  # Apache-2.0 (goodwill — free for OSS/non-commercial)
  LICENSE.commercial       # Terms summary + contact; one-time or per-device
  README.md                # Integration guide (the app repo README links here)
  sb-engine-core/
  sb-engine-android/
  (later: Maven Central publication)
```

- The **app repo stays MIT** — commercial terms never touch it.
- Every engine source file carries an SPDX header (`SPDX-License-Identifier: Apache-2.0 OR Commercial`) so license scanners resolve it cleanly.
- No license-key/dongle enforcement in v1 — honor-system + support tier. The commercial value is *permission + support*, not DRM.

---

## 5. Extraction steps (incremental, each step keeps the app building)

- ✅ **M1 — core**: `sb-engine-core` created; `SpamLabel`, `BlockPattern`, `PatternLearner`, `NumberRules`, `CallDecision` moved; **10 unit tests** passing.
- ✅ **M2 — data layer**: `sb-engine-android` created; Room entities/DAOs/`AppDatabase` moved; `BlockedSenderRegistry` + `PrefsSenderStore` + `BlockedNotification` (configurable) extracted; repository wrappers `HistoryRepository` / `WhitelistRepository` / `LabelRepository` added and exposed on `SbEngine`; **4 registry tests + 4 repository tests (Robolectric)** passing.
- ✅ **M3 — adapters + rewiring**: `SbEngine` facade introduced; `CallBlockerService` (172→36 lines), `SmsReceiver` (106→30), `SmsNotificationListener` (93→34) are thin adapters; behavior byte-identical.
- ✅ **M4 — publish**: Apache-2.0 `LICENSE` + `NOTICE` per module, `LICENSE.commercial` terms, SPDX headers on all 25 engine sources, `sb-engine-android/README.md` integration guide, `maven-publish` config on both modules (POM metadata ready), minimal `sample/` host app.

**Open (deferred):** Maven Central / GitHub Packages publication needs credentials + repository block; add when actually publishing.

---

## 6. Testing strategy (a genuine gap the SDK forces closed)

The app currently has **zero tests** — the engine must be testable by design:

- `PatternLearnerTest` — use the real-world seeds: `+6285592679948/125/231` → pattern `+6285592679` (count 3); `+622130179723/183252/911574` → the threshold boundary case; `+622135523726/3755` → `+6221355237`. Assert the `+62812`-chaos guard (prefix too short → no pattern).
- `NumberRulesTest` — phone-shape, VoIP handle (`"WhatsApp Call"`), private (`null`).
- `BlockedSenderRegistryTest` — 10-min window expiry, normalization matching.
- `HistoryRepositoryTest` — Room via Robolectric; Today-grouping, date-range filter, calls+SMS count.

---

## 7. What stays proprietary-free

The engine contains **no** crowd-sourced DB, no telemetry, no network code. The differentiator is architectural: *on-device pattern learning + the blocked-sender registry handoff + the privacy-native posture* — exactly what no incumbent ships.

*(Patent filing intentionally skipped — no time/funds. The moat is the packaging + privacy posture, not a patent.)*

---

## 8. Commercial note

- **Goodwill**: the app (MIT) + engine core (Apache-2.0) stay free forever.
- **Credit**: commercial license for proprietary embedding + optional support/SLA tier.
- The repo stays untainted: engine lives in its own repo/package with its own LICENSE files.
