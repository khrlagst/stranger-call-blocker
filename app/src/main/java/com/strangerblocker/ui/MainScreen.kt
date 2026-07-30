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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strangerblocker.data.BlockedCall
import com.strangerblocker.data.UpdateInfo
import com.strangerblocker.data.WhitelistedNumber
import com.strangerblocker.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Emerald = Color(0xFF10B981)
private val EmeraldDark = Color(0xFF059669)
private val Gray500 = Color(0xFF6B7280)
private val Emerald50 = Color(0xFFECFDF5)
private val Gray300 = Color(0xFFD1D5DB)
private val Gray200 = Color(0xFFE5E5E5)

// ── Root ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val bottomNavTab by viewModel.bottomNavTab.collectAsState()
    val showAbout by viewModel.showAbout.collectAsState()
    val isBlockingEnabled by viewModel.isBlockingEnabled.collectAsState()
    val isRoleHeld by viewModel.isRoleHeld.collectAsState()
    val groupedCalls by viewModel.groupedCalls.collectAsState()
    val totalBlocked by viewModel.totalBlocked.collectAsState()
    val whitelisted by viewModel.whitelisted.collectAsState(initial = emptyList())
    val selectedTab by viewModel.selectedTab.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    val updateDownloading by viewModel.updateDownloading.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationIconStyle by viewModel.notificationIconStyle.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val previewUpdates by viewModel.previewUpdates.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val showClearHistoryDialog by viewModel.showClearHistoryDialog.collectAsState()
    val showAddWhitelistDialog by viewModel.showAddWhitelistDialog.collectAsState()
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

    // ── About screen (full screen, own Scaffold) ──
    if (showAbout) {
        AboutScreen(
            appVersion = appVersion,
            updateInfo = updateInfo,
            updateDownloading = updateDownloading,
            onUpdateClick = {
                viewModel.closeAbout()
                viewModel.openUpdateDialog()
            },
            onBack = viewModel::closeAbout,
        )
        return
    }

    // ── Main layout with bottom nav ──
    Box(Modifier.fillMaxSize()) {
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
                    color = EmeraldDark, modifier = Modifier.weight(1f))
                RoleBadge(isActive = isRoleHeld, onTap = viewModel::refreshRoleStatus)
            }
            HorizontalDivider(thickness = 1.dp)

            // Tab content
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (bottomNavTab) {
                    BottomNavTab.DASHBOARD -> DashboardScreen(
                        totalBlocked = totalBlocked,
                        groupedCalls = groupedCalls,
                    )
                    BottomNavTab.CALLS -> CallsContent(
                        isBlockingEnabled = isBlockingEnabled,
                        groupedCalls = groupedCalls,
                        totalBlocked = totalBlocked,
                        whitelisted = whitelisted,
                        selectedTab = selectedTab,
                        onToggleBlocking = viewModel::toggleBlocking,
                        onSelectTab = viewModel::selectTab,
                        onAddToWhitelist = viewModel::openAddWhitelistDialog,
                        onExportCsv = { saveCsvLauncher.launch("blocked_calls.csv") },
                        onClearHistory = viewModel::openClearHistoryDialog,
                        onRemoveWhitelist = { viewModel.removeFromWhitelist(it.phoneNumber) },
                        onWhitelistCall = { viewModel.addToWhitelist(it.phoneNumber, null) },
                    )
                    BottomNavTab.SMS -> SmsScreen()
                    BottomNavTab.SETTINGS -> SettingsTab(
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsToggle = viewModel::toggleNotifications,
                        notificationIconStyle = notificationIconStyle,
                        onIconStyleChange = viewModel::setNotificationIconStyle,
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
    if (showUpdateDialog && updateInfo != null) {
        UpdateConfirmDialog(
            version = updateInfo!!.latestVersion,
            releaseNotes = updateInfo!!.releaseNotes,
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
                icon = { Icon(item.icon, contentDescription = item.label, tint = if (selectedTab == item.tab) Emerald else Gray500) },
                label = { Text(item.label, fontSize = 10.sp, color = if (selectedTab == item.tab) Emerald else Gray500) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Emerald50),
            )
        }
    }
}

// ── Dashboard Tab ──

