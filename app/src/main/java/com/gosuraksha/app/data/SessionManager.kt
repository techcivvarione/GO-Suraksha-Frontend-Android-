package com.gosuraksha.app.data

import com.gosuraksha.app.auth.model.UserResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {

    // 🔹 Logged in user (memory only)
    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user

    // 🔹 One-time session expired event
    private val _sessionExpired = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpired = _sessionExpired.asSharedFlow()

    // ✅ Set user after successful login
    fun setUser(user: UserResponse) {
        _user.value = user
    }

    // ✅ Clear user session (memory only)
    fun clear() {
        _user.value = null
    }

    // ✅ Trigger session expiry event
    fun notifySessionExpired() {
        if (_user.value != null) {
            _sessionExpired.tryEmit(Unit)
        }
    }

    fun isLoggedIn(): Boolean {
        return _user.value != null
    }

    fun isPaid(): Boolean {
        return _user.value?.plan == "PAID"
    }
}
