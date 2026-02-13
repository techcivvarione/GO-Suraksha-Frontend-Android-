package com.gosuraksha.app.network

import com.gosuraksha.app.news.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("news/")
    suspend fun getNews(
        @Query("lang") lang: String
    ): NewsResponse
}
