package com.gosuraksha.app.ui.components

// =============================================================================
// FullScreenLoader.kt — Final redesign
//
// Changes:
//   • Shield draws stroke → fills green from bottom → checkmark appears
//   • Loader is strictly BOUND to `visible` — stops the moment op completes
//   • Funny security quotes fade in/out every 2.6s (matches shield cycle)
//   • Title rotates every 2 cycles for variety
//   • No random infinite loop — coroutine cancels when visible = false
// =============================================================================

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// ── Quotes & titles ───────────────────────────────────────────────────────────
private val loaderQuotes = listOf(
    "Hackers hate this one weird app.",
    "Even your Wi-Fi password is safer with us.",
    "Your ex can't see this either. You're welcome.",
    "256-bit encryption — overkill? Never.",
    "Wrapping you in digital bubble wrap.",
    "No data was harmed in this login.",
    "Your privacy has entered the chat.",
    "Firewalls up. Threats down. Vibes immaculate.",
    "Making sure no one's lurking in the shadows.",
    "We take privacy more seriously than your diet."
)

private val loaderTitles = listOf(
    "Building your shield...",
    "Locking things down...",
    "Almost there...",
    "Armour engaged..."
)

// ── Timing constants (tied to shield animation duration) ──────────────────────
private const val SHIELD_CYCLE_MS  = 2600L   // one full shield build = 2.6s
private const val TITLE_CYCLE_MS   = 5200L   // title changes every 2 shield cycles

