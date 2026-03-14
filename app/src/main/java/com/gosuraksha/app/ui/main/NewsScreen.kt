package com.gosuraksha.app.ui.main

import com.gosuraksha.app.R
import androidx.compose.ui.res.stringResource
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
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
import com.gosuraksha.app.design.components.AppCard
import com.gosuraksha.app.core.periodicTickFlow
import com.gosuraksha.app.news.NewsViewModel
import com.gosuraksha.app.news.model.NewsItem
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

// ── Aurora palette ────────────────────────────────────────────────────────────
private val AuroraViolet     = Color(0xFF8B5CF6)
private val AuroraBlue       = Color(0xFF3B82F6)
private val AuroraPurpleDim  = Color(0x338B5CF6)   // 20% violet
private val AuroraBlueDim    = Color(0x333B82F6)    // 20% blue
private val GlassDark        = Color(0x0DFFFFFF)    // 5% white
private val GlassBorder      = Color(0x1FFFFFFF)    // 12% white
private val GlassBorderLight = Color(0xCCFFFFFF)    // 80% white (light mode)
private val GlassLight       = Color(0x99FFFFFF)    // 60% white (light mode bg)

// ─────────────────────────────────────────────────────────────────────────────
//  ROOT SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NewsScreen(
    newsViewModel: NewsViewModel = viewModel()
) {
    val news      by newsViewModel.news.collectAsStateWithLifecycle()
    val loading   by newsViewModel.loading.collectAsStateWithLifecycle()
    val bookmarks by newsViewModel.bookmarks.collectAsStateWithLifecycle()   // Set<String> of article IDs

    var selectedCategory by remember { mutableStateOf(NewsCategory.ALL) }
    val categories = remember { listOf(NewsCategory.ALL, NewsCategory.AI, NewsCategory.CYBER, NewsCategory.TECH) }

    val filteredNews = remember(news, selectedCategory) {
        news.filter {
            selectedCategory == NewsCategory.ALL ||
                    it.category.equals(selectedCategory.apiValue, ignoreCase = true)
        }
    }

    val isDark = !MaterialTheme.colorScheme.background.luminance().let { it > 0.5f }

    // Ambient orb colours shift per theme
    val orb1Color = if (isDark) AuroraPurpleDim else Color(0x33C084FC)
    val orb2Color = if (isDark) AuroraBlueDim   else Color(0x333B82F6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // ── Ambient orbs (purely decorative, no interaction) ──────────────────
        AuroraOrbLayer(orb1Color = orb1Color, orb2Color = orb2Color)

        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(Modifier.height(12.dp))

            // ── Category filter ───────────────────────────────────────────────
            AuroraCategoryBar(
                categories  = categories,
                selected    = selectedCategory,
                onSelect    = { selectedCategory = it },
                isDark      = isDark
            )

            Spacer(Modifier.height(16.dp))

            // ── Content ───────────────────────────────────────────────────────
            when {
                loading -> AuroraShimmerLoading()

                filteredNews.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text  = stringResource(R.string.ui_newsscreen_2),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    val pagerState = rememberPagerState(pageCount = { filteredNews.size })
                    val coroutineScope = rememberCoroutineScope()
                    val context = LocalContext.current

                    // ── Auto-scroll ───────────────────────────────────────────
                    val lifecycleOwner = LocalLifecycleOwner.current
                    LaunchedEffect(pagerState, filteredNews.size, lifecycleOwner) {
                        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            periodicTickFlow(periodMillis = 6_000L).collect {
                                if (filteredNews.isNotEmpty()) {
                                    val next = (pagerState.currentPage + 1) % filteredNews.size
                                    pagerState.animateScrollToPage(next)
                                }
                            }
                        }
                    }

                    // ── Haptic on page change ─────────────────────────────────
                    var previousPage by remember { mutableIntStateOf(pagerState.currentPage) }
                    LaunchedEffect(pagerState.currentPage) {
                        if (pagerState.currentPage != previousPage) {
                            previousPage = pagerState.currentPage
                            triggerHapticTick(context)
                        }
                    }

                    HorizontalPager(
                        state          = pagerState,
                        contentPadding = PaddingValues(horizontal = 22.dp),
                        pageSpacing    = 16.dp,
                        modifier       = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->

                        val item = filteredNews[page]
                        val articleId = item.link ?: "${item.source}|${item.title}|${item.published_at}"
                        val isBookmarked = bookmarks.contains(articleId)

                        val scale by animateFloatAsState(
                            targetValue    = if (page == pagerState.currentPage) 1f else 0.91f,
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness    = Spring.StiffnessMedium
                            ),
                            label = "card_scale"
                        )

                        val alpha by animateFloatAsState(
                            targetValue   = if (page == pagerState.currentPage) 1f else 0.72f,
                            animationSpec = tween(200),
                            label         = "card_alpha"
                        )

                        AuroraNewsCard(
                            item         = item,
                            isHero       = page == pagerState.currentPage,
                            isBookmarked = isBookmarked,
                            isDark       = isDark,
                            onBookmark   = { newsViewModel.toggleBookmark(articleId) },
                            onShare      = {
                                item.link?.let { url ->
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "${item.title}\n\n$url")
                                            },
                                            null
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale; scaleY = scale; this.alpha = alpha
                            }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Capsule indicator ─────────────────────────────────────
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AuroraCapsuleIndicator(
                            total   = filteredNews.size,
                            current = pagerState.currentPage
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  AURORA ORB LAYER  — blurred radial circles, rendered behind all content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AuroraOrbLayer(orb1Color: Color, orb2Color: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left orb
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .blur(radius = 80.dp)
                .background(
                    brush  = Brush.radialGradient(listOf(orb1Color, Color.Transparent)),
                    shape  = CircleShape
                )
        )
        // Bottom-right orb
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .blur(radius = 70.dp)
                .background(
                    brush  = Brush.radialGradient(listOf(orb2Color, Color.Transparent)),
                    shape  = CircleShape
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  AURORA NEWS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AuroraNewsCard(
    item         : NewsItem,
    isHero       : Boolean,
    isBookmarked : Boolean,
    isDark       : Boolean,
    onBookmark   : () -> Unit,
    onShare      : () -> Unit,
    modifier     : Modifier = Modifier
) {
    val context   = LocalContext.current
    val cardHeight = if (isHero) 510.dp else 470.dp

    // Glass surface colours adapt to theme
    val glassBg     = if (isDark) GlassDark      else GlassLight.copy(alpha = 0.65f)
    val glassBorder = if (isDark) GlassBorder    else GlassBorderLight

    // Animated accent gradient position on hero
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_card")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label          = "gradient_shift"
    )

    Card(
        shape    = RoundedCornerShape(28.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .border(
                width  = 1.dp,
                brush  = if (isHero)
                    Brush.linearGradient(
                        colors = listOf(
                            AuroraViolet.copy(alpha = 0.5f + gradientShift * 0.3f),
                            AuroraBlue.copy(alpha = 0.3f + gradientShift * 0.3f),
                            AuroraViolet.copy(alpha = 0.2f)
                        )
                    )
                else
                    Brush.linearGradient(listOf(glassBorder, glassBorder)),
                shape  = RoundedCornerShape(28.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
        ) {

            // ── Background image ──────────────────────────────────────────────
            SubcomposeAsyncImage(
                model               = item.image ?: "https://source.unsplash.com/800x600/?cybersecurity",
                contentDescription  = null,
                contentScale        = ContentScale.Crop,
                modifier            = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1A1035), Color(0xFF0F1A35))
                                    )
                                )
                        )
                    }
                    is AsyncImagePainter.State.Error   -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1A1035), Color(0xFF0F1A35))
                                    )
                                )
                        )
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }

            // ── Multi-stop gradient overlay ───────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Black.copy(alpha = 0.08f),
                                0.30f to Color.Black.copy(alpha = 0.15f),
                                0.60f to Color.Black.copy(alpha = 0.55f),
                                0.80f to Color.Black.copy(alpha = 0.78f),
                                1.00f to Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // ── Aurora tint overlay (violet → blue, bottom half) ──────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.50f to Color.Transparent,
                                1.00f to AuroraViolet.copy(alpha = 0.18f)
                            )
                        )
                    )
            )

            // ── Glass blur strip at bottom ────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(220.dp)
                    .blur(12.dp)
                    .background(glassBg)
            )

            // ── Category badge ────────────────────────────────────────────────
            AuroraGlassBadge(
                text     = item.category.uppercase(),
                color    = AuroraViolet,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            )

            // ── Trending badge ────────────────────────────────────────────────
            if (item.is_trending) {
                AuroraGlassBadge(
                    text     = "● Trending",
                    color    = Color(0xFFEF4444),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                )
            }

            // ── Bottom content ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 20.dp, bottom = 22.dp)
            ) {

                // Source
                Text(
                    text     = item.source.uppercase(),
                    color    = AuroraViolet,
                    fontSize = 10.sp,
                    fontWeight  = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(6.dp))

                // Title
                Text(
                    text      = item.title,
                    style     = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    ),
                    color     = Color.White,
                    maxLines  = if (isHero) 3 else 2,
                    overflow  = TextOverflow.Ellipsis
                )

                // Summary
                item.summary
                    ?.takeIf { it.isNotBlank() && !it.contains("updated shortly", true) }
                    ?.let { summary ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = summary,
                            color     = Color.White.copy(alpha = 0.68f),
                            fontSize  = 13.sp,
                            maxLines  = 2,
                            overflow  = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }

                Spacer(Modifier.height(16.dp))

                // ── Action row ────────────────────────────────────────────────
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Text(
                        text      = item.published_at.take(10),
                        color     = Color.White.copy(alpha = 0.45f),
                        fontSize  = 11.sp
                    )

                    Spacer(Modifier.weight(1f))

                    // Share
                    AuroraIconButton(
                        onClick  = onShare,
                        content  = {
                            Icon(
                                imageVector        = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint               = Color.White,
                                modifier           = Modifier.size(16.dp)
                            )
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    // Bookmark
                    AuroraIconButton(
                        onClick  = onBookmark,
                        glowing  = isBookmarked,
                        content  = {
                            Icon(
                                imageVector        = if (isBookmarked) Icons.Filled.Bookmark
                                else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint               = if (isBookmarked) AuroraViolet else Color.White,
                                modifier           = Modifier.size(16.dp)
                            )
                        }
                    )

                    Spacer(Modifier.width(10.dp))

                    // Read more
                    AuroraReadMoreButton(
                        onClick = {
                            item.link?.let { url ->
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SMALL REUSABLE COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AuroraGlassBadge(
    text     : String,
    color    : Color,
    modifier : Modifier = Modifier
) {
    Surface(
        shape  = RoundedCornerShape(50.dp),
        color  = color.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Text(
            text       = text,
            color      = Color.White,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun AuroraIconButton(
    onClick  : () -> Unit,
    glowing  : Boolean    = false,
    content  : @Composable () -> Unit
) {
    Box(
        modifier          = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
                if (glowing)
                    Brush.radialGradient(listOf(AuroraViolet.copy(alpha = 0.35f), Color.Transparent))
                else
                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent))
            )
            .border(1.dp, if (glowing) AuroraViolet.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f), CircleShape)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event  = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && !change.pressed) {
                            change.consume(); onClick()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun AuroraReadMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        AuroraViolet.copy(alpha = 0.75f),
                        AuroraBlue.copy(alpha = 0.75f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(AuroraViolet.copy(alpha = 0.6f), AuroraBlue.copy(alpha = 0.6f))),
                RoundedCornerShape(50.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event  = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && !change.pressed) {
                            change.consume(); onClick()
                        }
                    }
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text       = stringResource(R.string.ui_newsscreen_1),
                color      = Color.White,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector        = Icons.Outlined.OpenInNew,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(11.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CATEGORY FILTER BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AuroraCategoryBar(
    categories : List<NewsCategory>,
    selected   : NewsCategory,
    onSelect   : (NewsCategory) -> Unit,
    isDark     : Boolean
) {
    Row(
        modifier             = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selected

            val bgColor by animateColorAsState(
                targetValue   = when {
                    isSelected && isDark -> AuroraViolet.copy(alpha = 0.28f)
                    isSelected           -> AuroraViolet.copy(alpha = 0.18f)
                    isDark               -> Color.White.copy(alpha = 0.06f)
                    else                 -> Color.Black.copy(alpha = 0.05f)
                },
                animationSpec = tween(200),
                label         = "cat_bg"
            )

            val textColor by animateColorAsState(
                targetValue   = if (isSelected) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label         = "cat_text"
            )

            val borderColor by animateColorAsState(
                targetValue   = if (isSelected) AuroraViolet.copy(alpha = 0.6f)
                else Color.Transparent,
                animationSpec = tween(200),
                label         = "cat_border"
            )

            val label = when (category) {
                NewsCategory.ALL   -> stringResource(R.string.news_category_all)
                NewsCategory.AI    -> stringResource(R.string.news_category_ai)
                NewsCategory.CYBER -> stringResource(R.string.news_category_cyber)
                NewsCategory.TECH  -> stringResource(R.string.news_category_tech)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(50.dp))
                    .clickable { onSelect(category) }
                    .then(
                        if (isSelected) Modifier.background(
                            Brush.linearGradient(
                                listOf(
                                    AuroraViolet.copy(alpha = 0.25f),
                                    AuroraBlue.copy(alpha = 0.20f)
                                )
                            ),
                            RoundedCornerShape(50.dp)
                        ) else Modifier
                    )
            ) {
                Text(
                    text       = label,
                    color      = textColor,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier   = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CAPSULE PAGE INDICATOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AuroraCapsuleIndicator(total: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(total.coerceAtMost(8)) { index ->
            val isSelected = index == current

            val width by animateDpAsState(
                targetValue   = if (isSelected) 24.dp else 7.dp,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                label         = "dot_width"
            )

            val color by animateColorAsState(
                targetValue   = if (isSelected) AuroraViolet
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                animationSpec = tween(200),
                label         = "dot_color"
            )

            Box(
                modifier = Modifier
                    .height(7.dp)
                    .width(width)
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) Modifier.background(
                            Brush.horizontalGradient(listOf(AuroraViolet, AuroraBlue))
                        ) else Modifier.background(color)
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  AURORA SHIMMER LOADING
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AuroraShimmerLoading() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue  = -1000f,
        targetValue   = 1000f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label         = "shimmerX"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1A1035),
            Color(0xFF221545),
            Color(0xFF2E1F5E),
            Color(0xFF221545),
            Color(0xFF1A1035)
        ),
        start = Offset(shimmerX - 500f, 0f),
        end   = Offset(shimmerX + 500f, 0f)
    )

    Column(
        modifier             = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        verticalArrangement  = Arrangement.spacedBy(16.dp)
    ) {
        // Card skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmerBrush)
                .border(1.dp, AuroraViolet.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier             = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                verticalArrangement  = Arrangement.spacedBy(10.dp)
            ) {
                // Source line
                Box(Modifier.width(70.dp).height(10.dp).clip(RoundedCornerShape(50)).background(AuroraViolet.copy(alpha = 0.25f)))
                // Title lines
                Box(Modifier.fillMaxWidth(0.92f).height(20.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.08f)))
                Box(Modifier.fillMaxWidth(0.72f).height(20.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.08f)))
                // Summary
                Box(Modifier.fillMaxWidth(0.85f).height(13.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.04f)))
                Box(Modifier.fillMaxWidth(0.60f).height(13.dp).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.04f)))
                Spacer(Modifier.height(4.dp))
                // Action row skeleton
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(60.dp).height(11.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.05f)))
                    Spacer(Modifier.weight(1f))
                    repeat(3) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)))
                        if (it < 2) Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }

        // Dot skeleton
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(7.dp)
                        .width(if (i == 0) 24.dp else 7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (i == 0)
                                Brush.horizontalGradient(listOf(AuroraViolet.copy(alpha = 0.4f), AuroraBlue.copy(alpha = 0.4f)))
                            else
                                Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f)))
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HAPTIC HELPER
// ─────────────────────────────────────────────────────────────────────────────

private fun triggerHapticTick(context: android.content.Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(18, 60))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(18)
            }
        }
    } catch (_: Exception) { /* vibrator unavailable — silently skip */ }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CATEGORY ENUM
// ─────────────────────────────────────────────────────────────────────────────

private enum class NewsCategory(val apiValue: String) {
    ALL("ALL"), AI("AI"), CYBER("CYBER"), TECH("TECH")
}




