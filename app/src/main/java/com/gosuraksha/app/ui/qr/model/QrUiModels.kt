package com.gosuraksha.app.ui.qr.model

data class QrCoordinatorState(
    val hasCameraPermission: Boolean,
    val hasFlash: Boolean,
    val flashEnabled: Boolean,
    val scanningPaused: Boolean,
    val pendingDetectedRaw: String?,
    val detectionAnimating: Boolean,
)
