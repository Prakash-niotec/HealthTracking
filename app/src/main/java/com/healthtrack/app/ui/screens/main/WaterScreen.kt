package com.healthtrack.app.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrack.app.ui.model.HydrationLog
import com.healthtrack.app.ui.model.UserProfile
import com.healthtrack.app.ui.theme.HealthNexaButton
import com.healthtrack.app.ui.theme.HealthNexaCard
import com.healthtrack.app.ui.theme.HealthNexaChip
import com.healthtrack.app.ui.theme.HealthNexaTextField
import com.healthtrack.app.ui.theme.WaterBlueContainer
import com.healthtrack.app.ui.theme.WaterBluePrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WaterScreen(
    userProfile: UserProfile?,
    hydrationLogs: List<HydrationLog>,
    onAddHydration: (Int, String) -> Unit,
    onRestoreHydration: (HydrationLog) -> Unit = {},
    onDeleteHydration: (String) -> Unit
) {
    val totalWaterMl = hydrationLogs.sumOf { it.amountMl }
    val waterTargetMl = userProfile?.dailyWaterTarget ?: 2500
    val progress = (totalWaterMl.toFloat() / waterTargetMl.toFloat()).coerceIn(0f, 1f)

    var isCustomSelected by remember { mutableStateOf(false) }
    var customAmountStr by remember { mutableStateOf("300") }
    var customLabel by remember { mutableStateOf("Water") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular Progress Card
            HealthNexaCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = WaterBlueContainer
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Today's Hydration",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = WaterBluePrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Progress Canvas
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(180.dp)
                    ) {
                        val trackColor = WaterBluePrimary.copy(alpha = 0.2f)
                        val strokeWidth = 16.dp

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokePx = strokeWidth.toPx()
                            val radius = (size.minDimension - strokePx) / 2
                            val centerPt = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)

                            // Track
                            drawCircle(
                                color = trackColor,
                                radius = radius,
                                center = centerPt,
                                style = Stroke(width = strokePx)
                            )

                            // Progress Arc
                            val sweepAngle = progress * 360f
                            drawArc(
                                color = WaterBluePrimary,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }

                        // Center Info
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WaterDrop,
                                contentDescription = null,
                                tint = WaterBluePrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalWaterMl ml",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "/ $waterTargetMl ml",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = WaterBluePrimary,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (totalWaterMl >= waterTargetMl) {
                        Text(
                            text = "🎉 Daily Hydration Goal Reached!",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = WaterBluePrimary
                        )
                    } else {
                        val remaining = waterTargetMl - totalWaterMl
                        Text(
                            text = "$remaining ml remaining to reach daily target",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Log Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Log Intake:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (hydrationLogs.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val lastLog = hydrationLogs.first()
                                onDeleteHydration(lastLog.id)
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Removed ${lastLog.amountMl} ml (${lastLog.label})",
                                        actionLabel = "UNDO",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onRestoreHydration(lastLog)
                                    }
                                }
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Undo,
                                contentDescription = "Undo last log",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Undo Last", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100 to "+100ml", 250 to "+250ml", 500 to "+500ml").forEach { (amount, label) ->
                        HealthNexaChip(
                            text = label,
                            modifier = Modifier.weight(1f),
                            selected = !isCustomSelected && amount == 250,
                            onClick = {
                                isCustomSelected = false
                                onAddHydration(amount, "Water")
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Added $label Water",
                                        actionLabel = "UNDO",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        val latest = hydrationLogs.firstOrNull()
                                        if (latest != null) {
                                            onDeleteHydration(latest.id)
                                        }
                                    }
                                }
                            }
                        )
                    }

                    HealthNexaChip(
                        text = "Custom",
                        modifier = Modifier.weight(1f),
                        selected = isCustomSelected,
                        onClick = { isCustomSelected = !isCustomSelected }
                    )
                }

                // Custom Log Input Box
                AnimatedVisibility(
                    visible = isCustomSelected,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    HealthNexaCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Log Custom Amount",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HealthNexaTextField(
                                    value = customAmountStr,
                                    onValueChange = { customAmountStr = it },
                                    label = "Amount (ml)",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )

                                HealthNexaTextField(
                                    value = customLabel,
                                    onValueChange = { customLabel = it },
                                    label = "Type (e.g. Green Tea)",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HealthNexaButton(
                                text = "Log Intake",
                                icon = Icons.Rounded.Add,
                                onClick = {
                                    val amount = customAmountStr.toIntOrNull() ?: 250
                                    onAddHydration(amount, customLabel)
                                    isCustomSelected = false
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Added $amount ml ($customLabel)",
                                            actionLabel = "UNDO",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            val latest = hydrationLogs.firstOrNull()
                                            if (latest != null) {
                                                onDeleteHydration(latest.id)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Today's Timeline
            Text(
                text = "Today's Intake Timeline",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

            if (hydrationLogs.isEmpty()) {
                HealthNexaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalDrink,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No intake recorded today yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(hydrationLogs, key = { _, log -> log.id }) { index, log ->
                        HealthNexaCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Timeline Node Indicator
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(32.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = WaterBluePrimary,
                                            modifier = Modifier.size(12.dp)
                                        ) {}
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = "${log.amountMl} ml • ${log.label}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = timeFormatter.format(Date(log.timestamp)),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        onDeleteHydration(log.id)
                                        coroutineScope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Deleted ${log.amountMl} ml log",
                                                actionLabel = "UNDO",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                onRestoreHydration(log)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Delete entry",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
