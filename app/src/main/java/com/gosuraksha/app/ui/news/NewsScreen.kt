package com.gosuraksha.app.ui.news

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gosuraksha.app.news.NewsViewModel
import com.gosuraksha.app.news.NewsViewModelFactory

@Composable
fun NewsScreen(
    newsViewModel: NewsViewModel = viewModel(
        factory = NewsViewModelFactory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val news by newsViewModel.news.collectAsStateWithLifecycle()
    val loading by newsViewModel.loading.collectAsStateWithLifecycle()
    val error by newsViewModel.error.collectAsStateWithLifecycle()
    val bookmarks by newsViewModel.bookmarks.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf(NewsCategory.ALL) }
    val categories = remember { listOf(NewsCategory.ALL, NewsCategory.AI, NewsCategory.CYBER, NewsCategory.TECH) }
    val filteredNews = remember(news, selectedCategory) { filterNewsByCategory(news, selectedCategory) }

    val isDark = MaterialTheme.colorScheme.background.luminance() <= 0.5f
    val orb1Color = if (isDark) AuroraPurpleDim else Color(0x33C084FC)
    val orb2Color = if (isDark) AuroraBlueDim else Color(0x333B82F6)
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuroraOrbLayer(orb1Color = orb1Color, orb2Color = orb2Color)
        Column(modifier = Modifier.fillMaxSize()) {
            NewsHeader(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
                isDark = isDark
            )
            Spacer(Modifier.height(16.dp))
            when {
                loading -> NewsLoadingState()
                error != null -> NewsErrorState(error ?: "error_news_load_failed")
                filteredNews.isEmpty() -> NewsEmptyState()
                else -> {
                    Box(modifier = Modifier.weight(1f)) {
                        NewsList(
                            items = filteredNews,
                            bookmarks = bookmarks,
                            isDark = isDark,
                            onBookmark = { newsViewModel.toggleBookmark(it) },
                            onShare = { item ->
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
                            onPageChanged = { triggerHapticTick(context) }
                        )
                    }
                }
            }
        }
    }
}
