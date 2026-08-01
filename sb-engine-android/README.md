# Stranger Blocker Engine (sb-engine-android)

**On-device spam intelligence for Android — no account, no cloud, no data collection.**

This module bundles the data layer and the `SbEngine` facade for the Stranger
Blocker Engine. Pair it with `sb-engine-core` (pure decision logic), which it
depends on and re-exports.

- License: [Apache-2.0](LICENSE) for OSS/non-commercial use. Commercial
  embedding requires [LICENSE.commercial](LICENSE.commercial).

## Requirements

- minSdk 29, compileSdk 35, JDK 17
- Kotlin 2.x, AGP 8.x

## Add the dependency

```kotlin
// settings.gradle.kts
include(":sb-engine-android")
include(":sb-engine-core")

// app/build.gradle.kts
dependencies {
    implementation(project(":sb-engine-android"))
}
```

## Quick start

Create one shared engine instance (e.g. in your `Application`):

```kotlin
class MyApp : Application() {
    val engine: SbEngine by lazy {
        SbEngine(
            this,
            EngineConfig(prefsName = "my_app_prefs"),
            NotificationConfig(
                smallIconRes = R.drawable.ic_notification,
                channelId = "block_alerts",
            ),
        )
    }
}
```

### Wire call screening

```kotlin
class CallBlocker : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val number = details.handle?.schemeSpecificPart
        val engine = (applicationContext as MyApp).engine
        when (engine.shouldBlockCall(number)) {
            CallDecision.ALLOW -> respondToCall(details, CallResponse.Builder().build())
            CallDecision.BLOCK_PRIVATE -> respondToCall(details, block())
            else -> {
                respondToCall(details, block())
                engine.recordBlockedCall(number ?: return)
            }
        }
    }
    private fun block() = CallResponse.Builder()
        .setDisallowCall(true).setRejectCall(true)
        .setSkipCallLog(false).setSkipNotification(false).build()
}
```

Declare it with `android.permission.BIND_SCREENING_SERVICE`.

### Wire SMS interception

```kotlin
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val sender = messages.firstOrNull()?.originatingAddress ?: return
        val body = messages.joinToString("") { it.messageBody ?: "" }
        val engine = (context.applicationContext as MyApp).engine
        val reason = engine.smsBlockReason(sender, body) ?: return
        abortBroadcast()
        engine.recordBlockedSms(sender, body, reason)
    }
}
```

### Wire notification dismissal (Android 11+ fallback)

```kotlin
class NotifListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notif = sbn.notification ?: return
        val defaultSms = Telephony.Sms.getDefaultSmsPackage(this)
        val fromDefaultSmsApp = defaultSms != null && sbn.packageName == defaultSms
        if (notif.category != Notification.CATEGORY_MESSAGE && !fromDefaultSmsApp) return
        val title = notif.extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: notif.extras.getCharSequence(Notification.EXTRA_TITLE_BIG)
        val engine = (applicationContext as MyApp).engine
        if (engine.shouldDismissNotification(title?.toString(), fromDefaultSmsApp)) {
            cancelNotification(sbn.key)
        }
    }
}
```

## Public API

- `SbEngine(context, EngineConfig, NotificationConfig)`
  - `shouldBlockCall(number): CallDecision` — ALLOW / BLOCK / BLOCK_PRIVATE / BLOCK_VOIP
  - `recordBlockedCall(number)` / `recordBlockedSms(sender, body, reason)`
  - `smsBlockReason(sender, body): String?` — null means deliver
  - `shouldDismissNotification(title, fromDefaultSmsApp): Boolean`
  - `postBlockedCountNotification()`
  - `history` / `whitelist` / `labels` repositories
  - `db` — Room database (raw DAO access)
- `PatternLearner` (in `sb-engine-core`) — LCP spam-pattern learning
- `BlockedSenderRegistry` — receiver → listener handoff (10-minute window)

## Behavior notes

- Unknown numbers are always blocked unless whitelisted/contacts.
- Calls with no phone number on the handle (e.g. some VoIP) are governed by
  the `block_voip_calls` preference.
- Blocked counts in the daily notification are always derived from the DB.
