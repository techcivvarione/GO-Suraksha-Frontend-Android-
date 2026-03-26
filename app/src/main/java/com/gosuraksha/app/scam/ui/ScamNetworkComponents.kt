package com.gosuraksha.app.scam.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.design.components.AppButton
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ElevationTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens
import com.gosuraksha.app.scam.model.CheckNumberResponse
import com.gosuraksha.app.scam.model.ScamActivityPoint
import com.gosuraksha.app.scam.model.ScamAlertCampaign
import com.gosuraksha.app.scam.model.ScamCategory

@Composable
fun RegionalThreatBanner(title: String, subtitle: String) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = null,
        contentPadding = PaddingValues(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFF2E2), Color(0xFFFFE2E0), Color(0xFFFFF8E8))),
                    RoundedCornerShape(24.dp)
                )
                .border(1.dp, Color(0xFFF3C98B), RoundedCornerShape(24.dp))
                .padding(SpacingTokens.cardPaddingLarge)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                Text("Regional Threat Banner", style = TypographyTokens.labelLarge, color = Color(0xFF7E1E10))
                Text(title, style = TypographyTokens.headlineSmall, color = Color(0xFF25120B))
                Text(subtitle, style = TypographyTokens.bodyMedium, color = Color(0xFF5F4638))
            }
        }
    }
}

data class QuickActionUi(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionsRow(actions: List<QuickActionUi>) {
    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)
    ) {
        actions.forEach { action ->
            QuickActionCard(
                title = action.title,
                icon = action.icon,
                accent = action.accent,
                modifier = Modifier.fillMaxWidth(0.48f),
                onClick = action.onClick
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AppCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = ColorTokens.surface()),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(Modifier.width(SpacingTokens.sm))
            Text(title, style = TypographyTokens.titleSmall, color = ColorTokens.textPrimary())
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScamCategoryChips(selectedCategories: List<ScamCategory>, onToggle: (ScamCategory) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs), verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
        ScamCategory.values().forEach { category ->
            FilterChip(selected = category in selectedCategories, onClick = { onToggle(category) }, label = { Text(category.label) })
        }
    }
}

@Composable
fun PhoneCheckResultCard(result: CheckNumberResponse) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorTokens.surface()),
        border = BorderStroke(1.dp, if (result.suspicious) ColorTokens.error().copy(alpha = 0.2f) else ColorTokens.success().copy(alpha = 0.2f))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
            Text("Reported ${result.reportCount} times as suspicious", style = TypographyTokens.titleMedium, color = ColorTokens.textPrimary())
            Text("Last reported ${result.lastReportedLabel ?: "recently"}", style = TypographyTokens.bodyMedium, color = ColorTokens.textSecondary())
            Text("Category: ${result.category ?: "Unknown"}", style = TypographyTokens.bodyMedium, color = ColorTokens.textSecondary())
        }
    }
}

@Composable
fun ScamAlertCard(campaign: ScamAlertCampaign, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = ColorTokens.surface()),
        border = BorderStroke(1.dp, ColorTokens.border()),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.xs)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
            Text(campaign.scamType, style = TypographyTokens.titleMedium, color = ColorTokens.textPrimary())
            Text("${campaign.reportCount} reports", style = TypographyTokens.labelMedium, color = ColorTokens.error())
            Text("Regions affected: ${campaign.regionsAffected.joinToString()}", style = TypographyTokens.bodySmall, color = ColorTokens.textSecondary())
            Text(campaign.explanation, style = TypographyTokens.bodyMedium, color = ColorTokens.textPrimary(), maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text("Prevention: ${campaign.preventionTips.joinToString(limit = 2)}", style = TypographyTokens.bodySmall, color = ColorTokens.textSecondary())
        }
    }
}

@Composable
fun ScamAlertNetworkHeroCard(
    onOpenScamNetwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Color(0xFF0F3D2E), Color(0xFF195F4A), Color(0xFF2E7D5B))),
                    RoundedCornerShape(24.dp)
                )
                .padding(SpacingTokens.cardPaddingLarge)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                Text("Scam Alert Network", style = TypographyTokens.headlineSmall, color = Color.White)
                Text("Track live scam activity near you, report incidents fast, and verify suspicious numbers before you act.", style = TypographyTokens.bodyMedium, color = Color.White.copy(alpha = 0.88f))
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                    HeroBullet("live scam activity near user")
                    HeroBullet("quick actions")
                }
                AppButton(onClick = onOpenScamNetwork) {
                    Text("Open Scam Alert Hub")
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs)) {
        Text(title, style = TypographyTokens.screenTitle, color = ColorTokens.textPrimary())
        Text(subtitle, style = TypographyTokens.bodyMedium, color = ColorTokens.textSecondary())
    }
}

@Composable
fun CenterLoadingCard() {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = SpacingTokens.xl), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ColorTokens.accent())
    }
}

@Composable
fun InlineError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.errorLight(), RoundedCornerShape(16.dp))
            .border(1.dp, ColorTokens.error().copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(SpacingTokens.sm)
    ) {
        Text(message, style = TypographyTokens.bodySmall, color = ColorTokens.error())
    }
}

@Composable
fun InlineSuccess(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.successLight(), RoundedCornerShape(16.dp))
            .border(1.dp, ColorTokens.success().copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(SpacingTokens.sm)
    ) {
        Text(message, style = TypographyTokens.bodySmall, color = ColorTokens.success())
    }
}

@Composable
fun TimelineRow(point: ScamActivityPoint) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.surface(), RoundedCornerShape(16.dp))
            .border(1.dp, ColorTokens.border(), RoundedCornerShape(16.dp))
            .padding(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(ColorTokens.warning(), CircleShape))
        Spacer(Modifier.width(SpacingTokens.sm))
        Text(point.label, modifier = Modifier.weight(1f), style = TypographyTokens.bodyMedium, color = ColorTokens.textPrimary())
        Text(point.value.toString(), style = TypographyTokens.titleSmall, color = ColorTokens.textPrimary())
    }
}

@Composable
fun ChecklistRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorTokens.surface(), RoundedCornerShape(16.dp))
            .border(1.dp, ColorTokens.border(), RoundedCornerShape(16.dp))
            .padding(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(ColorTokens.accent(), CircleShape))
        Spacer(Modifier.width(SpacingTokens.sm))
        Text(text, style = TypographyTokens.bodyMedium, color = ColorTokens.textPrimary())
    }
}

@Composable
private fun HeroBullet(text: String) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFD166), CircleShape))
        Spacer(Modifier.width(SpacingTokens.xs))
        Text(text, color = Color.White, style = TypographyTokens.labelSmall)
    }
}

@Composable
private fun LegendPill(label: String, color: Color) {
    Row(
        modifier = Modifier
            .background(ColorTokens.surface(), RoundedCornerShape(999.dp))
            .border(1.dp, ColorTokens.border(), RoundedCornerShape(999.dp))
            .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(SpacingTokens.xs))
        Text(label, style = TypographyTokens.labelMedium, color = ColorTokens.textSecondary())
    }
}

@Preview(showBackground = true)
@Composable
private fun ScamHeroPreview() {
    ScamAlertNetworkHeroCard(onOpenScamNetwork = {})
}


