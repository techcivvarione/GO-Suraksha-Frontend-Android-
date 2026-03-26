package com.gosuraksha.app.alerts

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.BuildConfig
import com.gosuraksha.app.alerts.model.*
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.data.repository.AlertsRepository
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AlertsViewModel(
    application: Application,
    private val repository: AlertsRepository
) : AndroidViewModel(application) {

    private val _alerts = MutableStateFlow<List<AlertEvent>>(emptyList())
    val alerts: StateFlow<List<AlertEvent>> = _alerts

    // Non-nullable — starts with empty sentinel so the summary card is always rendered.
    private val _summary = MutableStateFlow(AlertsSummaryResponse.empty())
    val summary: StateFlow<AlertsSummaryResponse> = _summary

    // STEP 3: separate loading state for the summary card shimmer
    private val _summaryLoading = MutableStateFlow(false)
    val summaryLoading: StateFlow<Boolean> = _summaryLoading

    // STEP 4: true when all retry attempts have failed — shows manual retry banner
    private val _summaryFailed = MutableStateFlow(false)
    val summaryFailed: StateFlow<Boolean> = _summaryFailed

    private val _trusted = MutableStateFlow<List<TrustedAlertItem>>(emptyList())
    val trusted: StateFlow<List<TrustedAlertItem>> = _trusted

    private val _familyActivity = MutableStateFlow<List<FamilyActivityItem>>(emptyList())
    val familyActivity: StateFlow<List<FamilyActivityItem>> = _familyActivity

    // STEP 2: alert_events based family feed
    private val _familyFeed = MutableStateFlow<List<FamilyFeedItem>>(emptyList())
    val familyFeed: StateFlow<List<FamilyFeedItem>> = _familyFeed

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadAlerts() {
        if (!SessionManager.isLoggedIn()) {
            _alerts.value = emptyList()
            _error.value = "error_unauthorized"
            return
        }

        viewModelScope.launch {
            try {
                _loading.value = true
                if (BuildConfig.DEBUG) Log.d("AlertsViewModel", "Loading alerts")
                _alerts.value = repository.getAlerts()
                _error.value = null
            } catch (_: Exception) {
                _alerts.value = emptyList()
                _error.value = "error_server"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadSummary() {
        if (!SessionManager.isLoggedIn()) {
            _error.value = "error_unauthorized"
            return
        }

        viewModelScope.launch {
            _summaryFailed.value = false
            _summaryLoading.value = true
            var succeeded = false

            // STEP 4: auto-retry once after 1.5 s before showing failure banner
            repeat(2) { attempt ->
                if (!succeeded) {
                    if (attempt > 0) delay(1_500)
                    try {
                        val result = repository.getAlertsSummary()
                        Log.d(
                            "ALERTS_SUMMARY",
                            "total=${result.total_alerts}  high=${result.high_risk}  " +
                            "medium=${result.medium_risk}  low=${result.low_risk}  " +
                            "level=${result.risk_level_today}"
                        )
                        _summary.value = result
                        _error.value = null
                        _summaryFailed.value = false
                        succeeded = true
                    } catch (e: Exception) {
                        Log.e("ALERTS_SUMMARY", "loadSummary attempt ${attempt + 1} FAILED: ${e.message}", e)
                        if (attempt == 1) {
                            // Both attempts exhausted — show retry banner
                            _summaryFailed.value = true
                            _error.value = "error_server"
                        }
                    }
                }
            }

            _summaryLoading.value = false
        }
    }

    /** STEP 4: manual retry — re-loads both the summary card and the alerts list. */
    fun retrySummary() {
        loadSummary()
        loadAlerts()
    }

    fun refreshAlerts() {
        if (!SessionManager.isLoggedIn()) {
            _error.value = "error_unauthorized"
            return
        }

        viewModelScope.launch {
            try {
                repository.refreshAlerts()
                loadAlerts()
                loadSummary()   // refresh counts after pull-to-refresh
            } catch (_: Exception) {
                _error.value = "error_server"
            }
        }
    }

    fun loadTrusted(limit: Int = 20, offset: Int = 0) {
        if (!SessionManager.isLoggedIn()) {
            _trusted.value = emptyList()
            _error.value = "error_unauthorized"
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.getTrustedAlerts(limit, offset)
                _trusted.value = response.alerts
            } catch (_: Exception) {
                _trusted.value = emptyList()
                _error.value = "error_trusted_alerts_load_failed"
            }
        }
    }

    fun loadFamilyActivity() {
        if (!SessionManager.isLoggedIn()) return
        viewModelScope.launch {
            try {
                _familyActivity.value = repository.getFamilyActivity()
            } catch (_: Exception) {
                // non-critical; silently ignore so other tabs still load
            }
        }
    }

    /** STEP 2: load alert_events based family feed (richer than scan_history). */
    fun loadFamilyFeed() {
        if (!SessionManager.isLoggedIn()) return
        viewModelScope.launch {
            try {
                _familyFeed.value = repository.getFamilyFeed()
            } catch (_: Exception) {
                // non-critical; fallback to empty — family-activity still shows
            }
        }
    }

    fun markTrustedRead(id: String) {
        if (!SessionManager.isLoggedIn()) {
            _error.value = "error_unauthorized"
            return
        }

        viewModelScope.launch {
            try {
                repository.markTrustedAlertRead(id)
                loadTrusted()
            } catch (_: HttpException) {
                _error.value = "error_trusted_alert_not_found"
            } catch (_: Exception) {
                _error.value = "error_server"
            }
        }
    }

    fun subscribe(categories: List<String>) {
        if (!SessionManager.isLoggedIn()) {
            _error.value = "error_unauthorized"
            return
        }

        viewModelScope.launch {
            try {
                repository.subscribeAlerts(categories)
            } catch (_: Exception) {
                _error.value = "error_server"
            }
        }
    }
}

class AlertsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(
                application,
                AlertsRepository(ApiClient.alertsApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
