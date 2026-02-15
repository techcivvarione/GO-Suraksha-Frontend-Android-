package com.gosuraksha.app.auth.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.SessionManager
import com.gosuraksha.app.data.TokenDataStore
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    // Starts true — blocks navigation until restore completes
    private val _isLoadingSession = MutableStateFlow(true)
    val isLoadingSession: StateFlow<Boolean> = _isLoadingSession

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            try {
                val token = TokenDataStore
                    .getToken(getApplication())
                    .firstOrNull()

                if (!token.isNullOrEmpty()) {
                    try {
                        val me = ApiClient.authApi.getMe()
                        SessionManager.setUser(me)
                        _isLoggedIn.value = true
                    } catch (e: Exception) {
                        TokenDataStore.clearToken(getApplication())
                        SessionManager.clear()
                        _isLoggedIn.value = false
                    }
                } else {
                    _isLoggedIn.value = false
                }
            } finally {
                // Always unblocks navigation — whether success, failure, or exception
                _isLoadingSession.value = false
            }
        }
    }

    fun login(
        request: LoginRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = ApiClient.authApi.login(request)

                TokenDataStore.saveToken(
                    context = getApplication(),
                    token = response.access_token
                )

                val me = ApiClient.authApi.getMe()
                SessionManager.setUser(me)

                _isLoggedIn.value = true
                onSuccess()

            } catch (e: HttpException) {
                if (e.code() == 401) {
                    onError("Invalid email or password")
                } else {
                    onError("Server error: ${e.code()}")
                }
            } catch (e: Exception) {
                onError("Something went wrong")
            }
        }
    }

    fun signup(
        request: SignupRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                ApiClient.authApi.signup(request)
                onSuccess()
            } catch (e: HttpException) {
                onError("Signup failed: ${e.code()}")
            } catch (e: Exception) {
                onError("Something went wrong")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            TokenDataStore.clearToken(getApplication())
            SessionManager.clear()
            _isLoggedIn.value = false
        }
    }
}