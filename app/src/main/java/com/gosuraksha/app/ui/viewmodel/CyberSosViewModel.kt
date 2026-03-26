package com.gosuraksha.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import com.gosuraksha.app.data.mapper.requireData
import com.gosuraksha.app.data.remote.dto.CyberSosResponse
import com.gosuraksha.app.data.repository.SecurityRepository
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

class CyberSosViewModel(
    private val repository: SecurityRepository
) : ViewModel() {

    var uiState by mutableStateOf(CyberSosState())
        private set

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

    fun triggerSos(request: CyberSosRequest) {
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val response = repository.triggerCyberSos(request)

                if (response.isSuccessful) {
                    val body = try {
                        response.requireData("Cyber SOS response body missing")
                    } catch (_: IllegalStateException) {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = "error_cybersos_failed:${response.code()}"
                        )
                        return@launch
                    }
                    uiState = uiState.copy(
                        isLoading = false,
                        success = true,
                        data = body
                    )
                } else {
                    val parsed = parseCyberSosError(response.errorBody())
                    uiState = uiState.copy(
                        isLoading = false,
                        error = parsed ?: "error_cybersos_failed:${response.code()}"
                    )
                }

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "error_network"
                )
            }
        }
    }
}


data class CyberSosState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val data: CyberSosResponse? = null
)

class CyberSosViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CyberSosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CyberSosViewModel(
                SecurityRepository(ApiClient.securityApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
