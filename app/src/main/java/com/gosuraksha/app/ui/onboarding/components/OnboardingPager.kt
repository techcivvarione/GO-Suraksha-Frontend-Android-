package com.gosuraksha.app.ui.onboarding.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gosuraksha.app.ui.onboarding.model.OnboardingSlide

@Composable
fun OnboardingPager(
    pagerState: PagerState,
    slides: List<OnboardingSlide>,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(state = pagerState, modifier = modifier.fillMaxWidth()) { page ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            OnboardingPage(page = page, slide = slides[page])
        }
    }
}
