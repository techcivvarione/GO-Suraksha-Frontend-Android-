package com.gosuraksha.app.presentation.base

import androidx.lifecycle.ViewModel
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.presentation.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel : ViewModel() {

    protected fun <T> MutableStateFlow<UiState<T>>.setResult(result: AppResult<T>) {
        value = when (result) {
            is AppResult.Success -> UiState.Success(result.data)
            is AppResult.Failure -> UiState.Error(result.error.toMessage())
        }
    }

    protected fun AppError.toMessage(): String {
        return when (this) {
            AppError.Network -> "error_network"
            AppError.Timeout -> "error_timeout"
            AppError.Unauthorized -> "error_unauthorized"
            AppError.Forbidden -> "error_forbidden"
            AppError.NotFound -> "error_not_found"
            AppError.Server -> "error_server"
            is AppError.Validation -> message
            is AppError.Unknown -> message ?: "error_generic"
        }
    }
}
