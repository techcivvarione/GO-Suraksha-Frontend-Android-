package com.gosuraksha.app.risk

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.risk.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RiskViewModel(application: Application) : AndroidViewModel(application) {

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

    // -----------------------------
    // ERROR PARSER
    // -----------------------------

    private fun parseHttpError(e: HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody.isNullOrEmpty()) {
                "Server error (${e.code()})"
            } else {
                val json = Gson().fromJson(errorBody, JsonObject::class.java)

                when {
                    json.has("detail") -> json.get("detail").asString
                    json.has("error") -> json.get("error").asString
                    json.has("message") -> json.get("message").asString
                    else -> "Server error (${e.code()})"
                }
            }
        } catch (ex: Exception) {
            "Server error (${e.code()})"
        }
    }

    // -----------------------------
    // LOAD SCORE
    // -----------------------------

    fun loadScore() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _score.value = ApiClient.riskApi.getRiskScore()
            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    // -----------------------------
    // LOAD TIMELINE
    // -----------------------------

    fun loadTimeline() {
        viewModelScope.launch {
            try {
                val response = ApiClient.riskApi.getRiskTimeline()
                _timeline.value = response.points
            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // -----------------------------
    // LOAD INSIGHTS
    // -----------------------------

    fun loadInsights() {
        viewModelScope.launch {
            try {
                val response = ApiClient.riskApi.getRiskInsights()
                _insights.value = response.summary
                _upgradeRequired.value = false
            } catch (e: HttpException) {

                if (e.code() == 403) {
                    _upgradeRequired.value = true
                } else {
                    _error.value = parseHttpError(e)
                }

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
