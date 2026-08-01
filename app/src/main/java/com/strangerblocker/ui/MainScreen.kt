package com.strangerblocker.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strangerblocker.data.BlockedCall
import com.strangerblocker.data.BlockedSms
import com.strangerblocker.data.UpdateCheckResult
import com.strangerblocker.data.UpdateInfo
import com.strangerblocker.data.WhitelistedNumber
import com.strangerblocker.ui.theme.ThemeMode
import kotlinx.coroutines.delay
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
    val smsKeywords by viewModel.smsKeywords.collectAsState()
    val smsSearchQuery by viewModel.smsSearchQuery.collectAsState()
    val smsNotificationAccessGranted by viewModel.smsNotificationAccessGranted.collectAsState()
    val groupedBlockedSms by viewModel.groupedBlockedSms.collectAsState()
    val filteredGroupedSms by viewModel.filteredGroupedSms.collectAsState()
    val filteredWhitelistedSms by viewModel.filteredWhitelistedSms.collectAsState()
    val totalSmsBlocked by viewModel.totalSmsBlocked.collectAsState()
    val notificationIconStyle by viewModel.notificationIconStyle.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val previewUpdates by viewModel.previewUpdates.collectAsState()
    val weeklyCounts by viewModel.weeklyCounts.collectAsState()
    val weeklySmsCounts by viewModel.weeklySmsCounts.collectAsState()
    val callFilterFrom by viewModel.callFilterFrom.collectAsState()
    val callFilterTo by viewModel.callFilterTo.collectAsState()
    val callFilterCount by viewModel.callFilterCount.collectAsState()
    val smsFilterFrom by viewModel.smsFilterFrom.collectAsState()
    val smsFilterTo by viewModel.smsFilterTo.collectAsState()
    val smsFilterCount by viewModel.smsFilterCount.collectAsState()
    val showClearHistoryDialog by viewModel.showClearHistoryDialog.collectAsState()
    val showClearSmsHistoryDialog by viewModel.showClearSmsHistoryDialog.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val pendingUpdate by viewModel.pendingUpdate.collectAsState()
    val showAddWhitelistDialog by viewModel.showAddWhitelistDialog.collectAsState()
    val pendingWhitelistRemoval by viewModel.pendingWhitelistRemoval.collectAsState()
    val whitelistInputNumber by viewModel.whitelistInputNumber.collectAsState()
    val whitelistInputLabel by viewModel.whitelistInputLabel.collectAsState()
    val showManualBlockDialog by viewModel.showManualBlockDialog.collectAsState()
    val manualBlockInput by viewModel.manualBlockInput.collectAsState()
    val pendingManualBlocks by viewModel.pendingManualBlocks.collectAsState()
    val pendingWhitelistConfirm by viewModel.pendingWhitelistConfirm.collectAsState()
    val pendingKeywordRemoval by viewModel.pendingKeywordRemoval.collectAsState()
    val context = LocalContext.current

    // Live clock so the pause countdown ("59'") ticks down in the header.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = System.currentTimeMillis()
        }
    }

    // Reset UI state when the app returns to the foreground (skip the first start).
    val lifecycleOwner = LocalLifecycleOwner.current
    var firstUiStart by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (firstUiStart) firstUiStart = false else viewModel.resetUiState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var showFabOptions by remember { mutableStateOf(false) }
    var smsPermissionGranted by remember { mutableStateOf(checkSmsPermission(context)) }
    val isPaused = pauseUntil > now
    val callsActive = isRoleHeld && isBlockingEnabled && !isPaused
    val smsActive = smsPermissionGranted && smsBlockingEnabled && !isPaused
    val blockingState = when {
        !isRoleHeld -> BlockingBannerState.ROLE_MISSING
        callsActive && smsActive -> BlockingBannerState.ALL_ACTIVE
        callsActive -> BlockingBannerState.CALLS_ONLY
        smsActive -> BlockingBannerState.SMS_ONLY
        isPaused -> BlockingBannerState.PAUSED
        else -> BlockingBannerState.NONE
    }
    var pendingCallRoleEnable by remember { mutableStateOf(false) }
    var pendingSmsPermissionEnable by remember { mutableStateOf(false) }

    val saveCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri: Uri? ->
        if (uri != null) viewModel.exportCsvToUri(uri)
    }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refreshRoleStatus()
        val roleHeld = viewModel.isRoleHeld.value
        when {
            pendingCallRoleEnable && roleHeld -> {
                pendingCallRoleEnable = false
                viewModel.toggleBlocking(true)
                Toast.makeText(context, "Call screening role granted", Toast.LENGTH_SHORT).show()
            }
            pendingCallRoleEnable -> {
                pendingCallRoleEnable = false
                Toast.makeText(context, "Call screening role is required — call blocking won't work without it", Toast.LENGTH_LONG).show()
            }
            !roleHeld -> Toast.makeText(context, "Call screening role is required — call blocking won't work without it", Toast.LENGTH_LONG).show()
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        smsPermissionGranted = granted
        when {
            granted && pendingSmsPermissionEnable -> {
                pendingSmsPermissionEnable = false
                viewModel.toggleSmsBlocking(true)
                Toast.makeText(context, "SMS permission granted", Toast.LENGTH_SHORT).show()
            }
            !granted -> {
                pendingSmsPermissionEnable = false
                Toast.makeText(context, "SMS permission is required — SMS blocking won't work without it", Toast.LENGTH_LONG).show()
            }
        }
    }

    val pickContactLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact(),
    ) { uri: Uri? ->
        if (uri != null) {
            val number = context.contentResolver.query(
                uri,
                arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
            if (number != null) viewModel.whitelistInputNumber.value = number
        }
    }

    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }
    }

    // Update dialog — rendered above both About and the main screens.
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

    // ── About screen (full screen, own Scaffold) ──
    if (showAbout) {
        AboutScreen(
            appVersion = appVersion,
            updateCheckResult = updateCheckResult,
            updateDownloading = updateDownloading,
            checkingForUpdates = checkingForUpdates,
            onCheckForUpdates = viewModel::checkForUpdates,
            onUpdateClick = { update ->
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
                val isPaused = pauseUntil > now
                if (isRoleHeld) {
                    if (isPaused) {
                        Text("${((pauseUntil - now) / 60_000).toInt().coerceAtLeast(1)}'",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFB45309), modifier = Modifier.padding(end = 2.dp))
                        IconButton(onClick = {
                            viewModel.resumeBlocking()
                            Toast.makeText(context, "Screening is active again", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Resume screening", tint = Color(0xFFB45309))
                        }
                    } else {
                        IconButton(onClick = {
                            viewModel.pauseBlocking(60)
                            Toast.makeText(context, "Screening is paused for 1 hour", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.Pause, contentDescription = "Pause screening", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                RoleBadge(
                    isActive = isRoleHeld,
                    isPaused = isPaused,
                    onTap = {
                        if (isPaused) viewModel.resumeBlocking()
                        else viewModel.refreshRoleStatus()
                    },
                )
            }
            HorizontalDivider(thickness = 1.dp)

            if (bottomNavTab != BottomNavTab.SETTINGS) {
                BlockingStatusBanner(
                    state = blockingState,
                    remainingMinutes = ((pauseUntil - now) / 60_000).toInt().coerceAtLeast(1),
                    onTap = {
                        when (blockingState) {
                            BlockingBannerState.ROLE_MISSING -> {
                                pendingCallRoleEnable = false
                                val rm = context.getSystemService(RoleManager::class.java)
                                roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                            }
                            BlockingBannerState.ALL_ACTIVE -> {}
                            BlockingBannerState.CALLS_ONLY,
                            BlockingBannerState.SMS_ONLY,
                            BlockingBannerState.NONE -> viewModel.selectBottomTab(BottomNavTab.SETTINGS)
                            BlockingBannerState.PAUSED -> viewModel.resumeBlocking()
                        }
                    },
                )
            }

            // Tab content
            Surface(Modifier.weight(1f).fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                when (bottomNavTab) {
                    BottomNavTab.DASHBOARD -> DashboardScreen(
                        totalBlocked = totalBlocked,
                        groupedCalls = groupedCalls,
                        groupedBlockedSms = groupedBlockedSms,
                        totalSmsBlocked = totalSmsBlocked,
                        weeklyCounts = weeklyCounts,
                        weeklySmsCounts = weeklySmsCounts,
                        recentBlocked = recentBlocked,
                        onQuickWhitelist = { call ->
                            if (whitelisted.any { it.phoneNumber == call.phoneNumber }) {
                                Toast.makeText(context, "Already on the whitelist", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.requestWhitelistConfirm(call.phoneNumber, "")
                            }
                        },
                    )
                    BottomNavTab.CALLS -> CallsContent(
                        filteredGroupedCalls = filteredGroupedCalls,
                        filteredWhitelisted = filteredWhitelisted,
                        searchQuery = searchQuery,
                        totalBlocked = totalBlocked,
                        whitelisted = whitelisted,
                        selectedTab = selectedTab,
                        filterFrom = callFilterFrom,
                        filterTo = callFilterTo,
                        filterCount = callFilterCount,
                        onFilterApply = { from, to ->
                            viewModel.callFilterFrom.value = from
                            viewModel.callFilterTo.value = to
                        },
                        onSelectTab = viewModel::selectTab,
                        onSearchChange = viewModel::setSearchQuery,
                        onAddToWhitelist = viewModel::openAddWhitelistDialog,
                        onClearHistory = viewModel::openClearHistoryDialog,
                        onRemoveWhitelist = viewModel::requestRemoveWhitelist,
                        onWhitelistCall = { call ->
                            if (whitelisted.any { it.phoneNumber == call.phoneNumber }) {
                                Toast.makeText(context, "Already on the whitelist", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.requestWhitelistConfirm(call.phoneNumber, "")
                            }
                        },
                        onDeleteBlocked = viewModel::deleteBlockedByIds,
                    )
                    BottomNavTab.SMS -> SmsScreen(
                        filteredGroupedSms = filteredGroupedSms,
                        filteredWhitelistedSms = filteredWhitelistedSms,
                        smsSearchQuery = smsSearchQuery,
                        totalSmsBlocked = totalSmsBlocked,
                        whitelisted = whitelisted,
                        selectedTab = selectedTab,
                        filterFrom = smsFilterFrom,
                        filterTo = smsFilterTo,
                        filterCount = smsFilterCount,
                        onFilterApply = { from, to ->
                            viewModel.smsFilterFrom.value = from
                            viewModel.smsFilterTo.value = to
                        },
                        onSelectTab = viewModel::selectTab,
                        onSearchChange = viewModel::setSmsSearchQuery,
                        onAddToWhitelist = viewModel::openAddWhitelistDialog,
                        onRemoveWhitelist = viewModel::requestRemoveWhitelist,
                        onWhitelistSms = { sms ->
                            if (whitelisted.any { it.phoneNumber == sms.senderNumber }) {
                                Toast.makeText(context, "Already on the whitelist", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.requestWhitelistConfirm(sms.senderNumber, "")
                            }
                        },
                        onDeleteBlockedSms = viewModel::deleteBlockedSmsByIds,
                        onClearHistory = viewModel::openClearSmsHistoryDialog,
                    )
                    BottomNavTab.SETTINGS -> SettingsTab(
                        isBlockingEnabled = isBlockingEnabled,
                        onToggleBlocking = { enabled ->
                            if (enabled && !isRoleHeld) {
                                pendingCallRoleEnable = true
                                Toast.makeText(context, "Call screening role is required for blocking", Toast.LENGTH_SHORT).show()
                                val rm = context.getSystemService(RoleManager::class.java)
                                roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                            } else {
                                viewModel.toggleBlocking(enabled)
                                Toast.makeText(context, if (enabled) "Unknown numbers are silently rejected" else "All calls ring through", Toast.LENGTH_SHORT).show()
                            }
                        },
                        smsBlockingEnabled = smsBlockingEnabled,
                        onToggleSmsBlocking = { enabled ->
                            if (enabled && !smsPermissionGranted) {
                                pendingSmsPermissionEnable = true
                                Toast.makeText(context, "SMS permission is required for blocking", Toast.LENGTH_SHORT).show()
                                smsPermissionLauncher.launch(android.Manifest.permission.RECEIVE_SMS)
                            } else {
                                viewModel.toggleSmsBlocking(enabled)
                                Toast.makeText(context, if (enabled) "Messages from unknown senders are silently blocked" else "All SMS messages ring through", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isRoleHeld = isRoleHeld,
                        smsPermissionGranted = smsPermissionGranted,
                        onRequestCallScreeningRole = {
                            pendingCallRoleEnable = false
                            val rm = context.getSystemService(RoleManager::class.java)
                            roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                        },
                        onRequestSmsPermission = {
                            pendingSmsPermissionEnable = false
                            smsPermissionLauncher.launch(android.Manifest.permission.RECEIVE_SMS)
                        },
                        smsNotificationAccessGranted = smsNotificationAccessGranted,
                        onRequestSmsNotificationAccess = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        },
                        onExportData = { saveCsvLauncher.launch("blocked_calls.csv") },
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsToggle = viewModel::toggleNotifications,
                        notificationIconStyle = notificationIconStyle,
                        onIconStyleChange = viewModel::setNotificationIconStyle,
                        smsKeywords = smsKeywords,
                        onAddSmsKeyword = viewModel::addSmsKeyword,
                        onRequestRemoveSmsKeyword = viewModel::requestRemoveSmsKeyword,
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
                onSelectTab = { tab ->
                    viewModel.selectBottomTab(tab)
                    if (tab == BottomNavTab.SETTINGS) {
                        viewModel.refreshRoleStatus()
                        smsPermissionGranted = checkSmsPermission(context)
                        viewModel.refreshSmsNotificationAccess()
                    }
                },
            )
        }

        // Floating action button — quick actions on Dashboard / Calls / SMS
        if (bottomNavTab != BottomNavTab.SETTINGS) {
            Box(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 96.dp)) {
                FloatingActionButton(
                    onClick = { showFabOptions = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Quick actions")
                }
                if (showFabOptions) {
                    val bubbleOffset = with(LocalDensity.current) { IntOffset(0, -104.dp.toPx().toInt()) }
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = bubbleOffset,
                        onDismissRequest = { showFabOptions = false },
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp,
                        ) {
                            Column(Modifier.width(110.dp).padding(vertical = 2.dp)) {
                                Row(Modifier.fillMaxWidth().clickable {
                                    showFabOptions = false
                                    viewModel.openManualBlockDialog()
                                }.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Manual block", style = MaterialTheme.typography.labelMedium)
                                }
                                HorizontalDivider(thickness = 0.5.dp)
                                Row(Modifier.fillMaxWidth().clickable {
                                    showFabOptions = false
                                    viewModel.openAddWhitelistDialog()
                                }.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Whitelist", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Global dialogs
    if (showClearHistoryDialog) {
        ClearHistoryDialog(
            total = totalBlocked,
            noun = "blocked call records",
            onDismiss = viewModel::closeClearHistoryDialog,
            onConfirm = viewModel::confirmClearHistory,
        )
    }
    if (showClearSmsHistoryDialog) {
        ClearHistoryDialog(
            total = totalSmsBlocked,
            noun = "blocked SMS messages",
            onDismiss = viewModel::closeClearSmsHistoryDialog,
            onConfirm = viewModel::confirmClearSmsHistory,
        )
    }
    if (showAddWhitelistDialog) {
        AddWhitelistDialog(
            number = whitelistInputNumber,
            label = whitelistInputLabel,
            onNumberChange = { viewModel.whitelistInputNumber.value = it },
            onLabelChange = { viewModel.whitelistInputLabel.value = it },
            onAdd = viewModel::requestWhitelistConfirm,
            onDismiss = viewModel::closeAddWhitelistDialog,
            onPickContact = { pickContactLauncher.launch(null) },
            onPickRecent = {
                recentBlocked.firstOrNull()?.let { viewModel.whitelistInputNumber.value = it.phoneNumber }
            },
        )
    }
    if (pendingWhitelistRemoval != null) {
        val entry = pendingWhitelistRemoval!!
        AlertDialog(
            onDismissRequest = viewModel::cancelRemoveWhitelist,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Remove from whitelist?") },
            text = { Text("${entry.phoneNumber} will no longer be allowed through.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = viewModel::confirmRemoveWhitelist) { Text("Remove", color = Color(0xFFDC2626)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelRemoveWhitelist) { Text("Cancel") } },
        )
    }

    // Manual block overlay
    if (showManualBlockDialog) {
        ManualBlockDialog(
            input = manualBlockInput,
            onInputChange = { viewModel.manualBlockInput.value = it },
            onSave = viewModel::requestManualBlockConfirm,
            onDismiss = viewModel::closeManualBlockDialog,
        )
    }
    if (pendingManualBlocks.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = viewModel::cancelManualBlocksConfirm,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Block ${if (pendingManualBlocks.size == 1) "number" else "numbers"}?") },
            text = {
                Column {
                    Text("These numbers will be blocked for both calls and SMS:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    pendingManualBlocks.forEach {
                        Text(it, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::confirmManualBlocks) { Text("Block", color = Color(0xFFDC2626)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelManualBlocksConfirm) { Text("Cancel") } },
        )
    }
    if (pendingWhitelistConfirm != null) {
        val entry = pendingWhitelistConfirm!!
        AlertDialog(
            onDismissRequest = viewModel::cancelWhitelistConfirm,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Add to whitelist?") },
            text = { Text("${entry.phoneNumber} will be allowed through for both calls and SMS.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = {
                viewModel.confirmWhitelistConfirm()
                Toast.makeText(context, "${entry.phoneNumber} added to whitelist", Toast.LENGTH_SHORT).show()
            }) { Text("Add", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = viewModel::cancelWhitelistConfirm) { Text("Cancel") } },
        )
    }
    if (pendingKeywordRemoval != null) {
        val keyword = pendingKeywordRemoval!!
        AlertDialog(
            onDismissRequest = viewModel::cancelRemoveSmsKeyword,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Remove keyword?") },
            text = { Text("\"$keyword\" will no longer be blocked in SMS messages.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = viewModel::confirmRemoveSmsKeyword) { Text("Remove", color = Color(0xFFDC2626)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelRemoveSmsKeyword) { Text("Cancel") } },
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
    groupedBlockedSms: List<SmsGroup>,
    totalSmsBlocked: Int,
    weeklyCounts: List<Int>,
    weeklySmsCounts: List<Int>,
    recentBlocked: List<BlockedCall>,
    onQuickWhitelist: (BlockedCall) -> Unit,
) {
    val todayCount = groupedCalls.firstOrNull { it.header == "Today" }?.calls?.size ?: 0
    val thisWeekCount = groupedCalls.takeWhile { it.header != "Earlier" }.sumOf { it.calls.size }
    val todaySmsCount = groupedBlockedSms.firstOrNull { it.header == "Today" }?.smsList?.size ?: 0
    val smsThisWeekCount = groupedBlockedSms.takeWhile { it.header != "Earlier" }.sumOf { it.smsList.size }

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
                Text("Calls: $todayCount · SMS: $todaySmsCount",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("$smsThisWeekCount",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text("from $totalSmsBlocked total",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Column(Modifier.padding(16.dp)) {
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
                if (weeklyCounts.all { it == 0 } && weeklySmsCounts.all { it == 0 }) {
                    Text("No data this week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                } else {
                    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                    val maxVal = maxOf(weeklyCounts.max(), weeklySmsCounts.max()).coerceAtLeast(1)
                    // Baseline at bottom: bars grow UPWARD, calls + SMS side by side per day
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                        weeklyCounts.forEachIndexed { i, count ->
                            val smsCount = weeklySmsCounts[i]
                            val callsBarHeight = (count.toFloat() / maxVal * 96f).toInt().coerceAtLeast(if (count > 0) 4 else 0)
                            val smsBarHeight = (smsCount.toFloat() / maxVal * 96f).toInt().coerceAtLeast(if (smsCount > 0) 4 else 0)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth().height(120.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("$count",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(callsBarHeight.dp.coerceAtLeast(2.dp))
                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                .background(MaterialTheme.colorScheme.primary),
                                        )
                                    }
                                    Spacer(Modifier.width(3.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("$smsCount",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(smsBarHeight.dp.coerceAtLeast(2.dp))
                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(dayNames[i],
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Recent Activity — last 5 blocked calls (dense)
        if (recentBlocked.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Recent Activity", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.05.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    recentBlocked.forEach { call ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
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
        }
        Spacer(Modifier.height(100.dp))
    }
}

// ── Calls Tab ──

@Composable
private fun CallsContent(
    filteredGroupedCalls: List<CallGroup>,
    filteredWhitelisted: List<WhitelistedNumber>,
    searchQuery: String,
    totalBlocked: Int,
    whitelisted: List<WhitelistedNumber>,
    selectedTab: Tab,
    filterFrom: Long?,
    filterTo: Long?,
    filterCount: Int,
    onFilterApply: (Long?, Long?) -> Unit,
    onSelectTab: (Tab) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddToWhitelist: () -> Unit,
    onClearHistory: () -> Unit,
    onRemoveWhitelist: (WhitelistedNumber) -> Unit,
    onWhitelistCall: (BlockedCall) -> Unit,
    onDeleteBlocked: (List<Long>) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var selectionMode by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        val newTab = Tab.entries[pagerState.currentPage]
        if (newTab != selectedTab) onSelectTab(newTab)
    }

    Column(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp)) {
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
                SearchField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = "Search number or label",
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 12.dp),
                )
                CardHeader(
                    selectedTab = selectedTab,
                    blockedCount = totalBlocked,
                    selectedCount = if (selectionMode) selectedIds.size else 0,
                    filterFrom = filterFrom,
                    filterTo = filterTo,
                    filterCount = filterCount,
                    onAddToWhitelist = onAddToWhitelist,
                    onFilterApply = onFilterApply,
                    onClearHistory = onClearHistory,
                )
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (page) {
                            0 -> BlockedContent(
                                groups = filteredGroupedCalls,
                                onWhitelist = onWhitelistCall,
                                selectedIds = selectedIds,
                                selectionMode = selectionMode,
                                onToggleSelect = { call ->
                                    selectedIds = if (selectedIds.contains(call.id)) selectedIds - call.id else selectedIds + call.id
                                },
                                onLongPress = { call ->
                                    selectionMode = true
                                    selectedIds = selectedIds + call.id
                                },
                                onClearSelection = {
                                    selectedIds = emptySet()
                                    selectionMode = false
                                },
                                onDelete = { ids ->
                                    onDeleteBlocked(ids)
                                    selectedIds = emptySet()
                                    selectionMode = false
                                },
                            )
                            1 -> WhitelistContent(entries = filteredWhitelisted, onRemove = onRemoveWhitelist)
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
    filteredGroupedSms: List<SmsGroup>,
    filteredWhitelistedSms: List<WhitelistedNumber>,
    smsSearchQuery: String,
    totalSmsBlocked: Int,
    whitelisted: List<WhitelistedNumber>,
    selectedTab: Tab,
    filterFrom: Long?,
    filterTo: Long?,
    filterCount: Int,
    onFilterApply: (Long?, Long?) -> Unit,
    onSelectTab: (Tab) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddToWhitelist: () -> Unit,
    onRemoveWhitelist: (WhitelistedNumber) -> Unit,
    onWhitelistSms: (BlockedSms) -> Unit,
    onDeleteBlockedSms: (List<Long>) -> Unit,
    onClearHistory: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var selectedSmsIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var smsSelectionMode by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        val newTab = Tab.entries[pagerState.currentPage]
        if (newTab != selectedTab) onSelectTab(newTab)
    }

    Column(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp)) {
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
                SearchField(
                    value = smsSearchQuery,
                    onValueChange = onSearchChange,
                    placeholder = "Search sender or message",
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 12.dp),
                )
                CardHeader(
                    selectedTab = selectedTab,
                    blockedCount = totalSmsBlocked,
                    selectedCount = if (smsSelectionMode) selectedSmsIds.size else 0,
                    filterFrom = filterFrom,
                    filterTo = filterTo,
                    filterCount = filterCount,
                    onAddToWhitelist = onAddToWhitelist,
                    onFilterApply = onFilterApply,
                    onClearHistory = onClearHistory,
                )
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (page) {
                            0 -> SmsBlockedContent(
                                groups = filteredGroupedSms,
                                selectedSmsIds = selectedSmsIds,
                                selectionMode = smsSelectionMode,
                                onToggleSelect = { sms ->
                                    selectedSmsIds = if (selectedSmsIds.contains(sms.id)) selectedSmsIds - sms.id else selectedSmsIds + sms.id
                                },
                                onLongPress = { sms ->
                                    smsSelectionMode = true
                                    selectedSmsIds = selectedSmsIds + sms.id
                                },
                                onClearSelection = {
                                    selectedSmsIds = emptySet()
                                    smsSelectionMode = false
                                },
                                onWhitelist = onWhitelistSms,
                                onDelete = { ids ->
                                    onDeleteBlockedSms(ids)
                                    selectedSmsIds = emptySet()
                                    smsSelectionMode = false
                                },
                            )
                            1 -> WhitelistContent(entries = filteredWhitelistedSms, onRemove = onRemoveWhitelist)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SmsBlockedContent(
    groups: List<SmsGroup>,
    selectedSmsIds: Set<Long>,
    selectionMode: Boolean,
    onToggleSelect: (BlockedSms) -> Unit,
    onLongPress: (BlockedSms) -> Unit,
    onClearSelection: () -> Unit,
    onWhitelist: (BlockedSms) -> Unit,
    onDelete: (List<Long>) -> Unit,
) {
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
                    BlockedSmsRow(
                        sms = sms,
                        isSelected = selectedSmsIds.contains(sms.id),
                        onWhitelist = { onWhitelist(sms) },
                        onTap = { if (selectionMode) onToggleSelect(sms) },
                        onLongPress = { onLongPress(sms) },
                    )
                }
            }
        }
    }

    // Batch delete bar
    if (selectionMode) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth().padding(12.dp).clickable {
                onDelete(selectedSmsIds.toList())
                onClearSelection()
            }) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.width(8.dp))
                Text("Delete ${selectedSmsIds.size} messages", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                if (selectedSmsIds.isNotEmpty()) Text("Done", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BlockedSmsRow(sms: BlockedSms, isSelected: Boolean, onWhitelist: () -> Unit, onTap: () -> Unit, onLongPress: () -> Unit) {
    Row(Modifier.fillMaxWidth()
        .combinedClickable(onClick = onTap, onLongClick = onLongPress)
        .padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(sms.senderNumber, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sms.messageBody, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(sms.blockedAtMillis)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (sms.blockReason.startsWith("KEYWORD")) {
                    Spacer(Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Text("keyword", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                    }
                }
            }
        }
        if (isSelected) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Selected", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
        }
        IconButton(onClick = onWhitelist) { Icon(Icons.Default.PersonAdd, contentDescription = "Whitelist", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 36.dp), thickness = 0.5.dp)
}

// ── Settings Tab ──

@Composable
private fun SettingsTab(
    isBlockingEnabled: Boolean,
    onToggleBlocking: (Boolean) -> Unit,
    smsBlockingEnabled: Boolean,
    onToggleSmsBlocking: (Boolean) -> Unit,
    isRoleHeld: Boolean,
    smsPermissionGranted: Boolean,
    smsNotificationAccessGranted: Boolean,
    onRequestCallScreeningRole: () -> Unit,
    onRequestSmsPermission: () -> Unit,
    onRequestSmsNotificationAccess: () -> Unit,
    onExportData: () -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    notificationIconStyle: String,
    onIconStyleChange: (String) -> Unit,
    smsKeywords: List<String>,
    onAddSmsKeyword: (String) -> Unit,
    onRequestRemoveSmsKeyword: (String) -> Unit,
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
            Text("Blocking", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(start = 16.dp)) {
                Row(Modifier.fillMaxWidth().clickable(enabled = !isRoleHeld, onClick = onRequestCallScreeningRole).padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Block stranger calls",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                        Text(
                            if (isRoleHeld) "Unknown numbers are silently rejected" else "Call screening role not granted — tap to grant",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRoleHeld) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        )
                    }
                    Switch(checked = isBlockingEnabled, onCheckedChange = onToggleBlocking,
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().clickable(enabled = !smsPermissionGranted, onClick = onRequestSmsPermission).padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Block stranger SMS",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                        Text(
                            if (smsPermissionGranted) "Messages from unknown senders are silently blocked" else "SMS permission not granted — tap to grant",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (smsPermissionGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        )
                    }
                    Switch(checked = smsBlockingEnabled, onCheckedChange = onToggleSmsBlocking,
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().clickable(enabled = !smsNotificationAccessGranted, onClick = onRequestSmsNotificationAccess).padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Dismiss SMS notifications",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                        Text(
                            if (smsNotificationAccessGranted) "Blocked SMS notifications are cleared automatically"
                            else "Required on Android 11+ — tap to enable notification access",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (smsNotificationAccessGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Notifications", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(start = 16.dp)) {
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
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("SMS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(start = 16.dp)) {
                Text("Block by keyword", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                Text("Block SMS containing any of these words", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                val keywordContext = LocalContext.current
                var keywordInput by remember { mutableStateOf("") }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SearchField(
                        value = keywordInput,
                        onValueChange = { keywordInput = it },
                        placeholder = "Add keyword",
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (keywordInput.isBlank()) {
                            Toast.makeText(keywordContext, "Enter a keyword first", Toast.LENGTH_SHORT).show()
                        } else {
                            onAddSmsKeyword(keywordInput)
                            keywordInput = ""
                        }
                    }) { Text("Add keyword", color = MaterialTheme.colorScheme.primary) }
                }
                smsKeywords.forEach { kw ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(kw, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRequestRemoveSmsKeyword(kw) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove keyword", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Data", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(start = 16.dp)) {
                Text("Export blocked data", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                Text("Save blocked call history as a CSV file", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onExportData, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export CSV")
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Theme", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(start = 16.dp)) {
                listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (mode, label) ->
                    Row(Modifier.fillMaxWidth().clickable { onThemeChange(mode) }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = themeMode == mode, onClick = { onThemeChange(mode) })
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Updates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(start = 16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Preview builds", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.primary)
                        Text("Receive pre-release updates before stable release", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = previewUpdates, onCheckedChange = onPreviewToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)))
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("About", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(start = 16.dp)) {
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
            }
            Spacer(Modifier.height(100.dp))
        }
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
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary) }
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
                    if (item.startsWith("## ")) {
                        Text(item.removePrefix("## "), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
                    } else {
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text(item, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.weight(1f))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
            val aboutContext = LocalContext.current
            Text("github.com/khrlagst/stranger-call-blocker",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, textDecoration = TextDecoration.Underline),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp).clickable {
                    aboutContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/khrlagst/stranger-call-blocker")))
                })
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
                    TabItem("Blocked", blockedCount, selectedTab == Tab.BLOCKED,
                        { onSelectTab(Tab.BLOCKED) }, Modifier.weight(1f))
                    TabItem("Whitelist", whitelistCount, selectedTab == Tab.WHITELIST,
                        { onSelectTab(Tab.WHITELIST) }, Modifier.weight(1f))
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
    selectedTab: Tab,
    blockedCount: Int,
    selectedCount: Int,
    filterFrom: Long?,
    filterTo: Long?,
    filterCount: Int,
    onAddToWhitelist: () -> Unit,
    onFilterApply: (Long?, Long?) -> Unit,
    onClearHistory: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        when (selectedTab) {
            Tab.WHITELIST -> {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onAddToWhitelist) { Icon(Icons.Default.Add, contentDescription = "Add to whitelist", tint = MaterialTheme.colorScheme.primary) }
            }
            Tab.BLOCKED -> {
                if (selectedCount > 0) {
                    Text("$selectedCount selected", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (blockedCount > 0) {
                    DateRangeFilterButton(from = filterFrom, to = filterTo, count = filterCount, onApply = onFilterApply)
                    IconButton(onClick = onClearHistory) { Icon(Icons.Default.ClearAll, contentDescription = "Clear blocked history", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilterButton(from: Long?, to: Long?, count: Int, onApply: (Long?, Long?) -> Unit) {
    var showPopup by remember { mutableStateOf(false) }
    var pendingFrom by remember { mutableStateOf<Long?>(null) }
    var pendingTo by remember { mutableStateOf<Long?>(null) }
    var pickerTarget by remember { mutableStateOf<Boolean?>(null) }
    val active = from != null || to != null

    Box {
        IconButton(onClick = {
            pendingFrom = from
            pendingTo = to
            showPopup = true
        }) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter by date",
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (active && count > 0) {
            Box(Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(16.dp).clip(CircleShape)
                .background(Color(0xFFDC2626)), contentAlignment = Alignment.Center) {
                Text("$count", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = Color.White)
            }
        }
        if (showPopup) {
            val filterOffset = with(LocalDensity.current) { IntOffset(0, 8.dp.toPx().toInt()) }
            Popup(alignment = Alignment.TopEnd, offset = filterOffset, onDismissRequest = { showPopup = false }) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 6.dp, shadowElevation = 8.dp) {
                    Column(Modifier.width(190.dp).padding(vertical = 8.dp)) {
                        Text("Filter by date", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
                        Row(Modifier.fillMaxWidth().clickable { pickerTarget = true }.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("From", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Text(fmtFilterDate(pendingFrom), style = MaterialTheme.typography.labelMedium)
                        }
                        Row(Modifier.fillMaxWidth().clickable { pickerTarget = false }.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("To", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Text(fmtFilterDate(pendingTo), style = MaterialTheme.typography.labelMedium)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { pendingFrom = null; pendingTo = null }) { Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            TextButton(onClick = { onApply(pendingFrom, pendingTo); showPopup = false }) { Text("Confirm", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
        pickerTarget?.let { isFrom ->
            val state = rememberDatePickerState(initialSelectedDateMillis = if (isFrom) pendingFrom else pendingTo)
            DatePickerDialog(
                onDismissRequest = { pickerTarget = null },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { sel ->
                            val dayStart = localDayStartMillis(sel)
                            if (isFrom) pendingFrom = dayStart else pendingTo = dayStart + 86_399_999L
                        }
                        pickerTarget = null
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { pickerTarget = null }) { Text("Cancel") } },
            ) {
                DatePicker(state = state)
            }
        }
    }
}

private fun fmtFilterDate(millis: Long?): String =
    if (millis == null) "Any" else SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

private fun localDayStartMillis(utcMidnight: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = utcMidnight
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
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
private fun BlockedContent(
    groups: List<CallGroup>,
    onWhitelist: (BlockedCall) -> Unit,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onToggleSelect: (BlockedCall) -> Unit,
    onLongPress: (BlockedCall) -> Unit,
    onClearSelection: () -> Unit,
    onDelete: (List<Long>) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var selectedCall by remember { mutableStateOf<BlockedCall?>(null) }
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
                                if (selectionMode) onToggleSelect(first) else selectedCall = first
                            },
                            onLongPress = { onLongPress(first) },
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
                onClearSelection()
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
        ModalBottomSheet(onDismissRequest = { selectedCall = null }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceVariant) {
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
private fun RoleBadge(isActive: Boolean, isPaused: Boolean, onTap: () -> Unit) {
    val style = when {
        isPaused -> BadgeStyle(Color(0xFFFFF3E0), Color(0xFFF59E0B), "Paused", Color(0xFFB45309))
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

private enum class BlockingBannerState { ROLE_MISSING, ALL_ACTIVE, CALLS_ONLY, SMS_ONLY, PAUSED, NONE }

@Composable
private fun BlockingStatusBanner(state: BlockingBannerState, remainingMinutes: Int, onTap: () -> Unit) {
    val (bg, fg, label) = when (state) {
        BlockingBannerState.ROLE_MISSING -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), "Call screening role not granted — tap to enable")
        BlockingBannerState.ALL_ACTIVE -> Triple(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.primary, "Callers & SMS blocked")
        BlockingBannerState.CALLS_ONLY -> Triple(Color(0xFFFFF3E0), Color(0xFFB45309), "Callers blocked — SMS goes through")
        BlockingBannerState.SMS_ONLY -> Triple(Color(0xFFFFF3E0), Color(0xFFB45309), "SMS blocked — callers go through")
        BlockingBannerState.PAUSED -> Triple(Color(0xFFFFF3E0), Color(0xFFB45309), "Blocking paused — calls & SMS go through")
        BlockingBannerState.NONE -> Triple(Color(0xFFFFF3E0), Color(0xFFB45309), "Blocking off — calls & SMS go through")
    }
    val tapModifier = if (state != BlockingBannerState.ALL_ACTIVE) Modifier.clickable(onClick = onTap) else Modifier
    Surface(shape = RoundedCornerShape(12.dp), color = bg,
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp).then(tapModifier)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = fg)
        }
    }
}

private fun checkSmsPermission(context: Context): Boolean =
    android.content.pm.PackageManager.PERMISSION_GRANTED ==
        androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS)

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().height(38.dp).padding(horizontal = 12.dp),
            decorationBox = { inner ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    inner()
                }
            },
        )
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

@OptIn(ExperimentalFoundationApi::class)
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
private fun AddWhitelistDialog(number: String, label: String, onNumberChange: (String) -> Unit, onLabelChange: (String) -> Unit, onAdd: (String, String) -> Unit, onDismiss: () -> Unit, onPickContact: () -> Unit, onPickRecent: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = { Text("Add to whitelist", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SearchField(value = number, onValueChange = onNumberChange, placeholder = "Phone number", modifier = Modifier.fillMaxWidth())
                SearchField(value = label, onValueChange = onLabelChange, placeholder = "Label (optional)", modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onPickContact, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                        Text("From contacts", fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = onPickRecent, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                        Text("From recent", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(number, label) }, enabled = number.isNotBlank()) {
                Text("Add", color = if (number.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ManualBlockDialog(input: String, onInputChange: (String) -> Unit, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = { Text("Manual block", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary) },
        text = {
            Column {
                Text("Block these numbers for calls and SMS — separate multiple numbers with commas",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                SearchField(value = input, onValueChange = onInputChange, placeholder = "Enter number(s)", modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(input) }, enabled = input.isNotBlank()) {
                Text("Save", color = if (input.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun UpdateConfirmDialog(version: String, releaseNotes: String, isPreview: Boolean, downloading: Boolean, onCancel: () -> Unit, onUpdate: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
private fun ClearHistoryDialog(total: Int, noun: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = { Text("Clear blocked history?") },
        text = { Text("This will permanently delete all $total $noun. No undo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
    "## Fixes",
    "SMS blocking on Android 11+ — blocked SMS notifications are now dismissed automatically",
    "New Settings entry to enable notification access for the SMS fallback",
    "Precise notification dismissal via a blocked-sender registry",
)


