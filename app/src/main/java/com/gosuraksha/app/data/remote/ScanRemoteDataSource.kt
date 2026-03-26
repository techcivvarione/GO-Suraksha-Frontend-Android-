package com.gosuraksha.app.data.remote

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.data.remote.dto.scan.AiExplainRequestDto
import com.gosuraksha.app.data.remote.dto.scan.AiExplainResponseDto
import com.gosuraksha.app.data.remote.dto.scan.EmailScanRequest
import com.gosuraksha.app.data.remote.dto.scan.ImageExplainRequest
import com.gosuraksha.app.data.remote.dto.scan.ImageExplainResponse
import com.gosuraksha.app.data.remote.dto.scan.PasswordScanRequest
import com.gosuraksha.app.data.remote.dto.scan.ScanResponse
import com.gosuraksha.app.data.remote.dto.scan.ThreatScanRequest
import com.gosuraksha.app.network.AnalyzeApi
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class ScanRemoteDataSource(
    private val api: AnalyzeApi
) {
    /**
     * Delegates to the appropriate text scan endpoint and unwraps the response envelope
     * transparently. Callers receive Response<ScanResponse> regardless of middleware.
     * Note: /scan/threat is excluded from the envelope by the backend middleware.
     */
    suspend fun analyze(type: String, content: String): Response<ScanResponse> =
        when (type.uppercase()) {
            "PASSWORD" -> api.scanPassword(PasswordScanRequest(password = content)).unwrapEnvelope()
            "EMAIL" -> api.scanEmail(EmailScanRequest(email = content)).unwrapEnvelope()
            "THREAT", "MESSAGES" -> api.scanThreat(ThreatScanRequest(text = content))
            "QR" -> throw IllegalArgumentException("QR analyze is handled by /qr/analyze")
            else -> api.scanThreat(ThreatScanRequest(text = content))
        }

    /**
     * Calls /ai/explain and unwraps the envelope, returning the DTO directly.
     */
    suspend fun explain(text: String): AiExplainResponseDto =
        api.explain(AiExplainRequestDto(text = text)).data

    /**
     * Uploads an image to POST /scan/image (synchronous, no polling).
     * Unwraps the envelope transparently; callers receive Response<ScanResponse>.
     */
    suspend fun scanImage(part: MultipartBody.Part): Response<ScanResponse> =
        api.scanImage(part).unwrapEnvelope()

    /**
     * Calls POST /scan/image/explain with the scan outputs and returns a
     * plain-English explanation from GPT-4o-mini (or a template fallback).
     */
    suspend fun explainImage(request: ImageExplainRequest): ImageExplainResponse =
        api.explainImage(request).data

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /**
     * Converts Response<ApiResponse<T>> → Response<T> by unwrapping the envelope.
     * For error responses the type parameter is erased at runtime so the cast is safe.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> Response<ApiResponse<T>>.unwrapEnvelope(): Response<T> {
        if (!isSuccessful) return this as Response<T>
        val data = body()?.data
            ?: return Response.error(502, "Empty envelope data".toResponseBody())
        return Response.success(data, raw())
    }
}
