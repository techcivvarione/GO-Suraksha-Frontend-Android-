package com.gosuraksha.app.scan.qr

import com.gosuraksha.app.domain.model.scan.QrScanAnalysis

data class ParsedQr(
    val rawPayload: String
)

sealed class QrAnalysisUiState {
    data object Idle : QrAnalysisUiState()
    data object Loading : QrAnalysisUiState()
    data class Success(val data: QrScanAnalysis) : QrAnalysisUiState()
    data class Error(val message: String, val canRetry: Boolean) : QrAnalysisUiState()
}

data class QrUiState(
    val isScannerLoading: Boolean = false,
    val parsedQr: ParsedQr? = null,
    val analysisState: QrAnalysisUiState = QrAnalysisUiState.Idle
)

