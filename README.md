<div align="center">
  <br>

  # Stranger Blocker

  **Silence unknown callers and SMS before they reach you.**

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

Stranger Blocker is an Android app that intercepts incoming calls **before they ring** and silently blocks SMS from unknown senders. Calls are stopped at the OS level via `CallScreeningService` — before your phone wakes up. SMS are intercepted via `BroadcastReceiver` before they reach the messaging app.

**No ads, no tracking, no account required.** Built for people who want peace of mind without paying a subscription.

---

## Features

| Feature | Detail |
|---|---|
| **Pre-ring blocking** | Calls intercepted by Telecom framework before any ringtone or vibration |
| **SMS blocking** | Unknown SMS senders silently blocked before reaching your inbox |
| **Contact-aware** | Numbers in your device contacts always get through |
| **Shared whitelist** | One whitelist for both calls and SMS — add once, both channels respect it |
| **Dashboard** | Today's blocked count, Calls/SMS this week, all-time stats, weekly activity chart |
| **Bottom navigation** | 4 tabs: Dashboard, Calls, SMS, Settings |
| **Blocked history** | Calls grouped by Today / Yesterday / This Week / Earlier with CSV export |
| **Status bar notification** | Shield or circle-with-count icon for today's blocks (toggleable) |
| **Quick Settings tile** | Toggle call blocking from the notification shade |
| **Preview builds** | Opt into pre-release updates from Settings |
| **OTA updates** | Manual check for updates in About screen — downloads and installs in-place |
| **Theme support** | Light, Dark, or System-follow — emerald accent throughout |
| **Material 3 UI** | Jetpack Compose, emerald theme, smooth animations, pill tabs with swipe |
| **Pre-commit quality gate** | Automated checks run before every commit — bracket balance, imports, version consistency |

---

## How it works

### Calls
```
Incoming call
    │
    ▼
CallScreeningService (intercepted before ringing)
    │
    ├── No caller ID? ───────────────► Block + Reject
    ├── Blocking disabled? ──────────► Let through
    ├── In whitelist? ───────────────► Let through
    ├── In device contacts? ─────────► Let through
    └── Unknown number ──────────────► Block + Reject + Log to DB + Notify
```

### SMS
```
Incoming SMS
    │
    ▼
SmsReceiver (BroadcastReceiver, priority 1000)
    │
    ├── Blocking disabled? ──────────► Let through
    ├── Sender in whitelist? ────────► Let through
    ├── Sender in contacts? ─────────► Let through
    └── Unknown sender ──────────────► Log to DB + abortBroadcast()
```

The app never sees or stores call audio, call duration, or conversation data. Only phone numbers, SMS sender numbers, and timestamps are saved locally.

---

## Permissions

| Permission | Why |
|---|---|
| `BIND_SCREENING_SERVICE` | System permission required to intercept incoming calls |
| `READ_CONTACTS` | Check if the caller/sender is in your contacts |
| `READ_PHONE_STATE` | Call screening API requirement |
| `RECEIVE_SMS` | Intercept incoming SMS before the messaging app |
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

### Preview builds
Enable **Settings → Updates → Preview builds** to receive pre-release versions before they're released as stable.

---

## Build from source

