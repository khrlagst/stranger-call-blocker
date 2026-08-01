<div align="center">
  <br>

  # Stranger Blocker

  **Silence unknown callers and block spam SMS before they reach you.**

  <div><a href="https://github.com/khrlagst/stranger-call-blocker/releases"><img src="https://img.shields.io/github/v/release/khrlagst/stranger-call-blocker?color=10B981&label=latest&style=flat" alt="GitHub Release"></a><a href="https://github.com/khrlagst/stranger-call-blocker/releases"><img src="https://img.shields.io/github/downloads/khrlagst/stranger-call-blocker/total?color=10B981&style=flat" alt="Downloads"></a><a href="https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fkhrlagst%2Fstranger-call-blocker%2Fmain%2Fbadges%2Fapk-size.json"><img src="https://img.shields.io/endpoint?url=https%3A%2F%2Fraw.githubusercontent.com%2Fkhrlagst%2Fstranger-call-blocker%2Fmain%2Fbadges%2Fapk-size.json&style=flat" alt="APK size"></a><a href="https://snyk.io/test/github/khrlagst/stranger-call-blocker"><img src="https://snyk.io/test/github/khrlagst/stranger-call-blocker/badge.svg" alt="Snyk security"></a><a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-10B981?style=flat" alt="License"></a></div>


  <br>
</div>

---

## Overview

Stranger Blocker is a privacy-first Android call and SMS screening app. It intercepts incoming calls **before they ring** using the system's `CallScreeningService` — the same mechanism carriers use for spam detection — and silently rejects unknown numbers at the framework level. It also blocks SMS from unknown senders, both at broadcast time and via a notification fallback on newer Android versions.

**No ads, no tracking, no account, no sign-in.**

---

## Features

| Feature | Detail |
|---|---|
| **Pre-ring call blocking** | Unknown calls are intercepted by the Telecom framework before any ringtone or vibration |
| **SMS blocking** | Messages from unknown senders are blocked via broadcast + notification fallback (Android 11+) |
| **Spam labels** | Report blocked numbers as Spam / Scam / Telemarketer / Promo — stored locally, shown on blocked entries, auto-applies on repeat |
| **Pattern learning** | Detects prefixes shared by several blocked numbers and flags future matches |
| **Messaging-app controls** | Silence unknown senders on WhatsApp and other messaging apps; optionally block hidden-number VoIP calls |
| **Contact-aware** | Numbers in your device contacts always ring through |
| **Shared whitelist** | Allow specific numbers for both calls and SMS — with confirmation and duplicate detection |
| **Manual block** | Block any number instantly, multiple at once, via the floating action button |
| **Blocked history** | Grouped by Today / Yesterday / This Week / Earlier, with search and CSV export |
| **Date-range filters** | Filter blocked calls and SMS by any date range, with a live count badge |
| **Batch management** | Long-press to select multiple entries and delete them at once |
| **Status banner** | Always shows whether blocking is actually active for calls and/or SMS — and why not, when it isn't |
| **Permission-aware settings** | Each toggle shows its required permission and lets you grant it in one tap |
| **Status bar notification** | Count of blocked calls and SMS for the current day (toggleable) |
| **Quick Settings tile** | Toggle blocking on/off from the notification shade without opening the app |
| **OTA updates** | Checks GitHub for new releases — stable and preview channels — and installs in-place |
| **Material 3 UI** | Emerald accent, light/dark/system themes, Jetpack Compose |
| **Minimal footprint** | No webview, no analytics SDK, no third-party trackers |

---

## How it works

```
Incoming call
    │
    ▼
CallScreeningService (intercepted before ringing)
    │
    ├── No caller ID? ───────────────────► Block + Reject
    │
    ├── Blocking disabled / paused? ─────► Let through
    │
    ├── Whitelisted? ────────────────────► Let through
    │
    ├── Manually blocked? ───────────────► Block + Reject
    │
    ├── In device contacts? ─────────────► Let through
    │
    └── Unknown number ──────────────────► Block + Reject + Log + Notify
```

Incoming SMS follows the same rules: whitelist, manual block, keyword filters, and contacts are checked before a message is blocked.

The app never sees or stores call audio, call duration, or any conversation content. Only the phone number, timestamp, and (for SMS) the message body of *blocked* messages are saved locally.

---

## Permissions

