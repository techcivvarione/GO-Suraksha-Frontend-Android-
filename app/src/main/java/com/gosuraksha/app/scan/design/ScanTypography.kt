package com.gosuraksha.app.scan.design

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R

val ManropeFamily = FontFamily(
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

@Immutable
data class ScanTypography(
    val displayTitle: TextStyle,
    val sectionHeading: TextStyle,
    val cardTitle: TextStyle,
    val buttonText: TextStyle,
    val bodyText: TextStyle,
    val bodySmall: TextStyle,
    val chipLabel: TextStyle,
    val monoText: TextStyle,
)

val ScanType = ScanTypography(
    displayTitle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    sectionHeading = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    cardTitle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    buttonText = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyText = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    chipLabel = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    ),
    monoText = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
)

internal val ScanMaterialTypography = Typography(
    displaySmall  = ScanType.displayTitle,
    headlineSmall = ScanType.sectionHeading,
    titleLarge    = ScanType.cardTitle,
    labelLarge    = ScanType.buttonText,
    bodyLarge     = ScanType.bodyText,
    bodySmall     = ScanType.bodySmall,
    labelSmall    = ScanType.chipLabel,
)

val LocalScanTypography = staticCompositionLocalOf { ScanType }