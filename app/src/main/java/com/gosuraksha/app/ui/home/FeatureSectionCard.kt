package com.gosuraksha.app.ui.home

// =============================================================================
// FeatureSectionCard.kt — Go Suraksha
// Updated: DashboardColors → GS tokens (matches new HomeScreen.kt)
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// Data models (unchanged — same as before)
// ─────────────────────────────────────────────────────────────────────────────
data class HomeFeatureAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

data class HomeFeatureSection(
    val title: String,
    val items: List<HomeFeatureAction>
)

// ─────────────────────────────────────────────────────────────────────────────
// FeatureSectionCard
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FeatureSectionCard(
    section: HomeFeatureSection,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) GS.DarkSurface else GS.LightSurface)
            .padding(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text       = section.title,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            color      = GS.onSurf(isDark),
            modifier   = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(section.items) { item ->
                FeatureActionCard(action = item, isDark = isDark)
            }
        }
    }
}

@Composable
private fun FeatureActionCard(
    action: HomeFeatureAction,
    isDark: Boolean
) {
    // Icon background: use green-tinted surface matching the brand system
    val iconBg = if (isDark) GS.DarkIconGreen else GS.LightIconGreen

    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color(0xFF0A150B) else Color(0xFFF7FBF7))
            .clickable(onClick = action.onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = action.icon,
                contentDescription = action.title,
                tint               = action.accentColor,
                modifier           = Modifier.size(22.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text       = action.title,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = GS.onSurf(isDark),
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                text     = action.subtitle,
                fontSize = 10.sp,
                color    = GS.mutedText(isDark),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}