@Composable
fun FullScreenLoader(visible: Boolean) {
    if (!visible) return

    // ── Quote + title state ───────────────────────────────────────────────
    var quoteIndex by remember { mutableStateOf(0) }
    var titleIndex by remember { mutableStateOf(0) }
    var quoteVisible by remember { mutableStateOf(true) }

    // Strictly bound — cancels when visible flips to false
    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        var elapsed = 0L
        while (isActive) {
            delay(SHIELD_CYCLE_MS)
            elapsed += SHIELD_CYCLE_MS

            // Fade out → swap quote → fade in
            quoteVisible = false
            delay(400)
            quoteIndex = (quoteIndex + 1) % loaderQuotes.size
            quoteVisible = true

            // Title changes every 2 cycles
            if (elapsed % TITLE_CYCLE_MS == 0L) {
                titleIndex = (titleIndex + 1) % loaderTitles.size
            }
        }
    }

    // ── Shield build animation (0f → 1f, loops every SHIELD_CYCLE_MS) ────
    val shieldProgress by rememberInfiniteTransition(label = "shield")
        .animateFloat(
            initialValue   = 0f,
            targetValue    = 1f,
            animationSpec  = infiniteRepeatable(
                animation  = tween(
                    durationMillis = SHIELD_CYCLE_MS.toInt(),
                    easing         = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shieldProgress"
        )

    // ── Derived stages from progress ──────────────────────────────────────
    // 0.00 – 0.42 : stroke draws
    // 0.42 – 0.86 : green fill rises
    // 0.86 – 1.00 : checkmark draws, full shield holds
    val strokeProgress  = (shieldProgress / 0.42f).coerceIn(0f, 1f)
    val fillProgress    = ((shieldProgress - 0.42f) / 0.44f).coerceIn(0f, 1f)
    val checkProgress   = ((shieldProgress - 0.86f) / 0.14f).coerceIn(0f, 1f)

    val strokeAlpha     = if (fillProgress > 0f) (1f - fillProgress).coerceIn(0f, 1f) else 1f
    val accentGreen     = Color(0xFF52B788)

    // ── Full-screen dimmed overlay ────────────────────────────────────────
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Shield canvas ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 64.dp)
                    .drawBehind {
                        val w = size.width
                        val h = size.height

                        // Shield path points (scaled to canvas)
                        val path = shieldPath(w, h)

                        // 1. Ghost outline
                        drawPath(
                            path   = path,
                            color  = Color.White.copy(alpha = 0.15f),
                            style  = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round)
                        )

                        // 2. Stroke draw (white, disappears as fill comes in)
                        if (strokeProgress > 0f && strokeAlpha > 0f) {
                            val strokePath = partialPath(path, strokeProgress)
                            drawPath(
                                path   = strokePath,
                                color  = Color.White.copy(alpha = strokeAlpha),
                                style  = Stroke(
                                    width    = 2.5.dp.toPx(),
                                    join     = StrokeJoin.Round,
                                    cap      = StrokeCap.Round
                                )
                            )
                        }

                        // 3. Green fill rising from bottom
                        if (fillProgress > 0f) {
                            val fillTop = h * (1f - fillProgress)
                            clipRect(top = fillTop) {
                                drawPath(path = path, color = accentGreen)
                            }
                            // Green outline
                            drawPath(
                                path  = path,
                                color = accentGreen,
                                style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round)
                            )
                        }

                        // 4. Checkmark
                        if (checkProgress > 0f) {
                            val cx = w * 0.325f
                            val cy = h * 0.51f
                            val mx = w * 0.45f
                            val my = h * 0.625f
                            val ex = w * 0.675f
                            val ey = h * 0.40f

                            val totalLen  = dist(cx, cy, mx, my) + dist(mx, my, ex, ey)
                            val drawLen   = totalLen * checkProgress
                            val firstLen  = dist(cx, cy, mx, my)

                            val checkPaint = androidx.compose.ui.graphics.Paint().apply {
                                color       = Color.White
                                strokeWidth = 3.5.dp.toPx()
                                strokeCap   = StrokeCap.Round
                                strokeJoin  = StrokeJoin.Round
                                style       = PaintingStyle.Stroke
                            }

                            drawContext.canvas.apply {
                                if (drawLen <= firstLen) {
                                    val t = drawLen / firstLen
                                    drawLine(
                                        Offset(cx, cy),
                                        Offset(cx + (mx - cx) * t, cy + (my - cy) * t),
                                        checkPaint
                                    )
                                } else {
                                    drawLine(Offset(cx, cy), Offset(mx, my), checkPaint)
                                    val rem = drawLen - firstLen
                                    val secondLen = dist(mx, my, ex, ey)
                                    val t = (rem / secondLen).coerceIn(0f, 1f)
                                    drawLine(
                                        Offset(mx, my),
                                        Offset(mx + (ex - mx) * t, my + (ey - my) * t),
                                        checkPaint
                                    )
                                }
                            }
                        }
                    }
            )

            Spacer(Modifier.height(22.dp))

            // ── Title ──────────────────────────────────────────────────────
            Text(
                text       = loaderTitles[titleIndex],
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            // ── Quote fade in/out ──────────────────────────────────────────
            AnimatedVisibility(
                visible = quoteVisible,
                enter   = fadeIn(tween(400)),
                exit    = fadeOut(tween(400))
            ) {
                Text(
                    text      = loaderQuotes[quoteIndex],
                    fontSize  = 12.sp,
                    color     = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier  = Modifier.padding(horizontal = 40.dp)
                )
            }
        }
    }
}

// =============================================================================
// Shield path builder — normalised to canvas size
// =============================================================================
private fun shieldPath(w: Float, h: Float): Path {
    return Path().apply {
        moveTo(w * 0.5f, h * 0.045f)
        lineTo(w * 0.1f, h * 0.19f)
        lineTo(w * 0.1f, h * 0.43f)
        cubicTo(
            w * 0.1f,  h * 0.70f,
            w * 0.28f, h * 0.89f,
            w * 0.5f,  h * 0.975f
        )
        cubicTo(
            w * 0.72f, h * 0.89f,
            w * 0.9f,  h * 0.70f,
            w * 0.9f,  h * 0.43f
        )
        lineTo(w * 0.9f, h * 0.19f)
        close()
    }
}

// =============================================================================
// Partial path — approximates drawing strokeProgress fraction of the path
// Uses PathMeasure for accurate partial stroke
// =============================================================================
private fun partialPath(path: Path, progress: Float): Path {
    val pm     = PathMeasure()
    pm.setPath(path, false)
    val length = pm.length
    val dst    = Path()
    pm.getSegment(0f, length * progress, dst, true)
    return dst
}

// =============================================================================
// Distance helper
// =============================================================================
private fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    return kotlin.math.sqrt(dx * dx + dy * dy)
}