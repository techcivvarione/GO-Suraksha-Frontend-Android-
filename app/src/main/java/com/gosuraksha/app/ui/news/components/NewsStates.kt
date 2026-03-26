package com.gosuraksha.app.ui.news

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.R
import com.gosuraksha.app.ui.components.localizedUiMessage

@Composable
fun NewsErrorState(error: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = localizedUiMessage(error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun NewsEmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.ui_newsscreen_2),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun NewsLoadingState() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF1A1035), Color(0xFF221545), Color(0xFF2E1F5E), Color(0xFF221545), Color(0xFF1A1035)),
        start = Offset(shimmerX - 500f, 0f),
        end = Offset(shimmerX + 500f, 0f)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmerBrush)
                .border(1.dp, AuroraViolet.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.width(70.dp).height(10.dp).clip(RoundedCornerShape(50)).background(AuroraViolet.copy(alpha = 0.25f)))
                Box(Modifier.fillMaxWidth(0.92f).height(20.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.08f)))
                Box(Modifier.fillMaxWidth(0.72f).height(20.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.08f)))
                Box(Modifier.fillMaxWidth(0.85f).height(13.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.04f)))
                Box(Modifier.fillMaxWidth(0.60f).height(13.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.04f)))
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(60.dp).height(11.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.05f)))
                    Spacer(Modifier.weight(1f))
                    repeat(3) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)))
                        if (it < 2) Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(7.dp)
                        .width(if (i == 0) 24.dp else 7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (i == 0) Brush.horizontalGradient(listOf(AuroraViolet.copy(alpha = 0.4f), AuroraBlue.copy(alpha = 0.4f)))
                            else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)))
                        )
                )
            }
        }
    }
}
