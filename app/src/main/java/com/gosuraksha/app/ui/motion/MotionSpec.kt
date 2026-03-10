package com.gosuraksha.app.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object MotionSpec {

    // Fintech-smooth easing (matches the video feel)
    val easeOutSoft = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)

    // Global motion tokens
    const val FAST_MS = 120
    const val NORMAL_MS = 220
    const val SLOW_MS = 350

    val fastTween = tween<Float>(
        durationMillis = FAST_MS,
        easing = easeOutSoft
    )
    val normalTween = tween<Float>(
        durationMillis = NORMAL_MS,
        easing = easeOutSoft
    )
    val slowTween = tween<Float>(
        durationMillis = SLOW_MS,
        easing = easeOutSoft
    )

    // Entry (shield slide-in)
    const val ENTRY_SLIDE_DISTANCE = 120f
    const val ENTRY_DURATION_MS = 520

    val entryTween = tween<Float>(
        durationMillis = ENTRY_DURATION_MS,
        easing = easeOutSoft
    )

    // Card settle (micro-bounce, very subtle)
    val cardSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Fade timing
    const val FADE_DURATION_MS = 240
}
