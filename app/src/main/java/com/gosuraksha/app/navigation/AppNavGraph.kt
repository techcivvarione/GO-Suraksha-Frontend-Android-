package com.gosuraksha.app.navigation

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.gosuraksha.app.BuildConfig
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.core.OnboardingManager
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.domain.usecase.AuthUseCaseProvider
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.presentation.auth.AuthViewModelFactory
import com.gosuraksha.app.ui.auth.LoginScreen
import com.gosuraksha.app.ui.auth.OtpScreen
import com.gosuraksha.app.ui.auth.ProfileSetupScreen
import com.gosuraksha.app.ui.auth.VerifyPhoneScreen
import com.gosuraksha.app.ui.entry.EntryScreen
import com.gosuraksha.app.ui.history.HistoryScreen
import com.gosuraksha.app.ui.language.LanguageSelectorScreen
import com.gosuraksha.app.ui.main.MainShell
import com.gosuraksha.app.ui.onboarding.IntroOnboardingScreen
import com.gosuraksha.app.ui.screens.CyberSosScreen
import com.gosuraksha.app.ui.security.BiometricUnlockScreen
import com.gosuraksha.app.ui.signup.SignupScreen
import com.gosuraksha.app.ui.trusted.TrustedContactsScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val appContext = LocalContext.current.applicationContext
    val provider = appContext as AuthUseCaseProvider
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(appContext as android.app.Application, provider.authUseCases())
    )
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isLoadingSession by authViewModel.isLoadingSession.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val onboardingManager = remember(context) { OnboardingManager(context) }
    val scope = rememberCoroutineScope()
    var isFirstLaunch by remember(context) { mutableStateOf(onboardingManager.isFirstLaunch()) }

    val hasSelectedLanguage by LanguagePrefs
        .hasSelectedLanguage(context)
        .collectAsStateWithLifecycle(initialValue = false)

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
            authViewModel.logout()
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
                    navController.navigate(Screen.UnlockPin.route) {
                        popUpTo(Screen.Entry.route) { inclusive = true }
                    }
                } else if (isFirstLaunch) {
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
                    onboardingManager.setFirstLaunchCompleted()
                    isFirstLaunch = false
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
                onOtpSent = {
                    if (BuildConfig.DEBUG) {
                        Log.d("AUTH_DEBUG", "Current route before OTP nav = ${navController.currentBackStackEntry?.destination?.route}")
                    }
                    navController.navigate(Screen.Otp.route) {
                        launchSingleTop = true
                    }
                },
                onLoginResolved = { needsPhoneVerification ->
                    if (needsPhoneVerification) {
                        navController.navigate(Screen.VerifyPhone.route) {
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        composable(Screen.Otp.route) {
            OtpScreen(
                viewModel = authViewModel,
                onVerified = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate(Screen.ProfileSetup.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.VerifyPhone.route) {
            VerifyPhoneScreen(
                viewModel = authViewModel,
                onOtpSent = {
                    if (BuildConfig.DEBUG) {
                        Log.d("AUTH_DEBUG", "Current route before OTP nav = ${navController.currentBackStackEntry?.destination?.route}")
                    }
                    navController.navigate(Screen.Otp.route) {
                        launchSingleTop = true
                    }
                },
                onBack = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                authViewModel = authViewModel,
                onBackToLogin = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UnlockPin.route) {
            BiometricUnlockScreen(
                onUnlocked = {
                    navController.navigate(Screen.Home.route) {
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
