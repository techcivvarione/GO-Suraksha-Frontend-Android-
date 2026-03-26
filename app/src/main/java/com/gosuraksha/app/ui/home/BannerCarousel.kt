package com.gosuraksha.app.ui.home

// =============================================================================
// BannerCarousel.kt — Go Suraksha  (updated for green brand system)
// Same API, same BannerData/BannerIllustration data classes.
// Only change: dot active color = #22C55E (brand green), height = 170dp.
// Auto-scroll 6s, pause on drag, animated pill dots.
// =============================================================================

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gosuraksha.app.design.tokens.ColorTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BannerData(
    val title: String,
    val subtitle: String,
    val ctaLabel: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val illustrationType: BannerIllustration,
    val onClick: () -> Unit
)

enum class BannerIllustration { Lock, Scanner, Diamond }

@Composable
fun BannerCarousel(
    banners: List<BannerData>,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    val isDark = ColorTokens.LocalAppDarkMode.current
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState) {
        while (true) {
            delay(6000L)
            if (!isDragging) {
                val next = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(
                    page = next,
                    animationSpec = spring(
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                )
            }
        }
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            isDragging = event.changes.any { it.pressed }
                        }
                    }
                }
        ) { page ->
            BannerCard(banner = banners[page], isDark = isDark)
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isActive = pagerState.currentPage == index
                BannerDot(
                    isActive = isActive,
                    isDark   = isDark,
                    onClick  = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = index,
                                animationSpec = spring(
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
                if (index < banners.size - 1) Spacer(Modifier.width(5.dp))
            }
        }
    }
}

@Composable
private fun BannerCard(banner: BannerData, isDark: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource,
                indication = null, onClick = banner.onClick),
        shape           = RoundedCornerShape(20.dp),
        shadowElevation = if (isDark) 6.dp else 3.dp,
        tonalElevation  = 0.dp,
        color           = banner.gradientStart
    ) {
        Box(Modifier.fillMaxSize()) {
            // Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(banner.gradientStart, banner.gradientEnd),
                            start  = Offset(0f, 0f),
                            end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            )
            // Illustration
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind { drawBannerIllustration(banner.illustrationType) }
            )
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text       = banner.title,
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White,
                        lineHeight = 22.sp,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text       = banner.subtitle,
                        fontSize   = 12.sp,
                        color      = Color.White.copy(alpha = 0.72f),
                        lineHeight = 17.sp
                    )
                }
                // CTA pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text       = banner.ctaLabel,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                    Text("→", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun BannerDot(isActive: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val width by animateDpAsState(
        targetValue   = if (isActive) 20.dp else 6.dp,
        animationSpec = tween(300),
        label         = "dot_width"
    )
    // Brand green dot — both dark and light mode
    val activeColor   = Color(0xFF22C55E)
    val inactiveColor = if (isDark)
        Color(0xFF2D5535).copy(alpha = 0.60f)
    else
        Color(0xFF6B9E78).copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .width(width)
            .height(6.dp)
            .background(if (isActive) activeColor else inactiveColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick    = onClick
            )
    )
}

private fun DrawScope.drawBannerIllustration(type: BannerIllustration) {
    val c = Color.White.copy(alpha = 0.06f)
    when (type) {
        BannerIllustration.Lock -> {
            drawCircle(c, radius = size.width * 0.38f,
                center = Offset(size.width * 0.88f, size.height * 0.18f))
            drawCircle(c, radius = size.width * 0.22f,
                center = Offset(size.width * 0.78f, size.height * 0.85f))
            drawRect(c,
                topLeft = Offset(size.width * 0.72f, size.height * 0.55f),
                size    = Size(size.width * 0.22f, size.height * 0.35f))
        }
        BannerIllustration.Scanner -> {
            val stripeH = size.height / 15f
            repeat(6) { i ->
                drawRect(c,
                    topLeft = Offset(size.width * 0.55f, i * size.height / 6f + size.height * 0.1f),
                    size    = Size(size.width * 0.42f, stripeH))
            }
            drawCircle(c, radius = size.width * 0.28f,
                center = Offset(size.width * 0.90f, size.height * 0.12f))
        }
        BannerIllustration.Diamond -> {
            drawCircle(c, radius = size.width * 0.32f,
                center = Offset(size.width * 0.85f, size.height * 0.35f))
            drawCircle(c, radius = size.width * 0.18f,
                center = Offset(size.width * 0.70f, size.height * 0.78f))
            drawCircle(c, radius = size.width * 0.10f,
                center = Offset(size.width * 0.60f, size.height * 0.20f))
        }
    }
}