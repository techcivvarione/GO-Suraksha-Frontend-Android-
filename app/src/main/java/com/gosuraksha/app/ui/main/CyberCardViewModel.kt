package com.gosuraksha.app.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CyberCardViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "GO_SURAKSHA_CYBER_CARD"
    }

    private val _card = MutableStateFlow<CyberCardResponse?>(null)
    val card: StateFlow<CyberCardResponse?> = _card

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadCard() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _card.value = ApiClient.authApi.getCyberCard()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cyber card", e)
                _card.value = null
                _error.value = "Unable to load Cyber Card"
            } finally {
                _loading.value = false
            }
        }
    }
}
