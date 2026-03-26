package com.gosuraksha.app.ui.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.R
import androidx.compose.foundation.Image

@Composable
fun LoginHeader(isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(HeroBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawSecurityPattern() }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
        ) {
            Spacer(Modifier.height(5.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Go Suraksha logo",
                    modifier = Modifier.size(136.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

fun DrawScope.drawSecurityPattern() {
    val w = size.width
    val h = size.height
    val sp = 22.dp.toPx()
    val dotColor = Color(0xFF2A6640)
    val dotR = 1.3.dp.toPx()
    var gy = sp / 2f
    while (gy < h) {
        var gx = sp / 2f
        while (gx < w) {
            drawCircle(dotColor, radius = dotR, center = Offset(gx, gy))
            gx += sp
        }
        gy += sp
    }

    val traceColor = Color(0xFF2A6640)
    val traceW = 0.7.dp.toPx()
    fun seg(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(traceColor, Offset(x1, y1), Offset(x2, y2), traceW)
    val g = sp
    seg(2*g, .5f*g, 2*g, 2.5f*g); seg(2*g, 2.5f*g, 4.5f*g, 2.5f*g)
    seg(4.5f*g, 2.5f*g, 6.5f*g, 2.5f*g); seg(6.5f*g, 2.5f*g, 6.5f*g, 1.5f*g)
    seg(9.5f*g, .5f*g, 9.5f*g, 1.5f*g); seg(9.5f*g, 1.5f*g, 11.5f*g, 1.5f*g); seg(11.5f*g, 1.5f*g, 11.5f*g, .5f*g)
    seg(12.5f*g, .5f*g, 12.5f*g, 2.5f*g); seg(12.5f*g, 2.5f*g, 14.5f*g, 2.5f*g)
    seg(.5f*g, 3.5f*g, 2.5f*g, 3.5f*g); seg(2.5f*g, 3.5f*g, 2.5f*g, 4.5f*g); seg(2.5f*g, 4.5f*g, 4.5f*g, 4.5f*g); seg(4.5f*g, 4.5f*g, 4.5f*g, 3.5f*g); seg(4.5f*g, 3.5f*g, 6.5f*g, 3.5f*g)
    seg(9.5f*g, 3.5f*g, 11.5f*g, 3.5f*g); seg(11.5f*g, 3.5f*g, 11.5f*g, 4.5f*g); seg(11.5f*g, 4.5f*g, 13.5f*g, 4.5f*g)
    seg(.5f*g, 5.5f*g, 3.5f*g, 5.5f*g); seg(3.5f*g, 5.5f*g, 3.5f*g, 6.5f*g); seg(3.5f*g, 6.5f*g, 5.5f*g, 6.5f*g); seg(5.5f*g, 6.5f*g, 5.5f*g, 5.5f*g); seg(5.5f*g, 5.5f*g, 7.5f*g, 5.5f*g)
    seg(6.5f*g, 4.5f*g, 6.5f*g, 5.5f*g); seg(6.5f*g, 5.5f*g, 8.5f*g, 5.5f*g); seg(8.5f*g, 5.5f*g, 8.5f*g, 4.5f*g)
    seg(10.5f*g, 5.5f*g, 12.5f*g, 5.5f*g); seg(12.5f*g, 5.5f*g, 12.5f*g, 6.5f*g); seg(12.5f*g, 6.5f*g, 14.5f*g, 6.5f*g)

    val nodeColor = Color(0xFF3A7A50)
    fun node(x: Float, y: Float, r: Float = 2.2.dp.toPx()) = drawCircle(nodeColor, radius = r, center = Offset(x, y))
    node(2*g, 2.5f*g); node(4.5f*g, 2.5f*g, 1.8.dp.toPx()); node(6.5f*g, 2.5f*g, 1.8.dp.toPx())
    node(11.5f*g, 1.5f*g); node(2.5f*g, 4.5f*g, 1.8.dp.toPx()); node(4.5f*g, 4.5f*g)
    node(11.5f*g, 4.5f*g, 1.8.dp.toPx()); node(3.5f*g, 6.5f*g, 1.8.dp.toPx()); node(5.5f*g, 6.5f*g)
    node(6.5f*g, 4.5f*g, 1.8.dp.toPx()); node(8.5f*g, 4.5f*g, 1.8.dp.toPx()); node(12.5f*g, 6.5f*g, 1.8.dp.toPx())

    val iconColor = Color(0xFF3A7A50)
    val iconStroke = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round)
    val cx = w * 0.50f
    val cy = h * 0.42f
    val sw = 24.dp.toPx()
    val sh = 27.dp.toPx()
    val shield = Path().apply {
        moveTo(cx, cy - sh / 2f)
        lineTo(cx + sw / 2f, cy - sh / 2f + sh * 0.15f)
        lineTo(cx + sw / 2f, cy - sh / 2f + sh * 0.65f)
        quadraticBezierTo(cx + sw / 2f, cy + sh / 2f, cx, cy + sh / 2f)
        quadraticBezierTo(cx - sw / 2f, cy + sh / 2f, cx - sw / 2f, cy - sh / 2f + sh * 0.65f)
        lineTo(cx - sw / 2f, cy - sh / 2f + sh * 0.15f)
        close()
    }
    drawPath(shield, iconColor, style = iconStroke)
    val ck = 4.dp.toPx()
    drawPath(Path().apply {
        moveTo(cx - ck * 1.5f, cy + 1.dp.toPx())
        lineTo(cx - ck * 0.3f, cy + ck)
        lineTo(cx + ck * 1.5f, cy - ck * 0.8f)
    }, iconColor, style = iconStroke)
}
