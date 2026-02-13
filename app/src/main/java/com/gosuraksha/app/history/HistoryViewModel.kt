package com.gosuraksha.app.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.history.model.*
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history

    private val _selected = MutableStateFlow<HistoryDetailResponse?>(null)
    val selected: StateFlow<HistoryDetailResponse?> = _selected

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadHistory(limit: Int = 20, offset: Int = 0) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.historyApi.listHistory(limit, offset)
                _history.value = response.history
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            try {
                _selected.value = ApiClient.historyApi.getHistory(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteHistory(id: String) {
        viewModelScope.launch {
            try {
                ApiClient.historyApi.deleteHistory(id)
                loadHistory()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
