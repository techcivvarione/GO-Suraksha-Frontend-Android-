package com.gosuraksha.app.navigation

import com.gosuraksha.app.ui.screens.CyberSosScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.gosuraksha.app.auth.model.AuthViewModel
import com.gosuraksha.app.data.LanguageDataStore
import com.gosuraksha.app.data.SessionManager
import com.gosuraksha.app.ui.auth.LoginScreen
import com.gosuraksha.app.ui.auth.SignupScreen
import com.gosuraksha.app.ui.entry.EntryScreen
import com.gosuraksha.app.ui.history.HistoryScreen
import com.gosuraksha.app.ui.language.LanguageSelectorScreen
import com.gosuraksha.app.ui.main.MainShell
import com.gosuraksha.app.ui.security.PinManager
import com.gosuraksha.app.ui.security.SetPinScreen
import com.gosuraksha.app.ui.security.UnlockPinScreen
import com.gosuraksha.app.ui.trusted.TrustedContactsScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object Routes {
    const val ENTRY = "entry"
    const val LANGUAGE = "language"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val SET_PIN = "set_pin"
    const val UNLOCK_PIN = "unlock_pin"
    const val HISTORY = "history"
    const val TRUSTED = "trusted_contacts"
}

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoadingSession by authViewModel.isLoadingSession.collectAsState()
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    val scope = rememberCoroutineScope()

    val selectedLanguage by LanguageDataStore
        .getSelectedLanguage(context)
        .collectAsState(initial = null)

    if (isLoadingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(Unit) {
        SessionManager.sessionExpired.collectLatest {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.ENTRY
    ) {

        composable(Routes.ENTRY) {
            EntryScreen {
                if (selectedLanguage == null) {
                    navController.navigate(Routes.LANGUAGE) {
                        popUpTo(Routes.ENTRY) { inclusive = true }
                    }
                } else {
                    if (isLoggedIn) {
                        if (pinManager.isPinSet()) {
                            navController.navigate(Routes.UNLOCK_PIN) {
                                popUpTo(Routes.ENTRY) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.SET_PIN) {
                                popUpTo(Routes.ENTRY) { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ENTRY) { inclusive = true }
                        }
                    }
                }
            }
        }

        composable(Routes.LANGUAGE) {
            LanguageSelectorScreen(
                onLanguageSelected = { code ->
                    scope.launch {
                        LanguageDataStore.saveLanguage(context, code)
                    }
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LANGUAGE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    if (!pinManager.isPinSet()) {
                        navController.navigate(Routes.SET_PIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.UNLOCK_PIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }

        composable(Routes.SET_PIN) {
            SetPinScreen(
                pinManager = pinManager,
                onPinCreated = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.UNLOCK_PIN) {
            UnlockPinScreen(
                pinManager = pinManager,
                onUnlocked = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onForceLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SIGNUP) {

            val viewModel: AuthViewModel = viewModel()

            SignupScreen(
                viewModel = viewModel,
                onSignupSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                }
            )
        }

        // ✅ FIXED: Proper HOME route wrapper
        composable(Routes.HOME) {
            MainShell(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TRUSTED) {
            TrustedContactsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("cyber_sos") {
            CyberSosScreen()
        }

    }
}
