package com.healthtrack.app.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.healthtrack.app.ui.model.HydrationLog
import com.healthtrack.app.ui.model.MedicationItem
import com.healthtrack.app.ui.model.UserProfile
import com.healthtrack.app.ui.navigation.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    currentTab: MainTab,
    userProfile: UserProfile?,
    hydrationLogs: List<HydrationLog>,
    medications: List<MedicationItem>,
    onTabSelected: (MainTab) -> Unit,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToEditMedication: (String) -> Unit = {},
    onAddHydration: (Int, String) -> Unit,
    onRestoreHydration: (HydrationLog) -> Unit = {},
    onDeleteHydration: (String) -> Unit,
    onToggleMedicationTaken: (String) -> Unit,
    onDeleteMedication: (String) -> Unit,
    onEvaluateIngredient: (String, Double, String, String) -> Unit,
    onSignOutClicked: () -> Unit,
    onOpenReminderSettings: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HEALTHNEXA",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val icon: ImageVector = when (tab) {
                        MainTab.HOME -> Icons.Rounded.Home
                        MainTab.WATER -> Icons.Rounded.WaterDrop
                        MainTab.MEDS -> Icons.Rounded.Medication
                        MainTab.EVALUATE -> Icons.Rounded.QrCodeScanner
                        MainTab.PROFILE -> Icons.Rounded.Person
                    }

                    val displayTitle = if (tab == MainTab.MEDS) "Meds" else tab.title

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(imageVector = icon, contentDescription = displayTitle) },
                        label = {
                            Text(
                                text = displayTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                MainTab.HOME -> HomeScreen(
                    userProfile = userProfile,
                    hydrationLogs = hydrationLogs,
                    medications = medications,
                    onNavigateTab = onTabSelected,
                    onNavigateToAddMedication = onNavigateToAddMedication
                )

                MainTab.WATER -> WaterScreen(
                    userProfile = userProfile,
                    hydrationLogs = hydrationLogs,
                    onAddHydration = onAddHydration,
                    onRestoreHydration = onRestoreHydration,
                    onDeleteHydration = onDeleteHydration,
                    onOpenSettings = onOpenReminderSettings
                )

                MainTab.MEDS -> MedsScreen(
                    medications = medications,
                    onToggleTaken = onToggleMedicationTaken,
                    onDeleteMedication = onDeleteMedication,
                    onNavigateToAddMedication = onNavigateToAddMedication,
                    onNavigateToEditMedication = onNavigateToEditMedication
                )

                MainTab.EVALUATE -> EvaluateScreen(
                    userProfile = userProfile,
                    onEvaluateIngredient = onEvaluateIngredient
                )

                MainTab.PROFILE -> ProfileScreen(
                    userProfile = userProfile,
                    onSignOutClicked = onSignOutClicked,
                    onOpenSettings = onOpenReminderSettings
                )
            }
        }
    }
}