@Composable
private fun DashboardScreen(totalBlocked: Int, groupedCalls: List<CallGroup>) {
    val todayCount = groupedCalls.firstOrNull()?.calls?.size ?: 0
    val thisWeekCount = groupedCalls.takeWhile { it.header != "Earlier" }.sumOf { it.calls.size }

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
            Column(Modifier.padding(20.dp)) {
                Text("Total Blocked Today",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = Gray500)
                Spacer(Modifier.height(4.dp))
                Text("$todayCount",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = EmeraldDark)
                Spacer(Modifier.height(4.dp))
                Text("Calls: $todayCount · SMS: 0",
                    style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
        }
        Spacer(Modifier.height(12.dp))

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
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = Gray500)
                    Spacer(Modifier.height(4.dp))
                    Text("$thisWeekCount",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldDark)
                    Text("from $totalBlocked total",
                        style = MaterialTheme.typography.labelSmall, color = Gray500)
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
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = Gray500)
                    Spacer(Modifier.height(4.dp))
                    Text("0",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldDark)
                    Text("from 0 total",
                        style = MaterialTheme.typography.labelSmall, color = Gray500)
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
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = Gray500)
                    Spacer(Modifier.height(4.dp))
                    Text("$totalBlocked",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldDark)
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
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = Gray500)
                    Spacer(Modifier.height(4.dp))
                    Text("0",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldDark)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Weekly chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Weekly Activity",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = Gray500)

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Emerald))
                    Spacer(Modifier.width(4.dp))
                    Text("Calls", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Gray500)
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF6EE7B7)))
                    Spacer(Modifier.width(4.dp))
                    Text("SMS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Gray500)
                }

                if (groupedCalls.isEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("No data yet", style = MaterialTheme.typography.bodySmall, color = Gray500.copy(alpha = 0.6f))
                } else {
                    Spacer(Modifier.height(16.dp))
                    val maxCount = groupedCalls.maxOf { it.calls.size }.coerceAtLeast(1)
                    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(
                        Modifier.fillMaxWidth().height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        groupedCalls.forEachIndexed { idx, group ->
                            val barHeight = (group.calls.size.toFloat() / maxCount * 100f).toInt().coerceAtLeast(4)
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${group.calls.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Gray500)
                                Spacer(Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barHeight.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (group.header == "Today") Emerald else Emerald.copy(alpha = 0.5f)),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(dayLabels.getOrElse(idx) { group.header.take(3) },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Gray500)
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
    totalBlocked: Int,
    whitelisted: List<WhitelistedNumber>,
    selectedTab: Tab,
    onToggleBlocking: (Boolean) -> Unit,
    onSelectTab: (Tab) -> Unit,
    onAddToWhitelist: () -> Unit,
    onExportCsv: () -> Unit,
    onClearHistory: () -> Unit,
    onRemoveWhitelist: (WhitelistedNumber) -> Unit,
    onWhitelistCall: (BlockedCall) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val newTab = Tab.entries[pagerState.currentPage]
        if (newTab != selectedTab) onSelectTab(newTab)
    }

    Column(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 100.dp)) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Block strangers",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldDark)
                    Text(if (isBlockingEnabled) "Unknown numbers are silently rejected" else "All calls ring through",
                        style = MaterialTheme.typography.bodySmall, color = Gray500)
                }
                Switch(
                    checked = isBlockingEnabled,
                    onCheckedChange = onToggleBlocking,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Emerald,
                        checkedTrackColor = Emerald.copy(alpha = 0.2f),
                    ),
                )
            }

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
                                0 -> WhitelistContent(entries = whitelisted, onRemove = onRemoveWhitelist)
                                1 -> BlockedContent(groups = groupedCalls, onWhitelist = onWhitelistCall)
                            }
                        }
                    }
                }
        }
    }
}

// ── SMS Tab ──

