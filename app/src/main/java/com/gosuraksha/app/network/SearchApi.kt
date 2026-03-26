package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

data class SearchResultItem(
    val id: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val risk: String,
    val score: Int,
    val icon: String,
    val created_at: String,
)

data class SearchResponse(
    val query: String,
    val results: List<SearchResultItem>,
    val total: Int,
)

interface SearchApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String? = null,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<SearchResponse>
}
