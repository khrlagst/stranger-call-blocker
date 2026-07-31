package com.strangerblocker.ui

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.net.Uri
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strangerblocker.MainActivity
import com.strangerblocker.R
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.data.BlockedCall
import com.strangerblocker.data.BlockedSms
import com.strangerblocker.data.UpdateCheckResult
import com.strangerblocker.data.UpdateChecker
import com.strangerblocker.data.UpdateInfo
import com.strangerblocker.data.WhitelistedNumber
import com.strangerblocker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CallGroup(val header: String, val calls: List<BlockedCall>)

enum class Tab(val label: String) {
    WHITELIST("Whitelist"),
    BLOCKED("Blocked"),
}

enum class BottomNavTab {
    DASHBOARD,
    CALLS,
    SMS,
    SETTINGS,
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)

    private val db = (application as StrangerBlockerApp).db

    // ── Block history ──

    private val blockedCalls: Flow<List<BlockedCall>> = db.blockedCallDao().observeAll()

    val groupedCalls: StateFlow<List<CallGroup>> = blockedCalls.map { calls ->
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.get(Calendar.DAY_OF_YEAR)

        calls.groupBy { call ->
            cal.timeInMillis = call.blockedAtMillis
            val day = cal.get(Calendar.DAY_OF_YEAR)
            val year = cal.get(Calendar.YEAR)
            when {
                day == today && year == todayYear -> 0
                day == yesterday && year == todayYear -> 1
                year == todayYear -> 2
                else -> 3
            }
        }.entries.sortedBy { it.key }.map { (key, group) ->
            val label = when (key) {
                0 -> "Today"
                1 -> "Yesterday"
                2 -> "This Week"
                else -> "Earlier"
            }
            CallGroup(label, group)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBlocked: StateFlow<Int> = blockedCalls.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Daily counts for the last 7 days — 0=Monday .. 6=Sunday. */
    val weeklyCounts: StateFlow<List<Int>> = blockedCalls.map { calls ->
        val cal = Calendar.getInstance()
        val nowMillis = cal.timeInMillis
        // Compute day-of-week index for each of the last 7 days (Mon=0..Sun=6)
        val todayDoy = cal.get(Calendar.DAY_OF_WEEK)
        val monOffset = (todayDoy - Calendar.MONDAY + 7) % 7
        val midnightToday = nowMillis - (cal.get(Calendar.HOUR_OF_DAY) * 3600000L
            + cal.get(Calendar.MINUTE) * 60000L + cal.get(Calendar.SECOND) * 1000L
            + cal.get(Calendar.MILLISECOND))
        val dayStart = midnightToday - monOffset * 86400000L
        val buckets = LongArray(7) { dayStart + it * 86400000L }
        val counts = IntArray(7)
        for (call in calls) {
            for (i in 0..6) {
                val end = if (i < 6) buckets[i + 1] else Long.MAX_VALUE
                if (call.blockedAtMillis in buckets[i] until end) { counts[i]++; break }
            }
        }
        counts.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { 0 })

    // ── Tab selection ──

    val selectedTab = MutableStateFlow(Tab.WHITELIST)

    fun selectTab(tab: Tab) { selectedTab.value = tab }

    // ── Search ──

    val searchQuery = MutableStateFlow("")

    fun setSearchQuery(query: String) { searchQuery.value = query }

    /** Blocked calls filtered by [searchQuery] (matches phone number substring). */
    val filteredGroupedCalls: StateFlow<List<CallGroup>> = combine(groupedCalls, searchQuery) { groups, query ->
        if (query.isBlank()) groups
        else groups.mapNotNull { group ->
            val matched = group.calls.filter { it.phoneNumber.contains(query, ignoreCase = true) }
            if (matched.isEmpty()) null else CallGroup(group.header, matched)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Whitelist filtered by [searchQuery] (matches number or label substring). */
    val filteredWhitelisted: StateFlow<List<WhitelistedNumber>> = combine(whitelisted, searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.phoneNumber.contains(query, ignoreCase = true) ||
                (it.label?.contains(query, ignoreCase = true) == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Whitelist ──

    val whitelisted: Flow<List<WhitelistedNumber>> = db.whitelistedNumberDao().observeAll()

    fun addToWhitelist(number: String, label: String?) {
        viewModelScope.launch {
            db.whitelistedNumberDao().insert(
                WhitelistedNumber(
                    phoneNumber = number,
                    label = label?.takeIf { it.isNotBlank() },
                    addedAtMillis = System.currentTimeMillis(),
                )
            )
        }
    }

    fun removeFromWhitelist(number: String) {
        viewModelScope.launch {
            db.whitelistedNumberDao().delete(number)
        }
    }

    // ── SMS blocking ──

    private val _smsBlockingEnabled = MutableStateFlow(
        prefs.getBoolean("sms_blocking_enabled", false)
    )
    val smsBlockingEnabled: StateFlow<Boolean> = _smsBlockingEnabled.asStateFlow()

    private val blockedSms: Flow<List<BlockedSms>> = db.blockedSmsDao().observeAll()

    val groupedBlockedSms: StateFlow<List<CallGroup>> = blockedSms.map { smsList ->
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_YEAR)
        val todayYear = cal.get(Calendar.YEAR)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.get(Calendar.DAY_OF_YEAR)
        smsList.groupBy { sms ->
            cal.timeInMillis = sms.blockedAtMillis
            val day = cal.get(Calendar.DAY_OF_YEAR)
            val year = cal.get(Calendar.YEAR)
            when { day == today && year == todayYear -> 0; day == yesterday && year == todayYear -> 1; year == todayYear -> 2; else -> 3 }
        }.entries.sortedBy { it.key }.map { (key, group) ->
            CallGroup(when (key) { 0 -> "Today"; 1 -> "Yesterday"; 2 -> "This Week"; else -> "Earlier" }, emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSmsBlocked: StateFlow<Int> = blockedSms.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleSmsBlocking(enabled: Boolean) {
        prefs.edit().putBoolean("sms_blocking_enabled", enabled).apply()
        _smsBlockingEnabled.value = enabled
    }

    // ── Toggle ──

    private val _isBlockingEnabled = MutableStateFlow(
        prefs.getBoolean("blocking_enabled", true)
    )
    val isBlockingEnabled: StateFlow<Boolean> = _isBlockingEnabled.asStateFlow()

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "blocking_enabled" -> _isBlockingEnabled.value = prefs.getBoolean("blocking_enabled", true)
            "blocking_paused_until" -> _pauseUntil.value = prefs.getLong("blocking_paused_until", 0L)
        }
    }

    fun refreshBlockingState() {
        _isBlockingEnabled.value = prefs.getBoolean("blocking_enabled", true)
        _pauseUntil.value = prefs.getLong("blocking_paused_until", 0L)
    }

    fun toggleBlocking(enabled: Boolean) {
        prefs.edit().putBoolean("blocking_enabled", enabled).apply()
        _isBlockingEnabled.value = enabled
    }

    // ── Pause blocking (temporary) ──

    private val _pauseUntil = MutableStateFlow(prefs.getLong("blocking_paused_until", 0L))
    val pauseUntil: StateFlow<Long> = _pauseUntil.asStateFlow()

    fun pauseBlocking(durationMinutes: Int) {
        val until = System.currentTimeMillis() + durationMinutes * 60_000L
        prefs.edit().putLong("blocking_paused_until", until).apply()
        _pauseUntil.value = until
    }

    fun resumeBlocking() {
        prefs.edit().putLong("blocking_paused_until", 0L).apply()
        _pauseUntil.value = 0L
    }

    // ── Notifications ──

    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean("notifications_enabled", true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // ── Notification icon style ──

    private val _notificationIconStyle = MutableStateFlow(
        prefs.getString("notification_icon_style", "shield") ?: "shield"
    )
    val notificationIconStyle: StateFlow<String> = _notificationIconStyle.asStateFlow()

    fun setNotificationIconStyle(style: String) {
        prefs.edit().putString("notification_icon_style", style).apply()
        _notificationIconStyle.value = style
        maybePostNotification()
    }

    // ── Theme ──

    private val _themeMode = MutableStateFlow(
        try { ThemeMode.valueOf(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM") }
        catch (_: Exception) { ThemeMode.SYSTEM }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    // ── Preview updates ──

    private val _previewUpdates = MutableStateFlow(
        prefs.getBoolean("preview_updates", false)
    )
    val previewUpdates: StateFlow<Boolean> = _previewUpdates.asStateFlow()

    fun togglePreviewUpdates(enabled: Boolean) {
        prefs.edit().putBoolean("preview_updates", enabled).apply()
        _previewUpdates.value = enabled
        checkForUpdates()
    }

    fun toggleNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
        val app = getApplication<Application>()
        if (enabled) {
            viewModelScope.launch(Dispatchers.IO) {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val count = db.blockedCallDao().countSince(cal.timeInMillis)
                val intent = Intent(app, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val pi = PendingIntent.getActivity(
                    app, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                val iconStyle = prefs.getString("notification_icon_style", "shield") ?: "shield"
                val text = count.toString()
                val notification: Notification
                if (iconStyle == "circle_count") {
                    val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.parseColor("#10B981")
                        style = Paint.Style.FILL
                    }.let { canvas.drawCircle(24f, 24f, 24f, it) }
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.WHITE
                        textAlign = Paint.Align.CENTER
                        textSize = 26f
                        typeface = Typeface.DEFAULT_BOLD
                    }.let { canvas.drawText(text, 24f, 24f + 26f / 3f, it) }
                    val icon = Icon.createWithBitmap(bitmap)
                    notification = Notification.Builder(app, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(icon)
                        .setContentTitle("$text blocked today")
                        .setContentText("Stranger Blocker is active")
                        .setContentIntent(pi)
                        .build()
                } else {
                    notification = NotificationCompat.Builder(app, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("$text blocked today")
                        .setContentText("Stranger Blocker is active")
                        .setNumber(count)
                        .setContentIntent(pi)
                        .setAutoCancel(false)
                        .setSilent(true)
                        .build()
                }
                NotificationManagerCompat.from(app).notify(1001, notification)
            }
        } else {
            NotificationManagerCompat.from(app).cancel(1001)
        }
    }

    // ── Role ──

    private val _isRoleHeld = MutableStateFlow(checkRoleHeld())
    val isRoleHeld: StateFlow<Boolean> = _isRoleHeld.asStateFlow()

    fun refreshRoleStatus() {
        _isRoleHeld.value = checkRoleHeld()
    }

    private fun checkRoleHeld(): Boolean {
        val mgr = getApplication<Application>().getSystemService(Context.ROLE_SERVICE) as RoleManager
        return mgr.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    // ── Export ──

    fun exportCsvToUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                val calls = db.blockedCallDao().getAll()
                val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val csv = buildString {
                    appendLine("phone_number,blocked_at")
                    calls.forEach { call ->
                        appendLine("${call.phoneNumber},${dateFmt.format(Date(call.blockedAtMillis))}")
                    }
                }
                ctx.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csv.toByteArray())
                }
            } catch (_: Exception) {
                // silent
            }
        }
    }

    // ── Updates ──

    private val _updateCheckResult = MutableStateFlow(UpdateCheckResult(null, null))
    val updateCheckResult: StateFlow<UpdateCheckResult> = _updateCheckResult.asStateFlow()

    val updateAvailable: StateFlow<Boolean> = _updateCheckResult.map { it.hasAny }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** The update the user chose to install from the About screen. */
    private val _pendingUpdate = MutableStateFlow<UpdateInfo?>(null)
    val pendingUpdate: StateFlow<UpdateInfo?> = _pendingUpdate.asStateFlow()

    private val _updateDownloading = MutableStateFlow(false)
    val updateDownloading: StateFlow<Boolean> = _updateDownloading.asStateFlow()

    private val _checkingForUpdates = MutableStateFlow(false)
    val checkingForUpdates: StateFlow<Boolean> = _checkingForUpdates.asStateFlow()

    fun checkForUpdates() {
        if (_checkingForUpdates.value) return
        _checkingForUpdates.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                val currentVer = ctx.packageManager
                    .getPackageInfo(ctx.packageName, 0)
                    .versionName ?: "0.0.0"
                val wantPreview = _previewUpdates.value || currentVer.contains("-p")
                _updateCheckResult.value = UpdateChecker.check(currentVer, wantPreview)
            } catch (_: Exception) {
                // silent — network error or rate limit
            } finally {
                _checkingForUpdates.value = false
            }
        }
    }

    fun openUpdateDialog(update: UpdateInfo) {
        _pendingUpdate.value = update
        showUpdateDialog.value = true
    }

    fun downloadAndInstall() {
        val info = _pendingUpdate.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _updateDownloading.value = true
            try {
                val ctx = getApplication<Application>()
                val apk = File(ctx.cacheDir, "update.apk")
                UpdateChecker.download(info.downloadUrl, apk)
                val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", apk)
                _pendingUpdate.value = null
                _updateDownloading.value = false
                // Launch system package installer
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
            } catch (_: Exception) {
                _updateDownloading.value = false
            }
        }
    }

    // ── Bottom Navigation ──

    val bottomNavTab = MutableStateFlow(BottomNavTab.CALLS)

    fun selectBottomTab(tab: BottomNavTab) {
        if (tab == BottomNavTab.SETTINGS) checkForUpdates()
        bottomNavTab.value = tab
    }

    // ── About sub-screen inside Settings ──

    val showAbout = MutableStateFlow(false)

    fun openAbout() {
        checkForUpdates()
        showAbout.value = true
    }

    fun closeAbout() { showAbout.value = false }

    // ── Dialogs ──

    val showUpdateDialog = MutableStateFlow(false)
    val showAddWhitelistDialog = MutableStateFlow(false)

    fun openUpdateDialog() { showUpdateDialog.value = true }
    fun closeUpdateDialog() { showUpdateDialog.value = false }

    fun openAddWhitelistDialog() { showAddWhitelistDialog.value = true }
    fun closeAddWhitelistDialog() { showAddWhitelistDialog.value = false }

    // ── Whitelist input state ──

    val whitelistInputNumber = MutableStateFlow("")
    val whitelistInputLabel = MutableStateFlow("")

    fun confirmAddWhitelist() {
        val number = whitelistInputNumber.value.trim()
        if (number.isBlank()) return
        viewModelScope.launch {
            db.whitelistedNumberDao().insert(
                WhitelistedNumber(
                    phoneNumber = number,
                    label = whitelistInputLabel.value.trim().takeIf { it.isNotBlank() },
                    addedAtMillis = System.currentTimeMillis(),
                )
            )
        }
        whitelistInputNumber.value = ""
        whitelistInputLabel.value = ""
        showAddWhitelistDialog.value = false
    }

    // ── Clear history confirmation ──

    val showClearHistoryDialog = MutableStateFlow(false)

    fun openClearHistoryDialog() { showClearHistoryDialog.value = true }
    fun closeClearHistoryDialog() { showClearHistoryDialog.value = false }

    fun confirmClearHistory() {
        viewModelScope.launch { db.blockedCallDao().clearAll() }
        showClearHistoryDialog.value = false
    }

    private fun maybePostNotification() {
        val app = getApplication<Application>()
        val prefs = app.getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return
        viewModelScope.launch(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val count = db.blockedCallDao().countSince(cal.timeInMillis)
            val intent = Intent(app, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pi = PendingIntent.getActivity(
                app, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val iconStyle = prefs.getString("notification_icon_style", "shield") ?: "shield"
            val text = count.toString()
            val notification: Notification
            if (iconStyle == "circle_count") {
                val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#10B981")
                    style = Paint.Style.FILL
                }.let { canvas.drawCircle(24f, 24f, 24f, it) }
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    textSize = 26f
                    typeface = Typeface.DEFAULT_BOLD
                }.let { canvas.drawText(text, 24f, 24f + 26f / 3f, it) }
                val icon = Icon.createWithBitmap(bitmap)
                notification = Notification.Builder(app, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(icon)
                    .setContentTitle("$text blocked today")
                    .setContentText("Stranger Blocker is active")
                    .setContentIntent(pi)
                    .build()
            } else {
                notification = NotificationCompat.Builder(app, StrangerBlockerApp.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("$text blocked today")
                    .setContentText("Stranger Blocker is active")
                    .setNumber(count)
                    .setContentIntent(pi)
                    .setAutoCancel(false)
                    .setSilent(true)
                    .build()
            }
            NotificationManagerCompat.from(app).notify(1001, notification)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
        viewModelScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            db.blockedCallDao().deleteOlderThan(thirtyDaysAgo)
        }
        checkForUpdates()
        maybePostNotification()
    }
}