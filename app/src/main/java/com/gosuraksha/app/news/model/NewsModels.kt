package com.gosuraksha.app.news.model

data class NewsResponse(
    val count: Int,
    val news: List<NewsItem>
)

data class NewsItem(
    val source: String,
    val category: String,
    val title: String,
    val summary: String?,
    val image: String?,
    val link: String?,
    val published_at: String,
    val is_trending: Boolean
)
