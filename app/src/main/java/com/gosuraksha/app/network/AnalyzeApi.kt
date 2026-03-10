package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.scan.AiExplainRequestDto
import com.gosuraksha.app.data.remote.dto.scan.AiExplainResponseDto
import com.gosuraksha.app.data.remote.dto.scan.EmailScanRequest
import com.gosuraksha.app.data.remote.dto.scan.PasswordScanRequest
import com.gosuraksha.app.data.remote.dto.scan.QrScanRequest
import com.gosuraksha.app.data.remote.dto.scan.ScanResponse
import com.gosuraksha.app.data.remote.dto.scan.ThreatScanRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AnalyzeApi {

    @POST("/scan/password")
    suspend fun scanPassword(
        @Body request: PasswordScanRequest
    ): Response<ScanResponse>

    @POST("/scan/email")
    suspend fun scanEmail(
        @Body request: EmailScanRequest
    ): Response<ScanResponse>

    @POST("/scan/qr")
    suspend fun scanQr(
        @Body request: QrScanRequest
    ): Response<ScanResponse>

    @POST("/scan/threat")
    suspend fun scanThreat(
        @Body request: ThreatScanRequest
    ): Response<ScanResponse>

    @POST("/ai/explain")
    suspend fun explain(
        @Body request: AiExplainRequestDto
    ): AiExplainResponseDto

    @Multipart
    @POST("/scan/reality/image")
    suspend fun scanRealityImage(
        @Part file: MultipartBody.Part
    ): Response<ScanResponse>

    @Multipart
    @POST("/scan/reality/video")
    suspend fun scanRealityVideo(
        @Part file: MultipartBody.Part
    ): Response<ScanResponse>

    @Multipart
    @POST("/scan/reality/audio")
    suspend fun scanRealityAudio(
        @Part file: MultipartBody.Part
    ): Response<ScanResponse>
}
