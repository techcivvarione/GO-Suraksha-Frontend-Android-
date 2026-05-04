package com.gosuraksha.app.network

import com.google.gson.JsonElement
import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LearnApi {

    @GET("learn/articles")
    suspend fun getArticles(
        @Query("category") category: String? = null
    ): ApiResponse<JsonElement>

    @GET("learn/recommended")
    suspend fun getRecommended(): ApiResponse<JsonElement>
}
