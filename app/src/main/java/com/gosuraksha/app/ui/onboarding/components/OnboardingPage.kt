package com.gosuraksha.app.ui.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.ui.onboarding.model.DmSansFamily
import com.gosuraksha.app.ui.onboarding.model.GreenAccent
import com.gosuraksha.app.ui.onboarding.model.IlloFaint
import com.gosuraksha.app.ui.onboarding.model.IlloWhite
import com.gosuraksha.app.ui.onboarding.model.OnboardingSlide
import com.gosuraksha.app.ui.onboarding.model.SyneFamily
import com.gosuraksha.app.ui.onboarding.model.TextPri
import com.gosuraksha.app.ui.onboarding.model.WarnAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OnboardingPage(
    page: Int,
    slide: OnboardingSlide,
) {
    when (page) {
        0 -> Slide1Shield()
        1 -> Slide2Scan()
        2 -> Slide3Family()
        else -> Slide4Score()
    }
}

@Composable
private fun Slide1Shield() {
    val inf = rememberInfiniteTransition(label = "s1")
    val pulse1 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "p1")
    val pulse2 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, 733, easing = LinearEasing)), label = "p2")
    val pulse3 by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, 1466, easing = LinearEasing)), label = "p3")
    val shieldGlow by inf.animateFloat(0.7f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sg")

    Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            listOf(pulse1, pulse2, pulse3).forEach { p ->
                drawCircle(IlloWhite.copy(alpha = (1f - p) * 0.18f), radius = 44.dp.toPx() + p * 80.dp.toPx(), center = Offset(cx, cy), style = Stroke(1.2.dp.toPx()))
            }
            drawCircle(brush = Brush.radialGradient(listOf(IlloWhite.copy(alpha = 0.06f * shieldGlow), androidx.compose.ui.graphics.Color.Transparent), center = Offset(cx, cy), radius = 55.dp.toPx()), radius = 55.dp.toPx(), center = Offset(cx, cy))
            val sw = 52.dp.toPx()
            val sh = 60.dp.toPx()
            val sl = cx - sw / 2f
            val st = cy - sh * 0.48f
            val shieldPath = Path().apply {
                moveTo(cx, st)
                lineTo(sl + sw, st + sh * 0.18f)
                lineTo(sl + sw, st + sh * 0.62f)
                cubicTo(sl + sw, st + sh * 0.86f, cx, st + sh, cx, st + sh)
                cubicTo(cx, st + sh, sl, st + sh * 0.86f, sl, st + sh * 0.62f)
                lineTo(sl, st + sh * 0.18f)
                close()
            }
            drawPath(shieldPath, IlloWhite.copy(alpha = shieldGlow * 0.85f), style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))
            drawPath(shieldPath, IlloWhite.copy(alpha = 0.05f))
            val ckPath = Path().apply {
                moveTo(cx - 10.dp.toPx(), cy + 2.dp.toPx())
                lineTo(cx - 2.dp.toPx(), cy + 10.dp.toPx())
                lineTo(cx + 13.dp.toPx(), cy - 7.dp.toPx())
            }
            drawPath(ckPath, IlloWhite.copy(alpha = shieldGlow), style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            val nx = cx + 66.dp.toPx()
            val ny = cy - 54.dp.toPx()
            drawCircle(IlloFaint, 18.dp.toPx(), Offset(nx, ny))
            drawCircle(WarnAmber.copy(alpha = 0.55f), 18.dp.toPx(), Offset(nx, ny), style = Stroke(1.2.dp.toPx()))
            drawLine(WarnAmber.copy(alpha = 0.8f), Offset(nx, ny - 7.dp.toPx()), Offset(nx, ny + 2.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)
            drawCircle(WarnAmber.copy(alpha = 0.9f), 1.8.dp.toPx(), Offset(nx, ny + 6.dp.toPx()))
            drawLine(
                brush = Brush.linearGradient(listOf(WarnAmber.copy(alpha = 0.3f), androidx.compose.ui.graphics.Color.Transparent), start = Offset(nx, ny), end = Offset(cx + 20.dp.toPx(), cy - 22.dp.toPx())),
                start = Offset(nx - 10.dp.toPx(), ny + 14.dp.toPx()),
                end = Offset(cx + 20.dp.toPx(), cy - 24.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f))
            )
            val bx = cx - 62.dp.toPx()
            val by = cy + 44.dp.toPx()
            drawCircle(IlloFaint, 14.dp.toPx(), Offset(bx, by))
            drawCircle(IlloWhite.copy(alpha = 0.30f), 14.dp.toPx(), Offset(bx, by), style = Stroke(1.dp.toPx()))
            val xd = 5.dp.toPx()
            drawLine(IlloWhite.copy(alpha = 0.5f), Offset(bx - xd, by - xd), Offset(bx + xd, by + xd), 1.5.dp.toPx(), StrokeCap.Round)
            drawLine(IlloWhite.copy(alpha = 0.5f), Offset(bx + xd, by - xd), Offset(bx - xd, by + xd), 1.5.dp.toPx(), StrokeCap.Round)
        }
        Badge(text = "⚠  THREAT DETECTED", color = WarnAmber, alpha = 0.12f, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun Slide2Scan() {
    val inf = rememberInfiniteTransition(label = "s2")
    val laserY by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "laser")
    val cornerAlpha by inf.animateFloat(0.5f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ca")
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(width = 200.dp, height = 180.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val pad = 10.dp.toPx()
                val left = pad
                val top = pad
                val right = size.width - pad
                val bottom = size.height - pad
                val fW = right - left
                val fH = bottom - top
                val cLen = 18.dp.toPx()
                val cStroke = 2.5.dp.toPx()
                drawQrCells(left, top, fW, fH)
                val ly = top + fH * laserY
                drawLine(brush = Brush.horizontalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, IlloWhite.copy(alpha = 0.9f), IlloWhite, IlloWhite.copy(alpha = 0.9f), androidx.compose.ui.graphics.Color.Transparent), startX = left, endX = right), start = Offset(left, ly), end = Offset(right, ly), strokeWidth = 2.dp.toPx())
                drawLine(brush = Brush.horizontalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, IlloWhite.copy(alpha = 0.18f), androidx.compose.ui.graphics.Color.Transparent), startX = left, endX = right), start = Offset(left, ly), end = Offset(right, ly), strokeWidth = 12.dp.toPx())
                val cc = IlloWhite.copy(alpha = cornerAlpha)
                drawLine(cc, Offset(left, top + cLen), Offset(left, top), cStroke, StrokeCap.Round)
                drawLine(cc, Offset(left, top), Offset(left + cLen, top), cStroke, StrokeCap.Round)
                drawLine(cc, Offset(right - cLen, top), Offset(right, top), cStroke, StrokeCap.Round)
                drawLine(cc, Offset(right, top), Offset(right, top + cLen), cStroke, StrokeCap.Round)
                drawLine(cc, Offset(left, bottom - cLen), Offset(left, bottom), cStroke, StrokeCap.Round)
                drawLine(cc, Offset(left, bottom), Offset(left + cLen, bottom), cStroke, StrokeCap.Round)
                drawLine(cc, Offset(right - cLen, bottom), Offset(right, bottom), cStroke, StrokeCap.Round)
                drawLine(cc, Offset(right, bottom), Offset(right, bottom - cLen), cStroke, StrokeCap.Round)
            }
            Badge(text = "SCANNING", color = GreenAccent, alpha = 0.15f, small = true, modifier = Modifier.align(Alignment.TopEnd))
        }
        Spacer(Modifier.height(16.dp))
        ResultRows()
    }
}

