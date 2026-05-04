package com.gosuraksha.app.ui.learn

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phishing
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.learn.LearnTopics
import com.gosuraksha.app.learn.LearnViewModel
import com.gosuraksha.app.learn.LearnViewModelFactory
import com.gosuraksha.app.learn.model.LearnArticle
import com.gosuraksha.app.learn.model.LearnTopic

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun LearnScreen(
    onOpenArticle: (LearnArticle) -> Unit,
    learnViewModel: LearnViewModel = viewModel(
        factory = LearnViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by learnViewModel.uiState.collectAsStateWithLifecycle()
    val bookmarks by learnViewModel.bookmarks.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = learnViewModel::refresh
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pullRefresh(pullRefreshState)
    ) {
        when {
            uiState.isLoading -> LearnLoadingState()
            uiState.error != null && uiState.articles.isEmpty() && uiState.recommended.isEmpty() -> {
                LearnErrorState(
                    message = uiState.error.orEmpty(),
                    onRetry = learnViewModel::refresh
                )
            }
            uiState.featuredArticle == null && uiState.articles.isEmpty() -> {
                LearnEmptyState(onRetry = learnViewModel::refresh)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = SpacingTokens.screenPaddingHorizontal,
                        end = SpacingTokens.screenPaddingHorizontal,
                        top = SpacingTokens.md,
                        bottom = 104.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg)
                ) {
                    item {
                        uiState.featuredArticle?.let { article ->
                            LearnHeroCard(
                                article = article,
                                isBookmarked = bookmarks.contains(article.stableId),
                                onBookmark = { learnViewModel.toggleBookmark(article.stableId) },
                                onClick = { onOpenArticle(article) }
                            )
                        }
                    }
                    item {
                        LearnSectionHeader(
                            title = "Cyber Awareness",
                            subtitle = "Stay ahead of digital threats"
                        )
                    }
                    item {
                        TopicChipRow(
                            selectedTopic = uiState.selectedTopic,
                            onTopicSelected = learnViewModel::selectTopic
                        )
                    }
                    item {
                        BrowseByTopicGrid(onTopicSelected = learnViewModel::selectTopic)
                    }
                    if (uiState.recommendedCards.isNotEmpty()) {
                        item {
                            LearnSectionHeader(
                                title = "Recommended for You",
                                subtitle = "Curated guidance based on current scam patterns"
                            )
                        }
                        item {
                            RecommendedRow(
                                articles = uiState.recommendedCards,
                                bookmarks = bookmarks,
                                onBookmark = learnViewModel::toggleBookmark,
                                onOpenArticle = onOpenArticle
                            )
                        }
                    }
                    item {
                        LearnSectionHeader(
                            title = "Latest Articles",
                            subtitle = "Practical explainers you can act on immediately"
                        )
                    }
                    if (uiState.error != null) {
                        item {
                            InlineErrorBanner(
                                message = uiState.error.orEmpty(),
                                onRetry = learnViewModel::refresh
                            )
                        }
                    }
                    items(
                        items = uiState.articles,
                        key = { it.stableId }
                    ) { article ->
                        LearnArticleCard(
                            article = article,
                            isBookmarked = bookmarks.contains(article.stableId),
                            onBookmark = { learnViewModel.toggleBookmark(article.stableId) },
                            onClick = { onOpenArticle(article) }
                        )
                    }
                }
            }
        }
        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearnDetailScreen(
    article: LearnArticle?,
    onBack: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    isBookmarked: Boolean
) {
    if (article == null) {
        LearnErrorState(
            message = "This article is no longer available.",
            onRetry = onBack
        )
        return
    }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = SpacingTokens.screenPaddingHorizontal,
            end = SpacingTokens.screenPaddingHorizontal,
            top = SpacingTokens.md,
            bottom = 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                IconButton(onClick = { onToggleBookmark(article.stableId) }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark"
                    )
                }
            }
        }
        item {
            Surface(
                shape = ShapeTokens.cardLarge,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column {
                    ArticleVisual(
                        article = article,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    Column(
                        modifier = Modifier.padding(SpacingTokens.cardPaddingLarge),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                        ) {
                            TagPill(article.displayCategory)
                            article.displayLevel?.let { TagPill(it) }
                            TagPill(article.displayReadTime)
                        }
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        article.displaySubtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        item {
            Surface(
                shape = ShapeTokens.card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(SpacingTokens.cardPaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)
                ) {
                    Text(
                        text = article.content?.takeIf { it.isNotBlank() }
                            ?: article.displayDescription
                            ?: "Open the source article to continue reading.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    article.link?.takeIf { it.isNotBlank() }?.let { link ->
                        TextButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                            }
                        ) {
                            Text("Open source")
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Rounded.ArrowOutward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearnHeroCard(
    article: LearnArticle,
    isBookmarked: Boolean,
    onBookmark: () -> Unit,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val gradient = remember(colors) {
        Brush.linearGradient(
            colors = listOf(
                colors.primaryContainer,
                colors.secondaryContainer,
                colors.surfaceVariant
            )
        )
    }

    Surface(
        onClick = onClick,
        shape = ShapeTokens.cardLarge,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(gradient, ShapeTokens.cardLarge)
                .padding(SpacingTokens.cardPaddingLarge)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgePill(if (article.isTrending) "Trending" else "Recommended")
                    IconButton(onClick = onBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = article.displaySubtitle ?: "Understand how this scam works before it reaches you.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                    TagPill(article.displayCategory, inverse = true)
                    article.displayLevel?.let { TagPill(it, inverse = true) }
                }
            }
        }
    }
}

@Composable
private fun LearnSectionHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TopicChipRow(
    selectedTopic: LearnTopic,
    onTopicSelected: (LearnTopic) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
        items(LearnTopics.filters, key = { it.label }) { topic ->
            TopicChip(
                topic = topic,
                selected = topic == selectedTopic,
                onClick = { onTopicSelected(topic) }
            )
        }
    }
}

@Composable
private fun TopicChip(
    topic: LearnTopic,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surface,
        label = "topic_chip_bg"
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        label = "topic_chip_fg"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.98f,
        animationSpec = spring(),
        label = "topic_chip_scale"
    )

    Surface(
        shape = ShapeTokens.chip,
        color = container,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else colors.outline.copy(alpha = 0.4f)),
        modifier = Modifier.scale(scale)
    ) {
        Text(
            text = topic.label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun BrowseByTopicGrid(
    onTopicSelected: (LearnTopic) -> Unit
) {
    val pairs = LearnTopics.browse.chunked(2)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
        items(pairs, key = { it.joinToString { topic -> topic.label } }) { pair ->
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                pair.forEach { topic ->
                    TopicGridCard(
                        topic = topic,
                        icon = topicIcon(topic.label),
                        onClick = { onTopicSelected(topic) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicGridCard(
    topic: LearnTopic,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = ShapeTokens.card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.width(156.dp)
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = topic.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecommendedRow(
    articles: List<LearnArticle>,
    bookmarks: Set<String>,
    onBookmark: (String) -> Unit,
    onOpenArticle: (LearnArticle) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
        items(articles, key = { it.stableId }) { article ->
            Surface(
                onClick = { onOpenArticle(article) },
                shape = ShapeTokens.card,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.width(248.dp)
            ) {
                Column {
                    ArticleVisual(
                        article = article,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                    )
                    Column(
                        modifier = Modifier.padding(SpacingTokens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BadgePill("Recommended")
                            IconButton(onClick = { onBookmark(article.stableId) }) {
                                Icon(
                                    imageVector = if (bookmarks.contains(article.stableId)) {
                                        Icons.Rounded.Bookmark
                                    } else {
                                        Icons.Outlined.BookmarkBorder
                                    },
                                    contentDescription = "Bookmark"
                                )
                            }
                        }
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = article.displayDescription ?: article.displaySubtitle.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                            TagPill(article.displayCategory)
                            TagPill(article.displayReadTime)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearnArticleCard(
    article: LearnArticle,
    isBookmarked: Boolean,
    onBookmark: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = ShapeTokens.card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            verticalAlignment = Alignment.Top
        ) {
            ArticleVisual(
                article = article,
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(0.86f)
                    .clip(ShapeTokens.image)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = article.displayDescription ?: article.displaySubtitle.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)
                ) {
                    TagPill(article.displayCategory)
                    article.displayLevel?.let { TagPill(it) }
                    TagPill(article.displayReadTime)
                }
            }
        }
    }
}

@Composable
private fun ArticleVisual(
    article: LearnArticle,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val fallbackBrush = remember(colorScheme) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.primaryContainer,
                colorScheme.secondaryContainer,
                colorScheme.tertiaryContainer
            )
        )
    }

    if (!article.imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = article.imageUrl,
            contentDescription = article.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(fallbackBrush, ShapeTokens.image),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Article,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BadgePill(text: String) {
    Surface(
        shape = ShapeTokens.chip,
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun TagPill(text: String, inverse: Boolean = false) {
    val background = if (inverse) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (inverse) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(ShapeTokens.chip)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}

@Composable
private fun InlineErrorBanner(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        shape = ShapeTokens.card,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun LearnLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LearnEmptyState(onRetry: () -> Unit) {
    LearnCenteredState(
        title = "No learning content yet",
        body = "Try refreshing to pull the latest cyber awareness articles.",
        action = "Refresh",
        onAction = onRetry
    )
}

@Composable
private fun LearnErrorState(
    message: String,
    onRetry: () -> Unit
) {
    LearnCenteredState(
        title = "Unable to load Learn",
        body = message,
        action = "Try again",
        onAction = onRetry
    )
}

@Composable
private fun LearnCenteredState(
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.xl),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = ShapeTokens.cardLarge,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.cardPaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onAction) {
                    Text(action)
                }
            }
        }
    }
}

private fun topicIcon(label: String): ImageVector = when (label) {
    "Banking Fraud" -> Icons.Rounded.Payments
    "Social Media" -> Icons.Rounded.Public
    "UPI Fraud" -> Icons.Rounded.GppMaybe
    "Phishing" -> Icons.Rounded.Phishing
    "OTP Fraud" -> Icons.Rounded.Password
    "Identity Theft" -> Icons.Rounded.AccountCircle
    else -> Icons.Rounded.Shield
}
