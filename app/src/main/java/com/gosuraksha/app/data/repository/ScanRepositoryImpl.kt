package com.gosuraksha.app.data.repository

import com.gosuraksha.app.core.network.NetworkErrorMapper
import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.data.mapper.toDomain
import com.gosuraksha.app.data.mapper.toRealityDomain
import com.gosuraksha.app.data.remote.RealityMediaType
import com.gosuraksha.app.data.mapper.toDomain as toDomainError
import com.gosuraksha.app.data.remote.ScanRemoteDataSource
import com.gosuraksha.app.domain.model.scan.AiExplainResult
import com.gosuraksha.app.domain.model.scan.AiImageScanResult
import com.gosuraksha.app.domain.result.DomainError
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult
import com.gosuraksha.app.domain.repository.ScanRepository
import com.gosuraksha.app.domain.result.DomainResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class ScanRepositoryImpl(
    private val remote: ScanRemoteDataSource
) : ScanRepository {

    sealed class ScanResult<out T> {
        data class Success<T>(val data: T) : ScanResult<T>()
        data class Error(val code: Int?, val message: String) : ScanResult<Nothing>()
    }

    override suspend fun analyze(type: String, content: String): DomainResult<ScanAnalysisResult> {
        return try {
            when (val result = handleResponse(remote.analyze(type, content))) {
                is ScanResult.Success -> DomainResult.Success(result.data.toDomain())
                is ScanResult.Error -> mapErrorCodeToDomain(result.code, type)
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

    override suspend fun scanAiImage(bytes: ByteArray): DomainResult<AiImageScanResult> {
        return scanRealityMedia(bytes, "image/jpeg")
    }

    override suspend fun scanRealityMedia(bytes: ByteArray, mimeType: String): DomainResult<AiImageScanResult> {
        return try {
            val requestBody = bytes.toRequestBody(mimeType.toMediaType())
            val mediaType = when {
                mimeType.startsWith("video/", ignoreCase = true) -> RealityMediaType.VIDEO
                mimeType.startsWith("audio/", ignoreCase = true) -> RealityMediaType.AUDIO
                else -> RealityMediaType.IMAGE
            }
            val fileName = when (mediaType) {
                RealityMediaType.IMAGE -> "upload.jpg"
                RealityMediaType.VIDEO -> "upload.mp4"
                RealityMediaType.AUDIO -> "upload.mp3"
            }
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
            when (val result = handleResponse(remote.scanReality(part, mediaType))) {
                is ScanResult.Success -> DomainResult.Success(result.data.toRealityDomain())
                is ScanResult.Error -> mapErrorCodeToDomain(result.code, "REALITY")
            }
        } catch (t: Throwable) {
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }

    private fun <T> handleResponse(response: Response<T>): ScanResult<T> {
        if (!response.isSuccessful) {
            return ScanResult.Error(response.code(), "error_server")
        }
        val body = response.body() ?: return ScanResult.Error(response.code(), "error_server")
        return ScanResult.Success(body)
    }

    private fun mapErrorCodeToDomain(code: Int?, type: String): DomainResult.Failure {
        return when (code) {
            400 -> DomainResult.Failure(DomainError.Unknown("Invalid input"))
            429 -> DomainResult.Failure(DomainError.Unknown("Too many scans. Please try later."))
            500 -> DomainResult.Failure(DomainError.Unknown("Server error"))
            401 -> DomainResult.Failure(AppError.Unauthorized.toDomainError())
            403 -> DomainResult.Failure(AppError.Forbidden.toDomainError())
            404 -> DomainResult.Failure(AppError.NotFound.toDomainError())
            else -> DomainResult.Failure(DomainError.Unknown("error_server"))
        }
    }
}