@Composable
private fun Slide3Family() {
    val inf = rememberInfiniteTransition(label = "s3")
    val travel by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "travel")
    val ringPulse by inf.animateFloat(0.6f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ring")
    Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val orbitR = 86.dp.toPx()
            val satellites = listOf(315f, 45f, 225f, 135f).map { a ->
                val rad = a * PI.toFloat() / 180f
                Offset(cx + orbitR * cos(rad), cy + orbitR * sin(rad))
            }
            drawCircle(IlloWhite.copy(alpha = 0.10f), orbitR, Offset(cx, cy), style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
            satellites.forEach { sat ->
                val dx = sat.x - cx
                val dy = sat.y - cy
                drawLine(brush = Brush.linearGradient(listOf(IlloWhite.copy(alpha = 0.20f), IlloWhite.copy(alpha = 0.04f)), start = Offset(cx, cy), end = sat), start = Offset(cx, cy), end = sat, strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
                drawCircle(IlloWhite.copy(alpha = ((1f - travel) * 0.7f + 0.15f)), 3.dp.toPx(), Offset(cx + dx * travel, cy + dy * travel))
            }
            satellites.forEach { pos -> drawPersonNode(pos, ringPulse) }
            drawCircle(IlloWhite.copy(alpha = 0.07f), 34.dp.toPx(), Offset(cx, cy))
            drawCircle(IlloWhite.copy(alpha = ringPulse * 0.4f), 34.dp.toPx(), Offset(cx, cy), style = Stroke(1.5.dp.toPx()))
            drawShieldSmall(cx, cy, ringPulse)
            val adx = cx + 20.dp.toPx()
            val ady = cy - 20.dp.toPx()
            drawCircle(GreenAccent.copy(alpha = 0.3f), 8.dp.toPx(), Offset(adx, ady))
            drawCircle(GreenAccent, 4.dp.toPx(), Offset(adx, ady))
        }
        Badge(text = "ALERT SENT ✓", color = GreenAccent, alpha = 0.10f, modifier = Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun Slide4Score() {
    val scoreProgress by animateFloatAsState(750f / 900f, tween(1600, easing = FastOutSlowInEasing), label = "scoreArc")
    val inf = rememberInfiniteTransition(label = "s4")
    val dotPulse by inf.animateFloat(0.5f, 1f, infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "dotPulse")
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(width = 200.dp, height = 140.dp)) {
                val cx = size.width / 2f
                val cy = size.height * 0.60f
                val arcRx = size.width * 0.42f
                val arcRy = size.height * 0.72f
                val strokeW = 8.dp.toPx()
                val startAng = 210f
                val totalSw = 120f
                val aLeft = cx - arcRx
                val aTop = cy - arcRy
                val aSize = Size(arcRx * 2f, arcRy * 2f)
                drawArc(IlloWhite.copy(alpha = 0.08f), startAng, totalSw, false, Offset(aLeft, aTop), aSize, style = Stroke(strokeW, cap = StrokeCap.Round))
                val sweep = scoreProgress * totalSw
                drawArc(brush = Brush.sweepGradient(listOf(IlloWhite.copy(alpha = 0.3f), IlloWhite, IlloWhite), center = Offset(cx, cy)), startAngle = startAng, sweepAngle = sweep, useCenter = false, topLeft = Offset(aLeft, aTop), size = aSize, style = Stroke(strokeW, cap = StrokeCap.Round))
                val endRad = (startAng + sweep) * PI.toFloat() / 180f
                val dotX = cx + arcRx * cos(endRad)
                val dotY = cy + arcRy * sin(endRad)
                drawCircle(IlloWhite.copy(alpha = dotPulse * 0.35f), 11.dp.toPx(), Offset(dotX, dotY))
                drawCircle(IlloWhite.copy(alpha = dotPulse), 4.5.dp.toPx(), Offset(dotX, dotY))
                for (i in 0..8) {
                    val frac = i / 8f
                    val tRad = (startAng + frac * totalSw) * PI.toFloat() / 180f
                    val r1x = arcRx + 12.dp.toPx()
                    val r1y = arcRy + 12.dp.toPx()
                    val r2x = arcRx + 18.dp.toPx()
                    val r2y = arcRy + 18.dp.toPx()
                    val tx1 = cx + r1x * cos(tRad)
                    val ty1 = cy + r1y * sin(tRad)
                    val tx2 = cx + r2x * cos(tRad)
                    val ty2 = cy + r2y * sin(tRad)
                    drawLine(if (frac <= scoreProgress) IlloWhite.copy(alpha = 0.55f) else IlloWhite.copy(alpha = 0.10f), start = Offset(tx1, ty1), end = Offset(tx2, ty2), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center).padding(bottom = 8.dp)) {
                Text("750", fontFamily = SyneFamily, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp, letterSpacing = (-2).sp, color = TextPri)
                Text("/ 900", fontFamily = DmSansFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = TextPri.copy(alpha = 0.30f))
            }
        }
        Text("CYBER SCORE", fontFamily = SyneFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 2.5.sp, color = GreenAccent.copy(alpha = 0.50f))
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(IlloWhite.copy(alpha = 0.04f)).border(1.dp, Brush.horizontalGradient(listOf(IlloWhite.copy(alpha = 0.14f), IlloWhite.copy(alpha = 0.04f))), RoundedCornerShape(12.dp)).padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Card Number", fontFamily = DmSansFamily, fontSize = 10.sp, color = TextPri.copy(alpha = 0.28f))
                Text("CC08 · K38 · 56B", fontFamily = SyneFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPri.copy(alpha = 0.85f))
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Status", fontFamily = DmSansFamily, fontSize = 10.sp, color = TextPri.copy(alpha = 0.28f))
                Text("EXCELLENT", fontFamily = SyneFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GreenAccent.copy(alpha = 0.85f))
            }
        }
    }
}

