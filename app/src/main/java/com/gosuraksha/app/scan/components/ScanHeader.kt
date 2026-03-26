package com.gosuraksha.app.scan.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.scan.design.ScanTheme

// ─── Screen Header ────────────────────────────────────────────────────────────
@Composable
fun ScanHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Shield,
    badge: String? = null,
) {
    val colors     = ScanTheme.colors
    val spacing    = ScanTheme.spacing
    val typography = ScanTheme.typography

    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(colors.blueTint, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = colors.primaryBlue,
                modifier           = Modifier.size(26.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (badge != null) {
                Text(
                    text  = badge.uppercase(),
                    style = typography.chipLabel,
                    color = colors.primaryBlue,
                )
                Spacer(Modifier.height(spacing.xs))
            }
            Text(text = title,    style = typography.sectionHeading, color = colors.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, style = typography.bodySmall,      color = colors.textSecondary)
        }
    }
}

// ─── HeroRiskCard ─────────────────────────────────────────────────────────────
// Risk result summary at the top of results — clean, well-padded, no pulse clutter
@Composable
fun HeroRiskCard(
    title: String,
    summary: String,
    score: Int,
    tone: ScanRiskTone,
    modifier: Modifier = Modifier,
    label: String = "Analysis Score",
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors     = ScanTheme.colors
    val spacing    = ScanTheme.spacing
    val typography = ScanTheme.typography
    val toneColor  = tone.contentColor(colors)
    val cardShape  = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = tone.containerColor(colors, emphasized = false),
                shape = cardShape,
            )
            .border(
                width  = 1.dp,
                color  = toneColor.copy(alpha = 0.2f),
                shape  = cardShape,
            )
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        // Risk badge row
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            // Solid status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(toneColor, CircleShape)
            )
            RiskBadge(label = tone.name.lowercase().replaceFirstChar { it.uppercase() }, tone = tone)
        }

        // Title
        Text(
            text  = title,
            style = typography.cardTitle,
            color = colors.textPrimary,
        )

        // Summary
        Text(
            text  = summary,
            style = typography.bodySmall,
            color = colors.textSecondary,
        )

        // Score bar
        SecurityScoreMeter(score = score, tone = tone, label = label)

        // Optional trailing content (e.g. confidence breakdown)
        trailing?.invoke()
    }
}