package com.gosuraksha.app.scan.components

// =============================================================================
// ScanResults.kt — Full result card: hero → evidence → recommendation → actions
//
// ScanResultCard — Composed result view (no separate verdict banner — HeroRiskCard
//                  now carries the verdict visually)
//
// Design: Spring-press action buttons, left-accent recommendation card,
//         numbered evidence items, smooth AnimatedVisibility transitions.
// =============================================================================

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.scan.design.ScanTheme

private val CardShape   = RoundedCornerShape(20.dp)
private val ButtonShape = RoundedCornerShape(16.dp)

@Composable
fun RiskCard(
    riskLevel: String,
    riskScore: Int,
    tone: ScanRiskTone,
    modifier: Modifier = Modifier,
) {
    HeroRiskCard(
        title = riskLevel,
        summary = when (tone) {
            ScanRiskTone.DANGER -> "High risk found. Act now."
            ScanRiskTone.WARNING -> "Something feels wrong. Check before acting."
            ScanRiskTone.SAFE -> "No major warning found."
        },
        score = riskScore,
        tone = tone,
        label = "Risk Score",
        modifier = modifier
    )
}

@Composable
fun ExplainSimplyCard(
    explanation: String,
    tone: ScanRiskTone,
    modifier: Modifier = Modifier,
) {
    val colors = ScanTheme.colors
    val typography = ScanTheme.typography
    val toneColor = tone.contentColor(colors)
    val parts = explanation.split("\n\n").map { it.trim() }.filter { it.isNotBlank() }
    val verdict = parts.getOrElse(0) { "Review this carefully." }
    val why = parts.getOrElse(1) { "Something does not look right." }
    val action = parts.getOrElse(2) { "Pause before taking action." }
    val verdictIcon = when (tone) {
        ScanRiskTone.DANGER -> Icons.Outlined.ReportProblem
        ScanRiskTone.WARNING -> Icons.Outlined.Warning
        ScanRiskTone.SAFE -> Icons.Outlined.CheckCircle
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, CardShape, ambientColor = toneColor.copy(alpha = 0.12f), spotColor = toneColor.copy(alpha = 0.12f))
            .clip(CardShape)
            .background(colors.surface, CardShape)
            .border(1.dp, toneColor.copy(alpha = 0.20f), CardShape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = verdictIcon,
                contentDescription = null,
                tint = toneColor,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "EXPLAIN SIMPLY",
                style = typography.chipLabel,
                color = toneColor,
            )
        }
        Text(
            text = verdict,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        Text(
            text = why,
            style = typography.bodySmall,
            color = colors.textPrimary,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(toneColor.copy(alpha = 0.10f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = action,
                style = typography.bodySmall,
                color = toneColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun ThreatResultActions(
    tone: ScanRiskTone,
    onWhatToDo: () -> Unit,
    onPlayVoiceAlert: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ScanTheme.colors
    val toneColor = tone.contentColor(colors)
    val typography = ScanTheme.typography

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(ButtonShape)
                .background(toneColor)
                .clickable(onClick = onWhatToDo),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(text = "What to do", style = typography.chipLabel, color = Color.White)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(ButtonShape)
                .background(colors.surface)
                .border(1.dp, colors.border, ButtonShape)
                .clickable(onClick = onPlayVoiceAlert),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Outlined.RecordVoiceOver, contentDescription = null, tint = toneColor, modifier = Modifier.size(16.dp))
                Text(text = "Play voice alert", style = typography.chipLabel, color = colors.textPrimary)
            }
        }
    }
}

