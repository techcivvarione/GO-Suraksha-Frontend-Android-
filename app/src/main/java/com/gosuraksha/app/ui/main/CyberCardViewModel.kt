package com.gosuraksha.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CyberCardViewModel(application: Application) : AndroidViewModel(application) {

    private val _card = MutableStateFlow<CyberCardResponse?>(null)
    val card: StateFlow<CyberCardResponse?> = _card

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadCard() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _card.value = ApiClient.authApi.getCyberCard()
            } catch (e: Exception) {
                _card.value = null
            } finally {
                _loading.value = false
            }
        }
    }
}
