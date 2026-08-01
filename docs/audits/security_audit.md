# Security Audit — Stranger Blocker / Stranger Blocker Engine

**Target:** `/home/khrlagst/development/stranger-blocker` · StrangerCallBlocker
**Version audited:** v2.1.1 (versionCode 67) · commit `ad5749b` (SDK extraction M1–M4) · HEAD, tree clean
**Method:** white-box, read-only. No files modified, no builds run.
**Scope:** `:app` (Compose UI + framework adapters), `:sb-engine-core` (pure JVM), `:sb-engine-android` (Room data layer + `SbEngine` facade), `:sample` (host demo).
**Date:** 2026-08-01

---

## Severity key

| Level | Meaning |
|-------|---------|
| **H** | Fix before next release — RCE / supply-chain / unenforceable legal model |
| **M** | Fix soon — user-control bypass, PII at rest, ANR-caused feature failure, multi-tenant data mixing |
| **L** | Fix when convenient — hygiene, docs, perf, config |

---

## Summary

| ID | Severity | Title |
|----|----------|-------|
| H-1 | High | Self-update installs arbitrary APK with no signature verification; signing key protected by known fallback password `"android"` |
| H-2 | High | Licensing model contradicts Apache-2.0 grant; POM not Maven-Central publishable; core module ships dangling NOTICE |
| M-1 | Medium | `SbEngine.shouldBlockCall(null)` returns `BLOCK_PRIVATE` before the enabled/paused gate — "disabled" still blocks private numbers |
| M-2 | Medium | Full SMS bodies + sender numbers persisted plaintext; no retention policy in the SDK |
| M-3 | Medium | Synchronous DB + contacts queries on receiver/listener main thread → ANR → blocking silently fails |
| M-4 | Medium | `AppDatabase` is a process-global singleton; two engines in one process share all data |
| M-5 | Medium | `PatternLearner.learn()` is O(n²·len), recomputed on every history change on the main thread |
| L-1 | Low | Sample host app ships `allowBackup="true"` — teaches SDK consumers the wrong default |
| L-2 | Low | `isWhitelisted`/`isContact` swallow all exceptions → transient error silently blocks a whitelisted contact |
| L-3 | Low | SDK version hardcoded in three places; will drift from the app version |
| L-4 | Low | `AppDatabase` has `exportSchema = false` — no schema artifacts for migration validation |
| L-5 | Low | `BlockedSenderRegistry` stores sender numbers in plaintext `SharedPreferences` |

### Resolved since first audit
- ~~H-2: `sb-engine-core` untracked → CI/reproducibility break~~ — **fixed** in `ad5749b` (module committed with LICENSE, NOTICE, 10 tests).

### Positives (verified)
- `sb-engine-android` + `sb-engine-core` manifests are empty (`<manifest />`) — SDK requests **zero permissions, no INTERNET**. The "no account, no cloud, no data collection" claim is network-true.
- Main app: `allowBackup="false"`; no PII logging; Room DAOs fully parameterized (no SQL injection).
- FileProvider exposes only `cache/update/`, provider `exported=false`.
- `ci.keystore` never committed (`git log --all -- ci.keystore` empty); gitignored.
- All traffic HTTPS (GitHub API + release downloads); no cleartext flags.
- Exported components permission-guarded (`CallScreeningService`, `SmsReceiver` w/ `BROADCAST_SMS`, tile w/ `BIND_QUICK_SETTINGS_TILE`).
- R8/minify enabled for release; current deps (AGP 8.7.3, Kotlin 2.1.0, Compose BOM 2024.12.01, Room 2.6.1).
- 14 unit tests added with the SDK extraction (10 core + 4 android layer).

---

## H-1 — Self-update installs arbitrary APK, no signature verification; signing key protected by known fallback password `"android"`

**Severity:** High — supply-chain RCE if the release channel or signing key is compromised.

### Evidence

