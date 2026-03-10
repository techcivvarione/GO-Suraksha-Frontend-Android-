package com.gosuraksha.app.ui.home
import com.gosuraksha.app.design.tokens.ColorTokens

// =============================================================================
// BannerCarousel.kt — UI-ONLY reusable composable
// FIXED: theme-aware dot colors, elevation vs border logic.
// Auto-scroll 4s, pause on drag, animated pill dots.
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
import com.gosuraksha.app.design.tokens.SpacingTokens
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

    // Auto-scroll — LaunchedEffect scoped to pagerState, no leak
    LaunchedEffect(pagerState) {
        while (true) {
            delay(4000L)
            if (!isDragging) {
                val next = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(
                    page = next,
                    animationSpec = spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow)
                )
            }
        }
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
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

        // Pagination dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isActive = pagerState.currentPage == index
                PaginationDot(
                    isActive = isActive,
                    isDark = isDark,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = index,
                                animationSpec = spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow)
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
            .height(176.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(interactionSource = interactionSource, indication = null,
                onClick = banner.onClick),
        shape = RoundedCornerShape(22.dp),
        // Dark: elevation only. Light: slight border to separate from white bg.
        shadowElevation = if (isDark) 6.dp else 3.dp,
        tonalElevation = 0.dp,
        color = banner.gradientStart
    ) {
        Box(Modifier.fillMaxSize()) {
            // Gradient layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(banner.gradientStart, banner.gradientEnd),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            )
            // Illustration layer — Canvas, 6% opacity, no recompose cost
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind { drawBannerIllustration(banner.illustrationType) }
            )
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = banner.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 26.sp
                    )
                    Text(
                        text = banner.subtitle,
                        fontSize = 13.sp,
                        color = Color(0xCCFFFFFF),
                        lineHeight = 18.sp
                    )
                }
                // Frosted CTA pill
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${banner.ctaLabel}  →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PaginationDot(isActive: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val width by animateDpAsState(
        targetValue = if (isActive) 20.dp else 6.dp,
        animationSpec = tween(300),
        label = "dot_width"
    )
    val activeColor = Color(0xFF3B6FD4)
    val inactiveColor = if (isDark) Color(0xFF7A84A0).copy(alpha = 0.35f)
    else Color(0xFF94A3B8).copy(alpha = 0.50f)

    Box(
        modifier = Modifier
            .width(width)
            .height(6.dp)
            .background(if (isActive) activeColor else inactiveColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
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
                size = Size(size.width * 0.22f, size.height * 0.35f))
        }
        BannerIllustration.Scanner -> {
            val stripeH = size.height / 15f
            repeat(6) { i ->
                drawRect(c,
                    topLeft = Offset(size.width * 0.55f, i * size.height / 6f + size.height * 0.1f),
                    size = Size(size.width * 0.42f, stripeH))
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