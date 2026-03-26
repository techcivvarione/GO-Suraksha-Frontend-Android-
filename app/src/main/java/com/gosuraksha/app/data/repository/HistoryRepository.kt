package com.gosuraksha.app.data.repository

import com.gosuraksha.app.history.model.DeleteHistoryResponse
import com.gosuraksha.app.history.model.HistoryDetailResponse
import com.gosuraksha.app.history.model.HistoryListResponse
import com.gosuraksha.app.network.HistoryApi

class HistoryRepository(
    private val api: HistoryApi
) {
    suspend fun listHistory(limit: Int, offset: Int): HistoryListResponse =
        api.listHistory(limit, offset).data

    suspend fun getHistory(id: String): HistoryDetailResponse =
        api.getHistory(id).data

    suspend fun deleteHistory(id: String): DeleteHistoryResponse =
        api.deleteHistory(id).data
}
