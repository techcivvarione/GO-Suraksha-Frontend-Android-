package com.gosuraksha.app.risk

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gosuraksha.app.data.repository.RiskRepository
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.risk.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import retrofit2.HttpException

class RiskViewModel(
    application: Application,
    private val repository: RiskRepository
) : AndroidViewModel(application) {

    private val _score = MutableStateFlow<RiskScoreResponse?>(null)
    val score: StateFlow<RiskScoreResponse?> = _score

    private val _timeline = MutableStateFlow<List<RiskTimelinePoint>>(emptyList())
    val timeline: StateFlow<List<RiskTimelinePoint>> = _timeline

    private val _insights = MutableStateFlow<RiskInsightsSummary?>(null)
    val insights: StateFlow<RiskInsightsSummary?> = _insights

    private val _upgradeRequired = MutableStateFlow(false)
    val upgradeRequired: StateFlow<Boolean> = _upgradeRequired

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Counts how many async loads are in flight; loading = true while > 0.
    private val loadingCount = AtomicInteger(0)

    private fun beginLoad() {
        if (loadingCount.incrementAndGet() == 1) _loading.value = true
    }

    private fun endLoad() {
        if (loadingCount.decrementAndGet() == 0) _loading.value = false
    }

    // -----------------------------
    // ERROR PARSER
    // -----------------------------

    private fun parseHttpError(e: HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody.isNullOrEmpty()) {
                "error_server"
            } else {
                val json = Gson().fromJson(errorBody, JsonObject::class.java)

                when {
                    json.has("detail") -> json.get("detail").asString
                    json.has("error") -> json.get("error").asString
                    json.has("message") -> json.get("message").asString
                    else -> "error_server"
                }
            }
        } catch (ex: Exception) {
            "error_server"
        }
    }

    // -----------------------------
    // LOAD SCORE
    // -----------------------------

    fun loadScore() {
        viewModelScope.launch {
            beginLoad()
            try {
                _error.value = null
                _score.value = repository.getRiskScore()
            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = "error_server"
            } finally {
                endLoad()
            }
        }
    }

    // -----------------------------
    // LOAD TIMELINE
    // -----------------------------

    fun loadTimeline() {
        viewModelScope.launch {
            beginLoad()
            try {
                _timeline.value = repository.getRiskTimeline().points
            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = "error_server"
            } finally {
                endLoad()
            }
        }
    }

    // -----------------------------
    // LOAD INSIGHTS
    // -----------------------------

    fun loadInsights() {
        viewModelScope.launch {
            beginLoad()
            try {
                val response = repository.getRiskInsights()
                _insights.value = response.summary
                _upgradeRequired.value = false
            } catch (e: HttpException) {
                if (e.code() == 403) {
                    _upgradeRequired.value = true
                } else {
                    _error.value = parseHttpError(e)
                }
            } catch (e: Exception) {
                _error.value = "error_server"
            } finally {
                endLoad()
            }
        }
    }
}

class RiskViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RiskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RiskViewModel(
                application,
                RiskRepository(ApiClient.riskApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
