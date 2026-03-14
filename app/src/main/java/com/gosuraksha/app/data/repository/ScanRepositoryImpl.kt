package com.gosuraksha.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.gosuraksha.app.core.network.NetworkErrorMapper
import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.data.mapper.toDomain
import com.gosuraksha.app.data.mapper.toRealityDomain
import com.gosuraksha.app.data.remote.RealityMediaType
import com.gosuraksha.app.data.remote.ScanRemoteDataSource
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
            when (val result = handleResponse(remote.analyze(type, content))) {
                is ScanResult.Success -> DomainResult.Success(result.data.toDomain())
                is ScanResult.Error -> mapErrorCodeToDomain(result.code, type, result.message)
            }
        } catch (t: Throwable) {
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }

    override suspend fun analyzeQr(rawPayload: String): DomainResult<QrScanAnalysis> {
        return try {
            val response = qrApi.analyzeQr(com.gosuraksha.app.network.QrAnalyzeRequest(raw_payload = rawPayload))
            if (!response.isSuccessful) {
                val rawError = response.errorBody()?.string()
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
                val message = apiError?.detail?.ifBlank { null } ?: codeToMessage(response.code())
                return DomainResult.Failure(DomainError.Unknown(message))
            }
            val body = response.body()
            if (body == null) {
                DomainResult.Failure(DomainError.Unknown("error_server"))
            } else {
                DomainResult.Success(
                    QrScanAnalysis(
                        riskScore = body.risk_score,
                        riskLevel = body.risk_level,
                        detectedType = body.detected_type,
                        reasons = body.reasons,
                        recommendedAction = body.recommended_action,
                        isFlagged = body.is_flagged
                    )
                )
            }
        } catch (t: Throwable) {
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

    override suspend fun scanAiImage(context: Context, uri: Uri): DomainResult<AiImageScanResult> {
        val mimeType = context.contentResolver.getType(uri)?.ifBlank { null } ?: "image/jpeg"
        return scanRealityMedia(context, uri, mimeType)
    }

    override suspend fun scanRealityMedia(
        context: Context,
        uri: Uri,
        mimeType: String
    ): DomainResult<AiImageScanResult> {
        return try {
            val resolvedMimeType = context.contentResolver.getType(uri)
                ?.ifBlank { null }
                ?: mimeType
            val mediaType = when {
                resolvedMimeType.startsWith("video/", ignoreCase = true) -> RealityMediaType.VIDEO
                resolvedMimeType.startsWith("audio/", ignoreCase = true) -> RealityMediaType.AUDIO
                else -> RealityMediaType.IMAGE
            }
            val fileName = when (mediaType) {
                RealityMediaType.IMAGE -> "upload.jpg"
                RealityMediaType.VIDEO -> "upload.mp4"
                RealityMediaType.AUDIO -> "upload.mp3"
            }
            val part = MultipartBody.Part.createFormData(
                "file",
                fileName,
                StreamRequestBody(context, uri, resolvedMimeType)
            )
            when (val result = handleResponse(remote.scanReality(part, mediaType))) {
                is ScanResult.Success -> DomainResult.Success(result.data.toRealityDomain())
                is ScanResult.Error -> mapErrorCodeToDomain(result.code, "REALITY", result.message)
            }
        } catch (t: Throwable) {
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }

    private fun <T> handleResponse(response: Response<T>): ScanResult<T> {
        if (!response.isSuccessful) {
            val rawError = response.errorBody()?.string()
            val authError = StructuredApiErrorParser.parseAuthError(rawError)
            if (authError != null) {
                return ScanResult.Error(response.code(), authError.messageKey)
            }
            if (StructuredApiErrorParser.parseErrorCode(rawError) == "SCAN_LIMIT_REACHED") {
                return ScanResult.Error(response.code(), "error_scan_limit_reached")
            }
            return ScanResult.Error(response.code(), "error_server")
        }
        val body = response.body() ?: return ScanResult.Error(response.code(), "error_server")
        return ScanResult.Success(body)
    }

    private fun mapErrorCodeToDomain(code: Int?, type: String, message: String? = null): DomainResult.Failure {
        if (message == "error_scan_limit_reached") {
            return DomainResult.Failure(DomainError.ScanLimitReached)
        }
        return when (code) {
            400 -> DomainResult.Failure(DomainError.Unknown("Invalid input"))
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

