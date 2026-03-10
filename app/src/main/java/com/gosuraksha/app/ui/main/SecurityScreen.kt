package com.gosuraksha.app.security

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.security.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _status = MutableStateFlow<SecurityStatusResponse?>(null)
    val status: StateFlow<SecurityStatusResponse?> = _status

    private val _healthScore = MutableStateFlow<HealthScoreResponse?>(null)
    val healthScore: StateFlow<HealthScoreResponse?> = _healthScore

    private val _auditLogs = MutableStateFlow<List<AuditLogItem>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogItem>> = _auditLogs

    private val _healthTrend = MutableStateFlow<List<HealthTrendPoint>>(emptyList())
    val healthTrend: StateFlow<List<HealthTrendPoint>> = _healthTrend

    private fun parseCyberSosError(errorBody: ResponseBody?): String? {
        return try {
            val body = errorBody?.string() ?: return null
            val json = Gson().fromJson(body, JsonObject::class.java)

            if (json.has("detail")) {
                val detail = json.get("detail")
                if (detail.isJsonObject) {
                    val obj = detail.asJsonObject
                    if (obj.has("message")) return obj.get("message").asString
                    if (obj.has("error")) return obj.get("error").asString
                } else if (detail.isJsonPrimitive) {
                    return detail.asString
                }
            }

            if (json.has("message")) return json.get("message").asString
            if (json.has("error")) return json.get("error").asString
            null
        } catch (e: Exception) {
            null
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                _status.value = ApiClient.securityApi.getSecurityStatus()
                _healthScore.value = ApiClient.securityApi.getHealthScore()

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
                val response = ApiClient.securityApi.getAuditLogs(limit, offset, eventType)
                _auditLogs.value = response.data
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadHealthTrend(days: Int = 30) {
        viewModelScope.launch {
            try {
                val response = ApiClient.securityApi.getHealthTrend(days)
                _healthTrend.value = response.trend
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun logoutAll() {
        viewModelScope.launch {
            try {
                _loading.value = true
                ApiClient.securityApi.logoutAll()
                _message.value = "security_sessions_logged_out"
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

                ApiClient.securityApi.changePassword(
                    ChangePasswordRequest(
                        current_password = current,
                        new_password = new,
                        confirm_password = confirm
                    )
                )

                _message.value = "security_password_changed"

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun reportScam(request: ScamReportRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.securityApi.reportScam(request)
                _message.value = "security_report_submitted:${response.report_id}"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun confirmScam(request: ScamReportRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.securityApi.confirmScam(request)
                _message.value = response.next_steps.joinToString("\n")
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun previewCyberComplaint(request: CyberComplaintPreviewRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.securityApi.previewCyberComplaint(request)
                _message.value = response.complaint_text
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun exportEvidence(
        onSuccess: (ResponseBody) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val file = ApiClient.securityApi.exportEvidence()
                onSuccess(file)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
