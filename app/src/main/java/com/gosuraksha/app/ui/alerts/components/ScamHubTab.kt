package com.gosuraksha.app.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.design.components.AppButton
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ElevationTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.scam.ScamNetworkUiState
import com.gosuraksha.app.scam.model.ScamAlertCampaign

@Composable
fun ScamHubTab(
    uiState:           ScamNetworkUiState,
    onOpenScamNetwork: () -> Unit,
    onOpenScamDetail:  (String) -> Unit,
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = SpacingTokens.screenPaddingHorizontal,
            vertical   = SpacingTokens.md,
        ),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {

        // ── Cybersecurity dashboard header ───────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeTokens.card)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ColorTokens.accent().copy(alpha = 0.18f),
                                ColorTokens.accent().copy(alpha = 0.05f),
                            )
                        )
                    )
                    .border(1.dp, ColorTokens.accent().copy(alpha = 0.25f), ShapeTokens.card)
                    .padding(SpacingTokens.md),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ColorTokens.accent().copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Shield,
                            contentDescription = null,
                            tint     = ColorTokens.accent(),
                            modifier = Modifier.size(SpacingTokens.iconSize),
                        )
                    }
                    Spacer(Modifier.width(SpacingTokens.sm))
                    Column {
                        Text(
                            "Scam Network",
                            color = ColorTokens.textPrimary(),
                            style = TypographyTokens.labelMedium,
                        )
                        Text(
                            "Community-reported fraud alerts near you",
                            color = ColorTokens.textSecondary(),
                            style = TypographyTokens.bodySmall,
                        )
                    }
                }
            }
        }

        // ── Action row ───────────────────────────────────────────────────
        item {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                ActionChip(
                    label    = "Report Scam",
                    icon     = Icons.Outlined.Campaign,
                    color    = ColorTokens.error(),
                    modifier = Modifier.weight(1f),
                    onClick  = onOpenScamNetwork,
                )
                ActionChip(
                    label    = "Check Number",
                    icon     = Icons.Outlined.Warning,
                    color    = ColorTokens.warning(),
                    modifier = Modifier.weight(1f),
                    onClick  = onOpenScamNetwork,
                )
            }
        }

        // ── Trending scams section ───────────────────────────────────────
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint     = ColorTokens.error(),
                    modifier = Modifier.size(SpacingTokens.iconSizeSmall),
                )
                Spacer(Modifier.width(SpacingTokens.xs))
                Text(
                    "Trending Scams",
                    color = ColorTokens.textSecondary(),
                    style = TypographyTokens.labelSmall,
                )
                if (uiState.trendingScams.isNotEmpty()) {
                    Spacer(Modifier.width(SpacingTokens.xs))
                    Box(
                        modifier = Modifier
                            .clip(ShapeTokens.badge)
                            .background(ColorTokens.error().copy(alpha = 0.1f))
                            .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs),
                    ) {
                        Text(
                            "${uiState.trendingScams.size}",
                            color = ColorTokens.error(),
                            style = TypographyTokens.labelSmall,
                        )
                    }
                }
            }
        }

        when {
            uiState.loadingTrending -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color       = ColorTokens.accent(),
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(28.dp),
                    )
                }
            }

            uiState.trendingScams.isEmpty() -> item {
                ScamEmptyState()
            }

            else -> items(uiState.trendingScams) { campaign ->
                TrendingScamCard(
                    campaign = campaign,
                    onClick  = { onOpenScamDetail(campaign.id) },
                )
            }
        }

        // ── Community report CTA ─────────────────────────────────────────
        item {
            Spacer(Modifier.height(SpacingTokens.xs))
            AppCard(
                modifier  = Modifier.fillMaxWidth(),
                colors    = neutralCardColors(),
                border    = neutralCardBorder(),
                shape     = neutralCardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs),
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    Icon(
                        Icons.Outlined.Group,
                        contentDescription = null,
                        tint     = ColorTokens.accent(),
                        modifier = Modifier.size(SpacingTokens.iconSize),
                    )
                    Text(
                        "Help protect your community",
                        color = ColorTokens.textPrimary(),
                        style = TypographyTokens.labelMedium,
                    )
                    Text(
                        "Seen a scam? Report it so others can stay safe.",
                        color = ColorTokens.textSecondary(),
                        style = TypographyTokens.bodySmall,
                    )
                    AppButton(
                        onClick   = onOpenScamNetwork,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .height(SpacingTokens.authButtonHeight),
                    ) {
                        Text("Report a Scam", style = TypographyTokens.buttonText)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(SpacingTokens.xxxl)) }
    }
}

