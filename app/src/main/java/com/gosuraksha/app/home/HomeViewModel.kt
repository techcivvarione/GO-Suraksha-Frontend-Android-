package com.gosuraksha.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.home.model.HomeOverviewResponse
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _overview = MutableStateFlow<HomeOverviewResponse?>(null)
    val overview: StateFlow<HomeOverviewResponse?> = _overview

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadOverview()
    }

    fun loadOverview() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = ApiClient.homeApi.getOverview()
                _overview.value = response

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