```bash
git clone https://github.com/khrlagst/stranger-call-blocker.git
cd stranger-call-blocker
git config core.hooksPath .githooks   # enable pre-commit quality gate
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
| Database | Room (SQLite) with migrations |
| Architecture | MVVM (AndroidViewModel + StateFlow) |
| DI | None (service locator via Application) |
| Navigation | State-based (no navigation library) |
| Icons | Material Icons Extended |
| CI | GitHub Actions |
| Quality | Automated pre-commit hook + CI gate |

---

## Privacy

Stranger Blocker is designed to be privacy-first:

- **No data leaves your device.** Blocked numbers, SMS text, and timestamps are stored locally in an on-device SQLite database. Nothing is sent to any server.
- **No analytics, no tracking, no ads.** The app contains zero third-party analytics SDKs, crash reporters, or advertising libraries.
- **Contacts are read only for screening.** The app queries `ContactsContract.PhoneLookup` at call/SMS time to determine if the sender is known. Contact data is never stored, logged, or transmitted.
- **Internet access is used only for OTA updates.** A single request checks GitHub's releases API to compare versions. No telemetry, no pings, no usage stats.
- **`RECEIVE_SMS` permission** is required for SMS blocking. Play Protect may flag this because the app is not on the Play Store — this is a false positive. The app never stores message body text beyond what's needed for the blocked log, never transmits SMS data, and never reads existing messages.
- **No account required.** There is no sign-in, no registration, no cloud sync.

The APK is built from source via GitHub Actions. You can verify every release by building from source yourself.

---

## Version history

| Version | Highlights |
|---|---|
| **1.9.5-p29** | Batch select + bulk delete, whitelist removal confirm, header de-dup |
| **1.9.5-p28** | Dashboard recency — recent activity, quick whitelist, chart date range |
| **1.9.5-p27** | Blocked SMS preview — sender, message snippet, whitelist action, empty-state value |
| **1.9.5-p26** | Duplicate @OptIn + whitelist ordering fix (p25 build fix) |
| **1.9.5-p25** | Calls search bar, frequency badge (×N), tap action menu |
| **1.9.5-p24** | RoleBadge data class fix (p23 build fix) |
| **1.9.5-p23** | Pause/resume blocking, paused badge countdown, chart legend |
| **1.9.5-p22** | UpdateCheckResult import fix (p21 build fix) |
| **1.9.5-p21** | BlockedSms import fix (p20 build fix) |
| **1.9.5-p20** | Dual-channel updates (stable + preview), SMS runtime permission |
| **1.9.5-p19** | About update button opens overlay, changelog refresh |
| **1.9.5-p18** | SMS listener category check (SystemApi avoidance) |
| **1.9.5-p17** | SMS listener compile fixes |
| **1.9.5-p16** | Sync abortBroadcast, SMS notification listener for Android 11+ |
| **1.9.5-p15** | 7-day chart, SMS tab layout, About banner dark theme |
| **1.9.5-p12** | Chart day labels fix, consistent header names, zombie code cleanup |
| **1.9.5-p11** | Manual check-for-updates button, checking state, Play Protect docs |
| **1.9.5-p08** | SMS blocking backend (receiver, entity, DB migration, UI toggle) |
| **1.9.5-p07** | Dark mode surface background, preview OTA race fix |
| **1.9.5-p06** | Dark mode theme colors, card margins, tab fillMaxWidth |
| **1.9.5-p05** | Shared header across all tabs, dashboard cards/chart, tab ripple |
| **1.9.5-p04** | Dashboard matches mockup, Calls padding fix, chart bars from bottom |
| **1.9.5-p03** | Version compare fix for preview tags, navbar colors, dashboard bars |
| **1.9.5-p02** | Settings radio+icons, nav icons fix, bottom padding |
| **1.9.5-p01** | Bottom nav restructure, Dashboard, Calls, SMS, Settings tabs |
| **1.9.5** | Tab tap animates pager, indicator tracks swipe directly |
| **1.9.4** | Sliding tab indicator, pager persists across navigation |
| **1.9.3** | Icon Y 30, transparent status bar, badge ripple clip, tab animation |
| **1.9.2** | Icon Y fine-tune, whitelist top-aligned, notification counter fixed |
| **1.9.1** | Icon Y-center, tab ripple clip, swipe fillMaxSize, circle badge, dot in Settings |
| **1.9.0** | Theme support, pill tabs with swipe, dark mode contrast, icon centering |
| **1.8.x** | Tabs, Settings screen, update-in-About, emerald theme, clear confirmation |
| **1.7.x** | Status bar notification, Quick Settings tile, license, privacy |
| **1.6.x** | Tabs, dot indicator, Settings screen, toggle crash fix |
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
    Built by <a href="https://github.com/khrlagst">@khrlagst</a>
  </p>
  <p>
    <sub>Stranger Blocker is not affiliated with Google or Samsung.</sub>
  </p>
  <br>
</div>
