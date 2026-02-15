package com.gosuraksha.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.remote.dto.CyberSosRequest
import com.gosuraksha.app.data.remote.dto.CyberSosResponse
import com.gosuraksha.app.data.repository.SecurityRepository
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.launch
import retrofit2.Response

class CyberSosViewModel : ViewModel() {

    // create repository manually
    private val repository = SecurityRepository(ApiClient.securityApi)

    var uiState by mutableStateOf(CyberSosState())
        private set

    fun triggerSos(request: CyberSosRequest) {

        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val response = repository.triggerCyberSos(request)

                if (response.isSuccessful && response.body() != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        success = true,
                        data = response.body()
                    )
                } else {
                    if (response.code() == 429) {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = "Please wait 30 seconds before triggering again."
                        )
                    } else {
                        uiState = uiState.copy(
                            isLoading = false,
                            error = "Something went wrong."
                        )
                    }
                }

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Network error."
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
