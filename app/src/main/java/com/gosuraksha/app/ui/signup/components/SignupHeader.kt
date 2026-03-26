package com.gosuraksha.app.ui.signup.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.ui.signup.model.SignupGreen400
import com.gosuraksha.app.ui.signup.model.SignupHeroBg

@Composable
fun SignupHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(SignupHeroBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawSignupPattern() }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
        ) {
            Text("GO Suraksha · Security", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp, color = SignupGreen400.copy(alpha = 0.55f))
            Spacer(Modifier.height(5.dp))
            Text("Create Your\nSecure Account", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 25.sp, letterSpacing = (-0.4).sp, color = Color.White)
        }
    }
}

private fun DrawScope.drawSignupPattern() {
    val w = size.width
    val h = size.height
    val sp = 22.dp.toPx()
    val dotColor = Color(0xFF2A6640)
    val traceColor = Color(0xFF2A6640)
    val nodeColor = Color(0xFF3A7A50)
    val iconColor = Color(0xFF3A7A50)
    val traceW = 0.7.dp.toPx()
    var gy = sp / 2f
    while (gy < h) {
        var gx = sp / 2f
        while (gx < w) {
            drawCircle(dotColor, radius = 1.3.dp.toPx(), center = Offset(gx, gy))
            gx += sp
        }
        gy += sp
    }
    fun seg(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(traceColor, Offset(x1, y1), Offset(x2, y2), traceW)
    val g = sp
    seg(2 * g, .5f * g, 2 * g, 2.5f * g); seg(2 * g, 2.5f * g, 4.5f * g, 2.5f * g)
    seg(4.5f * g, 2.5f * g, 6.5f * g, 2.5f * g); seg(6.5f * g, 2.5f * g, 6.5f * g, 1.5f * g)
    seg(9.5f * g, .5f * g, 9.5f * g, 1.5f * g); seg(9.5f * g, 1.5f * g, 11.5f * g, 1.5f * g); seg(11.5f * g, 1.5f * g, 11.5f * g, .5f * g)
    seg(.5f * g, 3.5f * g, 2.5f * g, 3.5f * g); seg(2.5f * g, 3.5f * g, 2.5f * g, 4.5f * g); seg(2.5f * g, 4.5f * g, 4.5f * g, 4.5f * g); seg(4.5f * g, 4.5f * g, 4.5f * g, 3.5f * g)
    seg(9.5f * g, 3.5f * g, 11.5f * g, 3.5f * g); seg(11.5f * g, 3.5f * g, 11.5f * g, 4.5f * g)
    seg(.5f * g, 5.5f * g, 3.5f * g, 5.5f * g); seg(3.5f * g, 5.5f * g, 3.5f * g, 6.5f * g)
    seg(6.5f * g, 4.5f * g, 6.5f * g, 5.5f * g); seg(6.5f * g, 5.5f * g, 8.5f * g, 5.5f * g); seg(8.5f * g, 5.5f * g, 8.5f * g, 4.5f * g)
    fun node(x: Float, y: Float, r: Float = 2.2.dp.toPx()) = drawCircle(nodeColor, radius = r, center = Offset(x, y))
    node(2 * g, 2.5f * g); node(4.5f * g, 2.5f * g, 1.8.dp.toPx()); node(11.5f * g, 1.5f * g)
    node(2.5f * g, 4.5f * g, 1.8.dp.toPx()); node(4.5f * g, 4.5f * g); node(6.5f * g, 4.5f * g, 1.8.dp.toPx())
    val iconStroke = Stroke(1.1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val cx = w * 0.52f
    val cy = h * 0.40f
    val sw = 24.dp.toPx()
    val sh = 27.dp.toPx()
    drawPath(Path().apply {
        moveTo(cx, cy - sh / 2f)
        lineTo(cx + sw / 2f, cy - sh / 2f + sh * 0.15f)
        lineTo(cx + sw / 2f, cy - sh / 2f + sh * 0.65f)
        quadraticBezierTo(cx + sw / 2f, cy + sh / 2f, cx, cy + sh / 2f)
        quadraticBezierTo(cx - sw / 2f, cy + sh / 2f, cx - sw / 2f, cy - sh / 2f + sh * 0.65f)
        lineTo(cx - sw / 2f, cy - sh / 2f + sh * 0.15f)
        close()
    }, iconColor, style = iconStroke)
    val ck = 4.dp.toPx()
    drawPath(Path().apply {
        moveTo(cx - ck * 1.5f, cy + 1.dp.toPx())
        lineTo(cx - ck * 0.3f, cy + ck)
        lineTo(cx + ck * 1.5f, cy - ck * 0.8f)
    }, iconColor, style = iconStroke)
    val lx = w - 44.dp.toPx()
    val ly = h * 0.28f
    val lw = 16.dp.toPx()
    val lh = 12.dp.toPx()
    val lTop = ly + 8.dp.toPx()
    drawArc(iconColor, 180f, 180f, false, Offset(lx - lw / 2f + 3.dp.toPx(), ly - 5.dp.toPx()), Size(lw - 6.dp.toPx(), 10.dp.toPx()), style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))
    drawRoundRect(iconColor, Offset(lx - lw / 2f, lTop), Size(lw, lh), CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawCircle(iconColor, 1.6.dp.toPx(), Offset(lx, lTop + lh / 2f))
}
