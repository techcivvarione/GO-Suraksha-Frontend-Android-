package com.gosuraksha.app.domain.usecase

data class ScanUseCases(
    val analyze: AnalyzeTextUseCase,
    val analyzeQr: AnalyzeQrUseCase,
    val explain: ExplainScanUseCase,
    val scanAiImage: ScanAiImageUseCase
)

interface ScanUseCaseProvider {
    fun scanUseCases(): ScanUseCases
}