// ── Trending scam campaign card ───────────────────────────────────────────────

@Composable
private fun TrendingScamCard(
    campaign: ScamAlertCampaign,
    onClick:  () -> Unit,
) {
    val category = campaign.category?.uppercase() ?: campaign.scamType.uppercase()

    // Colour code by scam category
    val accentColor = when {
        category.contains("PHISHING")  -> ColorTokens.error()
        category.contains("PAYMENT")   -> ColorTokens.warning()
        category.contains("CALL")      -> ColorTokens.accent()
        category.contains("SMS")       -> Color(0xFF9C27B0)
        else                           -> ColorTokens.error()
    }
    val emoji = when {
        category.contains("PHISHING") -> "🎣"
        category.contains("PAYMENT")  -> "💸"
        category.contains("CALL")     -> "📞"
        category.contains("SMS")      -> "💬"
        else                          -> "⚠️"
    }

    AppCard(
        modifier  = Modifier.fillMaxWidth(),
        colors    = neutralCardColors(),
        border    = neutralCardBorder(),
        shape     = neutralCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(remember { MutableInteractionSource() }, null, onClick = onClick)
                .padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Text(emoji, style = TypographyTokens.labelMedium)
                    Text(
                        campaign.scamType,
                        color    = ColorTokens.textPrimary(),
                        style    = TypographyTokens.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(ShapeTokens.badge)
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxs),
                ) {
                    Text(
                        "${campaign.reportCount} reports",
                        color = accentColor,
                        style = TypographyTokens.labelSmall,
                    )
                }
            }

            Text(
                campaign.explanation,
                color    = ColorTokens.textSecondary(),
                style    = TypographyTokens.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (campaign.regionsAffected.isNotEmpty()) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint     = accentColor,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        "Active in: ${campaign.regionsAffected.take(3).joinToString(", ")}",
                        color    = accentColor,
                        style    = TypographyTokens.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (campaign.preventionTips.isNotEmpty()) {
                Text(
                    "💡 ${campaign.preventionTips.first()}",
                    color    = ColorTokens.textSecondary(),
                    style    = TypographyTokens.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Action chip ───────────────────────────────────────────────────────────────

@Composable
private fun ActionChip(
    label:    String,
    icon:     androidx.compose.ui.graphics.vector.ImageVector,
    color:    androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick:  () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(ShapeTokens.cardCompact)
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), ShapeTokens.cardCompact)
            .clickable(remember { MutableInteractionSource() }, null, onClick = onClick)
            .padding(SpacingTokens.sm),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(SpacingTokens.iconSizeSmall))
            Spacer(Modifier.width(SpacingTokens.xs))
            Text(label, color = color, style = TypographyTokens.labelSmall)
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun ScamEmptyState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(SpacingTokens.authLogoMedium)
                    .clip(CircleShape)
                    .background(ColorTokens.surfaceVariant())
                    .border(ShapeTokens.Border.thin, ColorTokens.border(), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Shield,
                    null,
                    tint     = ColorTokens.textSecondary(),
                    modifier = Modifier.size(SpacingTokens.iconSizeLarge),
                )
            }
            Spacer(Modifier.height(SpacingTokens.sm))
            Text(
                "No trending scams right now",
                color = ColorTokens.textSecondary(),
                style = TypographyTokens.bodySmall,
            )
            Text(
                "Your community is safe",
                color = ColorTokens.success(),
                style = TypographyTokens.labelSmall,
            )
        }
    }
}
