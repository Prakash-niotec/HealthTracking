package com.healthtrack.app.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtrack.app.ui.model.MedicationItem
import com.healthtrack.app.ui.model.DoseTime
import com.healthtrack.app.ui.model.MedicationHistoryEntry
import com.healthtrack.app.ui.theme.HealthNexaButton
import com.healthtrack.app.ui.theme.HealthNexaCard
import com.healthtrack.app.ui.theme.HealthNexaChip
import com.healthtrack.app.ui.theme.HealthNexaTextField

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationScreen(
    medicationToEdit: MedicationItem? = null,
    onBackClicked: () -> Unit,
    onSaveMedication: (MedicationItem) -> Unit
) {
    var name by remember { mutableStateOf(medicationToEdit?.name ?: "") }
    var formFactor by remember { mutableStateOf(medicationToEdit?.formFactor ?: "Pill/Tablet") }
    var strength by remember { mutableStateOf(medicationToEdit?.strength ?: "500") }
    var unit by remember { mutableStateOf(medicationToEdit?.unit ?: "mg") }

    var frequency by remember { mutableStateOf(medicationToEdit?.frequency ?: "Once daily") }

    val doseTimes = remember {
        mutableStateListOf<DoseTime>().apply {
            if (medicationToEdit != null && medicationToEdit.doseTimes.isNotEmpty()) {
                addAll(medicationToEdit.doseTimes)
            } else {
                add(DoseTime("8:00 AM", "Take with food / breakfast"))
            }
        }
    }

    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
    val selectedDays = remember {
        mutableStateListOf<String>().apply {
            if (medicationToEdit != null && medicationToEdit.activeDays.isNotEmpty()) {
                addAll(medicationToEdit.activeDays)
            } else {
                addAll(daysOfWeek)
            }
        }
    }

    var refillReminder by remember { mutableStateOf(medicationToEdit?.refillReminder ?: true) }
    var stockCount by remember { mutableStateOf(medicationToEdit?.stockCount ?: 30) }

    var specialInstructions by remember { mutableStateOf(medicationToEdit?.specialInstructions ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val nameSuggestions = listOf("Metformin XR", "Amoxicillin", "Atorvastatin")
    val formFactorOptions = listOf("Pill/Tablet", "Capsule", "Liquid/Drops", "Injection", "Inhaler")
    val unitOptions = listOf("mg", "mcg", "ml", "IU", "pills")
    val frequencyOptions = listOf("Once daily", "Twice daily", "Three times daily")
    val mealTagOptions = listOf(
        "Take with food / breakfast",
        "Take with lunch",
        "Take with dinner",
        "Take before meals",
        "Empty stomach"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (medicationToEdit != null) "Edit Medication" else "Add New Medication",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Sticky Bottom CTA Button
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    HealthNexaButton(
                        text = "Save Medication & Set Reminders",
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Please enter medication name"
                            } else {
                                val dosageStr = "$strength $unit"
                                val primaryTiming = doseTimes.firstOrNull()?.time ?: "8:00 AM"
                                val updatedItem = MedicationItem(
                                    id = medicationToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    dosage = dosageStr,
                                    timing = primaryTiming,
                                    frequency = frequency,
                                    formFactor = formFactor,
                                    isTaken = medicationToEdit?.isTaken ?: false,
                                    nextDoseTime = if (medicationToEdit?.isTaken == true) "Completed for today" else "Today, $primaryTiming",
                                    strength = strength,
                                    unit = unit,
                                    doseTimes = doseTimes.toList(),
                                    activeDays = selectedDays.toList(),
                                    refillReminder = refillReminder,
                                    stockCount = stockCount,
                                    specialInstructions = specialInstructions.trim(),
                                    history = medicationToEdit?.history ?: emptyList()
                                )
                                onSaveMedication(updatedItem)
                            }
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ==========================================
            // SECTION 1: MEDICATION BASICS
            // ==========================================
            HealthNexaCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "1. Medication Basics",
                    icon = Icons.Rounded.Medication
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Name input
                HealthNexaTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = "Medication Name",
                    leadingIcon = Icons.Rounded.Medication
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestions Chips
                Text(
                    text = "Suggestions:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    nameSuggestions.forEach { suggestion ->
                        HealthNexaChip(
                            text = suggestion,
                            selected = name.equals(suggestion, ignoreCase = true),
                            onClick = {
                                name = suggestion
                                errorMessage = null
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form Factor Chips
                Text(
                    text = "Form Factor:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    formFactorOptions.forEach { factor ->
                        HealthNexaChip(
                            text = factor,
                            selected = formFactor.equals(factor, ignoreCase = true),
                            onClick = { formFactor = factor }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Strength & Unit
                Text(
                    text = "Strength & Unit:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HealthNexaTextField(
                        value = strength,
                        onValueChange = { strength = it },
                        label = "Strength Value",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Unit",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            unitOptions.forEach { u ->
                                HealthNexaChip(
                                    text = u,
                                    selected = unit.equals(u, ignoreCase = true),
                                    onClick = { unit = u }
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 2: FREQUENCY & TIMING
            // ==========================================
            HealthNexaCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "2. Frequency & Timing",
                    icon = Icons.Rounded.AccessTime
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Frequency:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    frequencyOptions.forEach { freq ->
                        HealthNexaChip(
                            text = freq,
                            selected = frequency.equals(freq, ignoreCase = true),
                            onClick = {
                                frequency = freq
                                if (freq == "Once daily" && doseTimes.size > 1) {
                                    while (doseTimes.size > 1) doseTimes.removeLast()
                                } else if (freq == "Twice daily" && doseTimes.size < 2) {
                                    doseTimes.add(DoseTime("6:00 PM", "Take with dinner"))
                                } else if (freq == "Three times daily" && doseTimes.size < 3) {
                                    if (doseTimes.size == 1) doseTimes.add(DoseTime("1:00 PM", "Take with lunch"))
                                    doseTimes.add(DoseTime("8:00 PM", "Take with dinner"))
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Dose Times & Meal Instructions:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                doseTimes.forEachIndexed { index, doseTime ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dose ${index + 1}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                if (doseTimes.size > 1) {
                                    IconButton(
                                        onClick = { doseTimes.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remove dose time",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            HealthNexaTextField(
                                value = doseTime.time,
                                onValueChange = { newTime ->
                                    doseTimes[index] = doseTime.copy(time = newTime)
                                },
                                label = "Time (e.g. 8:00 AM)",
                                leadingIcon = Icons.Rounded.AccessTime
                            )

                            Text(
                                text = "Meal Tag:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                mealTagOptions.forEach { tag ->
                                    HealthNexaChip(
                                        text = tag,
                                        selected = doseTime.mealTag == tag,
                                        onClick = {
                                            doseTimes[index] = doseTime.copy(mealTag = tag)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val nextHour = (8 + doseTimes.size * 4) % 24
                        val period = if (nextHour >= 12) "PM" else "AM"
                        val formattedHour = if (nextHour % 12 == 0) 12 else nextHour % 12
                        doseTimes.add(DoseTime("$formattedHour:00 $period", "Take with food"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.AddCircleOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+ Add Another Dose Time")
                }
            }

            // ==========================================
            // SECTION 3: SCHEDULE & DURATION
            // ==========================================
            HealthNexaCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "3. Schedule & Duration",
                    icon = Icons.Rounded.CalendarToday
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Active Treatment Days:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays.remove(day)
                                    } else {
                                        selectedDays.add(day)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Refill Reminder Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Refill Reminder",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Get notified when stock is low",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = refillReminder,
                        onCheckedChange = { refillReminder = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stock Count Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Stock Count:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (stockCount > 0) stockCount-- },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Rounded.Remove, contentDescription = "Decrease stock")
                        }

                        Text(
                            text = "$stockCount pills",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { stockCount++ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Increase stock")
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 4: CLINICAL SAFETY & NOTES
            // ==========================================
            HealthNexaCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "4. Clinical Safety & Notes",
                    icon = Icons.Rounded.MedicalServices
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interaction Guidance Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Interaction Guidance",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ensure doses are taken with adequate water. If taking with antihypertensives or diabetes medications, monitor blood pressure and blood glucose levels regularly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HealthNexaTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it },
                    label = "Special Instructions / Notes",
                    singleLine = false,
                    modifier = Modifier.height(100.dp)
                )
            }

            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
