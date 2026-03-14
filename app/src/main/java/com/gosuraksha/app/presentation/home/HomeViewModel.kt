package com.gosuraksha.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.domain.model.home.HomeOverview
import com.gosuraksha.app.domain.result.DomainError
import com.gosuraksha.app.domain.result.DomainResult
import com.gosuraksha.app.domain.usecase.HomeUseCases
import com.gosuraksha.app.presentation.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val useCases: HomeUseCases
) : ViewModel() {

    private val _overviewState = MutableStateFlow<UiState<HomeOverview>>(UiState.Idle)
    val overviewState: StateFlow<UiState<HomeOverview>> = _overviewState

    init {
        loadOverview()
    }

    fun loadOverview() {
        viewModelScope.launch {
            _overviewState.value = UiState.Loading
            when (val result = useCases.getOverview(Unit)) {
                is DomainResult.Success -> _overviewState.value = UiState.Success(result.data)
                is DomainResult.Failure -> _overviewState.value = UiState.Error(result.error.toMessage())
            }
        }
    }
}

class HomeViewModelFactory(
    private val useCases: HomeUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(useCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun DomainError.toMessage(): String {
    return when (this) {
        DomainError.Network -> "error_network"
        DomainError.Timeout -> "error_timeout"
        DomainError.Unauthorized -> "error_unauthorized"
        DomainError.ScanLimitReached -> "error_generic"
        DomainError.Forbidden -> "error_forbidden"
        DomainError.NotFound -> "error_not_found"
        DomainError.Server -> "error_server"
        is DomainError.Unknown -> message ?: "error_generic"
    }
}
