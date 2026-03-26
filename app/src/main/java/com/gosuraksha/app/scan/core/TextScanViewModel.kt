package com.gosuraksha.app.scan.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult
import com.gosuraksha.app.domain.result.DomainError
import com.gosuraksha.app.domain.result.DomainResult
import com.gosuraksha.app.domain.usecase.AnalyzeTextParams
import com.gosuraksha.app.domain.usecase.ScanUseCases
import com.gosuraksha.app.presentation.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TextScanViewModel(
    private val useCases: ScanUseCases
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ScanAnalysisResult>>(UiState.Idle)
    val state: StateFlow<UiState<ScanAnalysisResult>> = _state

    private val _aiExplanation = MutableStateFlow<String?>(null)
    val aiExplanation: StateFlow<String?> = _aiExplanation

    private val _aiExplainLoading = MutableStateFlow(false)
    val aiExplainLoading: StateFlow<Boolean> = _aiExplainLoading

    fun analyzeThreat(text: String) = analyze("THREAT", text)

    fun analyzeEmail(email: String) = analyze("EMAIL", email)

    fun analyzePassword(password: String) = analyze("PASSWORD", password)

    fun loadAiExplanation(text: String) {
        viewModelScope.launch {
            _aiExplainLoading.value = true
            when (val result = useCases.explain(text)) {
                is DomainResult.Success -> _aiExplanation.value = result.data.aiExplanation
                is DomainResult.Failure -> _state.value = UiState.Error(result.error.toMessage())
            }
            _aiExplainLoading.value = false
        }
    }

    private fun analyze(type: String, content: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _aiExplanation.value = null
            when (val result = useCases.analyze(AnalyzeTextParams(type, content))) {
                is DomainResult.Success -> _state.value = UiState.Success(result.data)
                is DomainResult.Failure -> _state.value = UiState.Error(result.error.toMessage())
            }
        }
    }
}

class TextScanViewModelFactory(
    private val useCases: ScanUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TextScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TextScanViewModel(useCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun DomainError.toMessage(): String {
    return when (this) {
        DomainError.Network -> "error_network"
        DomainError.Timeout -> "error_timeout"
        DomainError.Unauthorized -> "error_unauthorized"
        DomainError.ScanLimitReached -> when {
            SessionManager.isUltra() -> "error_generic"        // ULTRA should never hit a limit
            SessionManager.isPaid()  -> "error_scan_limit_reached_pro"
            else                     -> "error_scan_limit_reached_free"
        }
        DomainError.Forbidden -> "error_forbidden"
        DomainError.NotFound -> "error_not_found"
        DomainError.Server -> "error_server"
        is DomainError.Unknown -> message ?: "error_generic"
    }
}
