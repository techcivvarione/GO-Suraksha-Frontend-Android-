package com.gosuraksha.app.learn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.repository.LearnRepository
import com.gosuraksha.app.learn.model.LearnArticle
import com.gosuraksha.app.learn.model.LearnTopic
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.news.db.AppDatabase
import com.gosuraksha.app.news.db.BookmarkDao
import com.gosuraksha.app.news.db.BookmarkEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LearnUiState(
    val selectedTopic: LearnTopic = LearnTopics.filters.first(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val articles: List<LearnArticle> = emptyList(),
    val recommended: List<LearnArticle> = emptyList(),
    val error: String? = null
) {
    val featuredArticle: LearnArticle?
        get() = recommended.firstOrNull() ?: articles.firstOrNull()

    val recommendedCards: List<LearnArticle>
        get() = recommended
            .filterNot { it.stableId == featuredArticle?.stableId }
            .take(3)
}

object LearnTopics {
    val filters = listOf(
        LearnTopic(label = "All Topics", apiValue = null),
        LearnTopic("Banking Fraud"),
        LearnTopic("Social Media"),
        LearnTopic("UPI Fraud"),
        LearnTopic("Phishing"),
        LearnTopic("OTP Fraud")
    )

    val browse = filters.drop(1) + LearnTopic("General Safety") + LearnTopic("Identity Theft")
}

class LearnViewModel(
    application: Application,
    private val repository: LearnRepository
) : AndroidViewModel(application) {

    private val bookmarkDao: BookmarkDao = AppDatabase.getInstance(application).bookmarkDao()

    private val _uiState = MutableStateFlow(LearnUiState())
    val uiState: StateFlow<LearnUiState> = _uiState

    val bookmarks: StateFlow<Set<String>> = bookmarkDao
        .observeAll()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    init {
        refresh()
    }

    fun selectTopic(topic: LearnTopic) {
        if (topic == _uiState.value.selectedTopic) return
        _uiState.update { it.copy(selectedTopic = topic) }
        refresh()
    }

    fun refresh() {
        val hasContent = _uiState.value.articles.isNotEmpty() || _uiState.value.recommended.isNotEmpty()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !hasContent,
                    isRefreshing = hasContent,
                    error = null
                )
            }

            runCatching {
                val selectedCategory = _uiState.value.selectedTopic.apiValue
                val articlesDeferred = async { repository.getArticles(selectedCategory) }
                val recommendedDeferred = async { repository.getRecommended() }
                articlesDeferred.await() to recommendedDeferred.await()
            }.onSuccess { (articles, recommended) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        articles = articles,
                        recommended = recommended,
                        error = null
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        articles = if (hasContent) it.articles else emptyList(),
                        recommended = if (hasContent) it.recommended else emptyList(),
                        error = "Unable to load learning content right now."
                    )
                }
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

class LearnViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearnViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LearnViewModel(
                application = application,
                repository = LearnRepository(ApiClient.learnApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
