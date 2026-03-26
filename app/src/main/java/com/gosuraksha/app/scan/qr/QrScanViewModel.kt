package com.gosuraksha.app.scan.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.domain.result.DomainResult
import com.gosuraksha.app.domain.usecase.AnalyzeQrUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QrScanViewModel(
    private val analyzeQrUseCase: AnalyzeQrUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QrUiState())
    val state: StateFlow<QrUiState> = _state

    private var lastParsedQr: ParsedQr? = null

    fun setScannerLoading(loading: Boolean) {
        _state.value = _state.value.copy(isScannerLoading = loading)
    }

    fun onQrRawDetected(rawValue: String?) {
        val parsed = parseRawPayload(rawValue)
        if (parsed == null) {
            _state.value = _state.value.copy(
                isScannerLoading = false,
                parsedQr = null,
                analysisState = QrAnalysisUiState.Error(
                    message = "error_qr_invalid_upi",
                    canRetry = false
                )
            )
            return
        }

        lastParsedQr = parsed
        // Skip Idle entirely — go straight to Loading so QrAnalysisResult never renders
        // the "analysis == null" branch with a generic error banner before the API responds.
        _state.value = _state.value.copy(
            parsedQr = parsed,
            analysisState = QrAnalysisUiState.Loading
        )

        analyzeParsed(parsed)
    }

    fun onScanFailure(message: String?) {
        _state.value = _state.value.copy(
            isScannerLoading = false,
            analysisState = QrAnalysisUiState.Error(
                message = message ?: "error_qr_unable_scan_now",
                canRetry = false
            )
        )
    }

    fun retryAnalyze() {
        val parsed = lastParsedQr ?: return
        analyzeParsed(parsed)
    }

    fun clearError() {
        val current = _state.value.analysisState
        if (current is QrAnalysisUiState.Error) {
            _state.value = _state.value.copy(analysisState = QrAnalysisUiState.Idle)
        }
    }

    fun startNewScan() {
        lastParsedQr = null
        _state.value = QrUiState()
    }

    private fun analyzeParsed(parsed: ParsedQr) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isScannerLoading = false,
                analysisState = QrAnalysisUiState.Loading
            )

            when (val result = analyzeQrUseCase(parsed.rawPayload)) {
                is DomainResult.Success -> {
                    _state.value = _state.value.copy(
                        analysisState = QrAnalysisUiState.Success(result.data)
                    )
                }

                is DomainResult.Failure -> {
                    val message = result.error.toMessage()
                    val canRetry = message == "error_network" || message == "error_timeout" || message == "error_server"
                    _state.value = _state.value.copy(
                        analysisState = QrAnalysisUiState.Error(
                            message = message,
                            canRetry = canRetry
                        )
                    )
                }
            }
        }
    }

    private fun parseRawPayload(rawValue: String?): ParsedQr? {
        val raw = rawValue?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return ParsedQr(rawPayload = raw)
    }
}

class QrScanViewModelFactory(
    private val analyzeQrUseCase: AnalyzeQrUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QrScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QrScanViewModel(analyzeQrUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun com.gosuraksha.app.domain.result.DomainError.toMessage(): String {
    return when (this) {
        com.gosuraksha.app.domain.result.DomainError.Network -> "error_network"
        com.gosuraksha.app.domain.result.DomainError.Timeout -> "error_timeout"
        com.gosuraksha.app.domain.result.DomainError.Unauthorized -> "error_unauthorized"
        com.gosuraksha.app.domain.result.DomainError.ScanLimitReached -> when {
            com.gosuraksha.app.core.session.SessionManager.isUltra() -> "error_generic"   // ULTRA never hits limits
            com.gosuraksha.app.core.session.SessionManager.isPaid()  -> "error_scan_limit_reached_pro"
            else                                                      -> "error_scan_limit_reached_free"
        }
        com.gosuraksha.app.domain.result.DomainError.Forbidden -> "error_forbidden"
        com.gosuraksha.app.domain.result.DomainError.NotFound -> "error_not_found"
        com.gosuraksha.app.domain.result.DomainError.Server -> "error_server"
        is com.gosuraksha.app.domain.result.DomainError.Unknown -> when (message) {
            "error_session_invalid", "INVALID_TOKEN", "Invalid token" -> "error_unauthorized"
            else -> message ?: "error_generic"
        }
    }
}

