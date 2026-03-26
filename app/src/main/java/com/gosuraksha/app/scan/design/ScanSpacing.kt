package com.gosuraksha.app.scan.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ScanSpacing(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val base: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp,
    val xxxl: Dp,
)

val ScanSpacingTokens = ScanSpacing(
    xs   = 4.dp,
    sm   = 8.dp,
    md   = 12.dp,
    base = 14.dp,
    lg   = 16.dp,
    xl   = 20.dp,
    xxl  = 22.dp,
    xxxl = 28.dp,
)

val LocalScanSpacing = staticCompositionLocalOf { ScanSpacingTokens }