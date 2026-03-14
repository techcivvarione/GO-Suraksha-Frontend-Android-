package com.gosuraksha.app.navigation

import com.gosuraksha.app.navigation.Screen
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
import android.app.Application
import com.gosuraksha.app.MainActivity
import com.gosuraksha.app.core.OnboardingPrefs
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.presentation.auth.AuthViewModelFactory
import com.gosuraksha.app.domain.usecase.AuthUseCaseProvider
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.ui.auth.LoginScreen
import com.gosuraksha.app.ui.auth.SignupScreen
import com.gosuraksha.app.ui.entry.EntryScreen
import com.gosuraksha.app.ui.history.HistoryScreen
import com.gosuraksha.app.ui.language.LanguageSelectorScreen
import com.gosuraksha.app.ui.main.MainShell
import com.gosuraksha.app.ui.onboarding.IntroOnboardingScreen
import com.gosuraksha.app.ui.security.PinManager
import com.gosuraksha.app.ui.security.SetPinScreen
import com.gosuraksha.app.ui.security.UnlockPinScreen
import com.gosuraksha.app.ui.trusted.TrustedContactsScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    val appContext = LocalContext.current.applicationContext
    val provider = appContext as AuthUseCaseProvider
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(appContext as Application, provider.authUseCases())
    )
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isLoadingSession by authViewModel.isLoadingSession.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pinManager = remember(context) { PinManager(context) }
    val scope = rememberCoroutineScope()

    val hasSelectedLanguage by LanguagePrefs
        .hasSelectedLanguage(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val hasCompletedOnboarding by OnboardingPrefs
        .isCompleted(context)
        .collectAsStateWithLifecycle(initialValue = null)

    if (isLoadingSession || hasCompletedOnboarding == null) {
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
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Entry.route
    ) {

        composable(Screen.Entry.route) {
            EntryScreen {
                if (isLoggedIn) {
                    if (pinManager.isPinSet()) {
                        navController.navigate(Screen.UnlockPin.route) {
                            popUpTo(Screen.Entry.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.SetPin.route) {
                            popUpTo(Screen.Entry.route) { inclusive = true }
                        }
                    }
                } else if (hasCompletedOnboarding == false) {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Entry.route) { inclusive = true }
                    }
                } else if (!hasSelectedLanguage) {
                    navController.navigate(Screen.Language.route) {
                        popUpTo(Screen.Entry.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Entry.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Onboarding.route) {
            IntroOnboardingScreen(
                onComplete = {
                    scope.launch {
                        OnboardingPrefs.setCompleted(context, true)
                    }
                    (context as? MainActivity)?.requestCallPermissions()
                    navController.navigate(
                        if (hasSelectedLanguage) Screen.Login.route else Screen.Language.route
                    ) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Language.route) {
            LanguageSelectorScreen(
                onLanguageSelected = { code ->
                    scope.launch {
                        LanguagePrefs.saveLanguage(context, code)
                    }
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Language.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    if (!pinManager.isPinSet()) {
                        navController.navigate(Screen.SetPin.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.UnlockPin.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        composable(Screen.SetPin.route) {
            SetPinScreen(
                pinManager = pinManager,
                onPinCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UnlockPin.route) {
            UnlockPinScreen(
                pinManager = pinManager,
                onUnlocked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onForceLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Signup.route) {

            SignupScreen(
                viewModel = authViewModel,
                onSignupSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        // ✅ FIXED: Proper HOME route wrapper
        composable(Screen.Home.route) {
            MainShell(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TrustedContacts.route) {
            TrustedContactsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CyberSos.route) {
            CyberSosScreen()
        }

    }
}


