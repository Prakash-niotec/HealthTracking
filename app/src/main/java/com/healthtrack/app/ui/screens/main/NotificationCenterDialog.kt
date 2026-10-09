package com.healthtrack.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.healthtrack.app.data.local.LocalCache
import com.healthtrack.app.data.local.LocalNotificationHistoryRepository
import com.healthtrack.app.data.model.NotificationRecord
import com.healthtrack.app.notifications.NotificationHelper
import com.healthtrack.app.ui.theme.HealthNexaCard
import com.healthtrack.app.ui.theme.HealthNexaChip
import com.healthtrack.app.ui.theme.HealthNexaTextField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationCenterDialog(
    onDismiss: () -> Unit,
    onSaveSettings: (intervalMin: Int, startHour: Int, endHour: Int, waterEnabled: Boolean, waterGoalMl: Int) -> Unit
) {
    val context = LocalContext.current
    val historyRepo = remember { LocalNotificationHistoryRepository(LocalCache(context)) }
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0 = History, 1 = Settings
    var historyLogs by remember { mutableStateOf<List<NotificationRecord>>(emptyList()) }

    // Load history
    remember {
        coroutineScope.launch {
            historyRepo.historyFlow.collect { list ->
                historyLogs = list
            }
        }
    }

    var waterEnabled by remember { mutableStateOf(true) }
    var intervalMin by remember { mutableStateOf("60") }
    var startHour by remember { mutableStateOf("8") }
    var endHour by remember { mutableStateOf("22") }
    var waterGoalMlStr by remember { mutableStateOf("2500") }

    val intervalPresets = listOf(30, 60, 90, 120)
    val dateFormatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notification Center", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Selector Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthNexaChip(
                        text = "History (${historyLogs.size})",
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = Icons.Rounded.History,
                        modifier = Modifier.weight(1f)
                    )

                    HealthNexaChip(
                        text = "Settings",
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = Icons.Rounded.Settings,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        text = {
            if (activeTab == 0) {
                // HISTORY TAB
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (historyLogs.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text(
                                text = "No notification history recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch { historyRepo.clearHistory() }
                                }
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Clear history", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear History", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(historyLogs, key = { it.id }) { record ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = record.category,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = dateFormatter.format(Date(record.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = record.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text = record.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // SETTINGS TAB
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Water Reminders", fontWeight = FontWeight.Bold)
                        Switch(
                            checked = waterEnabled,
                            onCheckedChange = { waterEnabled = it }
                        )
                    }

                    HorizontalDivider()

                    Text("Reminder Interval (Minutes):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        intervalPresets.forEach { p ->
                            HealthNexaChip(
                                text = "${p}m",
                                selected = intervalMin == p.toString(),
                                onClick = { intervalMin = p.toString() }
                            )
                        }
                    }

                    HealthNexaTextField(
                        value = intervalMin,
                        onValueChange = { intervalMin = it },
                        label = "Custom Interval (15-480 min)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    HorizontalDivider()

                    Text("Active Hours Window:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HealthNexaTextField(
                            value = startHour,
                            onValueChange = { startHour = it },
                            label = "Start Hour (0-23)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        HealthNexaTextField(
                            value = endHour,
                            onValueChange = { endHour = it },
                            label = "End Hour (0-23)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider()

                    Text("Daily Water Goal:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    HealthNexaTextField(
                        value = waterGoalMlStr,
                        onValueChange = { waterGoalMlStr = it },
                        label = "Goal (500-10000 ml)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedButton(
                        onClick = { waterGoalMlStr = "2500" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to Recommended (2500 ml)")
                    }

                    HorizontalDivider()

                    Button(
                        onClick = {
                            NotificationHelper.sendTestNotification(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Test Notification")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val interval = intervalMin.toIntOrNull()?.coerceIn(15, 480) ?: 60
                val start = startHour.toIntOrNull()?.coerceIn(0, 23) ?: 8
                val end = endHour.toIntOrNull()?.coerceIn(0, 23) ?: 22
                val goal = waterGoalMlStr.toIntOrNull()?.coerceIn(500, 10000) ?: 2500

                onSaveSettings(interval, start, end, waterEnabled, goal)
                onDismiss()
            }) {
                Text(if (activeTab == 1) "Save Settings" else "Close")
            }
        },
        dismissButton = {
            if (activeTab == 1) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
