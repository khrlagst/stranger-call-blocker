package com.strangerblocker.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.strangerblocker.data.BlockedCall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val updateDownloading by viewModel.updateDownloading.collectAsState()
    val context = LocalContext.current
    var showAddWhitelistDialog by remember { mutableStateOf(false) }

    LaunchedEffect(exportIntent) {
        exportIntent?.let {
            context.startActivity(Intent.createChooser(it, "Export blocked calls"))
            viewModel.clearExportIntent()
        }
    }

    Scaffold(
        topBar = {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, top = 16.dp, bottom = 8.dp)) {
                Text("Stranger Blocker", style = MaterialTheme.typography.titleLarge)
                if (totalBlocked > 0) {
                    Text(
                        "$totalBlocked blocked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
        ) {
            // ── Role status ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.refreshRoleStatus() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRoleHeld) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isRoleHeld) "Call screening active"
                    else "Call screening not granted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Update banner ──
            if (updateInfo != null) {
                Spacer(Modifier.height(4.dp))
                UpdateBanner(
                    version = updateInfo!!.latestVersion,
                    downloading = updateDownloading,
                    onUpdate = viewModel::downloadAndInstall,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Toggle ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Block strangers", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (isBlockingEnabled) "Unknown numbers are silently rejected"
                        else "All calls ring through",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = isBlockingEnabled,
                    onCheckedChange = viewModel::toggleBlocking,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    ),
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ── Whitelist ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Whitelist",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                if (whitelisted.isNotEmpty()) {
                    Text(
                        "${whitelisted.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { showAddWhitelistDialog = true }) {
                    Icon(
                        Icons.Default.Add, contentDescription = "Add to whitelist",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (whitelisted.isEmpty()) {
                Text(
                    "No numbers whitelisted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            } else {
                whitelisted.forEach { entry ->
                    WhitelistRow(
                        number = entry.phoneNumber,
                        label = entry.label,
                        onRemove = { viewModel.removeFromWhitelist(entry.phoneNumber) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ── Blocked calls header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Blocked",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                if (totalBlocked > 0) {
                    IconButton(onClick = { viewModel.exportCsv() }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                    IconButton(onClick = viewModel::clearHistory) {
                        Icon(Icons.Default.ClearAll, contentDescription = "Clear history")
                    }
                }
            }

            // ── Blocked calls list ──
            if (groupedCalls.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Shield, contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No blocked calls yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    groupedCalls.forEach { group ->
                        item(key = "header_${group.header}") {
                            Text(
                                group.header,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(group.calls, key = { it.id }) { call ->
                            BlockedCallRow(
                                call = call,
                                onWhitelist = {
                                    viewModel.addToWhitelist(call.phoneNumber, null)
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (showAddWhitelistDialog) {
        AddWhitelistDialog(
            onDismiss = { showAddWhitelistDialog = false },
            onAdd = { number, label ->
                viewModel.addToWhitelist(number, label)
                showAddWhitelistDialog = false
            },
        )
    }
}

// ── Sub-components ──

@Composable
private fun WhitelistRow(number: String, label: String?, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.PersonAdd, contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                number,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
            if (label != null) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Delete, contentDescription = "Remove",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BlockedCallRow(call: BlockedCall, onWhitelist: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Block, contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                call.phoneNumber,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                dateFormat.format(Date(call.blockedAtMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onWhitelist) {
            Icon(
                Icons.Default.PersonAdd, contentDescription = "Whitelist",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddWhitelistDialog(onDismiss: () -> Unit, onAdd: (String, String?) -> Unit) {
    var number by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to whitelist") },
        text = {
            Column {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Phone number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(number, label) }, enabled = number.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun UpdateBanner(version: String, downloading: Boolean, onUpdate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ),
        onClick = { if (!downloading) onUpdate() },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Download, contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Update v$version available",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    if (downloading) "Downloading\u2026" else "Tap to download & install",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (downloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}
