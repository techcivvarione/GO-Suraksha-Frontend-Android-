package com.gosuraksha.app.network

import com.gosuraksha.app.history.model.*
import retrofit2.http.*

interface HistoryApi {

    @GET("history/")
    suspend fun listHistory(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): HistoryListResponse

    @GET("history/{history_id}")
    suspend fun getHistory(
        @Path("history_id") id: String
    ): HistoryDetailResponse

    @DELETE("history/{history_id}")
    suspend fun deleteHistory(
        @Path("history_id") id: String
    ): DeleteHistoryResponse
}
