<div align="center">
  <br>
  <pre style="font-size:48px;margin:0;line-height:1">🛡️</pre>

  # Stranger Blocker

  **Silence unknown callers before the phone rings.**

  <p>
    <a href="https://github.com/khrlagst/stranger-call-blocker/releases">
      <img src="https://img.shields.io/github/v/release/khrlagst/stranger-call-blocker?color=10B981&label=latest&style=flat" alt="GitHub Release">
    </a>
    <a href="https://github.com/khrlagst/stranger-call-blocker/releases">
      <img src="https://img.shields.io/github/downloads/khrlagst/stranger-call-blocker/total?color=10B981&style=flat" alt="Downloads">
    </a>
    <a href="LICENSE">
      <img src="https://img.shields.io/badge/license-MIT-10B981?style=flat" alt="License">
    </a>
  </p>

  <br>
</div>

---

## Overview

Stranger Blocker is an Android call screening app that intercepts incoming calls **before they ring** and silently rejects unknown numbers. It uses the system's `CallScreeningService` API — the same mechanism carriers use for spam detection — to block calls at the framework level, before your phone even wakes up.

**It's a single-screen utility app.** No ads, no tracking, no account required.

---

## Features

| Feature | Detail |
|---|---|
| **Pre-ring blocking** | Calls are intercepted by the Telecom framework before any ringtone or vibration |
| **Contact-aware** | Numbers in your device contacts always ring through |
| **Whitelist** | Manually allow specific numbers even if they aren't in your contacts |
| **Blocked history** | Grouped by Today / Yesterday / This Week / Earlier with CSV export |
| **Status bar notification** | Count of blocked calls for the current day (toggleable) |
| **Quick Settings tile** | Toggle blocking on/off from the notification shade without opening the app |
| **OTA updates** | The app checks for new releases on GitHub and can download + install in-place |
| **Material 3 UI** | Emerald accent theme, dark mode support, Jetpack Compose |
| **$<$100KB APK** | Minimal footprint — no webview, no analytics SDK |

---

## How it works

```
Incoming call
    │
    ▼
CallScreeningService (intercepted before ringing)
    │
    ├── No caller ID? ───────────────► Block + Reject
    │
    ├── Blocking disabled? ──────────► Let through
    │
    ├── In whitelist? ───────────────► Let through
    │
    ├── In device contacts? ─────────► Let through
    │
    └── Unknown number ──────────────► Block + Reject + Log to DB + Notify
```

The app never sees or stores call audio, call duration, or any conversation data. Only the phone number and timestamp of blocked calls are saved locally.

---

## Permissions

| Permission | Why |
|---|---|
| `BIND_SCREENING_SERVICE` | System permission required to intercept incoming calls |
| `READ_CONTACTS` | Check if the caller is in your contacts |
| `READ_PHONE_STATE` | Call screening API requirement |
| `POST_NOTIFICATIONS` | Daily blocked count notification (Android 13+) |
| `INTERNET` | Check for OTA updates via GitHub API |
| `REQUEST_INSTALL_PACKAGES` | Install APK updates in-app |

---

## Download

Grab the latest APK from the [Releases](https://github.com/khrlagst/stranger-call-blocker/releases) page.

Two variants:
- **release** — minified via ProGuard, production-ready
- **debug** — debuggable, useful for testing

After installing, you'll be prompted to grant the **Call Screening** system role. This is a one-time setup — Android asks you to confirm in a system dialog. You can also find it later in *Settings → Apps → Special Access → Call Screening*.

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
| CI | GitHub Actions |

---

## Privacy

Stranger Blocker is designed to be privacy-first:

- **No data leaves your device.** Blocked numbers and timestamps are stored locally in an on-device SQLite database. Nothing is sent to any server.
- **No analytics, no tracking, no ads.** The app contains zero third-party analytics SDKs, crash reporters, or advertising libraries.
- **Contacts are read only for screening.** The app queries `ContactsContract.PhoneLookup` at call time to determine if the caller is known. Contact data is never stored, logged, or transmitted.
- **Internet access is used only for OTA updates.** A single request checks `api.github.com/repos/khrlagst/stranger-call-blocker/releases/latest` to compare versions. No telemetry, no pings, no usage stats.
- **No account required.** There is no sign-in, no registration, no cloud sync.

The APK is built from source via GitHub Actions. You can verify every release by building from source yourself — no binary transparency needed because the source is the trust anchor.

---

## Version history

| Version | Highlights |
|---|---|
| **1.8.0** | Sticky section headers, direct CSV save to local storage via document picker |
| **1.7.x** | Notification, Quick Settings tile, license, privacy section, README |
| **1.7.0** | Status bar notification, Quick Settings tile, notification toggle in Settings |
| **1.6.x** | Tabs, dot indicator, Settings screen, clear confirmation, toggle crash fix |
| **1.5.x** | Emerald theme redesign, symmetrical header, badge, cards |
| **1.4.0** | Larger header, badge in top bar, about dialog |
| **1.3.0** | Minimal redesign, persistent signing key |
| **1.2.0** | Whitelist, grouped history, CSV export |
| **1.1.0** | OTA updates, private number blocking |
| **1.0.0** | Initial release |

---

<div align="center">
  <br>
  <p>
    Built with ❤️ by <a href="https://github.com/khrlagst">@khrlagst</a>
  </p>
  <p>
    <sub>Stranger Blocker is not affiliated with Google or Samsung.</sub>
  </p>
  <br>
</div>
