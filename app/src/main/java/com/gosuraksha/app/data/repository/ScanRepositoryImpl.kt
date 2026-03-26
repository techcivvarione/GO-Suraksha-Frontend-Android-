package com.gosuraksha.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.core.network.NetworkErrorMapper
import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.data.mapper.toDomain
import com.gosuraksha.app.data.mapper.toRealityDomain
import com.gosuraksha.app.data.remote.ScanRemoteDataSource
import com.gosuraksha.app.data.remote.dto.scan.ImageExplainRequest
import com.gosuraksha.app.data.mapper.toDomain as toDomainError
import com.gosuraksha.app.domain.model.scan.AiExplainResult
import com.gosuraksha.app.domain.model.scan.AiImageScanResult
import com.gosuraksha.app.domain.model.scan.QrScanAnalysis
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult
import com.gosuraksha.app.domain.repository.ScanRepository
import com.gosuraksha.app.domain.result.DomainError
import com.gosuraksha.app.domain.result.DomainResult
import com.gosuraksha.app.network.ApiError
import com.gosuraksha.app.network.QrApi
import com.gosuraksha.app.network.StreamRequestBody
import com.gosuraksha.app.network.StructuredApiErrorParser
import okhttp3.MultipartBody
import retrofit2.Response

class ScanRepositoryImpl(
    private val remote: ScanRemoteDataSource,
    private val qrApi: QrApi
) : ScanRepository {

    sealed class ScanResult<out T> {
        data class Success<T>(val data: T) : ScanResult<T>()
        data class Error(val code: Int?, val message: String) : ScanResult<Nothing>()
    }

    override suspend fun analyze(type: String, content: String): DomainResult<ScanAnalysisResult> {
        return try {
            when (val result = handleResponse(remote.analyze(type, content), "TEXT/$type")) {
                is ScanResult.Success -> DomainResult.Success(result.data.toDomain())
                is ScanResult.Error -> mapErrorCodeToDomain(result.code, type, result.message)
            }
        } catch (t: Throwable) {
            Log.e("SCAN_DEBUG", "[TEXT/$type] analyze failed", t)
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }

    override suspend fun analyzeQr(rawPayload: String): DomainResult<QrScanAnalysis> {
        return try {
            val wrapped = qrApi.analyzeQr(com.gosuraksha.app.network.QrAnalyzeRequest(raw_payload = rawPayload))
            if (!wrapped.isSuccessful) {
                val rawError = wrapped.errorBody()?.string()
                Log.w("SCAN_DEBUG", "[QR] HTTP ${wrapped.code()} error body = $rawError")
                val authError = StructuredApiErrorParser.parseAuthError(rawError)
                if (authError != null) {
                    return when (authError.code) {
                        com.gosuraksha.app.network.AuthErrorCode.TOKEN_EXPIRED ->
                            DomainResult.Failure(DomainError.Unauthorized)
                        com.gosuraksha.app.network.AuthErrorCode.INVALID_TOKEN ->
                            DomainResult.Failure(DomainError.Unknown(authError.messageKey))
                    }
                }
                val apiError = parseApiError(rawError)
                val message = apiError?.detail?.ifBlank { null } ?: codeToMessage(wrapped.code())
                return DomainResult.Failure(DomainError.Unknown(message))
            }
            val body = wrapped.body()?.data
            if (BuildConfig.DEBUG) {
                Log.d("SCAN_DEBUG", "[QR] parsed body = ${Gson().toJson(body)}")
            }
            if (body == null) {
                Log.e("SCAN_DEBUG", "[QR] HTTP 200 but envelope data is null")
                return DomainResult.Failure(DomainError.Unknown("error_server"))
            }
            DomainResult.Success(
                QrScanAnalysis(
                    riskScore       = body.risk_score ?: 0,
                    riskLevel       = body.risk_level ?: "UNKNOWN",
                    detectedType    = body.detected_type,
                    reasons         = body.reasons ?: emptyList(),
                    recommendedAction = body.recommended_action,
                    isFlagged       = body.is_flagged ?: false,
                    isPayment       = body.is_payment ?: false,
                    merchantName    = body.merchant_name,
                    upiId           = body.upi_id,
                    amount          = body.amount,
                    summary         = body.summary,
                )
            )
        } catch (t: Throwable) {
            Log.e("SCAN_DEBUG", "[QR] analyze failed", t)
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }

    override suspend fun explain(text: String): DomainResult<AiExplainResult> {
        return try {
            val dto = remote.explain(text)
            DomainResult.Success(dto.toDomain())
        } catch (t: Throwable) {
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }

    override suspend fun explainImage(scan: AiImageScanResult): DomainResult<AiExplainResult> {
        return try {
            val request = ImageExplainRequest(
                riskLevel      = scan.riskLevel ?: "LOW",
                riskScore      = scan.riskScore,
                highlights     = scan.highlights,
                recommendation = scan.recommendation.orEmpty(),
            )
            val dto = remote.explainImage(request)
            val text = dto.explanation?.takeIf { it.isNotBlank() }
                ?: return DomainResult.Failure(DomainError.Unknown("error_generic"))
            DomainResult.Success(AiExplainResult(aiExplanation = text))
        } catch (t: Throwable) {
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }

    /**
     * Uploads the image at [uri] to POST /scan/image and returns a synchronous result.
     * No polling, no job IDs, no async state.
     */
    override suspend fun scanAiImage(context: Context, uri: Uri): DomainResult<AiImageScanResult> {
        return try {
            val mimeType = context.contentResolver.getType(uri)
                ?.ifBlank { null }
                ?: "image/jpeg"
            val part = MultipartBody.Part.createFormData(
                "file",
                "upload.jpg",
                StreamRequestBody(context, uri, mimeType)
            )
            when (val result = handleResponse(remote.scanImage(part), "IMAGE")) {
                is ScanResult.Success -> DomainResult.Success(result.data.toRealityDomain())
                is ScanResult.Error -> mapErrorCodeToDomain(result.code, "IMAGE", result.message)
            }
        } catch (t: Throwable) {
            Log.e("SCAN_DEBUG", "[IMAGE] scan failed", t)
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }

    private fun <T> handleResponse(response: Response<T>, source: String): ScanResult<T> {
        if (!response.isSuccessful) {
            val rawError = response.errorBody()?.string()
            Log.w("SCAN_DEBUG", "[$source] HTTP ${response.code()} error body = $rawError")
            val authError = StructuredApiErrorParser.parseAuthError(rawError)
            if (authError != null) {
                return ScanResult.Error(response.code(), authError.messageKey)
            }
            if (StructuredApiErrorParser.parseErrorCode(rawError) == "SCAN_LIMIT_REACHED") {
                return ScanResult.Error(response.code(), "error_scan_limit_reached")
            }
            return ScanResult.Error(response.code(), "error_server")
        }

        if (BuildConfig.DEBUG) {
            Log.d("SCAN_DEBUG", "[$source] parsed body = ${Gson().toJson(response.body())}")
        }
        val body = response.body()
        return if (body != null) {
            ScanResult.Success(body)
        } else {
            Log.e("SCAN_DEBUG", "[$source] HTTP ${response.code()} but response.body() is null")
            ScanResult.Error(response.code(), "error_server")
        }
    }

    private fun mapErrorCodeToDomain(code: Int?, type: String, message: String? = null): DomainResult.Failure {
        if (message == "error_scan_limit_reached") {
            return DomainResult.Failure(DomainError.ScanLimitReached)
        }
        return when (code) {
            400 -> DomainResult.Failure(DomainError.Unknown("Invalid input"))
            413 -> DomainResult.Failure(DomainError.Unknown("Image too large (max 10 MB)"))
            429 -> DomainResult.Failure(DomainError.Unknown("Too many scans. Please try later."))
            500 -> DomainResult.Failure(DomainError.Unknown("Server error"))
            401 -> when (message) {
                "error_session_invalid" -> DomainResult.Failure(DomainError.Unknown(message))
                else -> DomainResult.Failure(AppError.Unauthorized.toDomainError())
            }
            403 -> DomainResult.Failure(AppError.Forbidden.toDomainError())
            404 -> DomainResult.Failure(AppError.NotFound.toDomainError())
            else -> DomainResult.Failure(DomainError.Unknown("error_server"))
        }
    }

    private fun parseApiError(raw: String?): ApiError? {
        if (raw.isNullOrBlank()) return null
        return try {
            Gson().fromJson(raw, ApiError::class.java)
        } catch (_: Exception) {
            null
        }
    }

    private fun codeToMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid input"
            429 -> "Too many scans. Please try later."
            500 -> "Server error"
            else -> "error_server"
        }
    }
}
