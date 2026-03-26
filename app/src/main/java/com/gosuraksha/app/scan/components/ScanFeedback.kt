package com.gosuraksha.app.scan.components

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
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.scan.design.ScanShapes
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

    Box(
        modifier = modifier
            .background(tone.containerColor(colors), RoundedCornerShape(20.dp))
            .border(1.dp, tone.contentColor(colors).copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
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
    val progress by animateFloatAsState(
        targetValue   = score.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label         = "score_progress",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(label, style = typography.bodySmall, color = colors.textSecondary)
            Text(
                text  = "$score / 100",
                style = typography.chipLabel,
                color = tone.contentColor(colors),
            )
        }
        Spacer(Modifier.height(8.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
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
                        listOf(colors.primaryBlue, tone.contentColor(colors))
                    ),
                    cornerRadius = CornerRadius(32f, 32f),
                    size         = Size(size.width * progress, size.height),
                )
            }
        }
    }
}

// ─── Scan Loader — simple, not clumsy ────────────────────────────────────────
// Single spinner + label. No canvas scan-line box.
@Composable
fun ScanLoader(
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
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
            text  = label,
            style = typography.bodySmall,
            color = colors.textSecondary,
        )
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
            .background(colors.dangerRed.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
            .border(1.dp, colors.dangerRed.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(colors.dangerRed, CircleShape)
        )
        Text(
            text     = message,
            style    = typography.bodySmall,
            color    = colors.dangerRed,
            modifier = Modifier.weight(1f),
        )
    }
}