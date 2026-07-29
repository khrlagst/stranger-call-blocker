package com.strangerblocker.ui

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.data.BlockedCall
import com.strangerblocker.data.UpdateChecker
import com.strangerblocker.data.UpdateInfo
import com.strangerblocker.data.WhitelistedNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // ── Tab selection ──

    val selectedTab = MutableStateFlow(Tab.WHITELIST)

    fun selectTab(tab: Tab) { selectedTab.value = tab }

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

    // ── Toggle ──

    private val _isBlockingEnabled = MutableStateFlow(
        prefs.getBoolean("blocking_enabled", true)
    )
    val isBlockingEnabled: StateFlow<Boolean> = _isBlockingEnabled.asStateFlow()

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "blocking_enabled") {
            _isBlockingEnabled.value = prefs.getBoolean("blocking_enabled", true)
        }
    }

    fun toggleBlocking(enabled: Boolean) {
        prefs.edit().putBoolean("blocking_enabled", enabled).apply()
        _isBlockingEnabled.value = enabled
    }

    // ── Notifications ──

    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean("notifications_enabled", true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
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

    private val _exportIntent = MutableStateFlow<Intent?>(null)
    val exportIntent: StateFlow<Intent?> = _exportIntent.asStateFlow()

    fun exportCsv() {
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
                val file = File(ctx.cacheDir, "blocked_calls.csv")
                file.writeText(csv)
                val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
                _exportIntent.value = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (_: Exception) {
                _exportIntent.value = null
            }
        }
    }

    fun clearExportIntent() {
        _exportIntent.value = null
    }

    // ── Updates ──

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    val updateAvailable: StateFlow<Boolean> = _updateInfo.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _updateDownloading = MutableStateFlow(false)
    val updateDownloading: StateFlow<Boolean> = _updateDownloading.asStateFlow()

    fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                val currentVer = ctx.packageManager
                    .getPackageInfo(ctx.packageName, 0)
                    .versionName ?: "0.0.0"
                val info = UpdateChecker.check() ?: return@launch
                if (info.isNewerThan(currentVer) && info.latestVersion != currentVer) {
                    _updateInfo.value = info
                }
            } catch (_: Exception) {
                // silent — network error or rate limit
            }
        }
    }

    fun downloadAndInstall() {
        val info = _updateInfo.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _updateDownloading.value = true
            try {
                val ctx = getApplication<Application>()
                val apk = File(ctx.cacheDir, "update.apk")
                UpdateChecker.download(info.downloadUrl, apk)
                val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", apk)
                _updateInfo.value = null // clear banner
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

    // ── Dialogs / Screens ──

    val showSettings = MutableStateFlow(false)
    val showUpdateDialog = MutableStateFlow(false)
    val showAddWhitelistDialog = MutableStateFlow(false)

    fun openSettings() {
        checkForUpdates()
        showSettings.value = true
    }
    fun closeSettings() { showSettings.value = false }

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

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
        viewModelScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            db.blockedCallDao().deleteOlderThan(thirtyDaysAgo)
        }
        checkForUpdates()
    }
}