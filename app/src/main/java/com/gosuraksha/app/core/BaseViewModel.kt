package com.gosuraksha.app.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.core.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    protected fun <T> safeApiCall(
        block: suspend () -> T
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                block()
            } catch (e: Throwable) {

                val uiError = ErrorMapper.map(throwable = e)

                // 🔥 If unauthorized, trigger global logout
                if (uiError is UiError.Unauthorized) {
                    SessionManager.clear()
                    SessionManager.notifySessionExpired()
                }

                // ✅ FIX: Pass context to get string
                _error.value = uiError.message(getApplication())

            } finally {
                _loading.value = false
            }
        }
    }
}
