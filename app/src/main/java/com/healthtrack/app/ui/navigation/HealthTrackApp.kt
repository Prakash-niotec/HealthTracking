package com.healthtrack.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.healthtrack.app.ui.screens.auth.OnboardingScreen
import com.healthtrack.app.ui.screens.auth.SignInScreen
import com.healthtrack.app.ui.screens.auth.SignUpScreen
import com.healthtrack.app.ui.screens.auth.SplashScreen
import com.healthtrack.app.ui.screens.main.MainTabScreen
import com.healthtrack.app.ui.viewmodel.HealthViewModel
import com.healthtrack.app.ui.viewmodel.ViewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.healthtrack.app.HealthTrackApplication
import com.healthtrack.app.di.DefaultAppContainer

@Composable
fun HealthTrackApp() {
    val navController = rememberNavController()
    val useFirebase = false; val app = LocalContext.current.applicationContext as? HealthTrackApplication
    val container = app!!.container
    
    val viewModel: HealthViewModel = viewModel(factory = ViewModelFactory(container))

    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentTab by viewModel.currentMainTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val hydrationLogs by viewModel.hydrationLogs.collectAsState()
    val medications by viewModel.medications.collectAsState()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onSplashFinished = { navController.navigate("onboarding") { popUpTo("splash") { inclusive = true } } })
        }
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToSignIn = { navController.navigate("signin") },
                onNavigateToSignUp = { navController.navigate("signup") }
            )
        }
        composable("signin") {
            SignInScreen(
                onSignInClicked = { email, pass, onError -> 
                    viewModel.signIn(email, pass, { navController.navigate("main") { popUpTo("signin") { inclusive = true } } }, onError) 
                },
                onNavigateToSignUp = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpClicked = { name, email, pass, weight, conds, hipaa, onError -> 
                    viewModel.signUp(name, email, pass, weight, conds, hipaa, { navController.navigate("main") { popUpTo("signup") { inclusive = true } } }, onError) 
                },
                onNavigateToSignIn = { navController.navigate("signin") }
            )
        }
        composable("main") {
            MainTabScreen(
                currentTab = currentTab,
                userProfile = userProfile,
                hydrationLogs = hydrationLogs,
                medications = medications,
                onTabSelected = { viewModel.selectMainTab(it) },
                onNavigateToAddMedication = { },
                onAddHydration = { amount, label -> viewModel.addHydrationLog(amount, label) },
                onDeleteHydration = { id -> viewModel.deleteHydrationLog(id) },
                onToggleMedicationTaken = { id -> viewModel.toggleMedicationTaken(id) },
                onDeleteMedication = { id -> viewModel.deleteMedication(id) },
                onEvaluateIngredient = { name, amount, unit, cond -> viewModel.evaluateIngredient(name, amount, unit, cond) },
                onSignOutClicked = { viewModel.signOut(); navController.navigate("onboarding") { popUpTo("main") { inclusive = true } } }
            )
        }
    }
}