| Permission | Why |
|---|---|
| `BIND_SCREENING_SERVICE` | System permission required to intercept incoming calls |
| `READ_CONTACTS` | Check if the caller/sender is in your contacts |
| `READ_PHONE_STATE` | Call screening API requirement |
| `RECEIVE_SMS` | Intercept incoming SMS before the default messaging app |
| `POST_NOTIFICATIONS` | Daily blocked count notification (Android 13+) |
| `INTERNET` | Check for OTA updates via the GitHub API |
| `REQUEST_INSTALL_PACKAGES` | Install APK updates in-app |

Each blocking toggle in Settings shows whether its required permission is granted and lets you grant it directly — if you reject it, the app tells you blocking can't work without it.

---

## Download

Grab the latest APK from the [Releases](https://github.com/khrlagst/stranger-call-blocker/releases) page.

Two variants:
- **release** — minified via ProGuard, production-ready
- **debug** — debuggable, useful for testing

After installing, grant the **Call Screening** system role when prompted (a one-time system dialog). You can also find it later in *Settings → Apps → Special Access → Call Screening*. For SMS blocking, grant the **SMS** permission and, on Android 11+, enable notification access for Stranger Blocker.

---

## Build from source

```bash
git clone https://github.com/khrlagst/stranger-call-blocker.git
cd stranger-call-blocker
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/`.

Requirements:
- JDK 17
- Android SDK 35
- Gradle 8.11+

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Database | Room (SQLite) |
| Architecture | MVVM (AndroidViewModel + StateFlow) |
| DI | None (service locator via Application) |
| Icons | Material Icons Extended |
| CI | GitHub Actions (automated build + release) |

---

## Privacy

Stranger Blocker is designed to be privacy-first:

- **No data leaves your device.** Blocked numbers, timestamps, and SMS bodies are stored locally in an on-device SQLite database. Nothing is transmitted anywhere. Backup is disabled, so blocked-history data never reaches Google's cloud either.
- **No analytics, no tracking, no ads.** The app contains zero third-party analytics SDKs, crash reporters, or advertising libraries.
- **Contacts are read only for screening.** The app queries `ContactsContract.PhoneLookup` at call/SMS time to determine if the sender is known. Contact data is never stored, logged, or transmitted.
- **Internet access is used only for OTA updates.** A single request checks `api.github.com/repos/khrlagst/stranger-call-blocker/releases` to compare versions. No telemetry, no pings, no usage stats.
- **`RECEIVE_SMS` may trip Play Protect** because the app isn't on the Play Store — this is a false positive. The app never reads existing messages, never transmits SMS data, and only stores the bodies of messages it blocked.
- **No account required.** There is no sign-in, no registration, no cloud sync.

The APK is built from source via GitHub Actions, so every release can be verified by building from source yourself.

---

## Version history

| Version | Highlights |
|---|---|
| **2.1.2** | Signature-verified updates, private-number call logging, SMS history retention, ANR-safe SMS blocking |
| **2.1.1** | Notification count refreshes when SMS are blocked and counts both channels; toggle toasts |
| **2.1.0** | Spam labels (report as Spam/Scam/Telemarketer/Promo), pattern learning, messaging-app controls (silence unknown senders, block hidden-number VoIP calls) |
| **2.0.1** | Dismiss blocked SMS notifications on Android 11+ via notification access |
| **2.0.0** | SMS blocking, permission-aware Settings, date-range filters, batch select, in-card search, status banner, FAB quick actions, redesigned dashboard |
| **1.9.2** | Icon Y fine-tune, whitelist top-aligned, notification counter fixed |
| **1.9.1** | Icon Y-center fix, tab ripple clip, swipe fillMaxSize, circle count badge, update dot in Settings |
| **1.9.0** | Theme support (Light/Dark/System), pill tabs with swipe, dark mode contrast, icon centering |
| **1.8.x** | Icon centering & resizing, About screen, full-screen Settings, notification polish |
| **1.7.0** | Status bar notification, Quick Settings tile, notification toggle in Settings |
| **1.6.x** | Tabs, dot indicator, Settings screen, clear confirmation, toggle crash fix |
| **1.5.x** | Emerald theme redesign, symmetrical header, badge, cards |
| **1.4.0** | Larger header, badge in top bar, about dialog |
| **1.3.0** | Minimal redesign, persistent signing key |
| **1.2.0** | Whitelist, grouped history, CSV export |
| **1.1.0** | OTA updates, private number blocking |
| **1.0.0** | Initial release |
