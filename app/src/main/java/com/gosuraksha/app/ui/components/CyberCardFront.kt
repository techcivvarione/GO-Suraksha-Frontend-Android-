package com.gosuraksha.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Level colour helper ───────────────────────────────────────────────────────

private fun levelPill(level: String): Pair<Color, String> = when (level.uppercase()) {
    "EXCELLENT"     -> Color(0xFF4ADE80) to "EXCELLENT"
    "MOSTLY_SAFE"   -> Color(0xFF2EC472) to "MOSTLY SAFE"
    "MODERATE_RISK" -> Color(0xFFFBBF24) to "MODERATE"
    "HIGH_RISK"     -> Color(0xFFFF8C42) to "HIGH RISK"
    "CRITICAL"      -> Color(0xFFEF4444) to "CRITICAL"
    else            -> Color(0xFF6BAA80) to "LOADING"
}

// ── Main card composable ──────────────────────────────────────────────────────

@Composable
fun CyberCardFrontNew(
    userName: String,
    cardNumber: String,
    cyberScore: Int,        // receives the already-animated value from CyberCardNew
    generatedOn: String,
    validTill: String,
    level: String = ""
) {
    // ── Slow shimmer sweep — repeats every 3 s ────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue  = -0.6f,
        targetValue   = 1.6f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    val (pillColor, pillLabel) = levelPill(level)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        // ── Layer 1: deep matte background ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors     = listOf(Color(0xFF060D1C), Color(0xFF071814)),
                        start      = Offset(0f, 0f),
                        end        = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        )

        // ── Layer 2: subtle circuit-dot grid ─────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 32.dp.toPx()
            val dotR    = 1.2.dp.toPx()
            val cols    = (size.width  / spacing).toInt() + 2
            val rows    = (size.height / spacing).toInt() + 2
            for (c in 0..cols) {
                for (r in 0..rows) {
                    val x = c * spacing
                    val y = r * spacing
                    drawCircle(
                        color  = Color.White.copy(alpha = 0.06f),
                        radius = dotR,
                        center = Offset(x, y)
                    )
                    if (c < cols && (c + r) % 3 != 0) {
                        drawLine(
                            color       = Color.White.copy(alpha = 0.03f),
                            start       = Offset(x + dotR, y),
                            end         = Offset(x + spacing - dotR, y),
                            strokeWidth = 0.6.dp.toPx()
                        )
                    }
                    if (r < rows && (c + r) % 2 != 0) {
                        drawLine(
                            color       = Color.White.copy(alpha = 0.03f),
                            start       = Offset(x, y + dotR),
                            end         = Offset(x, y + spacing - dotR),
                            strokeWidth = 0.6.dp.toPx()
                        )
                    }
                }
            }
        }

        // ── Layer 3: diagonal shimmer sweep ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.035f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerX * 800f,  0f),
                        end   = Offset(shimmerX * 800f + 260f, 400f)
                    )
                )
        )

        // ── Layer 4: left accent stripe (green 4%) ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2EC472).copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ── Content ───────────────────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // TOP ROW — brand + level badge
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text         = "GO SURAKSHA",
                        color        = Color(0xFF2EC472),
                        fontSize     = 13.sp,
                        fontWeight   = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text         = "CYBER SHIELD",
                        color        = Color.White.copy(alpha = 0.42f),
                        fontSize     = 8.sp,
                        fontWeight   = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                }

                // Level pill — color-coded
                if (level.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = pillColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text         = pillLabel,
                            color        = pillColor,
                            fontSize     = 9.sp,
                            fontWeight   = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            // MIDDLE — big score
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(
                    verticalAlignment     = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text         = cyberScore.toString(),
                        color        = Color(0xFF2EC472),
                        fontSize     = 54.sp,
                        fontWeight   = FontWeight.Black,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        text         = "/ 1000",
                        color        = Color.White.copy(alpha = 0.35f),
                        fontSize     = 14.sp,
                        fontWeight   = FontWeight.Medium,
                        modifier     = Modifier.padding(bottom = 11.dp)
                    )
                }
                Text(
                    text         = "CYBER SAFETY SCORE",
                    color        = Color.White.copy(alpha = 0.28f),
                    fontSize     = 8.sp,
                    fontWeight   = FontWeight.Medium,
                    letterSpacing = 1.8.sp
                )
            }

            // BOTTOM ROW — cardholder + card ID
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text         = "CARDHOLDER",
                        color        = Color.White.copy(alpha = 0.36f),
                        fontSize     = 7.5.sp,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text         = userName.uppercase().take(20),
                        color        = Color.White,
                        fontSize     = 13.sp,
                        fontWeight   = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text         = "CARD ID",
                        color        = Color.White.copy(alpha = 0.36f),
                        fontSize     = 7.5.sp,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text         = cardNumber,
                        color        = Color.White.copy(alpha = 0.65f),
                        fontSize     = 10.sp,
                        fontWeight   = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
