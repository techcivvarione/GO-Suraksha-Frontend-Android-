package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.domain.model.scan.AiExplainResult
import com.gosuraksha.app.domain.model.scan.AiImageScanResult
import com.gosuraksha.app.domain.repository.ScanRepository
import com.gosuraksha.app.domain.result.DomainResult

/**
 * Calls POST /scan/image/explain with the full structured scan result.
 *
 * Unlike [ExplainScanUseCase] (which sends raw text to /ai/explain),
 * this use case sends risk_level, risk_score, highlights, and recommendation
 * so the backend can produce a specific, non-generic explanation for THIS image.
 *
 * Requires AI_EXPLAIN feature — FREE users receive DomainError.Forbidden.
 */
class ExplainImageUseCase(
    private val repository: ScanRepository,
    dispatchers: DispatcherProvider,
) : UseCase<AiImageScanResult, DomainResult<AiExplainResult>>(dispatchers) {

    override suspend fun execute(params: AiImageScanResult): DomainResult<AiExplainResult> =
        repository.explainImage(params)
}
