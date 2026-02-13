package com.gosuraksha.app.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object MotionSpec {

    // Fintech-smooth easing (matches the video feel)
    val easeOutSoft = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)

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
