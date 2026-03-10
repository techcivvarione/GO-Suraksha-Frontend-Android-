package com.gosuraksha.app.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult
import com.gosuraksha.app.domain.result.DomainError
import com.gosuraksha.app.domain.result.DomainResult
import com.gosuraksha.app.domain.usecase.ScanRealityParams
import com.gosuraksha.app.domain.usecase.AnalyzeTextParams
import com.gosuraksha.app.domain.usecase.ScanUseCases
import com.gosuraksha.app.presentation.state.UiState
import com.gosuraksha.app.ui.main.RealityScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ScanUiModel(
    val analysis: ScanAnalysisResult? = null,
    val aiExplanation: String? = null,
    val aiExplainLoading: Boolean = false,
    val upgradeRequired: Boolean = false,
    val aiImageResult: RealityScanResult? = null
)

class ScanViewModel(
    private val useCases: ScanUseCases
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ScanUiModel>>(UiState.Idle)
    val state: StateFlow<UiState<ScanUiModel>> = _state

    fun analyze(type: String, content: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            when (val result = useCases.analyze(AnalyzeTextParams(type, content))) {
                is DomainResult.Success -> {
                    val data = ScanUiModel(
                        analysis = result.data,
                        aiExplanation = null,
                        aiExplainLoading = false,
                        upgradeRequired = result.data.upgrade?.required == true,
                        aiImageResult = null
                    )
                    _state.value = UiState.Success(data)
                }
                is DomainResult.Failure -> _state.value = UiState.Error(result.error.toMessage())
            }
        }
    }

    fun loadAiExplanation(text: String) {
        viewModelScope.launch {
            val current = currentModel()
            _state.value = UiState.Success(current.copy(aiExplainLoading = true))
            when (val result = useCases.explain(text)) {
                is DomainResult.Success -> {
                    _state.value = UiState.Success(
                        current.copy(
                            aiExplanation = result.data.aiExplanation,
                            aiExplainLoading = false
                        )
                    )
                }
                is DomainResult.Failure -> _state.value = UiState.Error(result.error.toMessage())
            }
        }
    }

    fun scanAiImage(
        bytes: ByteArray,
        mimeType: String,
        onSuccess: (RealityScanResult) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = useCases.scanAiImage(ScanRealityParams(bytes, mimeType))) {
                is DomainResult.Success -> {
                    val ui = RealityScanResult(
                        riskLevel = result.data.riskLevel,
                        confidence = result.data.confidence,
                        reasons = result.data.reasons,
                        recommendation = result.data.recommendation
                    )
                    _state.value = UiState.Success(currentModel().copy(aiImageResult = ui))
                    onSuccess(ui)
                }
                is DomainResult.Failure -> {
                    val msg = result.error.toMessage()
                    _state.value = UiState.Error(msg)
                    onError(msg)
                }
            }
        }
    }

    private fun currentModel(): ScanUiModel {
        return (state.value as? UiState.Success)?.data ?: ScanUiModel()
    }
}

class ScanViewModelFactory(
    private val useCases: ScanUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanViewModel(useCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun DomainError.toMessage(): String {
    return when (this) {
        DomainError.Network -> "error_network"
        DomainError.Timeout -> "error_timeout"
        DomainError.Unauthorized -> "error_unauthorized"
        DomainError.Forbidden -> "error_forbidden"
        DomainError.NotFound -> "error_not_found"
        DomainError.Server -> "error_server"
        is DomainError.Unknown -> message ?: "error_generic"
    }
}