@Composable
fun ExpandableDetailsCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    details: List<String>,
    recommendation: String?,
    modifier: Modifier = Modifier,
) {
    val colors = ScanTheme.colors
    val typography = ScanTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.9f))
            .background(colors.surface, CardShape)
            .border(1.dp, colors.border, CardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.Campaign, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                Text(
                    text = "See detailed analysis",
                    style = typography.chipLabel,
                    color = colors.textPrimary,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (recommendation != null) {
                    Text(
                        text = recommendation,
                        style = typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
                details.forEachIndexed { index, detail ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(colors.blueTint, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryBlue,
                            )
                        }
                        Text(
                            text = detail,
                            style = typography.bodySmall,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

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
    val colors          = ScanTheme.colors
    val typography      = ScanTheme.typography
    val toneColor       = tone.contentColor(colors)
    val primaryBtnColor = accentColor ?: toneColor

    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── 1. Hero risk card — carries the verdict ────────────────────────
        HeroRiskCard(
            title  = title,
            summary = summary,
            score  = score,
            tone   = tone,
            label  = confidenceLabel ?: "Analysis Score",
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
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(colors.textTertiary, CircleShape),
                    )
                    Text(
                        text  = "WHY THIS RESULT",
                        style = typography.chipLabel,
                        color = colors.textTertiary,
                    )
                }
                Spacer(Modifier.height(12.dp))

                evidence.forEachIndexed { i, item ->
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Numbered circle
                        Box(
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .size(20.dp)
                                .background(toneColor.copy(alpha = 0.12f), CircleShape)
                                .border(1.dp, toneColor.copy(alpha = 0.20f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = "${i + 1}",
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color      = toneColor,
                            )
                        }
                        Text(
                            text     = item,
                            style    = typography.bodySmall,
                            color    = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (i < evidence.lastIndex) {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.border),
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        // ── 3. Recommendation — left accent border ─────────────────────────
        if (!recommendation.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(toneColor.copy(alpha = 0.06f), CardShape)
                    .border(1.dp, toneColor.copy(alpha = 0.14f), CardShape),
            ) {
                // Left accent bar — fills Row height via IntrinsicSize.Min
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                        .background(toneColor.copy(alpha = 0.60f)),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Info,
                            contentDescription = null,
                            tint               = toneColor,
                            modifier           = Modifier.size(13.dp),
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
        }

        // ── 4. Action buttons with spring press ────────────────────────────
        if (primaryActionLabel != null || secondaryActionLabel != null) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    val sec2Src  = remember { MutableInteractionSource() }
                    val sec2Pressed by sec2Src.collectIsPressedAsState()
                    val sec2Scale by animateFloatAsState(
                        targetValue   = if (sec2Pressed) 0.96f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.7f),
                        label         = "sec_btn_scale",
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .graphicsLayer { scaleX = sec2Scale; scaleY = sec2Scale }
                            .clip(ButtonShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border, ButtonShape)
                            .clickable(
                                indication        = null,
                                interactionSource = sec2Src,
                                onClick           = onSecondaryAction,
                            ),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Refresh,
                            contentDescription = null,
                            tint               = colors.textSecondary,
                            modifier           = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text  = secondaryActionLabel,
                            style = typography.chipLabel,
                            color = colors.textSecondary,
                        )
                    }
                }

                if (primaryActionLabel != null && onPrimaryAction != null) {
                    val primSrc  = remember { MutableInteractionSource() }
                    val primPressed by primSrc.collectIsPressedAsState()
                    val primScale by animateFloatAsState(
                        targetValue   = if (primPressed) 0.96f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.7f),
                        label         = "prim_btn_scale",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .graphicsLayer { scaleX = primScale; scaleY = primScale }
                            .shadow(
                                elevation    = if (!primPressed) 4.dp else 0.dp,
                                shape        = ButtonShape,
                                ambientColor = primaryBtnColor.copy(alpha = 0.20f),
                                spotColor    = primaryBtnColor.copy(alpha = 0.25f),
                            )
                            .clip(ButtonShape)
                            .background(primaryBtnColor)
                            .clickable(
                                indication        = null,
                                interactionSource = primSrc,
                                onClick           = onPrimaryAction,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = primaryActionLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 13.sp,
                            color      = Color.White,
                        )
                    }
                }
            }
        }
    }
}
