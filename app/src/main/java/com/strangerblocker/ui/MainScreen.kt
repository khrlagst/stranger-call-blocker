package com.strangerblocker.ui

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Emerald = Color(0xFF10B981)
private val EmeraldDark = Color(0xFF059669)
private val Gray500 = Color(0xFF6B7280)
private val Emerald50 = Color(0xFFECFDF5)
private val Gray300 = Color(0xFFD1D5DB)
private val Gray200 = Color(0xFFE5E5E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val isBlockingEnabled by viewModel.isBlockingEnabled.collectAsState()
    val isRoleHeld by viewModel.isRoleHeld.collectAsState()
    val groupedCalls by viewModel.groupedCalls.collectAsState()
    val totalBlocked by viewModel.totalBlocked.collectAsState()
    val whitelisted by viewModel.whitelisted.collectAsState(initial = emptyList())
    val selectedTab by viewModel.selectedTab.collectAsState()
    val exportIntent by viewModel.exportIntent.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    val updateDownloading by viewModel.updateDownloading.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val showClearHistoryDialog by viewModel.showClearHistoryDialog.collectAsState()
    val showAddWhitelistDialog by viewModel.showAddWhitelistDialog.collectAsState()
    val whitelistInputNumber by viewModel.whitelistInputNumber.collectAsState()
    val whitelistInputLabel by viewModel.whitelistInputLabel.collectAsState()
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) { "?" }
    }

    LaunchedEffect(exportIntent) {
        exportIntent?.let {
            context.startActivity(Intent.createChooser(it, "Export blocked calls"))
            viewModel.clearExportIntent()
        }
    }

    Scaffold(
        topBar = {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Stranger Blocker",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = EmeraldDark,
                        )
                    }
                    RoleBadge(isActive = isRoleHeld, onTap = viewModel::refreshRoleStatus)
                    Spacer(Modifier.width(4.dp))
                    Box {
                        IconButton(onClick = viewModel::openSettings) {
                            Icon(
                                Icons.Default.Settings, contentDescription = "Settings",
                                tint = Gray300,
                            )
                        }
                        if (updateAvailable) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Emerald)
                                    .align(Alignment.TopEnd)
                                    .padding(end = 1.dp, top = 1.dp),
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Toggle row ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Block strangers",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = EmeraldDark,
                    )
                    Text(
                        if (isBlockingEnabled) "Unknown numbers are silently rejected"
                        else "All calls ring through",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                    )
                }
                Switch(
                    checked = isBlockingEnabled,
                    onCheckedChange = viewModel::toggleBlocking,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Emerald,
                        checkedTrackColor = Emerald.copy(alpha = 0.2f),
                    ),
                )
            }

            // ── Tab bar ──
            TabBar(
                selectedTab = selectedTab,
                whitelistCount = whitelisted.size,
                blockedCount = totalBlocked,
                onSelectTab = viewModel::selectTab,
            )

            Spacer(Modifier.height(12.dp))

            // ── Shared card container ──
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(Modifier.fillMaxSize()) {
                    // Card header row (actions depend on active tab)
                    CardHeader(
                        selectedTab = selectedTab,
                        whitelistCount = whitelisted.size,
                        blockedCount = totalBlocked,
                        onAddToWhitelist = viewModel::openAddWhitelistDialog,
                        onExportCsv = { viewModel.exportCsv() },
                        onClearHistory = viewModel::openClearHistoryDialog,
                    )

                    // Scrollable content per tab
                    when (selectedTab) {
                        Tab.WHITELIST -> WhitelistContent(
                            entries = whitelisted,
                            onRemove = { viewModel.removeFromWhitelist(it.phoneNumber) },
                        )
                        Tab.BLOCKED -> BlockedContent(
                            groups = groupedCalls,
                            onWhitelist = { viewModel.addToWhitelist(it.phoneNumber, null) },
                        )
                    }
                }
            }
        }
    }

    // ── Dialogs ──

    if (showSettings) {
        SettingsSheet(
            appVersion = appVersion,
            updateInfo = updateInfo,
            updateDownloading = updateDownloading,
            notificationsEnabled = notificationsEnabled,
            onNotificationsToggle = viewModel::toggleNotifications,
            onDismiss = viewModel::closeSettings,
            onUpdateClick = {
                viewModel.closeSettings()
                viewModel.openUpdateDialog()
            },
        )
    }

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

// ── Tab Bar ──

@Composable
private fun TabBar(
    selectedTab: Tab,
    whitelistCount: Int,
    blockedCount: Int,
    onSelectTab: (Tab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        TabItem(
            label = "Whitelist",
            count = whitelistCount,
            selected = selectedTab == Tab.WHITELIST,
            onClick = { onSelectTab(Tab.WHITELIST) },
            modifier = Modifier.weight(1f),
        )
        TabItem(
            label = "Blocked",
            count = blockedCount,
            selected = selectedTab == Tab.BLOCKED,
            onClick = { onSelectTab(Tab.BLOCKED) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabItem(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                color = if (selected) EmeraldDark else Gray500,
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) Emerald else Gray300.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            ) {
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (selected) Color.White else Gray500,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (selected) Emerald else Color.Transparent),
        )
    }
}

// ── Card Header ──

