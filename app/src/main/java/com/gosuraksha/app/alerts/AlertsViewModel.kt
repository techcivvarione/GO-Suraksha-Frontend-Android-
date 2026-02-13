package com.gosuraksha.app.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.alerts.model.*
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val alerts: StateFlow<List<AlertItem>> = _alerts

    private val _summary = MutableStateFlow<AlertsSummaryResponse?>(null)
    val summary: StateFlow<AlertsSummaryResponse?> = _summary

    private val _trusted = MutableStateFlow<List<TrustedAlertItem>>(emptyList())
    val trusted: StateFlow<List<TrustedAlertItem>> = _trusted

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun loadAlerts() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.alertsApi.getAlerts()
                _alerts.value = response.alerts
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadSummary() {
        viewModelScope.launch {
            try {
                _summary.value = ApiClient.alertsApi.getAlertsSummary()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun refreshAlerts() {
        viewModelScope.launch {
            try {
                ApiClient.alertsApi.refreshAlerts()
                loadAlerts()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadTrusted(limit: Int = 20, offset: Int = 0) {
        viewModelScope.launch {
            try {
                val response = ApiClient.alertsApi.getTrustedAlerts(limit, offset)
                _trusted.value = response.alerts
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun markTrustedRead(id: String) {
        viewModelScope.launch {
            try {
                ApiClient.alertsApi.markTrustedAlertRead(id)
                loadTrusted()
            } catch (e: HttpException) {
                _error.value = "Trusted alert not found"
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun subscribe(categories: List<String>) {
        viewModelScope.launch {
            try {
                ApiClient.alertsApi.subscribeAlerts(
                    mapOf("categories" to categories)
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
