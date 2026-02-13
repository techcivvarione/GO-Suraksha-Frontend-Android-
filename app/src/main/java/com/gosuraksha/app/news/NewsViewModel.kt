package com.gosuraksha.app.news

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.LanguageDataStore
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.news.model.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val _news = MutableStateFlow<List<NewsItem>>(emptyList())
    val news: StateFlow<List<NewsItem>> = _news

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        observeLanguage()
    }

    private fun observeLanguage() {
        viewModelScope.launch {

            LanguageDataStore
                .getSelectedLanguage(getApplication())
                .collect { lang ->

                    loadNews(lang ?: "en")
                }
        }
    }

    private fun loadNews(lang: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = ApiClient.newsApi.getNews(lang)
                _news.value = response.news

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load news"
            } finally {
                _loading.value = false
            }
        }
    }
}
