package com.gosuraksha.app.data.repository

import com.gosuraksha.app.network.NewsApi
import com.gosuraksha.app.news.model.NewsResponse

class NewsRepository(
    private val api: NewsApi
) {
    suspend fun getNews(lang: String): NewsResponse = api.getNews(lang).data
}
