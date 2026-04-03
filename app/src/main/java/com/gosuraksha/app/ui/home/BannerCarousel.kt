package com.gosuraksha.app.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

private const val BannerAutoScrollMs = 4_000L

@Composable
fun BannerCarousel(
    items: List<BannerItem>,
    onBannerClick: (BannerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val actualCount = items.size
    val virtualCount = if (actualCount > 1) Int.MAX_VALUE else 1
    val initialPage = remember(actualCount) {
        if (actualCount == 1) {
            0
        } else {
            val midpoint = virtualCount / 2
            midpoint - midpoint % actualCount
        }
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { virtualCount }
    )

    var isPointerDown by remember { mutableStateOf(false) }
    val currentIndex = if (actualCount == 0) 0 else pagerState.currentPage % actualCount

    LaunchedEffect(actualCount, pagerState, isPointerDown) {
        if (actualCount <= 1) return@LaunchedEffect

        while (true) {
            delay(BannerAutoScrollMs)
            if (!isPointerDown && !pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 700)
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
                .clip(RoundedCornerShape(24.dp))
                .bannerPointerTracker(
                    onPressStateChanged = { isPressed ->
                        isPointerDown = isPressed
                    }
                )
        ) { page ->
            val item = items[page % actualCount]
            BannerCard(
                item = item,
                onClick = {
                    onBannerClick(item.toAction())
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(actualCount) { index ->
                BannerIndicatorDot(
                    isActive = index == currentIndex
                )
                if (index < actualCount - 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }
    }
}

@Composable
fun BannerCard(
    item: BannerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = bannerPalette(item)
    val icon = bannerIcon(item)
    val eyebrow = when (item) {
        is BannerItem.NewsBanner -> item.category.ifBlank { "Security Update" }
        is BannerItem.FeatureBanner -> "Go Suraksha"
    }
    val title = when (item) {
        is BannerItem.NewsBanner -> item.title
        is BannerItem.FeatureBanner -> item.title
    }
    val subtitle = when (item) {
        is BannerItem.NewsBanner -> "Stay ahead of the latest cyber threat patterns."
        is BannerItem.FeatureBanner -> item.subtitle
    }
    val ctaLabel = when (item) {
        is BannerItem.NewsBanner -> "Read More"
        is BannerItem.FeatureBanner -> featureCta(item.action)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = palette.primary,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(palette.primary, palette.secondary)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(124.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                if (item is BannerItem.NewsBanner && !item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.05f),
                                        Color.Black.copy(alpha = 0.25f)
                                    )
                                )
                            )
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(52.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 16.dp)
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.72f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = ctaLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerIndicatorDot(
    isActive: Boolean
) {
    val width by animateDpAsState(
        targetValue = if (isActive) 20.dp else 8.dp,
        animationSpec = tween(durationMillis = 250),
        label = "banner_indicator_width"
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(8.dp)
            .clip(CircleShape)
            .background(
                if (isActive) Color(0xFF22C55E) else Color(0x3322C55E)
            )
    )
}

private fun BannerItem.toAction(): BannerAction {
    return when (this) {
        is BannerItem.NewsBanner -> BannerAction.OPEN_NEWS
        is BannerItem.FeatureBanner -> action
    }
}

private fun bannerIcon(item: BannerItem): ImageVector {
    return when (item) {
        is BannerItem.NewsBanner -> Icons.Rounded.Campaign
        is BannerItem.FeatureBanner -> when (item.action) {
            BannerAction.OPEN_QR_SCAN -> Icons.Rounded.QrCode2
            BannerAction.OPEN_THREAT_SCAN -> Icons.Rounded.NotificationsActive
            BannerAction.OPEN_CYBER_SOS -> Icons.Rounded.Shield
            BannerAction.OPEN_RISK_SCORE -> Icons.Rounded.Star
            BannerAction.OPEN_FAMILY -> Icons.Rounded.FamilyRestroom
            BannerAction.OPEN_NEWS -> Icons.Rounded.Campaign
        }
    }
}

private fun featureCta(action: BannerAction): String {
    return when (action) {
        BannerAction.OPEN_QR_SCAN -> "Scan Now"
        BannerAction.OPEN_THREAT_SCAN -> "Check Now"
        BannerAction.OPEN_CYBER_SOS -> "Open SOS"
        BannerAction.OPEN_RISK_SCORE -> "View Score"
        BannerAction.OPEN_FAMILY -> "Protect Family"
        BannerAction.OPEN_NEWS -> "Read More"
    }
}

private fun bannerPalette(item: BannerItem): BannerPalette {
    return when (item) {
        is BannerItem.NewsBanner -> BannerPalette(
            primary = Color(0xFF102A43),
            secondary = Color(0xFF0B6E4F)
        )
        is BannerItem.FeatureBanner -> when (item.action) {
            BannerAction.OPEN_QR_SCAN -> BannerPalette(
                primary = Color(0xFF0B132B),
                secondary = Color(0xFF1C7293)
            )
            BannerAction.OPEN_THREAT_SCAN -> BannerPalette(
                primary = Color(0xFF3B1F2B),
                secondary = Color(0xFFB23A48)
            )
            BannerAction.OPEN_CYBER_SOS -> BannerPalette(
                primary = Color(0xFF381D2A),
                secondary = Color(0xFFD64550)
            )
            BannerAction.OPEN_RISK_SCORE -> BannerPalette(
                primary = Color(0xFF172A3A),
                secondary = Color(0xFF0FA3B1)
            )
            BannerAction.OPEN_FAMILY -> BannerPalette(
                primary = Color(0xFF1F2937),
                secondary = Color(0xFF2E8B57)
            )
            BannerAction.OPEN_NEWS -> BannerPalette(
                primary = Color(0xFF102A43),
                secondary = Color(0xFF0B6E4F)
            )
        }
    }
}

private data class BannerPalette(
    val primary: Color,
    val secondary: Color
)

private fun Modifier.bannerPointerTracker(
    onPressStateChanged: (Boolean) -> Unit
): Modifier {
    return pointerInput(Unit) {
        awaitEachGesture {
            onPressStateChanged(true)
            awaitFirstDown(requireUnconsumed = false)
            waitForUpOrCancellation()
            onPressStateChanged(false)
        }
    }
}
