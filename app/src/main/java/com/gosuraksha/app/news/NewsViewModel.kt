package com.gosuraksha.app.news

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.news.db.BookmarkDao
import com.gosuraksha.app.news.db.BookmarkEntity
import com.gosuraksha.app.news.db.AppDatabase
import com.gosuraksha.app.news.model.NewsItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val bookmarkDao: BookmarkDao =
        AppDatabase.getInstance(application).bookmarkDao()

    // ── News ──────────────────────────────────────────────────────────────────
    private val _news = MutableStateFlow<List<NewsItem>>(emptyList())
    val news: StateFlow<List<NewsItem>> = _news

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ── Bookmarks ─────────────────────────────────────────────────────────────
    val bookmarks: StateFlow<Set<String>> = bookmarkDao
        .observeAll()
        .map { it.toSet() }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    // ── Init ──────────────────────────────────────────────────────────────────
    init {
        observeLanguage()
    }

    // ── Language ──────────────────────────────────────────────────────────────
    private fun observeLanguage() {
        viewModelScope.launch {
            LanguagePrefs
                .getLanguage(getApplication())
                .collect { lang -> loadNews(lang ?: "en") }
        }
    }

    // ── Load news ─────────────────────────────────────────────────────────────
    private fun loadNews(lang: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value   = null
                _news.value    = ApiClient.newsApi.getNews(lang).news
            } catch (e: Exception) {
                _error.value = e.message ?: "error_news_load_failed"
            } finally {
                _loading.value = false
            }
        }
    }

    // ── Toggle bookmark ───────────────────────────────────────────────────────
    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            if (bookmarkDao.exists(articleId)) {
                bookmarkDao.delete(articleId)
            } else {
                bookmarkDao.insert(BookmarkEntity(articleId))
            }
        }
    }
}