package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.data.remote.dto.scan.AiExplainRequestDto
import com.gosuraksha.app.data.remote.dto.scan.AiExplainResponseDto
import com.gosuraksha.app.data.remote.dto.scan.EmailScanRequest
import com.gosuraksha.app.data.remote.dto.scan.ImageExplainRequest
import com.gosuraksha.app.data.remote.dto.scan.ImageExplainResponse
import com.gosuraksha.app.data.remote.dto.scan.PasswordScanRequest
import com.gosuraksha.app.data.remote.dto.scan.ScanResponse
import com.gosuraksha.app.data.remote.dto.scan.ThreatScanRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AnalyzeApi {

    // Middleware wraps all /scan/* responses except /scan/threat
    @POST("/scan/password")
    suspend fun scanPassword(
        @Body request: PasswordScanRequest
    ): Response<ApiResponse<ScanResponse>>

    @POST("/scan/email")
    suspend fun scanEmail(
        @Body request: EmailScanRequest
    ): Response<ApiResponse<ScanResponse>>

    // /scan/threat is explicitly excluded from the envelope middleware
    @POST("/scan/threat")
    suspend fun scanThreat(
        @Body request: ThreatScanRequest
    ): Response<ScanResponse>

    @POST("/ai/explain")
    suspend fun explain(
        @Body request: AiExplainRequestDto
    ): ApiResponse<AiExplainResponseDto>

    // Synchronous image authenticity scan — middleware wraps in ApiResponse envelope
    @Multipart
    @POST("/scan/image")
    suspend fun scanImage(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<ScanResponse>>

    // LLM-powered plain-English explanation of a scan result
    @POST("/scan/image/explain")
    suspend fun explainImage(
        @Body request: ImageExplainRequest
    ): ApiResponse<ImageExplainResponse>
}
