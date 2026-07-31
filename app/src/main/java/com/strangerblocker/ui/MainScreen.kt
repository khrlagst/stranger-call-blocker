package com.strangerblocker.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strangerblocker.data.BlockedCall
import com.strangerblocker.data.BlockedSms
import com.strangerblocker.data.UpdateCheckResult
import com.strangerblocker.data.UpdateInfo
import com.strangerblocker.data.WhitelistedNumber
import com.strangerblocker.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar



// ── Root ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val bottomNavTab by viewModel.bottomNavTab.collectAsState()
    val showAbout by viewModel.showAbout.collectAsState()
    val isBlockingEnabled by viewModel.isBlockingEnabled.collectAsState()
    val pauseUntil by viewModel.pauseUntil.collectAsState()
    val isRoleHeld by viewModel.isRoleHeld.collectAsState()
    val groupedCalls by viewModel.groupedCalls.collectAsState()
    val totalBlocked by viewModel.totalBlocked.collectAsState()
    val recentBlocked by viewModel.recentBlocked.collectAsState()
    val whitelisted by viewModel.whitelisted.collectAsState(initial = emptyList())
    val filteredGroupedCalls by viewModel.filteredGroupedCalls.collectAsState()
    val filteredWhitelisted by viewModel.filteredWhitelisted.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val updateCheckResult by viewModel.updateCheckResult.collectAsState()
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    val updateDownloading by viewModel.updateDownloading.collectAsState()
    val checkingForUpdates by viewModel.checkingForUpdates.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val smsBlockingEnabled by viewModel.smsBlockingEnabled.collectAsState()
    val groupedBlockedSms by viewModel.groupedBlockedSms.collectAsState()
    val totalSmsBlocked by viewModel.totalSmsBlocked.collectAsState()
    val notificationIconStyle by viewModel.notificationIconStyle.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val previewUpdates by viewModel.previewUpdates.collectAsState()
    val weeklyCounts by viewModel.weeklyCounts.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val pendingUpdate by viewModel.pendingUpdate.collectAsState()
    val showClearHistoryDialog by viewModel.showClearHistoryDialog.collectAsState()
    val showAddWhitelistDialog by viewModel.showAddWhitelistDialog.collectAsState()
    val pendingWhitelistRemoval by viewModel.pendingWhitelistRemoval.collectAsState()
    val whitelistInputNumber by viewModel.whitelistInputNumber.collectAsState()
    val whitelistInputLabel by viewModel.whitelistInputLabel.collectAsState()
    val context = LocalContext.current

    val saveCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri: Uri? ->
        if (uri != null) viewModel.exportCsvToUri(uri)
    }

    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }
    }

    val smsPermissionGranted = remember {
        android.content.pm.PackageManager.PERMISSION_GRANTED ==
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.RECEIVE_SMS
            )
    }

    // ── About screen (full screen, own Scaffold) ──
    if (showAbout) {
        AboutScreen(
            appVersion = appVersion,
            updateCheckResult = updateCheckResult,
            updateDownloading = updateDownloading,
            checkingForUpdates = checkingForUpdates,
            onCheckForUpdates = viewModel::checkForUpdates,
            onUpdateClick = { update ->
                viewModel.closeAbout()
                viewModel.openUpdateDialog(update)
            },
            onBack = viewModel::closeAbout,
        )
        return
    }

    // ── Main layout with bottom nav ──
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxSize()) {
            // Shared header (title + badge across all tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Stranger Blocker",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                RoleBadge(
                    isActive = isRoleHeld,
                    isPaused = pauseUntil > System.currentTimeMillis(),
                    pauseRemainingMinutes = ((pauseUntil - System.currentTimeMillis()) / 60_000).toInt(),
                    onTap = {
                        if (pauseUntil > System.currentTimeMillis()) viewModel.resumeBlocking()
                        else viewModel.refreshRoleStatus()
                    },
                )
            }
            HorizontalDivider(thickness = 1.dp)

            // Tab content
            Surface(Modifier.weight(1f).fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                when (bottomNavTab) {
                    BottomNavTab.DASHBOARD -> DashboardScreen(
                        totalBlocked = totalBlocked,
                        groupedCalls = groupedCalls,
                        weeklyCounts = weeklyCounts,
                        recentBlocked = recentBlocked,
                        pauseUntil = pauseUntil,
                        onPauseOneHour = { viewModel.pauseBlocking(60) },
                        onResumeBlocking = viewModel::resumeBlocking,
                        onQuickWhitelist = { viewModel.addToWhitelist(it.phoneNumber, null) },
                    )
                    BottomNavTab.CALLS -> CallsContent(
                        isBlockingEnabled = isBlockingEnabled,
                        groupedCalls = groupedCalls,
                        filteredGroupedCalls = filteredGroupedCalls,
                        filteredWhitelisted = filteredWhitelisted,
                        searchQuery = searchQuery,
                        totalBlocked = totalBlocked,
                        whitelisted = whitelisted,
                        selectedTab = selectedTab,
                        onToggleBlocking = viewModel::toggleBlocking,
                        onSelectTab = viewModel::selectTab,
                        onSearchChange = viewModel::setSearchQuery,
                        onAddToWhitelist = viewModel::openAddWhitelistDialog,
                        onExportCsv = { saveCsvLauncher.launch("blocked_calls.csv") },
                        onClearHistory = viewModel::openClearHistoryDialog,
                        onRemoveWhitelist = viewModel::requestRemoveWhitelist,
                        onWhitelistCall = { viewModel.addToWhitelist(it.phoneNumber, null) },
                        onDeleteBlocked = viewModel::deleteBlockedByIds,
                    )
                    BottomNavTab.SMS -> SmsScreen(
                        smsBlockingEnabled = smsBlockingEnabled,
                        onToggleSmsBlocking = viewModel::toggleSmsBlocking,
                        groupedBlockedSms = groupedBlockedSms,
                        totalSmsBlocked = totalSmsBlocked,
                        whitelisted = whitelisted,
                        selectedTab = selectedTab,
                        onSelectTab = viewModel::selectTab,
                        onAddToWhitelist = viewModel::openAddWhitelistDialog,
                        onRemoveWhitelist = viewModel::requestRemoveWhitelist,
                        onWhitelistSms = { viewModel.addToWhitelist(it.senderNumber, null) },
                    )
                    BottomNavTab.SETTINGS -> SettingsTab(
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsToggle = viewModel::toggleNotifications,
                        notificationIconStyle = notificationIconStyle,
                        onIconStyleChange = viewModel::setNotificationIconStyle,
                        smsBlockingEnabled = smsBlockingEnabled,
                        onSmsToggle = viewModel::toggleSmsBlocking,
                        smsPermissionGranted = smsPermissionGranted,
                        themeMode = themeMode,
                        onThemeChange = viewModel::setThemeMode,
                        previewUpdates = previewUpdates,
                        onPreviewToggle = viewModel::togglePreviewUpdates,
                        updateAvailable = updateAvailable,
                        onAbout = viewModel::openAbout,
                        onBack = {},
                    )
                }
            }
        }

        // Bottom nav overlay
        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            BottomNavBar(
                selectedTab = bottomNavTab,
                onSelectTab = viewModel::selectBottomTab,
            )
        }
    }

    // Global dialogs
    if (showUpdateDialog && pendingUpdate != null) {
        val update = pendingUpdate!!
        UpdateConfirmDialog(
            version = update.latestVersion,
            releaseNotes = update.releaseNotes,
            isPreview = update.isPreview,
            downloading = updateDownloading,
            onCancel = viewModel::closeUpdateDialog,
            onUpdate = {
                viewModel.downloadAndInstall()
                viewModel.closeUpdateDialog()
            },
        )
    }
    if (showClearHistoryDialog) {
        ClearHistoryDialog(
            total = totalBlocked,
            onDismiss = viewModel::closeClearHistoryDialog,
            onConfirm = viewModel::confirmClearHistory,
        )
    }
    if (showAddWhitelistDialog) {
        AddWhitelistDialog(
            number = whitelistInputNumber,
            label = whitelistInputLabel,
            onNumberChange = { viewModel.whitelistInputNumber.value = it },
            onLabelChange = { viewModel.whitelistInputLabel.value = it },
            onAdd = viewModel::confirmAddWhitelist,
            onDismiss = viewModel::closeAddWhitelistDialog,
        )
    }
    if (pendingWhitelistRemoval != null) {
        val entry = pendingWhitelistRemoval!!
        AlertDialog(
            onDismissRequest = viewModel::cancelRemoveWhitelist,
            title = { Text("Remove from whitelist?") },
            text = { Text("${entry.phoneNumber} will no longer be allowed through.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = viewModel::confirmRemoveWhitelist) { Text("Remove", color = Color(0xFFDC2626)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelRemoveWhitelist) { Text("Cancel") } },
        )
    }
}

// ── Bottom Navigation Bar ──

private data class NavItem(val tab: BottomNavTab, val icon: ImageVector, val label: String)

@Composable
private fun BottomNavBar(selectedTab: BottomNavTab, onSelectTab: (BottomNavTab) -> Unit) {
    val items = listOf(
        NavItem(BottomNavTab.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
        NavItem(BottomNavTab.CALLS, Icons.Default.Phone, "Calls"),
        NavItem(BottomNavTab.SMS, Icons.Default.Sms, "SMS"),
        NavItem(BottomNavTab.SETTINGS, Icons.Default.Settings, "Settings"),
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.tab,
                onClick = { onSelectTab(item.tab) },
                icon = { Icon(item.icon, contentDescription = item.label, tint = if (selectedTab == item.tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                label = { Text(item.label, fontSize = 10.sp, color = if (selectedTab == item.tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            )
        }
    }
}

// ── Dashboard Tab ──

@Composable
private fun DashboardScreen(
    totalBlocked: Int,
    groupedCalls: List<CallGroup>,
    weeklyCounts: List<Int>,
    recentBlocked: List<BlockedCall>,
    pauseUntil: Long,
    onPauseOneHour: () -> Unit,
    onResumeBlocking: () -> Unit,
    onQuickWhitelist: (BlockedCall) -> Unit,
) {
    val todayCount = groupedCalls.firstOrNull()?.calls?.size ?: 0
    val thisWeekCount = groupedCalls.takeWhile { it.header != "Earlier" }.sumOf { it.calls.size }
    val isPaused = pauseUntil > System.currentTimeMillis()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // Total Blocked Today
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Total Blocked Today",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("$todayCount",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text("Calls: $todayCount · SMS: 0",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))

        // Pause / resume control
        if (isPaused) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF3E0),
                modifier = Modifier.fillMaxWidth().clickable(onClick = onResumeBlocking)) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Paused — tap to resume blocking", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFB45309), modifier = Modifier.weight(1f))
                    Text("Resume", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFB45309))
                }
            }
        } else {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onPauseOneHour)) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Pause blocking for 1 hour", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text("Pause", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Recent Activity — last 5 blocked calls
        if (recentBlocked.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Recent Activity", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    recentBlocked.forEach { call ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(call.phoneNumber, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(relativeTime(call.blockedAtMillis), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onQuickWhitelist(call) }) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Whitelist", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Calls This Week + SMS This Week side by side
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Calls This Week",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("$thisWeekCount",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text("from $totalBlocked total",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("SMS This Week",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("0",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text("from 0 total",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // All Time — Calls + All Time — SMS
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("All Time — Calls",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("$totalBlocked",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("All Time — SMS",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("0",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Weekly chart — 7 days, bars grow upward from baseline
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Weekly Activity",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(weekDateRange(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))

                // Chart legend — calls (emerald) vs SMS (light emerald)
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
                    Spacer(Modifier.width(4.dp))
                    Text("Calls", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)))
                    Spacer(Modifier.width(4.dp))
                    Text("SMS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(16.dp))
                if (weeklyCounts.all { it == 0 }) {
                    Text("No data this week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                } else {
                    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                    val maxVal = weeklyCounts.max().coerceAtLeast(1)
                    // Baseline at bottom: bars grow UPWARD
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                        weeklyCounts.forEachIndexed { i, count ->
                            val barHeight = (count.toFloat() / maxVal * 100f).toInt().coerceAtLeast(if (count > 0) 4 else 0)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("$count",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barHeight.dp.coerceAtLeast(2.dp))
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (i == dayNames.size - 1 || i == dayNames.size - 2) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary),
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(dayNames[i],
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

// ── Calls Tab ──

@Composable
private fun CallsContent(
    isBlockingEnabled: Boolean,
    groupedCalls: List<CallGroup>,
    filteredGroupedCalls: List<CallGroup>,
    filteredWhitelisted: List<WhitelistedNumber>,
    searchQuery: String,
    totalBlocked: Int,
    whitelisted: List<WhitelistedNumber>,
    selectedTab: Tab,
    onToggleBlocking: (Boolean) -> Unit,
    onSelectTab: (Tab) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddToWhitelist: () -> Unit,
    onExportCsv: () -> Unit,
    onClearHistory: () -> Unit,
    onRemoveWhitelist: (WhitelistedNumber) -> Unit,
    onWhitelistCall: (BlockedCall) -> Unit,
    onDeleteBlocked: (List<Long>) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val newTab = Tab.entries[pagerState.currentPage]
        if (newTab != selectedTab) onSelectTab(newTab)
    }

    Column(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 100.dp)) {
            ToggleRow(
                title = "Block stranger calls",
                subtitle = if (isBlockingEnabled) "Unknown numbers are silently rejected" else "All calls ring through",
                checked = isBlockingEnabled,
                onToggle = onToggleBlocking,
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search number or label") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            TabBar(
                pagerState = pagerState,
                selectedTab = selectedTab,
                whitelistCount = whitelisted.size,
                blockedCount = totalBlocked,
                onSelectTab = { tab ->
                    onSelectTab(tab)
                    scope.launch { pagerState.animateScrollToPage(tab.ordinal) }
                },
            )
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.fillMaxSize()) {
                    CardHeader(
                        selectedTab = selectedTab,
                        blockedCount = totalBlocked,
                        onAddToWhitelist = onAddToWhitelist,
                        onExportCsv = onExportCsv,
                        onClearHistory = onClearHistory,
                    )
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            when (page) {
                                0 -> WhitelistContent(entries = if (searchQuery.isBlank()) whitelisted else filteredWhitelisted, onRemove = onRemoveWhitelist)
                                1 -> BlockedContent(groups = if (searchQuery.isBlank()) groupedCalls else filteredGroupedCalls, onWhitelist = onWhitelistCall, onDelete = onDeleteBlocked)
                            }
                        }
                    }
                }
        }
    }
}

// ── SMS Tab ──

@Composable
private fun SmsScreen(
    smsBlockingEnabled: Boolean,
    onToggleSmsBlocking: (Boolean) -> Unit,
    groupedBlockedSms: List<SmsGroup>,
    totalSmsBlocked: Int,
    whitelisted: List<WhitelistedNumber>,
    selectedTab: Tab,
    onSelectTab: (Tab) -> Unit,
    onAddToWhitelist: () -> Unit,
    onRemoveWhitelist: (WhitelistedNumber) -> Unit,
    onWhitelistSms: (BlockedSms) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val newTab = Tab.entries[pagerState.currentPage]
        if (newTab != selectedTab) onSelectTab(newTab)
    }

    Column(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 100.dp)) {
        ToggleRow(
            title = "Block stranger SMS",
            subtitle = if (smsBlockingEnabled) "Messages from unknown numbers are silently blocked" else "All SMS messages ring through",
            checked = smsBlockingEnabled,
            onToggle = onToggleSmsBlocking,
        )

        TabBar(
            pagerState = pagerState,
            selectedTab = selectedTab,
            whitelistCount = whitelisted.size,
            blockedCount = totalSmsBlocked,
            onSelectTab = { tab ->
                onSelectTab(tab)
                scope.launch { pagerState.animateScrollToPage(tab.ordinal) }
            },
        )
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.fillMaxSize()) {
                CardHeader(
                    selectedTab = selectedTab,
                    blockedCount = totalSmsBlocked,
                    onAddToWhitelist = onAddToWhitelist,
                    onExportCsv = {},  // SMS CSV export not yet implemented
                    onClearHistory = {},  // SMS clear not yet implemented
                )
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (page) {
                            0 -> WhitelistContent(entries = whitelisted, onRemove = onRemoveWhitelist)
                            1 -> SmsBlockedContent(groups = groupedBlockedSms, onWhitelist = onWhitelistSms)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SmsBlockedContent(groups: List<SmsGroup>, onWhitelist: (BlockedSms) -> Unit) {
    if (groups.isEmpty() || groups.all { it.smsList.isEmpty() }) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(6.dp))
                Text("No blocked SMS yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                Text("We'll display blocked messages here once detected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize()) {
            groups.forEach { group ->
                stickyHeader(key = "sms_header_${group.header}") {
                    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(start = 12.dp, top = 8.dp, bottom = 2.dp)) {
                        Text("${group.header} (${group.smsList.size})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                items(group.smsList, key = { it.id }) { sms ->
                    BlockedSmsRow(sms = sms, onWhitelist = { onWhitelist(sms) })
                }
            }
        }
    }
}

@Composable
private fun BlockedSmsRow(sms: BlockedSms, onWhitelist: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(sms.senderNumber, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sms.messageBody, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(sms.blockedAtMillis)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onWhitelist) { Icon(Icons.Default.PersonAdd, contentDescription = "Whitelist", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 36.dp), thickness = 0.5.dp)
}

// ── Settings Tab ──

@Composable
private fun SettingsTab(
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    notificationIconStyle: String,
    onIconStyleChange: (String) -> Unit,
    smsBlockingEnabled: Boolean,
    onSmsToggle: (Boolean) -> Unit,
    smsPermissionGranted: Boolean,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    previewUpdates: Boolean,
    onPreviewToggle: (Boolean) -> Unit,
    updateAvailable: Boolean,
    onAbout: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Notifications", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Block alerts",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                    Text("Show blocked call count in status bar",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = notificationsEnabled, onCheckedChange = onNotificationsToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Spacer(Modifier.height(4.dp))

            Text("Notification icon", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
            Text("Choose how the icon appears in the status bar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth().clickable { onIconStyleChange("shield") }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = notificationIconStyle == "shield", onClick = { onIconStyleChange("shield") })
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column { Text("Shield", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                    Text("Status bar shows the SB shield icon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Row(Modifier.fillMaxWidth().clickable { onIconStyleChange("circle_count") }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = notificationIconStyle == "circle_count", onClick = { onIconStyleChange("circle_count") })
                Spacer(Modifier.width(4.dp))
                Box(Modifier.size(20.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Text("N", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp))
                }
                Spacer(Modifier.width(12.dp))
                Column { Text("Circle with count", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                    Text("Status bar shows a circle with today's blocked count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("SMS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Block stranger SMS", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                    Text(
                        when {
                            !smsPermissionGranted -> "SMS permission required — tap to grant"
                            else -> "Silently block messages from unknown senders"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (smsPermissionGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    )
                }
                Switch(checked = smsBlockingEnabled, onCheckedChange = onSmsToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Theme", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (mode, label) ->
                Row(Modifier.fillMaxWidth().clickable { onThemeChange(mode) }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = themeMode == mode, onClick = { onThemeChange(mode) })
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Updates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Preview builds", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                    Text("Receive pre-release updates before stable release", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = previewUpdates, onCheckedChange = onPreviewToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("About", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onAbout)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Stranger Blocker", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                        Text("Version info, changelog & updates", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

// ── Shared toggle row for Calls & SMS tabs ──

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary)
            Text(subtitle,
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            ),
        )
    }
}

// ── About Screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreen(
    appVersion: String,
    updateCheckResult: UpdateCheckResult,
    updateDownloading: Boolean,
    checkingForUpdates: Boolean,
    onCheckForUpdates: () -> Unit,
    onUpdateClick: (UpdateInfo) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary) }
                    Text("About", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider()
            }
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Stranger Blocker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("v$appVersion", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(6.dp))

            // Check for updates / available updates section
            if (checkingForUpdates) {
                Surface(shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Checking for updates\u2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (updateCheckResult.hasAny) {
                // Stable update row
                updateCheckResult.stable?.let { stable ->
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onUpdateClick(stable) }) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("Update to v${stable.latestVersion} (Stable)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f))
                            if (updateDownloading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            else Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                // Preview update row
                updateCheckResult.preview?.let { preview ->
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onUpdateClick(preview) }) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Update to v${preview.latestVersion} (Preview)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            Text("›", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onCheckForUpdates)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Text("Check for updates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("What's new", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(horizontal = 20.dp)) {
                latestChangelog().forEach { item ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Text(item, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.weight(1f))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
            Text("github.com/khrlagst/stranger-call-blocker",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp))
        }
    }
}

// ── Tab Bar (with sliding indicator) ──

@Composable
private fun TabBar(
    pagerState: androidx.compose.foundation.pager.PagerState,
    selectedTab: Tab,
    whitelistCount: Int,
    blockedCount: Int,
    onSelectTab: (Tab) -> Unit,
) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints {
            val tabWidth = maxWidth / 2
            val slideProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction
            val indicatorOffset = tabWidth * slideProgress

            Box(Modifier.fillMaxWidth()) {
                Box(Modifier.offset(x = indicatorOffset + 3.dp).width(tabWidth - 6.dp)
                    .padding(vertical = 3.dp).height(36.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary))
                Row(Modifier.fillMaxWidth().padding(3.dp)) {
                    TabItem("Whitelist", whitelistCount, selectedTab == Tab.WHITELIST,
                        { onSelectTab(Tab.WHITELIST) }, Modifier.weight(1f))
                    TabItem("Blocked", blockedCount, selectedTab == Tab.BLOCKED,
                        { onSelectTab(Tab.BLOCKED) }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TabItem(label: String, count: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).height(36.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(5.dp))
        Box(Modifier.size(18.dp).clip(CircleShape).background(if (selected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center) {
            Text("$count", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Card Header ──

@Composable
private fun CardHeader(
    selectedTab: Tab, blockedCount: Int,
    onAddToWhitelist: () -> Unit, onExportCsv: () -> Unit, onClearHistory: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1f))
        when (selectedTab) {
            Tab.WHITELIST -> IconButton(onClick = onAddToWhitelist) { Icon(Icons.Default.Add, contentDescription = "Add to whitelist", tint = MaterialTheme.colorScheme.primary) }
            Tab.BLOCKED -> if (blockedCount > 0) {
                IconButton(onClick = onExportCsv) { Icon(Icons.Default.FileDownload, contentDescription = "Export blocked calls", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = onClearHistory) { Icon(Icons.Default.ClearAll, contentDescription = "Clear blocked history", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

// ── Whitelist Content ──

@Composable
private fun WhitelistContent(entries: List<WhitelistedNumber>, onRemove: (WhitelistedNumber) -> Unit) {
    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(6.dp))
                Text("No numbers whitelisted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            entries.forEach { entry ->
                WhitelistRow(number = entry.phoneNumber, label = entry.label, onRemove = { onRemove(entry) })
                HorizontalDivider(modifier = Modifier.padding(start = 36.dp), thickness = 0.5.dp)
            }
        }
    }
}

// ── Blocked Content ──

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BlockedContent(groups: List<CallGroup>, onWhitelist: (BlockedCall) -> Unit, onDelete: (List<Long>) -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var selectedCall by remember { mutableStateOf<BlockedCall?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateSetOf<Long>() }
    val sheetState = rememberModalBottomSheetState()

    if (groups.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(6.dp))
                Text("No blocked calls yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groups.forEach { group ->
                stickyHeader(key = "header_${group.header}") {
                    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(start = 12.dp, top = 8.dp, bottom = 2.dp)) {
                        Text("${group.header} (${group.calls.size})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                // Group identical numbers to show call-frequency badge
                val byNumber = group.calls.groupBy { it.phoneNumber }
                byNumber.forEach { (_, calls) ->
                    val first = calls.first()
                    val isSelected = selectedIds.contains(first.id)
                    item(key = "num_${first.id}") {
                        BlockedCallRow(
                            call = first,
                            frequency = calls.size,
                            isSelected = isSelected,
                            onWhitelist = { onWhitelist(first) },
                            onTap = {
                                if (selectionMode) {
                                    if (isSelected) selectedIds.remove(first.id) else selectedIds.add(first.id)
                                } else {
                                    selectedCall = first
                                }
                            },
                            onLongPress = {
                                selectionMode = true
                                selectedIds.add(first.id)
                            },
                        )
                    }
                }
            }
        }
    }

    // Batch delete bar
    if (selectionMode) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth().padding(12.dp).clickable {
                onDelete(selectedIds.toList())
                selectedIds.clear()
                selectionMode = false
            }) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.width(8.dp))
                Text("Delete ${selectedIds.size} blocked", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                if (selectedIds.isNotEmpty()) Text("Done", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }

    if (selectedCall != null) {
        val call = selectedCall!!
        ModalBottomSheet(onDismissRequest = { selectedCall = null }, sheetState = sheetState) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text(call.phoneNumber, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(call.blockedAtMillis)),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                // Whitelist
                Row(Modifier.fillMaxWidth().clickable {
                    onWhitelist(call); selectedCall = null
                }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Whitelist number", style = MaterialTheme.typography.bodyMedium)
                }
                // Copy number
                Row(Modifier.fillMaxWidth().clickable {
                    clipboard.setText(AnnotatedString(call.phoneNumber)); selectedCall = null
                }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Copy number", style = MaterialTheme.typography.bodyMedium)
                }
                // Call back
                Row(Modifier.fillMaxWidth().clickable {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${call.phoneNumber}")))
                    selectedCall = null
                }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Call back", style = MaterialTheme.typography.bodyMedium)
                }
                // Report spam (stub for now — future keyword/number classification)
                Row(Modifier.fillMaxWidth().clickable { selectedCall = null }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text("Report as spam", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Sub-components ──

private data class BadgeStyle(val bg: Color, val dot: Color, val label: String, val text: Color)

@Composable
private fun RoleBadge(isActive: Boolean, isPaused: Boolean, pauseRemainingMinutes: Int, onTap: () -> Unit) {
    val style = when {
        isPaused -> BadgeStyle(Color(0xFFFFF3E0), Color(0xFFF59E0B), "Paused • ${pauseRemainingMinutes.coerceAtLeast(1)}m", Color(0xFFB45309))
        isActive -> BadgeStyle(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.primary, "Active", MaterialTheme.colorScheme.primary)
        else -> BadgeStyle(Color(0xFFFEF2F2), Color(0xFFDC2626), "Inactive", Color(0xFFDC2626))
    }
    Surface(shape = RoundedCornerShape(14.dp), color = style.bg,
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onTap)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(style.dot))
            Spacer(Modifier.width(6.dp))
            Text(style.label, style = MaterialTheme.typography.labelSmall, color = style.text)
        }
    }
}

@Composable
private fun WhitelistRow(number: String, label: String?, onRemove: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(number, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
            if (label != null) Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun BlockedCallRow(call: BlockedCall, frequency: Int, isSelected: Boolean, onWhitelist: () -> Unit, onTap: () -> Unit, onLongPress: () -> Unit) {
    Row(Modifier.fillMaxWidth()
        .combinedClickable(onClick = onTap, onLongClick = onLongPress)
        .padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isSelected) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Selected", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
        } else {
            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            Spacer(Modifier.width(10.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(call.phoneNumber, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (frequency > 1) {
                    Spacer(Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Text("×$frequency", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                    }
                }
            }
            Text(SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(call.blockedAtMillis)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onWhitelist) { Icon(Icons.Default.PersonAdd, contentDescription = "Whitelist", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 36.dp), thickness = 0.5.dp)
}

// ── Dialogs ──

@Composable
private fun AddWhitelistDialog(number: String, label: String, onNumberChange: (String) -> Unit, onLabelChange: (String) -> Unit, onAdd: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to whitelist", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = number, onValueChange = onNumberChange, label = { Text("Phone number") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = label, onValueChange = onLabelChange, label = { Text("Label (optional)") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = onAdd, enabled = number.isNotBlank()) {
                Text("Add", color = if (number.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun UpdateConfirmDialog(version: String, releaseNotes: String, isPreview: Boolean, downloading: Boolean, onCancel: () -> Unit, onUpdate: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Update to v$version")
                if (isPreview) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Text("Preview", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        },
        text = { Column {
            if (releaseNotes.isNotBlank()) {
                Text("What's changed", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(releaseNotes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else Text("Version $version is ready to install.", style = MaterialTheme.typography.bodyMedium)
            if (downloading) { Spacer(Modifier.height(12.dp)); CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) }
        } },
        confirmButton = { TextButton(onClick = onUpdate, enabled = !downloading) { Text("Update") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
    )
}

@Composable
private fun ClearHistoryDialog(total: Int, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear blocked history?") },
        text = { Text("This will permanently delete all $total blocked call records. No undo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Clear All", color = Color(0xFFDC2626)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun weekDateRange(): String {
    val cal = java.util.Calendar.getInstance()
    val today = cal.get(java.util.Calendar.DAY_OF_WEEK)
    val monOffset = (today - java.util.Calendar.MONDAY + 7) % 7
    cal.add(java.util.Calendar.DAY_OF_YEAR, -monOffset)
    val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
    val start = fmt.format(cal.time)
    cal.add(java.util.Calendar.DAY_OF_YEAR, 6)
    val end = fmt.format(cal.time)
    return "$start – $end"
}

private fun relativeTime(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}

private fun latestChangelog(): List<String> = listOf(
    "Batch select blocked numbers (long-press) and delete in bulk",
    "Whitelist removal now asks for confirmation",
    "Removed duplicate header label in the calls card",
)


