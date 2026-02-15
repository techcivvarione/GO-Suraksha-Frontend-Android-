package com.gosuraksha.app.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.gosuraksha.app.news.NewsViewModel
import com.gosuraksha.app.news.model.NewsItem
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun NewsScreen() {

    val viewModel: NewsViewModel = viewModel()
    val news by viewModel.news.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL", "AI", "CYBER", "TECH")

    val filteredNews = remember(news, selectedCategory) {
        news.filter {
            selectedCategory == "ALL" || it.category.equals(selectedCategory, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Spacer(Modifier.height(12.dp))

        // ── CATEGORY FILTER BAR ──────────────────────────────────────
        CategoryFilterBar(
            categories = categories,
            selected = selectedCategory,
            onSelect = { selectedCategory = it }
        )

        Spacer(Modifier.height(16.dp))

        // ── CONTENT AREA ─────────────────────────────────────────────
        when {
            loading -> {
                ShimmerNewsLoading()
            }

            filteredNews.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No news in this category",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                val pagerState = rememberPagerState(pageCount = { filteredNews.size })

                // Auto-scroll every 5 seconds
                LaunchedEffect(pagerState, filteredNews.size) {
                    while (true) {
                        delay(5000)
                        if (filteredNews.isNotEmpty()) {
                            val next = (pagerState.currentPage + 1) % filteredNews.size
                            pagerState.animateScrollToPage(next)
                        }
                    }
                }

                // ── PAGER ──────────────────────────────────────────────
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->

                    val item = filteredNews[page]
                    val isHero = page == 0

                    val pageOffset = (
                            (pagerState.currentPage - page) +
                                    pagerState.currentPageOffsetFraction
                            ).absoluteValue

                    val scale by animateFloatAsState(
                        targetValue = if (page == pagerState.currentPage) 1f else 0.92f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "card_scale"
                    )

                    NewsCard(
                        item = item,
                        isHero = isHero && page == pagerState.currentPage,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── PAGE INDICATOR ─────────────────────────────────────
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CapsulePageIndicator(
                        total = filteredNews.size,
                        current = pagerState.currentPage
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── NEWS CARD ──────────────────────────────────────────────────────────────────

@Composable
fun NewsCard(
    item: NewsItem,
    isHero: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bookmarked by remember { mutableStateOf(false) }

    val cardHeight = if (isHero) 500.dp else 460.dp

    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHero) 16.dp else 8.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── BACKGROUND IMAGE ──────────────────────────────────────
            SubcomposeAsyncImage(
                model = item.image
                    ?: "https://source.unsplash.com/800x600/?cybersecurity,technology",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF1A1A2E),
                                            Color(0xFF16213E)
                                        )
                                    )
                                )
                        )
                    }
                    is AsyncImagePainter.State.Error -> {
                        AsyncImage(
                            model = "https://source.unsplash.com/800x600/?cybersecurity,technology",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }

            // ── GRADIENT OVERLAY ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.35f to Color.Black.copy(alpha = 0.1f),
                                0.75f to Color.Black.copy(alpha = 0.75f),
                                1.0f to Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // ── CATEGORY BADGE ────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = item.category.uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            // ── TRENDING BADGE ────────────────────────────────────────
            if (item.is_trending) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFFFF4444).copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🔥 TRENDING",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            // ── BOTTOM CONTENT ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {

                // Source
                Text(
                    text = item.source.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.height(6.dp))

                // Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    ),
                    color = Color.White,
                    maxLines = if (isHero) 3 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Summary
                item.summary
                    ?.takeIf { it.isNotBlank() && !it.contains("updated shortly", true) }
                    ?.let { summary ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = summary,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }

                Spacer(Modifier.height(14.dp))

                // Action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Published date
                    Text(
                        text = item.published_at.take(10),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.weight(1f))

                    // Bookmark
                    IconButton(
                        onClick = { bookmarked = !bookmarked },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                    ) {
                        Icon(
                            imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (bookmarked) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Read more button — pointerInput ensures tap is consumed
                    // and does NOT bubble up to the HorizontalPager
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.pointerInput(item.link) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull()
                                    if (change != null && change.pressed) {
                                        change.consume()
                                    } else if (change != null && !change.pressed) {
                                        change.consume()
                                        item.link?.let { url ->
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                "Read",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Outlined.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── CATEGORY FILTER BAR ────────────────────────────────────────────────────────

@Composable
fun CategoryFilterBar(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->

            val isSelected = category == selected

            val bgColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                animationSpec = tween(200),
                label = "chip_bg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected)
                    Color.White
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "chip_text"
            )

            Surface(
                shape = RoundedCornerShape(50.dp),
                color = bgColor,
                shadowElevation = if (isSelected) 6.dp else 0.dp,
                modifier = Modifier.clickable { onSelect(category) }
            ) {
                Text(
                    text = category,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ── CAPSULE PAGE INDICATOR ─────────────────────────────────────────────────────

@Composable
fun CapsulePageIndicator(
    total: Int,
    current: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total.coerceAtMost(8)) { index ->

            val isSelected = index == current

            val width by animateDpAsState(
                targetValue = if (isSelected) 22.dp else 7.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "dot_width"
            )

            val color by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                animationSpec = tween(200),
                label = "dot_color"
            )

            Box(
                modifier = Modifier
                    .height(7.dp)
                    .width(width)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

// ── SHIMMER LOADING ────────────────────────────────────────────────────────────

@Composable
fun ShimmerNewsLoading() {

    val transition = rememberInfiniteTransition(label = "shimmer")

    val shimmerX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1E1E2E),
            Color(0xFF2A2A3E),
            Color(0xFF323250),
            Color(0xFF2A2A3E),
            Color(0xFF1E1E2E)
        ),
        start = androidx.compose.ui.geometry.Offset(shimmerX - 500f, 0f),
        end = androidx.compose.ui.geometry.Offset(shimmerX + 500f, 0f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Main card shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmerBrush)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                )
            }
        }

        // Indicator shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(7.dp)
                        .width(if (i == 0) 22.dp else 7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = if (i == 0) 0.25f else 0.1f))
                )
            }
        }
    }
}