package com.gosuraksha.app.ui.home

// =============================================================================
// QuickActionItem.kt — Go Suraksha
// Updated: DashboardColors → GS tokens (matches new HomeScreen.kt)
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// Data model (unchanged)
// ─────────────────────────────────────────────────────────────────────────────
data class HomeQuickAction(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

// ─────────────────────────────────────────────────────────────────────────────
// QuickActionItem — circular icon tile with label
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuickActionItem(
    action: HomeQuickAction,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val iconBg = if (isDark) GS.DarkIconGreen else GS.LightIconGreen

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = action.onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = action.icon,
                contentDescription = action.label,
                tint               = action.accentColor,
                modifier           = Modifier.size(24.dp)
            )
        }
        Text(
            text       = action.label,
            fontSize   = 9.sp,
            fontWeight = FontWeight.Medium,
            color      = GS.mutedText(isDark),
            textAlign  = TextAlign.Center,
            lineHeight = 12.sp,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis
        )
    }
}