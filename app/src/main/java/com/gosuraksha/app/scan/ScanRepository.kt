package com.gosuraksha.app.scan

import com.gosuraksha.app.network.AnalyzeApi
import com.gosuraksha.app.scan.model.*

class ScanRepository(
    private val api: AnalyzeApi
) {

    suspend fun analyze(type: String, content: String): AnalyzeResponse {
        return api.analyze(
            AnalyzeRequest(
                type = type,
                content = content
            )
        )
    }

    suspend fun explain(scanId: String): AiExplainResponse {
        return api.explain(
            AiExplainRequest(scan_id = scanId)
        )
    }
}
