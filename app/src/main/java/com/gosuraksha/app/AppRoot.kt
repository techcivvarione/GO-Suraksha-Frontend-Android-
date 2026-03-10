package com.gosuraksha.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.gosuraksha.app.core.LanguageManager
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.navigation.AppNavGraph

@Composable
fun AppRoot() {
    LanguageManager {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ColorTokens.background()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppAmbientGlow()
                AppNavGraph()
            }
        }
    }
}

@Composable
private fun AppAmbientGlow() {
    val transition = rememberInfiniteTransition(label = "app_ambient_glow")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing)
        ),
        label = "app_ambient_glow_drift"
    )
    val accent = ColorTokens.accent()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = 0.035f),
                    accent.copy(alpha = 0f)
                ),
                center = Offset(x = w * 0.15f + drift * 0.03f, y = h * 0.12f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(x = w * 0.15f + drift * 0.03f, y = h * 0.12f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = 0.025f),
                    accent.copy(alpha = 0f)
                ),
                center = Offset(x = w * 0.85f - drift * 0.02f, y = h * 0.82f),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(x = w * 0.85f - drift * 0.02f, y = h * 0.82f)
        )
    }
}
