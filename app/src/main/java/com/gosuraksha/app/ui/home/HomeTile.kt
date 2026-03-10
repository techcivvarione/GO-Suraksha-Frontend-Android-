package com.gosuraksha.app.ui.home
import com.gosuraksha.app.design.tokens.ColorTokens

// =============================================================================
// HomeTile.kt — UI-ONLY component
// FIXED: Full light/dark theme awareness.
// Dark  → elevation only, NO border, dark surface #1E2236
// Light → white surface, 1dp border #E2EAF8, reduced shadow
// =============================================================================

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.design.tokens.TypographyTokens

enum class HomeTileVariant { Trending, Core }

@Composable
fun HomeTile(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    variant: HomeTileVariant = HomeTileVariant.Core,
    modifier: Modifier = Modifier
) {
    val isDark = ColorTokens.LocalAppDarkMode.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(130, easing = FastOutSlowInEasing),
        label = "tile_scale"
    )

    val radius: Dp
    val iconCircleSize: Dp
    val iconSize: Dp
    val tileHeight: Dp
    val elevation: Dp

    when (variant) {
        HomeTileVariant.Trending -> {
            radius = 18.dp; iconCircleSize = 52.dp; iconSize = 24.dp
            tileHeight = 104.dp; elevation = if (isDark) 4.dp else 2.dp
        }
        HomeTileVariant.Core -> {
            radius = 16.dp; iconCircleSize = 40.dp; iconSize = 20.dp
            tileHeight = 88.dp; elevation = if (isDark) 3.dp else 1.dp
        }
    }

    val surfaceColor = if (isDark) Color(0xFF1E2236) else Color(0xFFFFFFFF)
    val border = if (isDark) null else BorderStroke(1.dp, Color(0xFFE2EAF8))
    val labelColor = if (isDark) Color(0xFFE6E9F4) else Color(0xFF1A1F36)

    val tileModifier = when (variant) {
        HomeTileVariant.Trending -> modifier.width(96.dp).height(tileHeight)
        HomeTileVariant.Core     -> modifier.fillMaxWidth().height(tileHeight)
    }

    Surface(
        modifier = tileModifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(radius),
        color = surfaceColor,
        border = border,
        shadowElevation = elevation,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().height(tileHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(iconCircleSize)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label,
                    tint = accentColor, modifier = Modifier.size(iconSize))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = TypographyTokens.labelSmall,
                fontWeight = if (variant == HomeTileVariant.Trending) FontWeight.SemiBold
                else FontWeight.Normal,
                color = labelColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = TypographyTokens.labelSmall.lineHeight
            )
        }
    }
}