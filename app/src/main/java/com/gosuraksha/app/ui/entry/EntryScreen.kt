package com.gosuraksha.app.ui.entry

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.R
import com.gosuraksha.app.ui.motion.MotionSpec
import kotlinx.coroutines.delay
import kotlin.math.sqrt

private val EntryBg   = Color(0xFF0D1117)
private val TealDot   = Color(0xFF00E5C3)
private val TextMuted = Color(0xFF8B949E)

// =============================================================================
// EntryScreen
// =============================================================================
@Composable
fun EntryScreen(onFinish: () -> Unit) {

    // ── Breathing wave ────────────────────────────────────────────────
    // wave goes 0 → 1 representing how far the ripple has travelled
    val infinite    = rememberInfiniteTransition(label = stringResource(R.string.ui_entryscreen_3))
    val wavePhase by infinite.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = stringResource(R.string.ui_entryscreen_4)
    )

    // ── Logo entrance ─────────────────────────────────────────────────
    val logoScale  = remember { Animatable(0.6f) }
    val logoAlpha  = remember { Animatable(0f) }
    val tagAlpha   = remember { Animatable(0f) }
    val dotsAlpha  = remember { Animatable(0f) }
    val bottomAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1 — logo pops in
        logoAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        logoScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
        )
        delay(160)
        // 2 — tagline
        tagAlpha.animateTo(1f, tween(380))
        delay(80)
        // 3 — loading dots
        dotsAlpha.animateTo(1f, tween(280))
        // 4 — bottom strip
        bottomAlpha.animateTo(1f, tween(300))
        // hold then exit
        delay(1400)
        onFinish()
    }

    // Loading dot pulse (3 staggered)
    val dot1 by infinite.animateFloat(0.25f, 1f, infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = stringResource(R.string.ui_entryscreen_5))
    val dot2 by infinite.animateFloat(0.25f, 1f, infiniteRepeatable(tween(550, 150, FastOutSlowInEasing), RepeatMode.Reverse), label = stringResource(R.string.ui_entryscreen_6))
    val dot3 by infinite.animateFloat(0.25f, 1f, infiniteRepeatable(tween(550, 300, FastOutSlowInEasing), RepeatMode.Reverse), label = stringResource(R.string.ui_entryscreen_7))

    Box(
        modifier = Modifier.fillMaxSize().background(EntryBg),
        contentAlignment = Alignment.Center
    ) {

        // ── BREATHING GRID ────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols    = (size.width  / 36.dp.toPx()).toInt() + 2
            val rows    = (size.height / 36.dp.toPx()).toInt() + 2
            val spacingX = size.width  / (cols - 1).coerceAtLeast(1)
            val spacingY = size.height / (rows - 1).coerceAtLeast(1)
            val cx = size.width  / 2f
            val cy = size.height / 2f
            // max possible distance from center to a corner
            val maxDist = sqrt(cx * cx + cy * cy)
            // wave ring width as fraction of maxDist
            val ringWidth = 0.28f

            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val x  = col * spacingX
                    val y  = row * spacingY
                    val dx = x - cx
                    val dy = y - cy
                    val dist      = sqrt(dx * dx + dy * dy)
                    val normDist  = dist / maxDist          // 0..1

                    // how close is this dot to the travelling wave front
                    val waveFront = wavePhase              // 0..1
                    val delta     = normDist - waveFront
                    // dots behind the wave fade gently; wave crest glows bright
                    val alpha = when {
                        delta in -ringWidth..0f -> {
                            // inside the ring — brightness peaks at crest
                            val t = (delta + ringWidth) / ringWidth  // 0..1
                            // smooth falloff: bright near crest (t=1), dim at tail (t=0)
                            0.03f + 0.13f * (t * t)
                        }
                        delta in 0f..0.04f -> {
                            // just ahead of wave — tiny leading glow
                            0.03f + 0.05f * (1f - delta / 0.04f)
                        }
                        else -> 0.025f   // resting state — barely visible
                    }

                    drawCircle(
                        color  = TealDot.copy(alpha = alpha.coerceIn(0f, 1f)),
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // ── BOTTOM VIGNETTE ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, EntryBg)))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(EntryBg, Color.Transparent)))
        )

        // ── LOGO + TAGLINE ────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Free logo — no card, no border
            Image(
                painter            = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.ui_entryscreen_8),
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .size(150.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(Modifier.height(24.dp))

            // Tagline only — no app name text
            Text(
                text       = stringResource(R.string.ui_entryscreen_2),
                color      = TextMuted,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign  = TextAlign.Center,
                modifier   = Modifier
                    .alpha(tagAlpha.value)
                    .padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(48.dp))

            // Loading dots
            Row(
                modifier              = Modifier.alpha(dotsAlpha.value),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                listOf(dot1, dot2, dot3).forEach { a ->
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(TealDot.copy(alpha = a))
                    )
                }
            }
        }

        // ── BOTTOM BRAND STRIP ────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(bottomAlpha.value)
                .padding(bottom = 38.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(Modifier.width(18.dp).height(1.dp).background(TealDot.copy(alpha = 0.25f)))
            Text(
                stringResource(R.string.ui_entryscreen_1),
                color         = TealDot.copy(alpha = 0.4f),
                fontSize      = 9.sp,
                letterSpacing = 1.8.sp,
                fontWeight    = FontWeight.Medium
            )
            Box(Modifier.width(18.dp).height(1.dp).background(TealDot.copy(alpha = 0.25f)))
        }
    }
}