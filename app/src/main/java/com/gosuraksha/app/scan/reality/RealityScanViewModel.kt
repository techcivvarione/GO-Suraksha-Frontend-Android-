package com.gosuraksha.app.scan.reality

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.domain.model.scan.AiImageScanResult
import com.gosuraksha.app.domain.result.DomainError
import com.gosuraksha.app.domain.result.DomainResult
import com.gosuraksha.app.domain.usecase.ScanRealityParams
import com.gosuraksha.app.domain.usecase.ScanUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RealityScanViewModel(
    private val useCases: ScanUseCases
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _aiExplanation = MutableStateFlow<String?>(null)
    val aiExplanation: StateFlow<String?> = _aiExplanation

    private val _aiExplainLoading = MutableStateFlow(false)
    val aiExplainLoading: StateFlow<Boolean> = _aiExplainLoading

    /**
     * Scans an image URI synchronously via POST /scan/image.
     * The mimeType is resolved from the URI at the repository layer.
     */
    fun scanRealityMedia(
        context: Context,
        uri: Uri,
        mimeType: String,
        onSuccess: (RealityScanResult) -> Unit,
        onError: (String) -> Unit
    ) = scan(context, uri, mimeType, onSuccess, onError)

    /**
     * Calls POST /scan/image/explain with the full structured scan result so the
     * backend can produce a specific, non-generic explanation for THIS image.
     *
     * Sends risk_level, risk_score, highlights, and recommendation — the backend
     * builds the OpenAI prompt from all four fields and caches the result in Redis
     * to avoid duplicate API calls for the same scan.
     *
     * Requires AI_EXPLAIN feature (GO_PRO or GO_ULTRA).
     */
    fun explainImageResult(result: RealityScanResult) {
        viewModelScope.launch {
            _aiExplainLoading.value = true
            _aiExplanation.value = null

            // Map the screen-layer result back to the domain model so ExplainImageUseCase
            // can forward all four fields to /scan/image/explain.
            val scanParams = AiImageScanResult(
                riskLevel      = result.riskLevel,
                riskScore      = result.riskScore,
                highlights     = result.highlights,
                recommendation = result.recommendation,
            )

            when (val r = useCases.explainImage(scanParams)) {
                is DomainResult.Success -> _aiExplanation.value = r.data.aiExplanation
                is DomainResult.Failure -> {
                    // Surface forbidden (plan gate) but swallow other errors silently
                    if (r.error == DomainError.Forbidden) {
                        _aiExplanation.value = null
                    }
                }
            }
            _aiExplainLoading.value = false
        }
    }

    /** Clear the AI explanation when a new image is selected. */
    fun clearExplanation() {
        _aiExplanation.value = null
    }

    private fun scan(
        context: Context,
        uri: Uri,
        mimeType: String,
        onSuccess: (RealityScanResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!SessionManager.isLoggedIn()) {
            onError("error_unauthorized")
            return
        }

        _isScanning.value = true
        viewModelScope.launch {
            try {
                when (val result = useCases.scanAiImage(ScanRealityParams(context, uri, mimeType))) {
                    is DomainResult.Success -> onSuccess(
                        RealityScanResult(
                            riskLevel        = result.data.riskLevel ?: "LOW",
                            riskScore        = result.data.riskScore,
                            confidence       = result.data.confidence,
                            confidenceLabel  = result.data.confidenceLabel,
                            summary          = result.data.summary
                                ?: result.data.recommendation
                                ?: "Scan complete.",
                            highlights       = result.data.highlights.ifEmpty {
                                listOf("No detailed evidence was returned.")
                            },
                            technicalSignals = result.data.technicalSignals,
                            recommendation   = result.data.recommendation ?: "Scan complete.",
                        )
                    )
                    is DomainResult.Failure -> onError(result.error.toMessage())
                }
            } finally {
                _isScanning.value = false
            }
        }
    }
}

class RealityScanViewModelFactory(
    private val useCases: ScanUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RealityScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RealityScanViewModel(useCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun DomainError.toMessage(): String {
    return when (this) {
        DomainError.Network        -> "error_network"
        DomainError.Timeout        -> "error_timeout"
        DomainError.Unauthorized   -> "error_unauthorized"
        DomainError.ScanLimitReached -> when {
            SessionManager.isUltra() -> "error_generic"        // ULTRA should never hit a limit
            SessionManager.isPaid()  -> "error_scan_limit_reached_pro"
            else                     -> "error_scan_limit_reached_free"
        }
        DomainError.Forbidden -> "error_forbidden"
        DomainError.NotFound  -> "error_not_found"
        DomainError.Server    -> "error_server"
        is DomainError.Unknown -> message ?: "error_generic"
    }
}