@Composable
private fun ResultRows() {
    listOf(
        Triple("QR Code", "Safe ✓", true),
        Triple("Deepfake Audio", "Clean ✓", true),
        Triple("Email Link", "Suspicious", false),
    ).forEach { (label, value, safe) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IlloWhite.copy(alpha = 0.03f))
                .border(1.dp, IlloWhite.copy(alpha = 0.07f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontFamily = DmSansFamily, fontSize = 12.sp, color = TextPri.copy(alpha = 0.55f))
            Text(value, fontFamily = DmSansFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = if (safe) GreenAccent.copy(alpha = 0.9f) else WarnAmber.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun Badge(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    alpha: Float,
    small: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(end = 8.dp, top = if (small) 0.dp else 12.dp)
            .clip(RoundedCornerShape(if (small) 6.dp else 8.dp))
            .background(color.copy(alpha = alpha))
            .border(1.dp, color.copy(alpha = if (small) 0.40f else 0.30f), RoundedCornerShape(if (small) 6.dp else 8.dp))
            .padding(horizontal = if (small) 8.dp else 10.dp, vertical = if (small) 4.dp else 5.dp)
    ) {
        Text(text, fontFamily = SyneFamily, fontWeight = FontWeight.Bold, fontSize = 8.sp, letterSpacing = if (small) 1.5.sp else 0.8.sp, color = color)
    }
}

private fun DrawScope.drawShieldSmall(cx: Float, cy: Float, glow: Float) {
    val sw = 28.dp.toPx()
    val sh = 32.dp.toPx()
    val sl = cx - sw / 2f
    val st = cy - sh * 0.48f
    val path = Path().apply {
        moveTo(cx, st)
        lineTo(sl + sw, st + sh * 0.18f)
        lineTo(sl + sw, st + sh * 0.62f)
        cubicTo(sl + sw, st + sh * 0.86f, cx, st + sh, cx, st + sh)
        cubicTo(cx, st + sh, sl, st + sh * 0.86f, sl, st + sh * 0.62f)
        lineTo(sl, st + sh * 0.18f)
        close()
    }
    drawPath(path, IlloWhite.copy(alpha = glow * 0.75f), style = Stroke(1.5.dp.toPx(), join = StrokeJoin.Round))
    val ck = Path().apply {
        moveTo(cx - 5.dp.toPx(), cy + 1.dp.toPx())
        lineTo(cx - 1.dp.toPx(), cy + 5.dp.toPx())
        lineTo(cx + 7.dp.toPx(), cy - 4.dp.toPx())
    }
    drawPath(ck, IlloWhite.copy(alpha = glow), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawPersonNode(pos: Offset, pulse: Float) {
    val nr = 20.dp.toPx()
    drawCircle(IlloFaint, nr, pos)
    drawCircle(IlloWhite.copy(alpha = pulse * 0.28f), nr, pos, style = Stroke(1.dp.toPx()))
    drawCircle(IlloWhite.copy(alpha = 0.55f), 6.dp.toPx(), Offset(pos.x, pos.y - 6.dp.toPx()), style = Stroke(1.4.dp.toPx()))
    val shoulders = Path().apply {
        moveTo(pos.x - 9.dp.toPx(), pos.y + 12.dp.toPx())
        cubicTo(pos.x - 9.dp.toPx(), pos.y + 1.dp.toPx(), pos.x + 9.dp.toPx(), pos.y + 1.dp.toPx(), pos.x + 9.dp.toPx(), pos.y + 12.dp.toPx())
    }
    drawPath(shoulders, IlloWhite.copy(alpha = 0.55f), style = Stroke(1.4.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawQrCells(left: Float, top: Float, width: Float, height: Float) {
    val cols = 9
    val rows = 7
    val cw = width / cols
    val ch = height / rows
    val litCells = setOf(
        0 to 0, 1 to 0, 2 to 0, 0 to 1, 2 to 1, 0 to 2, 1 to 2, 2 to 2,
        6 to 0, 7 to 0, 8 to 0, 6 to 1, 8 to 1, 6 to 2, 7 to 2, 8 to 2,
        0 to 4, 1 to 4, 2 to 4, 0 to 5, 2 to 5, 0 to 6, 1 to 6, 2 to 6,
        4 to 1, 5 to 2, 4 to 3, 7 to 3, 5 to 4, 4 to 5, 6 to 6, 8 to 3,
        3 to 0, 5 to 0, 3 to 3, 8 to 5, 3 to 5, 7 to 5, 8 to 6
    )
    for (c in 0 until cols) {
        for (r in 0 until rows) {
            val alpha = if ((c to r) in litCells) 0.55f else 0.09f
            drawRoundRect(IlloWhite.copy(alpha = alpha), topLeft = Offset(left + c * cw + 1.5f, top + r * ch + 1.5f), size = Size(cw - 3f, ch - 3f), cornerRadius = CornerRadius(2.dp.toPx()))
        }
    }
}
