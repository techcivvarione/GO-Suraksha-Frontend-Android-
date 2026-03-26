package com.gosuraksha.app.ui.news

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.gosuraksha.app.R
import com.gosuraksha.app.news.model.NewsItem

@Composable
fun NewsItemCard(
    item: NewsItem,
    isHero: Boolean,
    isBookmarked: Boolean,
    isDark: Boolean,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardHeight = if (isHero) 510.dp else 470.dp
    val glassBg = if (isDark) GlassDark else GlassLight.copy(alpha = 0.65f)
    val glassBorder = if (isDark) GlassBorder else GlassBorderLight
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_card")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "gradient_shift"
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .border(
                width = 1.dp,
                brush = if (isHero) {
                    Brush.linearGradient(
                        colors = listOf(
                            AuroraViolet.copy(alpha = 0.5f + gradientShift * 0.3f),
                            AuroraBlue.copy(alpha = 0.3f + gradientShift * 0.3f),
                            AuroraViolet.copy(alpha = 0.2f)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(glassBorder, glassBorder))
                },
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp))) {
            SubcomposeAsyncImage(
                model = item.image?.takeIf { it.isNotBlank() } ?: R.drawable.logo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading,
                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(Color(0xFF1A1035), Color(0xFF0F1A35))))
                        )
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }

            Box(
                modifier = Modifier.fillMaxSize().background(
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
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(colorStops = arrayOf(0.50f to Color.Transparent, 1.00f to AuroraViolet.copy(alpha = 0.18f)))
                )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(220.dp)
                    .blur(12.dp)
                    .background(glassBg)
            )

            AuroraGlassBadge(
                text = item.category.uppercase(),
                color = AuroraViolet,
                modifier = Modifier.align(Alignment.TopStart).padding(14.dp)
            )
            if (item.is_trending) {
                AuroraGlassBadge(
                    text = "● Trending",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp)
                )
            }

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 22.dp)) {
                Text(
                    text = item.source.uppercase(),
                    color = AuroraViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 28.sp),
                    color = Color.White,
                    maxLines = if (isHero) 3 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.summary?.takeIf { it.isNotBlank() && !it.contains("updated shortly", true) }?.let { summary ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = summary,
                        color = Color.White.copy(alpha = 0.68f),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.published_at.take(10), color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
                    Spacer(Modifier.weight(1f))
                    AuroraIconButton(onClick = onShare) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    AuroraIconButton(onClick = onBookmark, glowing = isBookmarked) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) AuroraViolet else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    AuroraReadMoreButton {
                        item.link?.let { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    }
                }
            }
        }
    }
}

@Composable
fun AuroraGlassBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = color.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun AuroraIconButton(onClick: () -> Unit, glowing: Boolean = false, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
                if (glowing) Brush.radialGradient(listOf(AuroraViolet.copy(alpha = 0.35f), Color.Transparent))
                else Brush.radialGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent))
            )
            .border(1.dp, if (glowing) AuroraViolet.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f), CircleShape)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && !change.pressed) {
                            change.consume()
                            onClick()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
fun AuroraReadMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Brush.linearGradient(listOf(AuroraViolet.copy(alpha = 0.75f), AuroraBlue.copy(alpha = 0.75f))))
            .border(
                1.dp,
                Brush.linearGradient(listOf(AuroraViolet.copy(alpha = 0.6f), AuroraBlue.copy(alpha = 0.6f))),
                RoundedCornerShape(50.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && !change.pressed) {
                            change.consume()
                            onClick()
                        }
                    }
                }
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = stringResource(R.string.ui_newsscreen_1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
        }
    }
}
