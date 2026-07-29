package com.strangerblocker.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strangerblocker.data.BlockedCall
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
    val exportIntent by viewModel.exportIntent.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val updateAvailable by viewModel.updateAvailable.collectAsState()
    val updateDownloading by viewModel.updateDownloading.collectAsState()
    val showAboutDialog by viewModel.showAboutDialog.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
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
                        if (totalBlocked > 0) {
                            Text(
                                "$totalBlocked blocked",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500,
                            )
                        }
                    }
                    RoleBadge(isActive = isRoleHeld, onTap = viewModel::refreshRoleStatus)
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = viewModel::openAboutDialog) {
                        Icon(
                            Icons.Default.Info, contentDescription = "About",
                            tint = Gray300,
                        )
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
                .padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Update banner ──
            if (updateAvailable && updateInfo != null) {
                UpdateBanner(
                    version = updateInfo!!.latestVersion,
                    downloading = updateDownloading,
                    onUpdate = viewModel::openUpdateDialog,
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Toggle row (no card) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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

            Spacer(Modifier.height(8.dp))

            // ── Whitelist card (border, scroll max 132dp) ──
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(max = 132.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(1.dp, Gray200),
            ) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Whitelist",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = EmeraldDark,
                            modifier = Modifier.weight(1f),
                        )
                        if (whitelisted.isNotEmpty()) {
                            Text(
                                "${whitelisted.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500,
                            )
                        }
                        IconButton(onClick = viewModel::openAddWhitelistDialog) {
                            Icon(
                                Icons.Default.Add, contentDescription = "Add to whitelist",
                                tint = Emerald,
                            )
                        }
                    }
                    if (whitelisted.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "No numbers whitelisted",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500.copy(alpha = 0.6f),
                            )
                        }
                    } else {
                        whitelisted.forEach { entry ->
                            WhitelistRow(
                                number = entry.phoneNumber,
                                label = entry.label,
                                onRemove = { viewModel.removeFromWhitelist(entry.phoneNumber) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Blocked card (weight 1f, LazyColumn) ──
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                border = BorderStroke(1.dp, Gray200),
            ) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Blocked",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = EmeraldDark,
                            )
                            if (totalBlocked > 0) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "$totalBlocked calls",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray500,
                                )
                            }
                        }
                        if (totalBlocked > 0) {
                            IconButton(onClick = { viewModel.exportCsv() }) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Export CSV",
                                    tint = Gray300)
                            }
                            IconButton(onClick = viewModel::clearHistory) {
                                Icon(Icons.Default.ClearAll, contentDescription = "Clear history",
                                    tint = Gray300)
                            }
                        }
                    }

                    if (groupedCalls.isEmpty()) {
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
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        ) {
                            groupedCalls.forEach { group ->
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
                                        onWhitelist = {
                                            viewModel.addToWhitelist(call.phoneNumber, null)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(
            appVersion = appVersion,
            onDismiss = viewModel::closeAboutDialog,
        )
    }

    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            version = updateInfo!!.latestVersion,
            downloading = updateDownloading,
            onDownload = viewModel::downloadAndInstall,
            onDismiss = viewModel::closeUpdateDialog,
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
private fun AboutDialog(appVersion: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About") },
        text = {
            Column {
                Text("Stranger Blocker", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("v$appVersion", style = MaterialTheme.typography.bodySmall, color = Gray500)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("What's new", style = MaterialTheme.typography.labelLarge, color = EmeraldDark)
                Spacer(Modifier.height(6.dp))
                Text("1.5.0 — UI redesign, emerald theme, about/update dialogs", style = MaterialTheme.typography.bodySmall)
                Text("1.4.0 — Larger header, badge in top bar, about dialog", style = MaterialTheme.typography.bodySmall)
                Text("1.3.0 — Minimal redesign, persistent signing key", style = MaterialTheme.typography.bodySmall)
                Text("1.2.0 — Whitelist, grouped history, CSV export", style = MaterialTheme.typography.bodySmall)
                Text("1.1.0 — OTA updates, private number blocking", style = MaterialTheme.typography.bodySmall)
                Text("1.0.0 — Initial release", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Silently blocks incoming calls from unknown numbers.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("github.com/khrlagst/stranger-call-blocker", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = EmeraldDark)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun UpdateDialog(
    version: String,
    downloading: Boolean,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update available") },
        text = {
            Column {
                Text(
                    "Version $version is ready to install.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    if (downloading) "Downloading\u2026" else "Download the latest APK and install it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                )
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
            TextButton(onClick = onDownload, enabled = !downloading) {
                Text("Download & Install")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Later") }
        },
    )
}

@Composable
private fun UpdateBanner(version: String, downloading: Boolean, onUpdate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Emerald50,
        ),
        onClick = { if (!downloading) onUpdate() },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Download, contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Emerald,
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Update v$version available",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    if (downloading) "Downloading\u2026" else "Tap to download & install",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500,
                )
            }
            if (downloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}