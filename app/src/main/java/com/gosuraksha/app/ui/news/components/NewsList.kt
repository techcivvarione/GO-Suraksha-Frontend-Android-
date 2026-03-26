package com.gosuraksha.app.ui.news

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.gosuraksha.app.core.periodicTickFlow
import com.gosuraksha.app.news.model.NewsItem

@Composable
fun NewsList(
    items: List<NewsItem>,
    bookmarks: Set<String>,
    isDark: Boolean,
    onBookmark: (String) -> Unit,
    onShare: (NewsItem) -> Unit,
    onPageChanged: (Int) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(pagerState, items.size, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            periodicTickFlow(periodMillis = 6_000L).collect {
                if (items.isNotEmpty()) {
                    val next = (pagerState.currentPage + 1) % items.size
                    pagerState.animateScrollToPage(next)
                }
            }
        }
    }

    var previousPage by remember { mutableIntStateOf(pagerState.currentPage) }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != previousPage) {
            previousPage = pagerState.currentPage
            onPageChanged(pagerState.currentPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 22.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        val item = items[page]
        val articleId = articleIdFor(item)
        val isBookmarked = bookmarks.contains(articleId)
        val scale by animateFloatAsState(
            targetValue = if (page == pagerState.currentPage) 1f else 0.91f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "card_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (page == pagerState.currentPage) 1f else 0.72f,
            animationSpec = tween(200),
            label = "card_alpha"
        )
        val cardModifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }

        if (page == pagerState.currentPage) {
            FeaturedNewsCard(
                item = item,
                isBookmarked = isBookmarked,
                isDark = isDark,
                onBookmark = { onBookmark(articleId) },
                onShare = { onShare(item) },
                modifier = cardModifier
            )
        } else {
            NewsItemCard(
                item = item,
                isHero = false,
                isBookmarked = isBookmarked,
                isDark = isDark,
                onBookmark = { onBookmark(articleId) },
                onShare = { onShare(item) },
                modifier = cardModifier
            )
        }
    }

    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AuroraCapsuleIndicator(total = items.size, current = pagerState.currentPage)
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
fun AuroraCapsuleIndicator(total: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total.coerceAtMost(8)) { index ->
            val isSelected = index == current
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 7.dp,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                label = "dot_width"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) AuroraViolet else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                animationSpec = tween(200),
                label = "dot_color"
            )
            Box(
                modifier = Modifier
                    .height(7.dp)
                    .width(width)
                    .background(
                        if (isSelected) Brush.horizontalGradient(listOf(AuroraViolet, AuroraBlue))
                        else Brush.horizontalGradient(listOf(color, color)),
                        RoundedCornerShape(50)
                    )
            )
        }
    }
}
