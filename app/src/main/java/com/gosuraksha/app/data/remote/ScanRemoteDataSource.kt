package com.gosuraksha.app.data.remote

import com.gosuraksha.app.data.remote.dto.scan.AiExplainRequestDto
import com.gosuraksha.app.data.remote.dto.scan.EmailScanRequest
import com.gosuraksha.app.data.remote.dto.scan.PasswordScanRequest
import com.gosuraksha.app.data.remote.dto.scan.ThreatScanRequest
import com.gosuraksha.app.network.AnalyzeApi
import okhttp3.MultipartBody

enum class RealityMediaType {
    IMAGE, VIDEO, AUDIO
}

class ScanRemoteDataSource(
    private val api: AnalyzeApi
) {
    suspend fun analyze(type: String, content: String) = when (type.uppercase()) {
        "PASSWORD" -> api.scanPassword(PasswordScanRequest(password = content))
        "EMAIL" -> api.scanEmail(EmailScanRequest(email = content))
        "THREAT", "MESSAGES" -> api.scanThreat(ThreatScanRequest(text = content))
        "QR" -> throw IllegalArgumentException("QR analyze is handled by /qr/analyze")
        else -> api.scanThreat(ThreatScanRequest(text = content))
    }

    suspend fun explain(text: String) =
        api.explain(AiExplainRequestDto(text = text))

    suspend fun scanReality(part: MultipartBody.Part, mediaType: RealityMediaType) = when (mediaType) {
        RealityMediaType.IMAGE -> api.scanRealityImage(part)
        RealityMediaType.VIDEO -> api.scanRealityVideo(part)
        RealityMediaType.AUDIO -> api.scanRealityAudio(part)
    }
}
