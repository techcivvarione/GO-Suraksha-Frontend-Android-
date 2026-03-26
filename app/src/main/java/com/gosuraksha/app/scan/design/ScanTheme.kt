package com.gosuraksha.app.scan.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.gosuraksha.app.design.tokens.ColorTokens

// ─── ScanTheme accessor ───────────────────────────────────────────────────────
object ScanTheme {
    val colors: ScanColors
        @Composable @ReadOnlyComposable get() = LocalScanColors.current

    val typography: ScanTypography
        @Composable @ReadOnlyComposable get() = LocalScanTypography.current

    val spacing: ScanSpacing
        @Composable @ReadOnlyComposable get() = LocalScanSpacing.current
}

// ─── GoSurakshaScanTheme ──────────────────────────────────────────────────────
// Default reads ColorTokens.LocalAppDarkMode — the same bool your toggle writes.
// All scan screens automatically follow the app-wide dark/light toggle with zero
// extra wiring. Pass darkTheme = true to force dark (e.g. QR camera stage).
@Composable
fun GoSurakshaScanTheme(
    darkTheme: Boolean = ColorTokens.LocalAppDarkMode.current,
    content: @Composable () -> Unit,
) {
    val colors         = if (darkTheme) DarkPalette else LightPalette
    val materialColors = if (darkTheme) ScanDarkColorScheme else ScanLightColorScheme

    CompositionLocalProvider(
        LocalScanColors     provides colors,
        LocalScanTypography provides ScanType,
        LocalScanSpacing    provides ScanSpacingTokens,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography  = ScanMaterialTypography,
            shapes      = ScanMaterialShapes,
            content     = content,
        )
    }
}
