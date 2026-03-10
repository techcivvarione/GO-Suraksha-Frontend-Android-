package com.gosuraksha.app.presentation.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.data.remote.dto.auth.EmailRequest
import com.gosuraksha.app.data.remote.dto.auth.VerifyOtpRequest
import com.gosuraksha.app.domain.usecase.LoginParams
import com.gosuraksha.app.domain.usecase.LoginUseCase
import com.gosuraksha.app.domain.usecase.RestoreSessionUseCase
import com.gosuraksha.app.domain.usecase.SignupParams
import com.gosuraksha.app.domain.usecase.SignupUseCase
import com.gosuraksha.app.domain.usecase.AuthUseCases
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.core.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val restoreSessionUseCase: RestoreSessionUseCase
) : AndroidViewModel(application) {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isLoadingSession = MutableStateFlow(true)
    val isLoadingSession: StateFlow<Boolean> = _isLoadingSession

    // OTP WIRING START
    private val _isSendingOtp = MutableStateFlow(false)
    val isSendingOtp: StateFlow<Boolean> = _isSendingOtp

    private val _isVerifyingOtp = MutableStateFlow(false)
    val isVerifyingOtp: StateFlow<Boolean> = _isVerifyingOtp

    private val _otpSecondsLeft = MutableStateFlow(0)
    val otpSecondsLeft: StateFlow<Int> = _otpSecondsLeft

    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent

    private val _emailVerified = MutableStateFlow(false)
    val emailVerified: StateFlow<Boolean> = _emailVerified

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError

    private var otpTimerJob: Job? = null
    // OTP WIRING END

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            try {
                when (val result = restoreSessionUseCase(Unit)) {
                    is AppResult.Success -> {
                        SessionManager.setUser(result.data.user)
                        _isLoggedIn.value = true
                    }
                    is AppResult.Failure -> {
                        SessionManager.clear()
                        _isLoggedIn.value = false
                    }
                }
            } finally {
                _isLoadingSession.value = false
            }
        }
    }

    fun login(
        identifier: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = loginUseCase(LoginParams(identifier, password))) {
                is AppResult.Success -> {
                    SessionManager.setUser(result.data.user)
                    _isLoggedIn.value = true
                    onSuccess()
                }
                is AppResult.Failure -> onError(result.error.toMessage())
            }
        }
    }

    fun signup(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (
                val result = signupUseCase(
                    SignupParams(
                        name = name,
                        email = email,
                        phone = phone,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                )
            ) {
                is AppResult.Success -> onSuccess()
                is AppResult.Failure -> onError(result.error.toMessage())
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            SessionManager.clear()
            _isLoggedIn.value = false
        }
    }

    // OTP WIRING START
    fun resetOtpState() {
        _isOtpSent.value = false
        _emailVerified.value = false
        _otpError.value = null
        _otpSecondsLeft.value = 0
        otpTimerJob?.cancel()
    }

    fun sendOtp(email: String, emailError: String?) = viewModelScope.launch {
        if (emailError != null) return@launch
        _isSendingOtp.value = true
        _otpError.value = null

        try {
            val response = ApiClient.authApi.sendEmailOtp(EmailRequest(email))
            _isSendingOtp.value = false

            Log.d("OTP_WIRING", "sendEmailOtp code=${response.code()}")
            Log.d("OTP_WIRING", "sendEmailOtp errorBody=${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                _isOtpSent.value = true
                startTimer(60)
            }
        } catch (t: Throwable) {
            _isSendingOtp.value = false
            Log.d("OTP_WIRING", "sendEmailOtp exception=${t.message}")
        }
    }

    fun verifyOtp(email: String, otp: String) = viewModelScope.launch {
        if (otp.length != 6) return@launch

        _isVerifyingOtp.value = true
        _otpError.value = null

        try {
            val response = ApiClient.authApi.verifyEmailOtp(
                VerifyOtpRequest(email, otp)
            )

            _isVerifyingOtp.value = false

            Log.d("OTP_WIRING", "verifyEmailOtp code=${response.code()}")
            Log.d("OTP_WIRING", "verifyEmailOtp errorBody=${response.errorBody()?.string()}")

            if (response.isSuccessful && response.body()?.success == true) {
                _emailVerified.value = true
            } else {
                _otpError.value = "Invalid code"
            }
        } catch (t: Throwable) {
            _isVerifyingOtp.value = false
            _otpError.value = "Invalid code"
            Log.d("OTP_WIRING", "verifyEmailOtp exception=${t.message}")
        }
    }

    private fun startTimer(seconds: Int) {
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            _otpSecondsLeft.value = seconds
            while (_otpSecondsLeft.value > 0) {
                delay(1_000)
                _otpSecondsLeft.value = _otpSecondsLeft.value - 1
            }
        }
    }
    // OTP WIRING END
}

private fun com.gosuraksha.app.core.result.AppError.toMessage(): String {
    return when (this) {
        com.gosuraksha.app.core.result.AppError.Network -> "error_network"
        com.gosuraksha.app.core.result.AppError.Timeout -> "error_timeout"
        com.gosuraksha.app.core.result.AppError.Unauthorized -> "error_auth_invalid_credentials"
        com.gosuraksha.app.core.result.AppError.Forbidden -> "error_forbidden"
        com.gosuraksha.app.core.result.AppError.NotFound -> "error_not_found"
        com.gosuraksha.app.core.result.AppError.Server -> "error_server"
        is com.gosuraksha.app.core.result.AppError.Validation -> message
        is com.gosuraksha.app.core.result.AppError.Unknown -> message ?: "error_generic"
    }
}

class AuthViewModelFactory(
    private val application: Application,
    private val useCases: AuthUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                application,
                useCases.login,
                useCases.signup,
                useCases.restoreSession
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
