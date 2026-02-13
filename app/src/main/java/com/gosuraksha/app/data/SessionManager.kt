package com.gosuraksha.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.gosuraksha.app.auth.model.UserResponse

object SessionManager {

    private const val PREF_NAME = "gosuraksha_session"
    private const val KEY_TOKEN = "access_token"

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user

    private val _sessionExpired = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpired = _sessionExpired.asSharedFlow()

    private var token: String? = null

    fun saveToken(context: Context, accessToken: String) {
        token = accessToken
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, accessToken)
            .apply()
    }

    fun getToken(context: Context): String? {
        if (token != null) return token
        token = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
        return token
    }

    fun clearToken(context: Context) {
        token = null
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    fun setUser(user: UserResponse) {
        _user.value = user
    }

    fun clear(context: Context) {
        _user.value = null
        clearToken(context)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }

    fun notifySessionExpired() {
        if (_user.value != null) {
            _sessionExpired.tryEmit(Unit)
        }
    }

    fun isPaid(): Boolean {
        return _user.value?.plan == "PAID"
    }
}
