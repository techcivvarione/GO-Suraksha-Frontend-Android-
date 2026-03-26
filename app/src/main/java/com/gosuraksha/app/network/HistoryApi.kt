package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.history.model.*
import retrofit2.http.*

interface HistoryApi {

    @GET("history/")
    suspend fun listHistory(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ApiResponse<HistoryListResponse>

    @GET("history/{history_id}")
    suspend fun getHistory(
        @Path("history_id") id: String
    ): ApiResponse<HistoryDetailResponse>

    @DELETE("history/{history_id}")
    suspend fun deleteHistory(
        @Path("history_id") id: String
    ): ApiResponse<DeleteHistoryResponse>
}