- `app/src/main/java/com/strangerblocker/ui/MainViewModel.kt:703–726` — `downloadAndInstall()`:
  - `UpdateChecker.download(info.downloadUrl, apk)` → no checksum, no signature check
  - `FileProvider.getUriForFile(...)` + `Intent(ACTION_VIEW)` with `application/vnd.android.package-archive` → system installer
- `app/src/main/java/com/strangerblocker/data/UpdateChecker.kt:81–95` — `parseRelease()` takes `assets.getJSONObject(0).browser_download_url` — **the first asset, unpinned by name or type**. No SHA-256, no signature field in `UpdateInfo` (lines 9–31).
- `app/build.gradle.kts:20–27` — signing `ci` config:
  ```kotlin
  storePassword = System.getenv("CI_KEYSTORE_PASSWORD") ?: "android"
  keyAlias      = System.getenv("CI_KEY_ALIAS")          ?: "strangerblocker"
  keyPassword   = System.getenv("CI_KEY_PASSWORD")       ?: "android"
  ```
  Used by **both** `release` and `debug` build types (lines 36, 41).
- `.github/workflows/build.yml` — if `secrets.RELEASE_KEYSTORE` is unset, CI **generates a fresh keystore each run** (keytool, `-storepass android`) → release signatures not stable across CI runs; also means a locally-obtained keystore + the fallback password signs anything.

### Impact

- An attacker who compromises the GitHub release channel (repo/account/CI secret) publishes a malicious APK; every installed device installs it as an in-place update — **full RCE, silent**.
- Anyone who obtains `ci.keystore` (sits in the repo root on disk) plus the publicly-known fallback password can sign malware Android accepts as an update to the installed app.
- The fresh-keystore CI fallback breaks update installs (signature mismatch) — a correctness bug on top of the security hole.

### Fix

1. Verify the downloaded APK's signer certificate matches the installed app's cert (`PackageManager.getPackageArchiveInfo` + signature comparison) **before** launching the installer.
2. Pin the expected asset name pattern (e.g. `stranger-blocker-*-release.apk`) and check a published SHA-256 alongside the release.
3. Make `RELEASE_KEYSTORE` secret **mandatory** in CI — fail the build if absent; remove the `"android"`/`"strangerblocker"` password fallbacks (fail fast on missing env vars).
4. Move `ci.keystore` out of the repo root (e.g. `~/.keystores/`), restrict file permissions.

---

## H-2 — Licensing model contradicts Apache-2.0 grant; POM not publishable; core module ships dangling NOTICE

**Severity:** High — undermines the stated commercial goal of the SDK.

### Evidence

- **SPDX headers vs. commercial restriction.** All 25 engine source files carry `// SPDX-License-Identifier: Apache-2.0`. Apache-2.0 §2 grants **commercial** use with no field-of-use restriction. But:
  - `sb-engine-android/LICENSE.commercial`: "free for open-source and **non-commercial** use… **Unauthorized commercial use is not permitted under the Apache-2.0 grant**."
  - `sb-engine-android/NOTICE`: "Commercial use… requires a separate commercial license."
  - These restrictions are **legally unenforceable** against anyone who received the code under Apache-2.0 — the SPDX grant they receive *is* the license. You cannot ship Apache-2.0 headers and claw back commercial rights.
- **Invalid POM license expression.** Both `sb-engine-core/build.gradle.kts:38–44` and `sb-engine-android/build.gradle.kts:66–72` publish:
  ```kotlin
  license {
      name.set("Apache-2.0 OR Commercial")
      url.set("https://www.apache.org/licenses/LICENSE-2.0")
  }
  ```
  `"Commercial"` is not an SPDX identifier → Maven Central **rejects** this POM.
- **Publishing config incomplete for Central.** Neither module configures:
  - `publishing { repositories { maven { url = ... } } }` — `publish` has no destination
  - `sourcesJar` / `javadocJar` — required by Central
  - GPG `signing` plugin — required by Central
