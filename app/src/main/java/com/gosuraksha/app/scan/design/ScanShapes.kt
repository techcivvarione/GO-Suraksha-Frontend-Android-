package com.gosuraksha.app.scan.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object ScanShapes {
    val sm           = RoundedCornerShape(8.dp)
    val md           = RoundedCornerShape(12.dp)
    val card         = RoundedCornerShape(16.dp)
    val cardLarge    = RoundedCornerShape(18.dp)
    val cardExtraLarge = RoundedCornerShape(20.dp)
    val hero         = RoundedCornerShape(22.dp)
    val screen       = RoundedCornerShape(24.dp)
}

internal val ScanMaterialShapes = Shapes(
    extraSmall = ScanShapes.sm,
    small      = ScanShapes.md,
    medium     = ScanShapes.card,
    large      = ScanShapes.hero,
    extraLarge = ScanShapes.screen,
)