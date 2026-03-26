package com.gosuraksha.app.presentation.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.auth.GoogleSignInManager
import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.data.local.TokenLocalDataSource
import com.gosuraksha.app.data.mapper.toDomain
import com.gosuraksha.app.data.remote.AuthRemoteDataSource
import com.gosuraksha.app.data.remote.dto.auth.EmailRequest
import com.gosuraksha.app.data.remote.dto.auth.PhoneOtpRequest
import com.gosuraksha.app.data.remote.dto.auth.VerifyOtpRequest
import com.gosuraksha.app.data.remote.dto.auth.VerifyPhoneOtpRequest
import com.gosuraksha.app.data.repository.AuthOtpRepository
import com.gosuraksha.app.domain.model.AuthSession
import com.gosuraksha.app.domain.usecase.AuthUseCases
import com.gosuraksha.app.domain.usecase.GoogleLoginUseCase
import com.gosuraksha.app.domain.usecase.LoginParams
import com.gosuraksha.app.domain.usecase.LoginUseCase
import com.gosuraksha.app.domain.usecase.RestoreSessionUseCase
import com.gosuraksha.app.domain.usecase.SignupParams
import com.gosuraksha.app.domain.usecase.SignupUseCase
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.security.EncryptedTokenStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val loginUseCase: LoginUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val restoreSessionUseCase: RestoreSessionUseCase,
    private val authOtpRepository: AuthOtpRepository,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val tokenLocalDataSource: TokenLocalDataSource
) : AndroidViewModel(application) {

    private val _isLoggedIn       = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isLoadingSession = MutableStateFlow(true)
    val isLoadingSession: StateFlow<Boolean> = _isLoadingSession

    private val _isSendingOtp     = MutableStateFlow(false)
    val isSendingOtp: StateFlow<Boolean> = _isSendingOtp

    private val _isVerifyingOtp   = MutableStateFlow(false)
    val isVerifyingOtp: StateFlow<Boolean> = _isVerifyingOtp

    private val _otpSecondsLeft   = MutableStateFlow(0)
    val otpSecondsLeft: StateFlow<Int> = _otpSecondsLeft

    private val _isOtpSent        = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent

    private val _emailVerified    = MutableStateFlow(false)
    val emailVerified: StateFlow<Boolean> = _emailVerified

    private val _otpError         = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError

    private val _pendingPhone     = MutableStateFlow<String?>(null)
    val pendingPhone: StateFlow<String?> = _pendingPhone

    private val _isNewUser        = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> = _isNewUser

    private val _isPhoneVerified  = MutableStateFlow(false)
    val isPhoneVerified: StateFlow<Boolean> = _isPhoneVerified

    private var otpTimerJob: Job? = null

    private var restoreSessionJob: Job? = null
    init { restoreSession() }

    // ─────────────────────────────────────────────────────────────────────
    // Session restore
    // ─────────────────────────────────────────────────────────────────────
    private fun restoreSession() {
        restoreSessionJob?.cancel()
        restoreSessionJob = viewModelScope.launch {
            try {
                if (SessionManager.consumeAccountDeletedFlag()) {
                    tokenLocalDataSource.clearToken()
                    clearAuthState(clearSession = true)
                    return@launch
                }

                val localToken = tokenLocalDataSource.getToken()
                if (localToken.isNullOrBlank()) {
                    clearAuthState(clearSession = true)
                    return@launch
                }

                when (val result = restoreSessionUseCase(Unit)) {
                    is AppResult.Success -> applyAuthenticatedSession(result.data)
                    is AppResult.Failure -> clearAuthState(clearSession = true)
                }
            } finally {
                _isLoadingSession.value = false
                restoreSessionJob = null
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Email / password login
    // ─────────────────────────────────────────────────────────────────────
    fun login(
        identifier: String,
        password:   String,
        onSuccess:  () -> Unit,
        onError:    (String) -> Unit
    ) {
        loginWithResult(
            identifier = identifier,
            password   = password,
            onResult   = { onSuccess() },
            onError    = onError
        )
    }

    fun loginWithResult(
        identifier: String,
        password:   String,
        onResult:   (Boolean) -> Unit,
        onError:    (String) -> Unit
    ) {
        val safeIdentifier = identifier.trim()
        val safePassword   = password.trim()
        if (safeIdentifier.isBlank() || safePassword.isBlank()) {
            onError("Something went wrong. Please try again")
            return
        }
        viewModelScope.launch {
            clearAuthState(clearSession = true)
            when (val result = loginUseCase(LoginParams(safeIdentifier, safePassword))) {
                is AppResult.Success -> {
                    onResult(result.data.needsPhoneVerification)
                    applyAuthenticatedSession(result.data)
                }
                is AppResult.Failure -> {
                    clearAuthState(clearSession = true)
                    onError(result.error.toMessage())
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Signup
    // ─────────────────────────────────────────────────────────────────────
    fun signup(
        name:            String,
        email:           String,
        phone:           String,
        password:        String,
        confirmPassword: String,
        onSuccess:       () -> Unit,
        onError:         (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = signupUseCase(
                SignupParams(
                    name            = name,
                    email           = email,
                    phone           = phone,
                    password        = password,
                    confirmPassword = confirmPassword
                )
            )) {
                is AppResult.Success -> onSuccess()
                is AppResult.Failure -> onError(result.error.toMessage())
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Google login
    // ─────────────────────────────────────────────────────────────────────
    fun loginWithGoogle(
        idToken:   String,
        onSuccess: () -> Unit,
        onError:   (String) -> Unit
    ) {
        loginWithGoogleResult(
            idToken  = idToken,
            onResult = { onSuccess() },
            onError  = onError
        )
    }

    fun loginWithGoogleResult(
        idToken:  String,
        onResult: (Boolean) -> Unit,
        onError:  (String) -> Unit
    ) {
        val safeIdToken = idToken.trim()
        if (safeIdToken.isBlank()) {
            onError("Something went wrong. Please try again")
            return
        }
        viewModelScope.launch {
            if (BuildConfig.DEBUG) Log.d("GoogleSignInFlow", "Starting backend exchange")
            SessionManager.clear()
            tokenLocalDataSource.clearToken()
            clearAuthState(clearSession = true)
            when (val result = googleLoginUseCase(safeIdToken)) {
                is AppResult.Success -> {
                    if (BuildConfig.DEBUG) Log.d("GoogleSignInFlow", "Backend exchange succeeded")
                    onResult(result.data.needsPhoneVerification)
                    applyAuthenticatedSession(result.data)
                }
                is AppResult.Failure -> {
                    clearAuthState(clearSession = true)
                    if (BuildConfig.DEBUG) Log.e("GoogleSignInFlow", "Backend exchange failed")
                    onError(result.error.toMessage())
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Phone OTP — send
    // ─────────────────────────────────────────────────────────────────────
    fun sendOtp(phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val safePhone = phone.trim()
        if (safePhone.isBlank()) {
            onError("Please enter a valid phone number.")
            return
        }
        viewModelScope.launch {
            SessionManager.clear()
            tokenLocalDataSource.clearToken()
            _isSendingOtp.value = true
            _otpError.value     = null

            val result = runCatching {
                authOtpRepository.sendPhoneOtp(PhoneOtpRequest(safePhone))
            }

            result.onSuccess { response ->
                if (BuildConfig.DEBUG) {
                    Log.d("AUTH_DEBUG", "sendPhoneOtp raw status = '${response.status}'")
                }
                // Covers: "success", "ok", "OK", "200", "true", "sent"
                val isSuccess = response.status.trim().lowercase() in
                        setOf("success", "ok", "200", "true", "sent")

                _isSendingOtp.value = false

                if (isSuccess) {
                    _pendingPhone.value = safePhone
                    if (BuildConfig.DEBUG) Log.d("AUTH_DEBUG", "OTP SUCCESS — emitting event")
                    startTimer(30)
                    onSuccess()                    // → onOtpSent → navigate to OtpScreen
                } else {
                    val message = response.data?.message?.ifBlank { null }
                        ?: "Something went wrong. Please try again"
                    _otpError.value = message
                    onError(message)
                }
            }.onFailure {
                _isSendingOtp.value = false
                val message = it.toMessage()
                _otpError.value = message
                if (BuildConfig.DEBUG) Log.e("AUTH_DEBUG", "OTP FAILED", it)
                onError(message)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Phone OTP — verify
    // ─────────────────────────────────────────────────────────────────────
    fun verifyOtp(
        phone:    String,
        otp:      String,
        onResult: (Boolean) -> Unit,
        onError:  (String) -> Unit
    ) {
        val safePhone = phone.trim()
        val safeOtp   = otp.trim()
        if (safePhone.isBlank() || safeOtp.length != 6) {
            onError("Please enter a valid OTP.")
            return
        }
        viewModelScope.launch {
            _isVerifyingOtp.value = true
            _otpError.value       = null

            when (val verificationResult =
                authOtpRepository.verifyPhoneOtp(VerifyPhoneOtpRequest(safePhone, safeOtp))
            ) {
                is AppResult.Success -> {
                    val data        = verificationResult.data
                    val accessToken = data.token

                    if (BuildConfig.DEBUG) Log.d("AUTH_FLOW", "verifyOtp repository success")

                    _otpError.value = null

                    // CRITICAL FIX: persist the token to EncryptedStorage FIRST so that
                    // restoreSession() can find it on the next cold start.
                    // Previously only SessionManager.saveToken() (in-memory) was called here,
                    // which meant the session was lost every time the app was restarted after
                    // OTP login — the user was redirected back to the login screen.
                    tokenLocalDataSource.saveToken(accessToken)    // ← writes to EncryptedStorage
                    SessionManager.saveToken(accessToken)          // ← keeps in-memory flow current

                    _pendingPhone.value    = safePhone
                    _isPhoneVerified.value = data.phoneVerified
                    _isNewUser.value       = data.isNewUser

                    try {
                        val user = authRemoteDataSource.getMe().toDomain()
                        if (BuildConfig.DEBUG) Log.d("AUTH_FLOW", "User loaded: ${user.name}")
                        SessionManager.setSession(user, accessToken)
                        _isLoggedIn.value = true

                        // Navigate via callback — AppNavGraph drives navigation purely
                        // through onResult, not through _isLoggedIn observation, so there
                        // is no risk of double-navigation here.
                        onResult(data.isNewUser)

                    } catch (t: Throwable) {
                        if (BuildConfig.DEBUG) Log.e("AUTH_FLOW", "Post-OTP session bootstrap failed", t)
                        clearAuthState(clearSession = true)
                        val message = t.toMessage()
                        _otpError.value = message
                        onError(message)
                    } finally {
                        _isVerifyingOtp.value = false
                    }
                }
                is AppResult.Failure -> {
                    _isVerifyingOtp.value = false
                    val message = verificationResult.error.toMessage()
                    _otpError.value = message
                    if (BuildConfig.DEBUG) Log.d("AUTH_FLOW", "verifyOtp failure: $message")
                    onError(message)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Profile setup completion
    // ─────────────────────────────────────────────────────────────────────
    fun completeProfileSetup(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val accessToken = SessionManager.accessToken.value
                ?: tokenLocalDataSource.getToken().orEmpty()
            if (accessToken.isBlank()) {
                clearAuthState(clearSession = true)
                onError(AppError.Unauthorized.toMessage())
                return@launch
            }
            runCatching {
                val user = authRemoteDataSource.getMe().toDomain()
                SessionManager.setSession(user, accessToken)
                _isLoggedIn.value      = true
                _isNewUser.value       = false
                _isPhoneVerified.value = !user.phone.isNullOrBlank()
            }.onSuccess {
                onSuccess()
            }.onFailure {
                clearAuthState(clearSession = true)
                onError(it.toMessage())
            }
        }
    }

    fun setPendingPhone(phone: String) {
        _pendingPhone.value = phone.trim().ifBlank { null }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────────────────────────────
    fun logout() {
        restoreSessionJob?.cancel()
        otpTimerJob?.cancel()
        viewModelScope.launch {
            GoogleSignInManager(getApplication()).signOut()
            SessionManager.clear()
            tokenLocalDataSource.clearToken()
            clearAuthState(clearSession = true)
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Email OTP (signup flow)
    // ─────────────────────────────────────────────────────────────────────
    fun resetOtpState() {
        _isOtpSent.value = false
        _emailVerified.value = false
        _otpError.value = null
        _otpSecondsLeft.value = 0
        otpTimerJob?.cancel()
    }

    fun sendOtp(email: String, emailError: String?) = viewModelScope.launch {
        val safeEmail = email.trim()
        if (emailError != null || safeEmail.isBlank()) {
            _otpError.value = "Something went wrong. Please try again"
            return@launch
        }
        _isSendingOtp.value = true
        _otpError.value = null
        try {
            val response = ApiClient.authApi.sendEmailOtp(EmailRequest(safeEmail))
            _isSendingOtp.value = false
            if (response.isSuccessful) {
                _isOtpSent.value = true
                if (BuildConfig.DEBUG) Log.d("AUTH_DEBUG", "Email OTP sent successfully")
                startTimer(60)
            } else {
                _otpError.value = "Something went wrong. Please try again"
            }
        } catch (t: Throwable) {
            _isSendingOtp.value = false
            _otpError.value = t.toMessage()
            if (BuildConfig.DEBUG) Log.e("OTP_WIRING", "sendEmailOtp failed", t)
        }
    }

    fun verifyOtp(email: String, otp: String) = viewModelScope.launch {
        val safeEmail = email.trim()
        val safeOtp = otp.trim()
        if (safeEmail.isBlank() || safeOtp.length != 6) {
            _otpError.value = "Something went wrong. Please try again"
            return@launch
        }
        _isVerifyingOtp.value = true
        _otpError.value = null
        try {
            val response = ApiClient.authApi.verifyEmailOtp(VerifyOtpRequest(safeEmail, safeOtp))
            _isVerifyingOtp.value = false
            if (response.isSuccessful && response.body()?.data?.success == true) {
                _emailVerified.value = true
            } else {
                _otpError.value = "Something went wrong. Please try again"
            }
        } catch (t: Throwable) {
            _isVerifyingOtp.value = false
            _otpError.value = t.toMessage()
            if (BuildConfig.DEBUG) Log.e("OTP_WIRING", "verifyEmailOtp failed", t)
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────
    private fun applyAuthenticatedSession(session: AuthSession) {
        SessionManager.saveToken(session.accessToken)
        SessionManager.setSession(session.user, session.accessToken)
        _isLoggedIn.value = true
        _isNewUser.value = session.isNewUser
        _isPhoneVerified.value = session.phoneVerified && !session.needsPhoneVerification
        _pendingPhone.value = session.user.phone?.takeIf { it.isNotBlank() }
    }

    private fun clearAuthState(clearSession: Boolean) {
        if (clearSession) SessionManager.clear()
        _isLoggedIn.value = false
        _isNewUser.value = false
        _isPhoneVerified.value = false
        _pendingPhone.value = null
        _isOtpSent.value = false
        _emailVerified.value = false
        _otpError.value = null
        _otpSecondsLeft.value = 0
        _isSendingOtp.value = false
        _isVerifyingOtp.value = false
    }

    private fun startTimer(seconds: Int) {
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            _otpSecondsLeft.value = seconds
            while (_otpSecondsLeft.value > 0) {
                delay(1_000)
                _otpSecondsLeft.value -= 1
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error mappers
// ─────────────────────────────────────────────────────────────────────────────
private fun AppError.toMessage(): String = when (this) {
    AppError.Network      -> "Something went wrong. Please try again"
    AppError.Timeout      -> "Something went wrong. Please try again"
    AppError.Unauthorized -> "Session expired. Please login again"
    AppError.Forbidden    -> "Something went wrong. Please try again"
    AppError.NotFound     -> "Something went wrong. Please try again"
    AppError.Server       -> "Something went wrong. Please try again"
    is AppError.Validation -> message.ifBlank { "Something went wrong. Please try again" }
    is AppError.Unknown   -> if (message == "HTTP 409")
        "This phone number is already linked to another account"
    else "Something went wrong. Please try again"
}

private fun Throwable.toMessage(): String = when (this) {
    is retrofit2.HttpException -> when (code()) {
        401  -> "Session expired. Please login again"
        409  -> "This phone number is already linked to another account"
        else -> "Something went wrong. Please try again"
    }
    else -> "Something went wrong. Please try again"
}

// ─────────────────────────────────────────────────────────────────────────────
// Factory
// ─────────────────────────────────────────────────────────────────────────────
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
                useCases.googleLogin,
                useCases.signup,
                useCases.restoreSession,
                AuthOtpRepository(
                    ApiClient.authApi,
                    TokenLocalDataSource(EncryptedTokenStorage(application.applicationContext))
                ),
                AuthRemoteDataSource(ApiClient.authApi),
                TokenLocalDataSource(EncryptedTokenStorage(application.applicationContext))
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