- **Dangling NOTICE in core.** `sb-engine-core/NOTICE` says "see LICENSE.commercial" but that file exists **only** in `sb-engine-android`. The published core artifact references a file it doesn't contain.

### Fix

Decide the model, then make the artifacts consistent:
- **Option A (recommended, simplest):** true Apache-2.0 for both modules; drop the "non-commercial" language from NOTICE/LICENSE.commercial; monetize support/SLA (LICENSE.commercial already frames commercial terms as "permission and support" — that's the viable product).
- **Option B:** real dual-license (e.g. BUSL or source-available with `LicenseRef` SPDX) — then the 25 SPDX headers must change to match, and the POM must carry a valid SPDX expression.
- Either way: fix POM license id to valid SPDX, add publishing repo + sources/javadoc jars + signing, and ship `LICENSE.commercial` in **both** modules or drop the reference from `sb-engine-core/NOTICE`.

---

## M-1 — `SbEngine.shouldBlockCall(null)` blocks before the enabled/paused gate

**Severity:** Medium — user-control bypass; now part of the SDK's public contract.

### Evidence

- `sb-engine-android/src/main/kotlin/com/strangerblocker/engine/SbEngine.kt:43–53`:
  ```kotlin
  fun shouldBlockCall(number: String?): CallDecision {
      if (number == null) return CallDecision.BLOCK_PRIVATE   // ← BEFORE the gate
      if (!isBlockingEnabled() || isPaused()) return CallDecision.ALLOW
      ...
  }
  ```
- `app/src/main/java/com/strangerblocker/service/CallBlockerService.kt:17–34` delegates directly — the old service bug is now the SDK decision contract.
- `sb-engine-android/README.md` teaches hosts to wire `when (engine.shouldBlockCall(number))` verbatim.

### Impact

Disabling or pausing blocking does **not** stop private/unknown-number rejection. Every SDK consumer inherits "disabled ≠ off" for private numbers.

### Fix

```kotlin
fun shouldBlockCall(number: String?): CallDecision {
    if (!isBlockingEnabled() || isPaused()) return CallDecision.ALLOW
    if (number == null) return CallDecision.BLOCK_PRIVATE
    ...
}
```

---

## M-2 — Full SMS bodies + sender numbers persisted plaintext; no retention in the SDK

**Severity:** Medium — PII at rest, unbounded retention.

### Evidence

- `sb-engine-android/src/main/kotlin/com/strangerblocker/engine/HistoryRepository.kt:33–36` — `recordSms(sender, body, reason)` stores the **entire message body** (`BlockedSms.messageBody`).
- `sb-engine-android/src/main/kotlin/com/strangerblocker/engine/data/AppDatabase.kt:11–15,76` — plaintext Room DB, name hardcoded `"stranger_blocker.db"`.
- Retention: app prunes calls at 30 days (`MainViewModel` ~804); **SMS retention is not wired anywhere in the SDK** — consumers get unbounded plaintext SMS retention by default.
- Mitigations in place: main app `allowBackup="false"`; DB is app-private.

### Fix

Prune SMS on the same 30-day schedule (or default), and/or expose `retentionDays` in `EngineConfig`. Consider SQLCipher for the SDK data layer.

---

## M-3 — Synchronous DB + contacts queries on the receiver/listener main thread

**Severity:** Medium — ANR risk makes the core feature silently fail.

### Evidence

- `SbEngine.kt:123–129` — `isWhitelisted()` does `runBlocking(Dispatchers.IO)`.
- `SbEngine.kt:131–139` — `isContact()` does a **synchronous** `contentResolver.query`.
- Call chains from the main thread:
  - `app/.../receiver/SmsReceiver.kt:22–28` → `engine.smsBlockReason()` → `isWhitelisted()` / `isContact()` — inside `onReceive`, 10s ANR window; a kill = SMS delivered **unblocked**.
  - `app/.../service/SmsNotificationListener.kt:29–32` → `shouldDismissNotification()` → `isUnknownSender()` → same two queries — `NotificationListenerService` callbacks run on main.

### Fix

Preload whitelist/contact-number sets into memory (invalidate on DB/contacts change); keep the decision path I/O-free. If queries must stay, use `goAsync()` in the receiver.

---

## M-4 — `AppDatabase` is a process-global singleton; two engines in one process share all data

**Severity:** Medium — data isolation gap for the commercial/multi-tenant use case.

### Evidence

- `AppDatabase.kt:23–82` — `INSTANCE` is process-global, keyed on context only; DB filename hardcoded `"stranger_blocker.db"`.
- `SbEngine` namespaces **prefs** per `EngineConfig(prefsName)` but **not the database**.

### Impact

A host embedding two engines (multi-tenant / white-label — the stated commercial use case) gets complete history cross-contamination: tenant A's blocked SMS bodies visible to tenant B's engine instance.

### Fix

Scope the DB per `EngineConfig` (e.g. filename derived from `prefsName`) or document **single-engine-per-process** as a hard constraint in the README.

---

## M-5 — `PatternLearner.learn()` is O(n²·len), recomputed on every history change on the main thread

**Severity:** Medium — UI jank / DoS at scale; PII in learned patterns.

### Evidence

- `sb-engine-core/src/main/kotlin/com/strangerblocker/engine/PatternLearner.kt:23–41` — per number, per prefix length: `numbers.count { it.startsWith(prefix) }` → O(n²·len).
- `app/.../ui/MainViewModel.kt:327–329` — recomputes via `combine(blockedCalls, blockedSms, dismissed)` on **every** DB change, on the main thread (`viewModelScope`).
- `BlockPattern` (`BlockPattern.kt:5`) retains `example` — a full phone number (PII) in memory/UI.

### Fix

Index prefixes (sort + binary search, or a trie); cap input size; throttle recompute. Consider dropping `example` from the public model or documenting it as PII.

---

## Low severity

### L-1 — Sample host app ships `allowBackup="true"`
`sample/src/main/AndroidManifest.xml:5` — the *reference host app* teaches SDK consumers the opposite of the main app's `false`. Flip it.

### L-2 — Silent fail-to-block in `isWhitelisted`/`isContact`
`SbEngine.kt:127,137` — `catch (_: Exception) { false }`: a transient DB/ContactsProvider error **silently blocks a whitelisted contact**, with zero logging. Fail open on whitelist/contact errors, or at minimum log (consistent with the app's no-logging stance, but this is a safety-critical path).

### L-3 — SDK version drift
`version = "2.1.1"` hardcoded in **three** places: `app/build.gradle.kts`, `sb-engine-core/build.gradle.kts`, `sb-engine-android/build.gradle.kts`. No single source of truth → next app bump silently desyncs the published artifact version. Move to `gradle.properties` or a version catalog.

### L-4 — `exportSchema = false`
`AppDatabase.kt:14` — for an SDK shipping migrations to external consumers, schema export + migration validation should be on (`Room` schema JSON artifacts).

### L-5 — Plaintext sender registry in prefs
`BlockedSenderRegistry` (`BlockedSenderRegistry.kt:25`) stores `"$sender|$now"` strings in `SharedPreferences` (`recent_blocked_sms_senders`). App-private, 10-min window — low risk, but same plaintext-PII theme; note in SDK docs.

---

## Remediation priority

1. **H-1** — signature-verify updates; mandatory CI keystore; kill `"android"` fallbacks.
2. **H-2** — decide license model; fix POM SPDX, publishing repo, sources/javadoc, signing; fix core NOTICE.
3. **M-1** — gate `shouldBlockCall` before private-number handling.
4. **M-4** — per-engine DB scoping or documented single-engine constraint.
5. **M-3** — in-memory whitelist/contact sets.
6. **M-2** — SMS retention / `retentionDays`.
7. **M-5** — prefix indexing + throttled recompute.
8. **L-1..L-5** — hygiene pass.
