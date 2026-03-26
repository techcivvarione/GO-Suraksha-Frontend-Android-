package com.gosuraksha.app.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.presentation.auth.AuthViewModel
import com.gosuraksha.app.ui.signup.components.SignupForm
import com.gosuraksha.app.ui.signup.components.SignupHeader
import com.gosuraksha.app.ui.signup.model.SignupDarkBg
import com.gosuraksha.app.ui.signup.model.SignupFormState
import com.gosuraksha.app.ui.signup.model.SignupLightBg

@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val isDark = ColorTokens.LocalAppDarkMode.current
    var state by remember { mutableStateOf(SignupFormState()) }
    val isSendingOtp by viewModel.isSendingOtp.collectAsStateWithLifecycle()
    val isVerifyingOtp by viewModel.isVerifyingOtp.collectAsStateWithLifecycle()
    val otpSecondsLeft by viewModel.otpSecondsLeft.collectAsStateWithLifecycle()
    val isOtpSent by viewModel.isOtpSent.collectAsStateWithLifecycle()
    val emailVerified by viewModel.emailVerified.collectAsStateWithLifecycle()
    val otpError by viewModel.otpError.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(if (isDark) SignupDarkBg else SignupLightBg)) {
        SignupHeader()
        SignupForm(
            isDark = isDark,
            state = state,
            isSendingOtp = isSendingOtp,
            isVerifyingOtp = isVerifyingOtp,
            otpSecondsLeft = otpSecondsLeft,
            isOtpSent = isOtpSent,
            emailVerified = emailVerified,
            otpError = otpError,
            onNameChange = { state = state.copy(name = it, errorMessage = null) },
            onEmailChange = { state = state.copy(email = it, otp = "", errorMessage = null); viewModel.resetOtpState() },
            onPhoneChange = { state = state.copy(phone = it, errorMessage = null) },
            onPasswordChange = { state = state.copy(password = it, errorMessage = null) },
            onConfirmChange = { state = state.copy(confirm = it, errorMessage = null) },
            onOtpChange = { state = state.copy(otp = it) },
            onPasswordToggle = { state = state.copy(showPassword = !state.showPassword) },
            onConfirmToggle = { state = state.copy(showConfirm = !state.showConfirm) },
            onTermsChange = { state = state.copy(acceptedTerms = it, errorMessage = null) },
            onSendOtp = { emailError -> viewModel.sendOtp(state.email, emailError) },
            onVerifyOtp = { viewModel.verifyOtp(state.email, state.otp) },
            onSubmit = {
                if (!state.acceptedTerms) {
                    state = state.copy(errorMessage = "Please accept Privacy Policy and Terms to continue.")
                } else {
                    state = state.copy(isSubmitting = true, errorMessage = null)
                    viewModel.signup(
                        name = state.name,
                        email = state.email,
                        phone = state.phone,
                        password = state.password,
                        confirmPassword = state.confirm,
                        onSuccess = { state = state.copy(isSubmitting = false); onSignupSuccess() },
                        onError = { state = state.copy(isSubmitting = false, errorMessage = it) }
                    )
                }
            },
            onNavigateToLogin = onNavigateToLogin
        )
    }
}
