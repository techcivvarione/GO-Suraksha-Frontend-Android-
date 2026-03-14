package com.gosuraksha.app.ui.home

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    var isDragging = false

    LaunchedEffect(pagerState, banners.size) {
        while (true) {
            delay(6_000L)
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
                .height(180.dp)
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
            .height(180.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = banner.onClick),
        shape = RoundedCornerShape(22.dp),
        shadowElevation = if (isDark) 6.dp else 3.dp,
        tonalElevation = 0.dp,
        color = banner.gradientStart
    ) {
        Box(Modifier.fillMaxSize()) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind { drawBannerIllustration(banner.illustrationType) }
            )
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
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${banner.ctaLabel}  ->",
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
    val alpha = 0.08f
    when (type) {
        BannerIllustration.Lock -> {
            drawRoundRect(
                color = Color.White.copy(alpha = alpha),
                topLeft = Offset(size.width * 0.74f, size.height * 0.20f),
                size = Size(size.width * 0.16f, size.height * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(26f, 26f)
            )
        }
        BannerIllustration.Scanner -> {
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = size.minDimension * 0.18f,
                center = Offset(size.width * 0.80f, size.height * 0.42f)
            )
        }
        BannerIllustration.Diamond -> {
            val w = size.width
            val h = size.height
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.82f, h * 0.18f)
                lineTo(w * 0.90f, h * 0.34f)
                lineTo(w * 0.82f, h * 0.50f)
                lineTo(w * 0.74f, h * 0.34f)
                close()
            }
            drawPath(path, Color.White.copy(alpha = alpha))
        }
    }
}
