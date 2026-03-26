package com.gosuraksha.app.ui.screens.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R

val SosRed = Color(0xFFEF4444)
private val SosRedDeep = Color(0xFF7F1D1D)
private val SosRedDeeper = Color(0xFF3B0A0A)
val LiveGreen = Color(0xFF4ADE80)
val TealAccent = Color(0xFF1BBE9B)
val AmberStatus = Color(0xFFF59E0B)

val HeroBrushDark = Brush.linearGradient(
    colorStops = arrayOf(
        0.00f to SosRedDeeper,
        0.35f to SosRedDeep,
        0.70f to SosRed,
        1.00f to Color(0xFFF87171)
    )
)

val HeroBrushLight = Brush.linearGradient(
    colorStops = arrayOf(
        0.00f to SosRedDeep,
        0.50f to SosRed,
        1.00f to Color(0xFFF87171)
    )
)

val HeroBrushMutedDark = Brush.linearGradient(
    colorStops = arrayOf(
        0.00f to Color(0xFF1A0505),
        0.50f to Color(0xFF3B0A0A),
        1.00f to Color(0xFF5A1010)
    )
)

val HeroBrushMutedLight = Brush.linearGradient(
    colorStops = arrayOf(
        0.00f to Color(0xFFFECACA),
        0.50f to Color(0xFFFCA5A5),
        1.00f to Color(0xFFF87171)
    )
)

@Composable
fun CyberSosHero(
    isDark: Boolean,
    condensed: Boolean,
    titleText: String,
    subtitleText: String,
    onBack: (() -> Unit)?,
    showStats: Boolean
) {
    val vertPad = if (condensed) 16.dp else 24.dp
    val heroBrush = if (isDark) HeroBrushDark else HeroBrushLight
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(1300, easing = EaseInOut), RepeatMode.Reverse),
        label = "live_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(heroBrush)
            // statusBarsPadding() removed: the outer MainScaffold already accounts
            // for the EnterpriseTopBar height via innerPadding — adding it here
            // again caused a visible empty gap below the header on every CyberSOS step.
            .padding(horizontal = 20.dp, vertical = vertPad)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(if (condensed) 14.dp else 18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { onBack?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "CyberSOS",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (condensed) 44.dp else 52.dp)
                            .clip(RoundedCornerShape(if (condensed) 12.dp else 14.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.25f),
                                RoundedCornerShape(if (condensed) 12.dp else 14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (condensed) 22.dp else 26.dp)
                        )
                    }

                    if (!condensed) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(LiveGreen.copy(alpha = liveAlpha))
                            )
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.4).sp,
                            fontSize = if (condensed) 18.sp else 20.sp
                        )
                    )
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            if (showStats) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStatChip(number = "1930", label = "Helpline")
                    HeroStatChip(number = "24/7", label = "Support")
                    HeroStatChip(number = "₹2L+", label = "Avg Loss")
                }
            }
        }
    }
}

@Composable
fun HeroStatChip(number: String, label: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 14.sp
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
}
