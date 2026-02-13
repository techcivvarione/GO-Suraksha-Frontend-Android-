package com.gosuraksha.app.scan

import com.gosuraksha.app.scan.model.AnalyzeResponse

data class ScanUiState(
    val loading: Boolean = false,
    val result: AnalyzeResponse? = null,
    val aiExplanation: String? = null,
    val error: String? = null
)
