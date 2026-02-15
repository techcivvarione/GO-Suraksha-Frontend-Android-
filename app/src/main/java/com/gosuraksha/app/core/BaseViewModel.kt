package com.gosuraksha.app.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    protected fun safeApiCall(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                block()

            } catch (e: Throwable) {

                val uiError = ErrorMapper.map(e)

                // 🔐 If unauthorized, trigger global logout
                if (uiError is UiError.Unauthorized) {
                    SessionManager.clear()
                    SessionManager.notifySessionExpired()
                }

                _error.value = uiError.message()

            } finally {
                _loading.value = false
            }
        }
    }
}
