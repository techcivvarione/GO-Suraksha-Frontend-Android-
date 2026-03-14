package com.gosuraksha.app.core.session

import com.gosuraksha.app.domain.model.Plan
import com.gosuraksha.app.domain.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _sessionExpired = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpired = _sessionExpired.asSharedFlow()

    fun setUser(user: User) {
        _user.value = user
    }

    fun clear() {
        _user.value = null
    }

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }

    fun isLoggedIn(): Boolean = _user.value != null

    fun isPaid(): Boolean = _user.value?.plan == Plan.PAID
}
