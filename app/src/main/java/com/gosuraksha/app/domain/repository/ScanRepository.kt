package com.gosuraksha.app.domain.repository

import android.content.Context
import android.net.Uri
import com.gosuraksha.app.domain.model.scan.AiExplainResult
import com.gosuraksha.app.domain.model.scan.AiImageScanResult
import com.gosuraksha.app.domain.model.scan.QrScanAnalysis
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult
import com.gosuraksha.app.domain.result.DomainResult

interface ScanRepository {
    suspend fun analyze(type: String, content: String): DomainResult<ScanAnalysisResult>
    suspend fun analyzeQr(rawPayload: String): DomainResult<QrScanAnalysis>
    suspend fun explain(text: String): DomainResult<AiExplainResult>
    suspend fun scanAiImage(context: Context, uri: Uri): DomainResult<AiImageScanResult>
    /** Calls POST /scan/image/explain with structured scan data for a result-specific explanation. */
    suspend fun explainImage(scan: AiImageScanResult): DomainResult<AiExplainResult>
}
