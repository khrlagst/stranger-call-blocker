package com.strangerblocker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
) {
    val isBlockingEnabled by viewModel.isBlockingEnabled.collectAsState()
    val isRoleHeld by viewModel.isRoleHeld.collectAsState()
    val blockedCalls by viewModel.blockedCalls.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stranger Blocker") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Role status banner ──
            RoleBanner(isRoleHeld = isRoleHeld, onCheck = viewModel::refreshRoleStatus)

            Spacer(Modifier.height(16.dp))

            // ── Toggle card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Block unknown callers",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = if (isBlockingEnabled)
                                "Unknown numbers will be silently rejected"
                            else
                                "All calls ring through normally",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = isBlockingEnabled,
                        onCheckedChange = viewModel::toggleBlocking,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Block history header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Blocked Calls",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (blockedCalls.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearHistory) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear history",
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Blocked calls list ──
            if (blockedCalls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.height(48.dp),
                            tint = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No blocked calls yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(blockedCalls, key = { it.id }) { call ->
                        BlockedCallRow(call)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleBanner(
    isRoleHeld: Boolean,
    onCheck: () -> Unit,
) {
    val (icon, text, color) = if (isRoleHeld) {
        Triple(
            Icons.Default.Shield,
            "Call Screening is active",
            MaterialTheme.colorScheme.primary,
        )
    } else {
        Triple(
            Icons.Default.PhoneForwarded,
            "Call Screening role not granted — blocking won't work",
            MaterialTheme.colorScheme.error,
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f),
        ),
        onClick = onCheck,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
            )
            Spacer(Modifier.padding(start = 8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BlockedCallRow(call: com.strangerblocker.data.BlockedCall) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.padding(start = 12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.callerName ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = call.phoneNumber,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = dateFormat.format(Date(call.blockedAtMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
