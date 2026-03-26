package com.gosuraksha.app.ui.onboarding.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.ui.onboarding.model.DmSansFamily
import com.gosuraksha.app.ui.onboarding.model.GreenAccent
import com.gosuraksha.app.ui.onboarding.model.GreenMid
import com.gosuraksha.app.ui.onboarding.model.OnboardingSlide
import com.gosuraksha.app.ui.onboarding.model.SyneFamily
import com.gosuraksha.app.ui.onboarding.model.TextPri
import com.gosuraksha.app.ui.onboarding.model.TextSec

@Composable
fun OnboardingFooter(
    slide: OnboardingSlide,
    isLast: Boolean,
    pageKey: Int,
    onCtaClick: () -> Unit,
) {
    AnimatedContent(
        targetState = pageKey,
        transitionSpec = {
            (fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 8 })
                .togetherWith(fadeOut(tween(160)))
        },
        label = "slideText"
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = slide.tag.uppercase(),
                fontFamily = SyneFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                letterSpacing = 2.2.sp,
                color = GreenAccent.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = slide.titleLine1,
                fontFamily = SyneFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.5).sp,
                color = TextPri
            )
            Text(
                text = slide.titleLine2,
                fontFamily = SyneFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.5).sp,
                color = GreenAccent
            )
            Spacer(Modifier.height(9.dp))
            Text(
                text = slide.description,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = TextSec
            )
        }
    }

    Spacer(Modifier.height(18.dp))

    val ctaBorderColor by animateColorAsState(
        if (isLast) Color.Transparent else GreenAccent.copy(alpha = 0.28f),
        tween(280),
        label = "ctaBorder"
    )
    val ctaTextColor by animateColorAsState(
        if (isLast) Color(0xFF041008) else GreenAccent,
        tween(280),
        label = "ctaText"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isLast) Brush.horizontalGradient(listOf(GreenMid, GreenAccent))
                else Brush.horizontalGradient(listOf(GreenAccent.copy(alpha = 0.08f), GreenAccent.copy(alpha = 0.08f)))
            )
            .border(1.dp, ctaBorderColor, RoundedCornerShape(14.dp))
            .clickable(remember { MutableInteractionSource() }, null, onClick = onCtaClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isLast) "Get Started" else "Next  →",
            fontFamily = SyneFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 0.3.sp,
            color = ctaTextColor
        )
    }
}
