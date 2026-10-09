package com.healthtrack.app.ui.screens.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.healthtrack.app.ui.theme.HealthNexaButton
import com.healthtrack.app.ui.theme.HealthNexaChip
import com.healthtrack.app.ui.theme.HealthNexaTextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignUpScreen(
    onSignUpClicked: (
        name: String,
        email: String,
        pass: String,
        weight: Double,
        conditions: List<String>,
        hipaaConsent: Boolean,
        onError: (String) -> Unit
    ) -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("70") }
    var hipaaConsentAccepted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val availableConditions = listOf(
        "Hypertension",
        "Type 2 Diabetes",
        "Kidney Disease",
        "High Cholesterol",
        "Sodium Sensitivity",
        "Gluten Intolerance"
    )

    val selectedConditions = remember { mutableStateListOf("Hypertension", "Sodium Sensitivity") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create HealthNexa Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Personalize AI ingredient scanning & daily target calculations",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            HealthNexaTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = "Full Name",
                leadingIcon = Icons.Rounded.Person
            )

            Spacer(modifier = Modifier.height(14.dp))

            HealthNexaTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = "Email Address",
                leadingIcon = Icons.Rounded.Email
            )

            Spacer(modifier = Modifier.height(14.dp))

            HealthNexaTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = "Password (min 6 chars)",
                leadingIcon = Icons.Rounded.Lock
            )

            Spacer(modifier = Modifier.height(14.dp))

            HealthNexaTextField(
                value = weightStr,
                onValueChange = { weightStr = it; errorMessage = null },
                label = "Weight (kg)",
                leadingIcon = Icons.Rounded.MonitorWeight
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Health Conditions & Focus Areas:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableConditions.forEach { condition ->
                        val isSelected = selectedConditions.contains(condition)
                        HealthNexaChip(
                            text = condition,
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedConditions.remove(condition)
                                } else {
                                    selectedConditions.add(condition)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // HIPAA Consent Checkbox
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .clickable {
                            hipaaConsentAccepted = !hipaaConsentAccepted
                            errorMessage = null
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hipaaConsentAccepted,
                        onCheckedChange = {
                            hipaaConsentAccepted = it
                            errorMessage = null
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )

                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )

                    Text(
                        text = "I consent to HIPAA-compliant encrypted processing of my health profile data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HealthNexaButton(
                text = "Create Account",
                enabled = hipaaConsentAccepted,
                onClick = {
                    val weight = weightStr.toDoubleOrNull() ?: 70.0
                    if (email.isBlank() || password.length < 6) {
                        errorMessage = "Please fill in email and password (min 6 characters)"
                    } else if (!hipaaConsentAccepted) {
                        errorMessage = "HIPAA consent is required to create a health profile"
                    } else {
                        onSignUpClicked(
                            name,
                            email,
                            password,
                            weight,
                            selectedConditions.toList(),
                            hipaaConsentAccepted
                        ) { err ->
                            errorMessage = err
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }
    }
}
