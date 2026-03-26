package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.domain.model.scan.QrScanAnalysis
import com.gosuraksha.app.domain.repository.ScanRepository
import com.gosuraksha.app.domain.result.DomainResult

class AnalyzeQrUseCase(
    private val repository: ScanRepository,
    dispatchers: DispatcherProvider
) : UseCase<String, DomainResult<QrScanAnalysis>>(dispatchers) {
    override suspend fun execute(params: String): DomainResult<QrScanAnalysis> {
        return repository.analyzeQr(params)
    }
}
