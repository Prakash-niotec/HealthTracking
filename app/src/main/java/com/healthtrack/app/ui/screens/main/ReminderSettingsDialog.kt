package com.healthtrack.app.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.healthtrack.app.notifications.NotificationHelper
import com.healthtrack.app.ui.theme.HealthNexaChip
import com.healthtrack.app.ui.theme.HealthNexaTextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSettingsDialog(
    onDismiss: () -> Unit,
    onSaveSettings: (intervalMin: Int, startHour: Int, endHour: Int, waterEnabled: Boolean, waterGoalMl: Int) -> Unit
) {
    val context = LocalContext.current

    var waterEnabled by remember { mutableStateOf(true) }
    var intervalMin by remember { mutableStateOf("60") }
    var startHour by remember { mutableStateOf("8") }
    var endHour by remember { mutableStateOf("22") }
    var waterGoalMlStr by remember { mutableStateOf("2500") }

    val intervalPresets = listOf(30, 60, 90, 120)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reminder Settings", fontWeight = FontWeight.Bold)
            }
        },
        text = {
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
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
