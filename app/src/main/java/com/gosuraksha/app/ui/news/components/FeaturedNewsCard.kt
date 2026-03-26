package com.gosuraksha.app.ui.news

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gosuraksha.app.news.model.NewsItem

@Composable
fun FeaturedNewsCard(
    item: NewsItem,
    isBookmarked: Boolean,
    isDark: Boolean,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    NewsItemCard(
        item = item,
        isHero = true,
        isBookmarked = isBookmarked,
        isDark = isDark,
        onBookmark = onBookmark,
        onShare = onShare,
        modifier = modifier
    )
}
