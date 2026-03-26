package com.gosuraksha.app.ui.auth

// =============================================================================
// AuthHero.kt — PhonePe-style animated security hero
//
// Features:
//   • Deep navy gradient background (always dark — security aesthetic)
//   • Animated radar / sonar pulsing rings (3 staggered rings)
//   • Logo.png centered with glowing ring underneath
//   • Floating security dot pattern (hex grid)
//   • "SECURED · ENCRYPTED · TRUSTED" trust badge
//   • Title + subtitle at bottom-left
//
// Used by: LoginScreen (height=300dp), OtpScreen (height=220dp)
// =============================================================================

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R

@Composable
internal fun AuthHero(
    isDark:     Boolean,
    title:      String,
    subtitle:   String,
    height:     Dp     = 280.dp,
    emoji:      String? = null,
    showShield: Boolean = true
) {
    // ── Infinite transition for radar pulses ──────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")

    // Ring 1 — fastest
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue   = 0.35f,
        targetValue    = 1.4f,
        animationSpec  = infiniteRepeatable(
            animation   = tween(2200, easing = LinearOutSlowInEasing),
            repeatMode  = RepeatMode.Restart
        ),
        label = "ring1"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue   = 0.6f,
        targetValue    = 0f,
        animationSpec  = infiniteRepeatable(
            animation   = tween(2200, easing = FastOutLinearInEasing),
            repeatMode  = RepeatMode.Restart
        ),
        label = "ring1a"
    )

    // Ring 2 — medium, 700ms offset
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue   = 0.35f,
        targetValue    = 1.4f,
        animationSpec  = infiniteRepeatable(
            animation   = tween(2200, delayMillis = 700, easing = LinearOutSlowInEasing),
            repeatMode  = RepeatMode.Restart
        ),
        label = "ring2"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue   = 0.6f,
        targetValue    = 0f,
        animationSpec  = infiniteRepeatable(
            animation   = tween(2200, delayMillis = 700, easing = FastOutLinearInEasing),
            repeatMode  = RepeatMode.Restart
        ),
        label = "ring2a"
    )

    // Ring 3 — slowest, 1400ms offset
    val ring3Scale by infiniteTransition.animateFloat(
        initialValue   = 0.35f,
        targetValue    = 1.4f,
        animationSpec  = infiniteRepeatable(
            animation   = tween(2200, delayMillis = 1400, easing = LinearOutSlowInEasing),
            repeatMode  = RepeatMode.Restart
        ),
        label = "ring3"
    )
    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue   = 0.6f,
        targetValue    = 0f,
        animationSpec  = infiniteRepeatable(
            animation   = tween(2200, delayMillis = 1400, easing = FastOutLinearInEasing),
            repeatMode  = RepeatMode.Restart
        ),
        label = "ring3a"
    )

    // Logo breathing pulse
    val logoGlow by infiniteTransition.animateFloat(
        initialValue   = 0.4f,
        targetValue    = 0.9f,
        animationSpec  = infiniteRepeatable(
            animation   = tween(1800, easing = FastOutSlowInEasing),
            repeatMode  = RepeatMode.Reverse
        ),
        label = "logoPulse"
    )

    // Floating dot positions (subtle drift)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue   = -4f,
        targetValue    = 4f,
        animationSpec  = infiniteRepeatable(
            animation   = tween(3000, easing = FastOutSlowInEasing),
            repeatMode  = RepeatMode.Reverse
        ),
        label = "float"
    )

    val heroBrush = Brush.verticalGradient(
        colors = listOf(
            AuthColors.heroTop(isDark),
            AuthColors.heroMid(isDark),
            AuthColors.heroBot(isDark)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(heroBrush)
    ) {

        // ── Background hex dot grid ────────────────────────────────────────
        HexDotGrid(modifier = Modifier.fillMaxSize())

        // ── Radar rings — centered ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.Center)
                .offset(y = (-14).dp)
        ) {
            // Ring 3 (outermost)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(ring3Scale)
                    .alpha(ring3Alpha)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.Transparent,
                                    AuthColors.RadarGlow.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Ring 2
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(ring2Scale)
                    .alpha(ring2Alpha)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.Transparent,
                                    AuthColors.RadarGlow.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Ring 1 (innermost — most visible)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(ring1Scale)
                    .alpha(ring1Alpha)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.Transparent,
                                    AuthColors.RadarGlow.copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // ── Logo glow halo ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.Center)
                .offset(y = (-14).dp)
                .alpha(logoGlow)
                .blur(20.dp)
                .clip(CircleShape)
                .background(AuthColors.RadarGlow.copy(alpha = 0.35f))
        )

        // ── Logo image ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.Center)
                .offset(y = (-14).dp)
                .clip(CircleShape)
                .background(Color(0xFF0A1628))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter            = painterResource(id = R.drawable.logo),
                contentDescription = "GoSuraksha",
                modifier           = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
        }

        // ── Floating security icons ────────────────────────────────────────
        // Top-left lock
        Icon(
            imageVector        = Icons.Rounded.Lock,
            contentDescription = null,
            tint               = AuthColors.Accent.copy(alpha = 0.25f),
            modifier           = Modifier
                .size(18.dp)
                .align(Alignment.TopStart)
                .offset(x = 28.dp, y = (floatOffset + 24).dp)
        )
        // Top-right shield
        Icon(
            imageVector        = Icons.Rounded.Shield,
            contentDescription = null,
            tint               = AuthColors.RadarGlowAlt.copy(alpha = 0.2f),
            modifier           = Modifier
                .size(22.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-24).dp, y = (-floatOffset + 20).dp)
        )
        // Bottom-right lock (smaller)
        Icon(
            imageVector        = Icons.Rounded.Lock,
            contentDescription = null,
            tint               = AuthColors.Accent.copy(alpha = 0.18f),
            modifier           = Modifier
                .size(14.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-32).dp, y = (floatOffset - 36).dp)
        )

        // ── Trust badge (top-center) ───────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1AB87A).copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector        = Icons.Rounded.Shield,
                contentDescription = null,
                tint               = AuthColors.Accent,
                modifier           = Modifier.size(9.dp)
            )
            Text(
                text          = "SECURED  ·  ENCRYPTED  ·  TRUSTED",
                fontSize      = 8.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color         = AuthColors.Accent.copy(alpha = 0.9f)
            )
        }

        // ── App name below logo ────────────────────────────────────────────
        Text(
            text       = "GoSuraksha",
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color      = Color.White.copy(alpha = 0.85f),
            modifier   = Modifier
                .align(Alignment.Center)
                .offset(y = 30.dp)
        )

        // ── Title + subtitle — bottom left ─────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 22.dp, bottom = 22.dp, end = 22.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (emoji != null) {
                Text(emoji, fontSize = 26.sp)
                Spacer(Modifier.height(2.dp))
            }
            Text(
                text          = title,
                fontSize      = if (height <= 180.dp) 18.sp else 22.sp,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                lineHeight    = 28.sp,
                color         = Color.White
            )
            Text(
                text       = subtitle,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Normal,
                color      = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Hex dot grid ──────────────────────────────────────────────────────────────
@Composable
private fun HexDotGrid(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.alpha(0.35f)) {
        val dotRadius = 1.8f
        val colSpacing = 28.dp.toPx()
        val rowSpacing = 24.dp.toPx()
        val cols = (size.width / colSpacing).toInt() + 2
        val rows = (size.height / rowSpacing).toInt() + 2

        for (row in 0..rows) {
            for (col in 0..cols) {
                val offsetX = if (row % 2 == 0) 0f else colSpacing / 2f
                val x = col * colSpacing + offsetX
                val y = row * rowSpacing
                drawCircle(
                    color  = Color(0xFF1A3A5C),
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}
