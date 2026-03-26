package com.gosuraksha.app.ui.history.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.design.tokens.ColorTokens

@Composable
fun HistoryLoadingState() {
    val shimmerAnim = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerAnim.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "shimmer_offset"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ShimmerDateHeader(shimmerOffset) }
        items(4) { ShimmerCard(shimmerOffset) }
    }
}

@Composable
fun shimmerBrush(offset: Float): Brush {
    val base = ColorTokens.border().copy(alpha = 0.3f)
    val highlight = ColorTokens.textSecondary().copy(alpha = 0.15f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(offset * 400f, 0f),
        end = Offset(offset * 400f + 400f, 0f)
    )
}

@Composable
fun ShimmerDateHeader(offset: Float) {
    Box(
        modifier = Modifier
            .padding(start = 4.dp, top = 8.dp, bottom = 6.dp)
            .width(120.dp)
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(shimmerBrush(offset))
    )
}

@Composable
fun ShimmerCard(offset: Float) {
    val brush = shimmerBrush(offset)
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(brush)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(13.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(brush)
            )
        }

        Divider(modifier = Modifier.padding(horizontal = 14.dp), color = ColorTokens.border().copy(alpha = 0.3f))

        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(45.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush)
            )
        }

        Row(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun HistoryEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ColorTokens.accent().copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = ColorTokens.accent().copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "No Scans Yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ColorTokens.textPrimary()
                )
            )

            Text(
                text = "Scan QR codes, URLs, or files to protect yourself.\nYour scan history will appear here.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ColorTokens.textSecondary()
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HistoryErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "⚠️", fontSize = 40.sp)
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ColorTokens.textPrimary()
                )
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ColorTokens.textSecondary()
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
