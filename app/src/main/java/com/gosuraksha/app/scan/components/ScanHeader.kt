package com.gosuraksha.app.scan.components

// =============================================================================
// ScanHeader.kt — Screen header + hero risk summary card
//
// ScanHeader   — Icon box + title + subtitle with optional badge pill
// HeroRiskCard — Premium risk summary: verdict icon circle, title, score bar
// =============================================================================

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        // Icon box with subtle shadow
        Box(
            modifier = Modifier
                .shadow(
                    elevation    = 4.dp,
                    shape        = RoundedCornerShape(16.dp),
                    ambientColor = colors.primaryBlue.copy(alpha = 0.12f),
                    spotColor    = colors.primaryBlue.copy(alpha = 0.18f),
                )
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
                // Badge pill
                Box(
                    modifier = Modifier
                        .background(colors.blueMid, RoundedCornerShape(20.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text(
                        text  = badge.uppercase(),
                        style = typography.chipLabel,
                        color = colors.primaryBlue,
                    )
                }
                Spacer(Modifier.height(spacing.xs))
            }
            Text(
                text  = title,
                style = typography.sectionHeading,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = subtitle,
                style = typography.bodySmall,
                color = colors.textSecondary,
            )
        }
    }
}

// ─── HeroRiskCard ─────────────────────────────────────────────────────────────
// Premium result summary card — verdict icon circle + title + score bar
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

    // Verdict emoji
    val verdictEmoji = when (tone) {
        ScanRiskTone.DANGER  -> "🚨"
        ScanRiskTone.WARNING -> "⚠️"
        ScanRiskTone.SAFE    -> "✅"
    }
    val verdictLabel = when (tone) {
        ScanRiskTone.DANGER  -> "HIGH RISK"
        ScanRiskTone.WARNING -> "MODERATE RISK"
        ScanRiskTone.SAFE    -> "ALL CLEAR"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 2.dp,
                shape        = cardShape,
                ambientColor = toneColor.copy(alpha = 0.10f),
                spotColor    = toneColor.copy(alpha = 0.10f),
            )
            .clip(cardShape)
            .background(
                color = tone.containerColor(colors, emphasized = false),
                shape = cardShape,
            )
            .border(
                width = 1.dp,
                color = toneColor.copy(alpha = 0.22f),
                shape = cardShape,
            )
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        // ── Verdict row: icon circle + badge ──────────────────────────────
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Large colored circle with verdict emoji
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(toneColor.copy(alpha = 0.18f), CircleShape)
                    .border(1.dp, toneColor.copy(alpha = 0.30f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(verdictEmoji, fontSize = 20.sp)
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                // Compact verdict label badge
                Box(
                    modifier = Modifier
                        .background(toneColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, toneColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text       = verdictLabel,
                        style      = typography.chipLabel,
                        color      = toneColor,
                    )
                }
            }
        }

        // ── Title ─────────────────────────────────────────────────────────
        Text(
            text       = title,
            fontWeight = FontWeight.Bold,
            fontSize   = 16.sp,
            color      = colors.textPrimary,
        )

        // ── Summary ───────────────────────────────────────────────────────
        Text(
            text  = summary,
            style = typography.bodySmall,
            color = colors.textSecondary,
        )

        // ── Subtle divider ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(toneColor.copy(alpha = 0.12f)),
        )

        // ── Score bar ─────────────────────────────────────────────────────
        SecurityScoreMeter(score = score, tone = tone, label = label)

        // ── Optional trailing content ─────────────────────────────────────
        trailing?.invoke()
    }
}