@Composable
private fun CardHeader(
    selectedTab: Tab,
    whitelistCount: Int,
    blockedCount: Int,
    onAddToWhitelist: () -> Unit,
    onExportCsv: () -> Unit,
    onClearHistory: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            when (selectedTab) {
                Tab.WHITELIST -> "Whitelist"
                Tab.BLOCKED -> "Blocked"
            },
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = EmeraldDark,
            modifier = Modifier.weight(1f),
        )
        when (selectedTab) {
            Tab.WHITELIST -> {
                IconButton(onClick = onAddToWhitelist) {
                    Icon(
                        Icons.Default.Add, contentDescription = "Add to whitelist",
                        tint = Emerald,
                    )
                }
            }
            Tab.BLOCKED -> {
                if (blockedCount > 0) {
                    IconButton(onClick = onExportCsv) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV",
                            tint = Gray300)
                    }
                    IconButton(onClick = onClearHistory) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear history",
                            tint = Gray300)
                    }
                }
            }
        }
    }
}

// ── Whitelist Content ──

@Composable
private fun WhitelistContent(
    entries: List<WhitelistedNumber>,
    onRemove: (WhitelistedNumber) -> Unit,
) {
    if (entries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PersonAdd, contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Gray300.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "No numbers whitelisted",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500.copy(alpha = 0.6f),
                )
            }
        }
    } else {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            entries.forEach { entry ->
                WhitelistRow(
                    number = entry.phoneNumber,
                    label = entry.label,
                    onRemove = { onRemove(entry) },
                )
            }
        }
    }
}

// ── Blocked Content ──

@Composable
private fun BlockedContent(
    groups: List<CallGroup>,
    onWhitelist: (BlockedCall) -> Unit,
) {
    if (groups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Shield, contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Gray300.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "No blocked calls yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500.copy(alpha = 0.6f),
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groups.forEach { group ->
                item(key = "header_${group.header}") {
                    Text(
                        group.header,
                        style = MaterialTheme.typography.labelMedium,
                        color = Emerald,
                        modifier = Modifier.padding(
                            start = 12.dp, top = 8.dp, bottom = 2.dp,
                        ),
                    )
                }
                items(group.calls, key = { it.id }) { call ->
                    BlockedCallRow(
                        call = call,
                        onWhitelist = { onWhitelist(call) },
                    )
                }
            }
        }
    }
}

// ── Sub-components ──

@Composable
private fun RoleBadge(isActive: Boolean, onTap: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) Emerald50 else Color(0xFFFEF2F2),
        modifier = Modifier.clickable(onClick = onTap),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Emerald else Color(0xFFDC2626))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (isActive) "Active" else "Inactive",
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) Emerald else Color(0xFFDC2626),
            )
        }
    }
}

@Composable
private fun WhitelistRow(number: String, label: String?, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.PersonAdd, contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Emerald.copy(alpha = 0.5f),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                number,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            )
            if (label != null) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500,
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Delete, contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = Gray300,
            )
        }
    }
}

@Composable
private fun BlockedCallRow(call: BlockedCall, onWhitelist: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Block, contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Emerald.copy(alpha = 0.5f),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                call.phoneNumber,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                dateFormat.format(Date(call.blockedAtMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
            )
        }
        IconButton(onClick = onWhitelist) {
            Icon(
                Icons.Default.PersonAdd, contentDescription = "Whitelist",
                modifier = Modifier.size(16.dp),
                tint = Gray300,
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 36.dp),
        thickness = 0.5.dp,
    )
}

// ── Dialogs ──

@Composable
private fun AddWhitelistDialog(
    number: String,
    label: String,
    onNumberChange: (String) -> Unit,
    onLabelChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to whitelist") },
        text = {
            Column {
                OutlinedTextField(
                    value = number,
                    onValueChange = onNumberChange,
                    label = { Text("Phone number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = onLabelChange,
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAdd, enabled = number.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SettingsSheet(
    appVersion: String,
    updateInfo: UpdateInfo?,
    updateDownloading: Boolean,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onUpdateClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Settings", modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close",
                        tint = Gray300)
                }
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // ── Notifications section ──
                Text(
                    "Notifications",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldDark,
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald50,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Block alerts",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = EmeraldDark,
                            )
                            Text(
                                "Show blocked call count in status bar",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500,
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onNotificationsToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Emerald,
                                checkedTrackColor = Emerald.copy(alpha = 0.2f),
                                uncheckedThumbColor = Gray500,
                                uncheckedTrackColor = Gray300.copy(alpha = 0.4f),
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // ── About section ──
                Text(
                    "About",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldDark,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Stranger Blocker",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "v$appVersion",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                )
                Spacer(Modifier.height(12.dp))

                // Update available row
                if (updateInfo != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Emerald50,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onUpdateClick),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Emerald,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Update to v${updateInfo.latestVersion}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = EmeraldDark,
                                modifier = Modifier.weight(1f),
                            )
                            if (updateDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    "›",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Emerald,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    "What's new",
                    style = MaterialTheme.typography.labelMedium,
                    color = Emerald,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    latestChangelog(),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "github.com/khrlagst/stranger-call-blocker",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = EmeraldDark,
                )
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun UpdateConfirmDialog(
    version: String,
    releaseNotes: String,
    downloading: Boolean,
    onCancel: () -> Unit,
    onUpdate: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Update to v$version") },
        text = {
            Column {
                if (releaseNotes.isNotBlank()) {
                    Text(
                        "What's changed",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldDark,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        releaseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                    )
                } else {
                    Text(
                        "Version $version is ready to install.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (downloading) {
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onUpdate, enabled = !downloading) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
    )
}

@Composable
private fun ClearHistoryDialog(
    total: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear blocked history?") },
        text = {
            Text(
                "This will permanently delete all $total blocked call records. No undo.",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear All", color = Color(0xFFDC2626))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun latestChangelog(): String {
    return "1.7.2 — Version bump from 1.7.0, license, privacy section, README"
}
