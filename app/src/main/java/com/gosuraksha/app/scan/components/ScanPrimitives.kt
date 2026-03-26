package com.gosuraksha.app.scan.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.gosuraksha.app.scan.design.ScanColors

enum class ScanRiskTone {
    SAFE,
    WARNING,
    DANGER,
}

fun ScanRiskTone.containerColor(colors: ScanColors, emphasized: Boolean = false): Color {
    val alpha = if (emphasized) 0.22f else 0.12f
    return when (this) {
        ScanRiskTone.SAFE -> colors.safeGreen.copy(alpha = alpha)
        ScanRiskTone.WARNING -> colors.warningOrange.copy(alpha = alpha)
        ScanRiskTone.DANGER -> colors.dangerRed.copy(alpha = alpha)
    }
}

fun ScanRiskTone.contentColor(colors: ScanColors): Color = when (this) {
    ScanRiskTone.SAFE -> colors.safeGreen
    ScanRiskTone.WARNING -> colors.warningOrange
    ScanRiskTone.DANGER -> colors.dangerRed
}

fun String.toScanRiskTone(): ScanRiskTone = when (uppercase()) {
    "SAFE", "LOW" -> ScanRiskTone.SAFE
    "WARNING", "MEDIUM" -> ScanRiskTone.WARNING
    else -> ScanRiskTone.DANGER
}

@Immutable
data class ScanToolCardModel(
    val title: String,
    val description: String,
    val buttonLabel: String,
    val icon: ImageVector,
    val tags: List<String> = emptyList(),
    val variant: ScanToolVariant,
)
