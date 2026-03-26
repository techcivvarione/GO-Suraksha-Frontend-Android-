package com.gosuraksha.app.ui.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gosuraksha.app.ui.onboarding.model.DotOff
import com.gosuraksha.app.ui.onboarding.model.GreenAccent

@Composable
fun OnboardingIndicators(
    count: Int,
    currentPage: Int,
) {
    Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
        repeat(count) { i ->
            val active = currentPage == i
            val width by animateDpAsState(if (active) 22.dp else 6.dp, tween(280), label = "width_$i")
            val color by animateColorAsState(if (active) GreenAccent else DotOff, tween(280), label = "color_$i")
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .width(width)
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}
