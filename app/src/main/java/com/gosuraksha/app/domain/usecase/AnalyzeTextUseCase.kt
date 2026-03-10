package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.domain.model.scan.ScanAnalysisResult
import com.gosuraksha.app.domain.repository.ScanRepository
import com.gosuraksha.app.domain.result.DomainResult

data class AnalyzeTextParams(
    val type: String,
    val content: String
)

class AnalyzeTextUseCase(
    private val repository: ScanRepository,
    dispatchers: DispatcherProvider
) : UseCase<AnalyzeTextParams, DomainResult<ScanAnalysisResult>>(dispatchers) {
    override suspend fun execute(params: AnalyzeTextParams): DomainResult<ScanAnalysisResult> {
        return repository.analyze(params.type, params.content)
    }
}
