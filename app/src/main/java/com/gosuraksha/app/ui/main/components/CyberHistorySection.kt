package com.gosuraksha.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// =============================================================================
// CyberHistorySection — Insights + Actions (V2)
// =============================================================================

@Composable
fun CyberHistorySection(
    isDark: Boolean,
    insights: List<String>,
    actions: List<CyberAction>,
    onActionClick: (CyberAction) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // ── Insights ─────────────────────────────────────────────────────────
        if (insights.isNotEmpty()) {
            InsightsPanel(isDark = isDark, insights = insights)
        }

        // ── Actions ──────────────────────────────────────────────────────────
        if (actions.isNotEmpty()) {
            ActionsPanel(isDark = isDark, actions = actions, onActionClick = onActionClick)
        }
    }
}

// ── Insights panel — staggered fade-in per item ───────────────────────────────

@Composable
private fun InsightsPanel(isDark: Boolean, insights: List<String>) {
    // Track which items are visible; start revealing after 500 ms, 120 ms apart
    val visible = remember(insights) { mutableStateListOf(*Array(insights.size) { false }) }

    LaunchedEffect(insights) {
        delay(500)
        insights.indices.forEach { i ->
            visible[i] = true
            delay(120)
        }
    }

    Surface(
        color          = cardBg(isDark),
        shape          = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFFFBBF24).copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb, null,
                        tint     = Color(0xFFFBBF24),
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    "Insights",
                    color      = onSurf(isDark),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(divider(isDark))
            )

            insights.forEachIndexed { index, insight ->
                AnimatedVisibility(
                    visible = visible.getOrElse(index) { false },
                    enter   = fadeIn(tween(280)) +
                              slideInVertically(tween(280)) { it / 3 }
                ) {
                    InsightRow(isDark = isDark, text = insight)
                }
            }
        }
    }
}

@Composable
private fun InsightRow(isDark: Boolean, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(6.dp)
                .background(Green400, CircleShape)
        )
        Text(text, color = subText(isDark), fontSize = 13.sp, lineHeight = 19.sp)
    }
}

// ── Actions panel ─────────────────────────────────────────────────────────────

@Composable
private fun ActionsPanel(
    isDark: Boolean,
    actions: List<CyberAction>,
    onActionClick: (CyberAction) -> Unit
) {
    Surface(
        color          = cardBg(isDark),
        shape          = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Green400.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.TaskAlt, null,
                        tint     = Green400,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    "Recommended Actions",
                    color      = onSurf(isDark),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(divider(isDark))
            )

            actions.forEach { action ->
                ActionRow(isDark = isDark, action = action, onClick = { onActionClick(action) })
            }
        }
    }
}

@Composable
private fun ActionRow(isDark: Boolean, action: CyberAction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(chipBg(isDark), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                actionIcon(action.icon), null,
                tint     = Green400,
                modifier = Modifier.size(18.dp)
            )
        }

        // Title + subtitle
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                action.title,
                color      = onSurf(isDark),
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (action.subtitle.isNotBlank()) {
                Text(action.subtitle, color = subText(isDark), fontSize = 11.sp)
            }
        }

        Icon(
            Icons.Rounded.ChevronRight, null,
            tint     = subText(isDark),
            modifier = Modifier.size(18.dp)
        )
    }
}

// =============================================================================
// Legacy components — kept for backward-compat with other screens
// =============================================================================

@Composable
fun InfoPanel(isDark: Boolean, title: String, value: String) {
    Surface(
        color          = cardBg(isDark),
        shape          = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        modifier       = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = subText(isDark), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(value, color = onSurf(isDark),  fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