@Composable
private fun SmsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📱",
                    style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(12.dp))
                Text("SMS Blocking",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldDark)
                Spacer(Modifier.height(6.dp))
                Text("Coming in a future preview",
                    style = MaterialTheme.typography.bodyMedium, color = Gray500)
                Spacer(Modifier.height(4.dp))
                Text("SMS blocking will be available\nin preview p03 and beyond",
                    style = MaterialTheme.typography.bodySmall, color = Gray500.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 40.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

// ── Settings Tab ──

@Composable
private fun SettingsTab(
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    notificationIconStyle: String,
    onIconStyleChange: (String) -> Unit,
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
            Text("Notifications", style = MaterialTheme.typography.labelLarge, color = EmeraldDark)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Block alerts",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = EmeraldDark)
                    Text("Show blocked call count in status bar",
                        style = MaterialTheme.typography.labelSmall, color = Gray500)
                }
                Switch(checked = notificationsEnabled, onCheckedChange = onNotificationsToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Emerald, checkedTrackColor = Emerald.copy(alpha = 0.2f),
                        uncheckedThumbColor = Gray500, uncheckedTrackColor = Gray300.copy(alpha = 0.4f)))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Spacer(Modifier.height(4.dp))

            Text("Notification icon", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = EmeraldDark)
            Text("Choose how the icon appears in the status bar", style = MaterialTheme.typography.labelSmall, color = Gray500)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth().clickable { onIconStyleChange("shield") }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = notificationIconStyle == "shield", onClick = { onIconStyleChange("shield") })
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(20.dp), tint = Emerald)
                Spacer(Modifier.width(12.dp))
                Column { Text("Shield", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = EmeraldDark)
                    Text("Status bar shows the SB shield icon", style = MaterialTheme.typography.labelSmall, color = Gray500) }
            }
            Row(Modifier.fillMaxWidth().clickable { onIconStyleChange("circle_count") }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = notificationIconStyle == "circle_count", onClick = { onIconStyleChange("circle_count") })
                Spacer(Modifier.width(4.dp))
                Box(Modifier.size(20.dp).background(Emerald, CircleShape), contentAlignment = Alignment.Center) {
                    Text("N", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp))
                }
                Spacer(Modifier.width(12.dp))
                Column { Text("Circle with count", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = EmeraldDark)
                    Text("Status bar shows a circle with today's blocked count", style = MaterialTheme.typography.labelSmall, color = Gray500) }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Theme", style = MaterialTheme.typography.labelLarge, color = EmeraldDark)
            Spacer(Modifier.height(8.dp))
            listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (mode, label) ->
                Row(Modifier.fillMaxWidth().clickable { onThemeChange(mode) }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = themeMode == mode, onClick = { onThemeChange(mode) })
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall, color = EmeraldDark)
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Updates", style = MaterialTheme.typography.labelLarge, color = EmeraldDark)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Preview builds", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = EmeraldDark)
                    Text("Receive pre-release updates before stable release", style = MaterialTheme.typography.labelSmall, color = Gray500)
                }
                Switch(checked = previewUpdates, onCheckedChange = onPreviewToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Emerald, checkedTrackColor = Emerald.copy(alpha = 0.2f),
                        uncheckedThumbColor = Gray500, uncheckedTrackColor = Gray300.copy(alpha = 0.4f)))
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("About", style = MaterialTheme.typography.labelLarge, color = EmeraldDark)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onAbout)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(20.dp), tint = Emerald)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Stranger Blocker", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = EmeraldDark)
                        Text("Version info, changelog & updates", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    }
                    Text("›", style = MaterialTheme.typography.titleMedium, color = Gray300)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

