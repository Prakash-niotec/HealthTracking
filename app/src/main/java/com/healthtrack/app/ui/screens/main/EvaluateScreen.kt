package com.healthtrack.app.ui.screens.main

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthtrack.app.ui.model.UserProfile
import com.healthtrack.app.ui.theme.HealthNexaButton
import com.healthtrack.app.ui.theme.HealthNexaCard
import com.healthtrack.app.ui.theme.HealthNexaChip
import com.healthtrack.app.ui.theme.HealthNexaTextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EvaluateScreen(
    userProfile: UserProfile?,
    onEvaluateIngredient: (name: String, amount: Double, unit: String, condition: String) -> Unit
) {
    var ingredientName by remember { mutableStateOf("Sodium") }
    var amountStr by remember { mutableStateOf("1800") }
    var unit by remember { mutableStateOf("mg") }
    var selectedCondition by remember {
        mutableStateOf(userProfile?.healthConditions?.firstOrNull() ?: "Hypertension")
    }

    val popularIngredients = listOf("Sodium", "Sugar", "Caffeine", "Potassium", "Saturated Fat")
    val availableConditions = listOf("Hypertension", "Type 2 Diabetes", "Kidney Disease", "High Cholesterol")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Card
        HealthNexaCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            elevation = 1.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Rule-based Ingredient Safety Scanner",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Evaluates ingredient risk ratio against specific medical conditions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Common Presets
        Text(
            text = "Quick Select Ingredient:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            popularIngredients.forEach { item ->
                HealthNexaChip(
                    text = item,
                    selected = ingredientName.equals(item, ignoreCase = true),
                    onClick = {
                        ingredientName = item
                        if (item.equals("Sugar", ignoreCase = true)) {
                            amountStr = "35"
                            unit = "g"
                        } else if (item.equals("Sodium", ignoreCase = true)) {
                            amountStr = "1800"
                            unit = "mg"
                        } else if (item.equals("Caffeine", ignoreCase = true)) {
                            amountStr = "150"
                            unit = "mg"
                        }
                    }
                )
            }
        }

        // Evaluation Form Inputs
        HealthNexaCard(modifier = Modifier.fillMaxWidth()) {
            HealthNexaTextField(
                value = ingredientName,
                onValueChange = { ingredientName = it },
                label = "Ingredient Name",
                leadingIcon = Icons.Rounded.Search
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthNexaTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = "Amount per Serving",
                    modifier = Modifier.weight(1f)
                )

                HealthNexaTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = "Unit (mg/g)",
                    modifier = Modifier.weight(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Target Health Condition:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableConditions.forEach { cond ->
                    HealthNexaChip(
                        text = cond,
                        selected = selectedCondition.equals(cond, ignoreCase = true),
                        onClick = { selectedCondition = cond }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HealthNexaButton(
                text = "Evaluate Ingredient Safety",
                icon = Icons.Rounded.Shield,
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 100.0
                    onEvaluateIngredient(ingredientName, amount, unit, selectedCondition)
                }
            )
        }
    }
}
