package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.domain.model.scan.AiExplainResult
import com.gosuraksha.app.domain.repository.ScanRepository
import com.gosuraksha.app.domain.result.DomainResult

data class ExplainScanParams(
    val text: String,
    val language: String = "en"
)

class ExplainScanUseCase(
    private val repository: ScanRepository,
    dispatchers: DispatcherProvider
) : UseCase<ExplainScanParams, DomainResult<AiExplainResult>>(dispatchers) {
    override suspend fun execute(params: ExplainScanParams): DomainResult<AiExplainResult> {
        return repository.explain(params.text, params.language)
    }
}
