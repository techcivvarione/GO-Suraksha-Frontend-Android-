package com.gosuraksha.app.ui.onboarding

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.ui.onboarding.components.OnboardingFooter
import com.gosuraksha.app.ui.onboarding.components.OnboardingIndicators
import com.gosuraksha.app.ui.onboarding.components.OnboardingPager
import com.gosuraksha.app.ui.onboarding.model.BgColor
import com.gosuraksha.app.ui.onboarding.model.DmSansFamily
import com.gosuraksha.app.ui.onboarding.model.GreenAccent
import com.gosuraksha.app.ui.onboarding.model.IlloWhite
import com.gosuraksha.app.ui.onboarding.model.SyneFamily
import com.gosuraksha.app.ui.onboarding.model.TextPri
import com.gosuraksha.app.ui.onboarding.model.onboardingSlides
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IntroOnboardingScreen(onComplete: () -> Unit) {
    val activity = LocalContext.current as? Activity
    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })
    var autoSlide by remember { mutableStateOf(true) }
    var programmatic by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(autoSlide, pagerState.currentPage) {
        if (!autoSlide || pagerState.currentPage >= onboardingSlides.lastIndex) return@LaunchedEffect
        delay(3500)
        programmatic = true
        pagerState.animateScrollToPage(
            page = pagerState.currentPage + 1,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
        programmatic = false
    }

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress && !programmatic) autoSlide = false
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 28.dp.toPx()
            val dotR = 1.dp.toPx()
            val cols = (size.width / spacing).toInt() + 2
            val rows = (size.height / spacing).toInt() + 2
            for (r in 0..rows) {
                for (c in 0..cols) {
                    drawCircle(IlloWhite.copy(alpha = 0.06f), radius = dotR, center = Offset(c * spacing, r * spacing))
                }
            }
        }

        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .drawBehind {
                    drawCircle(Brush.radialGradient(listOf(GreenAccent.copy(alpha = 0.07f), Color.Transparent)))
                }
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(52.dp))
            OnboardingTopBar(
                onSkip = {
                    autoSlide = false
                    scope.launch {
                        programmatic = true
                        pagerState.animateScrollToPage(onboardingSlides.lastIndex)
                        programmatic = false
                    }
                }
            )
            OnboardingPager(pagerState = pagerState, slides = onboardingSlides, modifier = Modifier.weight(1f))
            Spacer(Modifier.height(14.dp))
            OnboardingIndicators(count = onboardingSlides.size, currentPage = pagerState.currentPage)
            Spacer(Modifier.height(18.dp))
            OnboardingFooter(
                slide = onboardingSlides[pagerState.currentPage],
                isLast = pagerState.currentPage == onboardingSlides.lastIndex,
                pageKey = pagerState.currentPage,
                onCtaClick = {
                    if (pagerState.currentPage == onboardingSlides.lastIndex) {
                        onComplete()
                    } else {
                        autoSlide = false
                        scope.launch {
                            programmatic = true
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = tween(500, easing = FastOutSlowInEasing)
                            )
                            programmatic = false
                        }
                    }
                }
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun OnboardingTopBar(
    onSkip: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(GreenAccent)
                    .drawBehind { drawCircle(GreenAccent.copy(alpha = 0.4f), size.minDimension * 1.5f) }
            )
            Text(
                text = "GO SURAKSHA",
                fontFamily = SyneFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp,
                color = TextPri.copy(alpha = 0.22f)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(remember { MutableInteractionSource() }, null, onClick = onSkip)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                "Skip",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = TextPri.copy(alpha = 0.25f)
            )
        }
    }
}
