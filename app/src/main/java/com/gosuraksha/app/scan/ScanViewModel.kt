package com.gosuraksha.app.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.scan.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val _result = MutableStateFlow<AnalyzeResponse?>(null)
    val result: StateFlow<AnalyzeResponse?> = _result

    private val _aiExplanation = MutableStateFlow<String?>(null)
    val aiExplanation: StateFlow<String?> = _aiExplanation

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
    // ANALYZE
    // -----------------------------

    fun analyze(type: String, content: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _upgradeRequired.value = false
                _aiExplanation.value = null

                val response = ApiClient.analyzeApi.analyze(
                    AnalyzeRequest(
                        type = type,
                        content = content
                    )
                )

                _result.value = response

                // FREE user upgrade case (EMAIL)
                if (response.upgrade?.required == true) {
                    _upgradeRequired.value = true
                }

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
    // AI EXPLAIN (PAID ONLY)
    // -----------------------------

    fun loadAiExplanation(scanId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.analyzeApi.explain(
                    AiExplainRequest(scan_id = scanId)
                )
                _aiExplanation.value = response.ai_explanation
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
