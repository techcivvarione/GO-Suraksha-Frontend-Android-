package com.gosuraksha.app.data.repository

import com.gosuraksha.app.alerts.model.AlertEvent
import com.gosuraksha.app.alerts.model.AlertsSummaryResponse
import com.gosuraksha.app.alerts.model.FamilyActivityItem
import com.gosuraksha.app.alerts.model.FamilyActivityResponse
import com.gosuraksha.app.alerts.model.FamilyFeedItem
import com.gosuraksha.app.alerts.model.RefreshAlertsResponse
import com.gosuraksha.app.alerts.model.SubscribeAlertsResponse
import com.gosuraksha.app.alerts.model.TrustedAlertReadResponse
import com.gosuraksha.app.alerts.model.TrustedAlertsResponse
import com.gosuraksha.app.network.AlertsApi

class AlertsRepository(
    private val api: AlertsApi
) {
    suspend fun getAlerts(): List<AlertEvent> {
        val response = api.getAlerts()
        if (!response.isSuccessful) {
            val code = response.code()
            if (code == 404) return emptyList()
            throw retrofit2.HttpException(response)
        }
        return response.body()?.data?.alerts.orEmpty()
    }

    suspend fun getAlertsSummary(): AlertsSummaryResponse {
        // .data is non-nullable in ApiResponse<T>, but guard against a misconfigured
        // Gson where T gets deserialized as LinkedTreeMap and then cast later.
        return try {
            api.getAlertsSummary().data
        } catch (e: Exception) {
            android.util.Log.e("AlertsRepository", "getAlertsSummary failed: ${e.message}", e)
            AlertsSummaryResponse.empty()
        }
    }

    suspend fun refreshAlerts(): RefreshAlertsResponse = api.refreshAlerts().data

    suspend fun subscribeAlerts(categories: List<String>): SubscribeAlertsResponse {
        return api.subscribeAlerts(mapOf("categories" to categories)).data
    }

    suspend fun getTrustedAlerts(limit: Int, offset: Int): TrustedAlertsResponse {
        return api.getTrustedAlerts(limit, offset).data
    }

    suspend fun markTrustedAlertRead(id: String): TrustedAlertReadResponse {
        return api.markTrustedAlertRead(id).data
    }

    suspend fun getFamilyActivity(limit: Int = 20): List<FamilyActivityItem> {
        return try {
            api.getFamilyActivity(limit).data.activity
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getFamilyFeed(limit: Int = 20): List<FamilyFeedItem> {
        return try {
            api.getFamilyFeed(limit).data.feed
        } catch (_: Exception) {
            emptyList()
        }
    }
}
