package com.gosuraksha.app.security.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gosuraksha.app.data.repository.SecurityRepository
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.security.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException

class SecurityViewModel(
    application: Application,
    private val repository: SecurityRepository
) : AndroidViewModel(application) {

    // -----------------------------
    // GLOBAL STATE
    // -----------------------------

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // -----------------------------
    // DASHBOARD DATA
    // -----------------------------

    private val _status = MutableStateFlow<SecurityStatusResponse?>(null)
    val status: StateFlow<SecurityStatusResponse?> = _status

    private val _healthScore = MutableStateFlow<HealthScoreResponse?>(null)
    val healthScore: StateFlow<HealthScoreResponse?> = _healthScore

    private val _auditLogs = MutableStateFlow<List<AuditLogItem>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogItem>> = _auditLogs

    private val _healthTrend = MutableStateFlow<List<HealthTrendPoint>>(emptyList())
    val healthTrend: StateFlow<List<HealthTrendPoint>> = _healthTrend

    // -----------------------------
    // ERROR PARSER (NEW)
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
    // DASHBOARD LOAD
    // -----------------------------

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                _status.value = repository.getSecurityStatus()
                _healthScore.value = repository.getHealthScore()

            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadAuditLogs(
        limit: Int = 20,
        offset: Int = 0,
        eventType: String? = null
    ) {
        viewModelScope.launch {
            try {
                val response = repository.getAuditLogs(limit, offset, eventType)
                _auditLogs.value = response.data
            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadHealthTrend(days: Int = 30) {
        viewModelScope.launch {
            try {
                val response = repository.getHealthTrend(days)
                _healthTrend.value = response.trend
            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // -----------------------------
    // PASSWORD / SESSION
    // -----------------------------

    fun logoutAll() {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.logoutAll()
                _message.value = "security_sessions_logged_out"
            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun changePassword(
        current: String,
        new: String,
        confirm: String
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true

                repository.changePassword(
                    ChangePasswordRequest(
                        current_password = current,
                        new_password = new,
                        confirm_password = confirm
                    )
                )

                _message.value = "security_password_changed"

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
    // EXPORT
    // -----------------------------

    fun exportEvidence(
        onSuccess: (ResponseBody) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val file = repository.exportEvidence()
                onSuccess(file)
            } catch (e: HttpException) {
                _error.value = parseHttpError(e)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}

class SecurityViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecurityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecurityViewModel(
                application,
                SecurityRepository(ApiClient.securityApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
