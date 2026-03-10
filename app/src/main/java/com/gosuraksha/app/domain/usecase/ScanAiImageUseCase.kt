package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.domain.model.scan.AiImageScanResult
import com.gosuraksha.app.domain.repository.ScanRepository
import com.gosuraksha.app.domain.result.DomainResult

data class ScanRealityParams(
    val bytes: ByteArray,
    val mimeType: String
)

class ScanAiImageUseCase(
    private val repository: ScanRepository,
    dispatchers: DispatcherProvider
) : UseCase<ScanRealityParams, DomainResult<AiImageScanResult>>(dispatchers) {
    override suspend fun execute(params: ScanRealityParams): DomainResult<AiImageScanResult> {
        return repository.scanRealityMedia(params.bytes, params.mimeType)
    }
}
