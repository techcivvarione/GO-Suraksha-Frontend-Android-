package com.gosuraksha.app.domain.repository

import com.gosuraksha.app.domain.model.scan.AiExplainResult
import com.gosuraksha.app.domain.model.scan.AiImageScanResult
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult
import com.gosuraksha.app.domain.result.DomainResult

interface ScanRepository {
    suspend fun analyze(type: String, content: String): DomainResult<ScanAnalysisResult>
    suspend fun explain(text: String): DomainResult<AiExplainResult>
    suspend fun scanAiImage(bytes: ByteArray): DomainResult<AiImageScanResult>
    suspend fun scanRealityMedia(bytes: ByteArray, mimeType: String): DomainResult<AiImageScanResult>
}
