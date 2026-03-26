package com.gosuraksha.app.ui.qr.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.scan.design.ScanTheme

@Composable
fun ScannerOverlay(modifier: Modifier = Modifier, animateLine: Boolean) {
    val colors = ScanTheme.colors
    val transition = rememberInfiniteTransition(label = "qr_scan_line")
    val lineProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 2400, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "qr_scan_line_progress"
    )
    Box(modifier = modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.Black.copy(alpha = 0.40f))
            val fw = size.width * 0.72f
            val fh = size.height * 0.30f
            val left = (size.width - fw) / 2f
            val top = (size.height - fh) / 2f
            val right = left + fw
            val bottom = top + fh
            drawRoundRect(color = Color.Transparent, topLeft = androidx.compose.ui.geometry.Offset(left, top), size = androidx.compose.ui.geometry.Size(fw, fh), cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f, 40f), blendMode = BlendMode.Clear)
            val path = Path().apply {
                addRoundRect(androidx.compose.ui.geometry.RoundRect(left = left, top = top, right = right, bottom = bottom, radiusX = 40f, radiusY = 40f))
            }
            drawPath(path, color = colors.primaryBlue, style = Stroke(width = 4f))
            if (animateLine) {
                val y = top + (fh * lineProgress)
                drawLine(brush = Brush.horizontalGradient(listOf(Color.Transparent, colors.primaryBlue, Color.Transparent)), start = androidx.compose.ui.geometry.Offset(left + 16f, y), end = androidx.compose.ui.geometry.Offset(right - 16f, y), strokeWidth = 6f)
            }
        }
    }
}

@Composable
fun QrControlButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Surface(onClick = onClick, enabled = enabled, shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.12f), tonalElevation = 0.dp) {
        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
            content()
        }
    }
}
