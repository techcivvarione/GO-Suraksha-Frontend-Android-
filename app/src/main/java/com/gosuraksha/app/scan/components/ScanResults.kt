package com.gosuraksha.app.scan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.scan.design.ScanTheme

private val CardShape   = RoundedCornerShape(20.dp)
private val ButtonShape = RoundedCornerShape(14.dp)

// ─── ScanResultCard ───────────────────────────────────────────────────────────
// Full results view: hero → evidence → recommendation → actions
// accentColor: overrides the primary action button color when provided
@Composable
fun ScanResultCard(
    title: String,
    summary: String,
    tone: ScanRiskTone,
    score: Int,
    evidence: List<String>,
    recommendation: String?,
    modifier: Modifier = Modifier,
    confidenceLabel: String? = null,
    accentColor: Color? = null,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    val colors     = ScanTheme.colors
    val typography = ScanTheme.typography
    val toneColor  = tone.contentColor(colors)
    // Primary button uses accentColor if given, else falls back to tone color
    val primaryBtnColor = accentColor ?: toneColor

    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── 0. Strong verdict banner ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(toneColor.copy(alpha = 0.13f), CardShape)
                .border(1.dp, toneColor.copy(alpha = 0.30f), CardShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text     = when (tone) {
                        ScanRiskTone.DANGER  -> "🚨"
                        ScanRiskTone.WARNING -> "⚠️"
                        ScanRiskTone.SAFE    -> "✅"
                    },
                    fontSize = 26.sp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text       = when (tone) {
                            ScanRiskTone.DANGER  -> "Unsafe — Do NOT trust this"
                            ScanRiskTone.WARNING -> "Be careful — This may not be safe"
                            ScanRiskTone.SAFE    -> "Safe — No major issues found"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = toneColor,
                    )
                    Text(
                        text     = when (tone) {
                            ScanRiskTone.DANGER  -> "High risk detected. Take immediate action."
                            ScanRiskTone.WARNING -> "Some indicators found. Review before trusting."
                            ScanRiskTone.SAFE    -> "No threats detected. Looks clean."
                        },
                        fontSize = 12.sp,
                        color    = colors.textSecondary,
                    )
                }
            }
        }

        // ── 1. Hero risk card ──────────────────────────────────────────────
        HeroRiskCard(
            title   = title,
            summary = summary,
            score   = score,
            tone    = tone,
            label   = confidenceLabel ?: "Analysis Score",
        )

        // ── 2. Evidence list ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = evidence.isNotEmpty(),
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, CardShape)
                    .border(1.dp, colors.border, CardShape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(
                    text  = "WHY THIS RESULT",
                    style = typography.chipLabel,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(12.dp))

                evidence.forEachIndexed { i, item ->
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .size(6.dp)
                                .background(toneColor.copy(alpha = 0.7f), CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text     = item,
                            style    = typography.bodySmall,
                            color    = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (i < evidence.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // ── 3. Recommendation card ─────────────────────────────────────────
        if (!recommendation.isNullOrBlank()) {
            val recBg     = toneColor.copy(alpha = 0.07f)
            val recBorder = toneColor.copy(alpha = 0.15f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(recBg, CardShape)
                    .border(1.dp, recBorder, CardShape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Info,
                        contentDescription = null,
                        tint               = toneColor,
                        modifier           = Modifier.size(14.dp),
                    )
                    Text(
                        text  = "WHAT TO DO",
                        style = typography.chipLabel,
                        color = toneColor,
                    )
                }
                Text(
                    text  = recommendation,
                    style = typography.bodySmall,
                    color = colors.textPrimary,
                )
            }
        }

        // ── 4. Action buttons ──────────────────────────────────────────────
        if (primaryActionLabel != null || secondaryActionLabel != null) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(ButtonShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border, ButtonShape)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick           = onSecondaryAction,
                            ),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Refresh,
                            contentDescription = null,
                            tint               = colors.textSecondary,
                            modifier           = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = secondaryActionLabel,
                            style = typography.chipLabel,
                            color = colors.textSecondary,
                        )
                    }
                }

                if (primaryActionLabel != null && onPrimaryAction != null) {
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .clip(ButtonShape)
                            .background(primaryBtnColor)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick           = onPrimaryAction,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text  = primaryActionLabel,
                            style = typography.chipLabel,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}