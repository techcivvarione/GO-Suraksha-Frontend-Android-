package com.gosuraksha.app.ui.home
import com.gosuraksha.app.design.tokens.ColorTokens

// =============================================================================
// SecuritySnapshotCard.kt — UI-ONLY composable
// FIXED: theme-aware surface color, border, elevation.
// Dark  → #1C2035 surface, no border, 8dp elevation
// Light → white surface, 1dp border #E2EAF8, 4dp elevation
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.gosuraksha.app.design.tokens.TypographyTokens

@Composable
fun SecuritySnapshotCard(
    scans: Int,
    threats: Int,
    risk: String
) {
    val isDark = ColorTokens.LocalAppDarkMode.current

    val riskNormalized = risk.lowercase().trim()
    val riskColor = when (riskNormalized) {
        "high"   -> Color(0xFFB91C1C)
        "medium" -> Color(0xFFD97706)
        else     -> Color(0xFF047857)
    }
    val riskLabel = when (riskNormalized) {
        "high"   -> "HIGH RISK"
        "medium" -> "MEDIUM RISK"
        else     -> "LOW RISK  ↗"
    }
    val riskSubtitle = when (riskNormalized) {
        "high"   -> "Immediate action needed"
        "medium" -> "Some issues detected"
        else     -> "All systems normal"
    }
    val riskProgress = when (riskNormalized) {
        "high"   -> 0.20f
        "medium" -> 0.52f
        else     -> 0.85f
    }

    // Theme-resolved tokens
    val surfaceColor = if (isDark) Color(0xFF1C2035) else Color(0xFFFFFFFF)
    val border = if (isDark) null else BorderStroke(1.dp, Color(0xFFE2EAF8))
    val elevation = if (isDark) 8.dp else 4.dp
    val labelAlpha = if (isDark) 0.50f else 0.45f
    val dividerAlpha = if (isDark) 0.08f else 0.10f
    val accentBar = Color(0xFF3B6FD4)
    val onSurface = if (isDark) Color(0xFFE6E9F4) else Color(0xFF1A1F36)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = surfaceColor,
        border = border,
        shadowElevation = elevation,
        tonalElevation = 0.dp
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {

            // Left accent bar — 3dp, accent color
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentBar,
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section label
                Text(
                    text = "YOUR SECURITY STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSurface.copy(alpha = labelAlpha),
                    letterSpacing = 1.sp
                )

                // Progress + risk label block
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(onSurface.copy(alpha = 0.10f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(riskProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(riskColor)
                        )
                    }
                    Text(
                        text = riskLabel,
                        style = TypographyTokens.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                    Text(
                        text = riskSubtitle,
                        style = TypographyTokens.bodySmall,
                        color = onSurface.copy(alpha = labelAlpha)
                    )
                }

                // Tonal divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(onSurface.copy(alpha = dividerAlpha))
                )

                // Metric row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SnapshotMetric(value = scans.toString(),
                        label = "Scans Done", onSurface = onSurface, labelAlpha = labelAlpha)
                    SnapshotMetric(value = threats.toString(),
                        label = "Threats Found", onSurface = onSurface, labelAlpha = labelAlpha)
                    // Risk pill
                    Box(
                        modifier = Modifier
                            .background(riskColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = riskNormalized.uppercase(),
                            style = TypographyTokens.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = riskColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SnapshotMetric(
    value: String,
    label: String,
    onSurface: Color,
    labelAlpha: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            style = TypographyTokens.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = onSurface
        )
        Text(
            text = label,
            style = TypographyTokens.labelSmall,
            color = onSurface.copy(alpha = labelAlpha)
        )
    }
}