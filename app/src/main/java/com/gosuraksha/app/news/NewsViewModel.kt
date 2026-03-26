package com.gosuraksha.app.news

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.core.LanguagePrefs
import com.gosuraksha.app.data.repository.NewsRepository
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.news.db.BookmarkDao
import com.gosuraksha.app.news.db.BookmarkEntity
import com.gosuraksha.app.news.db.AppDatabase
import com.gosuraksha.app.news.model.NewsItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewsViewModel(
    application: Application,
    private val repository: NewsRepository
) : AndroidViewModel(application) {

    private val bookmarkDao: BookmarkDao =
        AppDatabase.getInstance(application).bookmarkDao()

    private val _news = MutableStateFlow<List<NewsItem>>(emptyList())
    val news: StateFlow<List<NewsItem>> = _news

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val bookmarks: StateFlow<Set<String>> = bookmarkDao
        .observeAll()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    init {
        observeLanguage()
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            LanguagePrefs
                .getLanguage(getApplication())
                .collect { lang -> loadNews(lang ?: "en") }
        }
    }

    private fun loadNews(lang: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _news.value = repository.getNews(lang).news
            } catch (_: Exception) {
                _error.value = "error_news_load_failed"
                _news.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

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

class NewsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(
                application,
                NewsRepository(ApiClient.newsApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
