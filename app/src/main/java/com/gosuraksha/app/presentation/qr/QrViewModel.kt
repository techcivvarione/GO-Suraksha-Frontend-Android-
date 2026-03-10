package com.gosuraksha.app.presentation.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.repository.QrErrorType
import com.gosuraksha.app.data.repository.QrRepoResult
import com.gosuraksha.app.data.repository.QrRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QrViewModel(
    private val repository: QrRepository
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
        _state.value = _state.value.copy(
            parsedQr = parsed,
            analysisState = QrAnalysisUiState.Idle
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

            when (val result = repository.analyzeQr(parsed.rawPayload)) {
                is QrRepoResult.Success -> {
                    _state.value = _state.value.copy(
                        analysisState = QrAnalysisUiState.Success(result.data)
                    )
                }

                is QrRepoResult.Error -> {
                    val canRetry = result.type == QrErrorType.NETWORK || result.type == QrErrorType.SERVER
                    _state.value = _state.value.copy(
                        analysisState = QrAnalysisUiState.Error(
                            message = result.message,
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

class QrViewModelFactory(
    private val repository: QrRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QrViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QrViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