// ── About Screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreen(
    appVersion: String,
    updateInfo: UpdateInfo?,
    updateDownloading: Boolean,
    onUpdateClick: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = EmeraldDark) }
                    Text("About", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldDark)
                }
                HorizontalDivider()
            }
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(48.dp), tint = Emerald)
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Stranger Blocker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldDark)
                    Spacer(Modifier.width(8.dp))
                    Text("v$appVersion", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Gray500)
                }
            }
            Spacer(Modifier.height(6.dp))
            Spacer(Modifier.height(24.dp))

            if (updateInfo != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Emerald50,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onUpdateClick)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald)
                        Spacer(Modifier.width(8.dp))
                        Text("Update to v${updateInfo.latestVersion}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = EmeraldDark, modifier = Modifier.weight(1f))
                        if (updateDownloading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("›", style = MaterialTheme.typography.titleMedium, color = Emerald)
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
            }

            Text("What's new", style = MaterialTheme.typography.labelLarge, color = EmeraldDark,
                modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(horizontal = 20.dp)) {
                latestChangelog().forEach { item ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", style = MaterialTheme.typography.bodySmall, color = Emerald)
                        Text(item, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.weight(1f))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
            Text("github.com/khrlagst/stranger-call-blocker",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = EmeraldDark,
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
                    .padding(vertical = 3.dp).height(36.dp).clip(RoundedCornerShape(20.dp)).background(Emerald))
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
    Row(modifier = modifier.clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).height(36.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = if (selected) Color.White else Gray500)
        Spacer(Modifier.width(5.dp))
        Box(Modifier.size(18.dp).clip(CircleShape).background(if (selected) Color.White.copy(alpha = 0.25f) else Gray300.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center) {
            Text("$count", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = if (selected) Color.White else Gray500)
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
        Text(when (selectedTab) { Tab.WHITELIST -> "Whitelist"; Tab.BLOCKED -> "Blocked" },
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = EmeraldDark,
            modifier = Modifier.weight(1f))
        when (selectedTab) {
            Tab.WHITELIST -> IconButton(onClick = onAddToWhitelist) { Icon(Icons.Default.Add, contentDescription = "Add to whitelist", tint = Emerald) }
            Tab.BLOCKED -> if (blockedCount > 0) {
                IconButton(onClick = onExportCsv) { Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = Gray300) }
                IconButton(onClick = onClearHistory) { Icon(Icons.Default.ClearAll, contentDescription = "Clear history", tint = Gray300) }
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
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(24.dp), tint = Gray300.copy(alpha = 0.5f))
                Spacer(Modifier.height(6.dp))
                Text("No numbers whitelisted", style = MaterialTheme.typography.bodySmall, color = Gray500.copy(alpha = 0.6f))
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BlockedContent(groups: List<CallGroup>, onWhitelist: (BlockedCall) -> Unit) {
    if (groups.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(24.dp), tint = Gray300.copy(alpha = 0.5f))
                Spacer(Modifier.height(6.dp))
                Text("No blocked calls yet", style = MaterialTheme.typography.bodySmall, color = Gray500.copy(alpha = 0.6f))
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groups.forEach { group ->
                stickyHeader(key = "header_${group.header}") {
                    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(start = 12.dp, top = 8.dp, bottom = 2.dp)) {
                        Text("${group.header} (${group.calls.size})", style = MaterialTheme.typography.labelMedium, color = Emerald)
                    }
                }
                items(group.calls, key = { it.id }) { call -> BlockedCallRow(call = call, onWhitelist = { onWhitelist(call) }) }
            }
        }
    }
}

// ── Sub-components ──

@Composable
private fun RoleBadge(isActive: Boolean, onTap: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = if (isActive) Emerald50 else Color(0xFFFEF2F2),
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onTap)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (isActive) Emerald else Color(0xFFDC2626)))
            Spacer(Modifier.width(6.dp))
            Text(if (isActive) "Active" else "Inactive", style = MaterialTheme.typography.labelSmall,
                color = if (isActive) Emerald else Color(0xFFDC2626))
        }
    }
}

@Composable
private fun WhitelistRow(number: String, label: String?, onRemove: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp), tint = Emerald.copy(alpha = 0.5f))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(number, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
            if (label != null) Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
        }
        IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = Gray300) }
    }
}

@Composable
private fun BlockedCallRow(call: BlockedCall, onWhitelist: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp), tint = Emerald.copy(alpha = 0.5f))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(call.phoneNumber, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(call.blockedAtMillis)), style = MaterialTheme.typography.labelSmall, color = Gray500)
        }
        IconButton(onClick = onWhitelist) { Icon(Icons.Default.PersonAdd, contentDescription = "Whitelist", modifier = Modifier.size(16.dp), tint = Gray300) }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 36.dp), thickness = 0.5.dp)
}

// ── Dialogs ──

@Composable
private fun AddWhitelistDialog(number: String, label: String, onNumberChange: (String) -> Unit, onLabelChange: (String) -> Unit, onAdd: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to whitelist", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = number, onValueChange = onNumberChange, label = { Text("Phone number") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = label, onValueChange = onLabelChange, label = { Text("Label (optional)") }, singleLine = true, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = onAdd, enabled = number.isNotBlank()) {
                Text("Add", color = if (number.isNotBlank()) Emerald else Gray300, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun UpdateConfirmDialog(version: String, releaseNotes: String, downloading: Boolean, onCancel: () -> Unit, onUpdate: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Update to v$version") },
        text = { Column {
            if (releaseNotes.isNotBlank()) {
                Text("What's changed", style = MaterialTheme.typography.labelMedium, color = EmeraldDark)
                Spacer(Modifier.height(8.dp))
                Text(releaseNotes, style = MaterialTheme.typography.bodySmall, color = Gray500)
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
        text = { Text("This will permanently delete all $total blocked call records. No undo.", style = MaterialTheme.typography.bodySmall, color = Gray500) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Clear All", color = Color(0xFFDC2626)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun latestChangelog(): List<String> = listOf(
    "Shared header across all tabs with Active badge",
    "Dashboard: full cards set, chart legend, bars from bottom",
    "Calls: fixed padding, tab ripple shape, extra brace removed",
    "Preview builds: check triggers on toggle, version compare fixed",
)


