package com.gosuraksha.app.scan.components

// =============================================================================
// ScanFeedback.kt — Loading, badge, score-meter components
//
// RiskBadge          — Pill label colored by ScanRiskTone
// SecurityScoreMeter — Animated progress bar with score label
// ScanLoader         — Scanning indicator with animated dot trio
// ScanErrorBanner    — Danger-colored error row
// =============================================================================

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.scan.design.ScanTheme

// ─── Risk Badge ───────────────────────────────────────────────────────────────
@Composable
fun RiskBadge(
    label: String,
    tone: ScanRiskTone,
    modifier: Modifier = Modifier,
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography

    Row(
        modifier = modifier
            .background(tone.containerColor(colors), RoundedCornerShape(20.dp))
            .border(1.dp, tone.contentColor(colors).copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // Status indicator dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(tone.contentColor(colors), CircleShape),
        )
        Text(
            text  = label,
            style = typography.chipLabel,
            color = tone.contentColor(colors),
        )
    }
}

// ─── Security Score Meter ─────────────────────────────────────────────────────
@Composable
fun SecurityScoreMeter(
    score: Int,
    tone: ScanRiskTone,
    modifier: Modifier = Modifier,
    label: String = "Analysis Score",
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography
    val toneColor  = tone.contentColor(colors)
    val progress by animateFloatAsState(
        targetValue   = score.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label         = "score_progress",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = label,
                style = typography.bodySmall,
                color = colors.textSecondary,
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text       = "$score",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = toneColor,
                )
                Text(
                    text     = "/ 100",
                    fontSize = 12.sp,
                    color    = colors.textTertiary,
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            // Track
            drawRoundRect(
                color        = colors.border,
                cornerRadius = CornerRadius(32f, 32f),
                size         = size,
            )
            // Fill
            if (progress > 0f) {
                drawRoundRect(
                    brush        = Brush.horizontalGradient(
                        listOf(colors.primaryBlue.copy(alpha = 0.8f), toneColor)
                    ),
                    cornerRadius = CornerRadius(32f, 32f),
                    size         = Size(size.width * progress, size.height),
                )
            }
        }
    }
}

// ─── Scan Loader ─────────────────────────────────────────────────────────────
// Spinner + label + animated dot trio to indicate active scanning
@Composable
fun ScanLoader(
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography

    // Animated bouncing dots
    val transition = rememberInfiniteTransition(label = "loader_dots")
    val waveOffset by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 3f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dot_wave",
    )

    fun dotAlpha(index: Int): Float {
        val shifted = (waveOffset - index).mod(3f)
        return if (shifted < 1f) 0.3f + shifted * 0.7f
        else if (shifted < 2f) 0.3f + (2f - shifted) * 0.7f
        else 0.3f
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(18.dp))
            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CircularProgressIndicator(
            color       = colors.primaryBlue,
            strokeWidth = 2.dp,
            strokeCap   = StrokeCap.Round,
            modifier    = Modifier.size(20.dp),
        )

        Text(
            text     = label,
            style    = typography.bodySmall,
            color    = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )

        // Animated dot trio
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(
                            color = colors.primaryBlue.copy(alpha = dotAlpha(index)),
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

// ─── Error Banner ─────────────────────────────────────────────────────────────
@Composable
fun ScanErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.dangerRed.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .border(1.dp, colors.dangerRed.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(colors.dangerRed, CircleShape),
        )
        Text(
            text     = message,
            style    = typography.bodySmall,
            color    = colors.dangerRed,
            modifier = Modifier.weight(1f),
        )
    }
}
