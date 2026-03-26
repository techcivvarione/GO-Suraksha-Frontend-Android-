package com.gosuraksha.app.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.repository.HistoryRepository
import com.gosuraksha.app.history.model.*
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    application: Application,
    private val repository: HistoryRepository
) : AndroidViewModel(application) {

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
                val response = repository.listHistory(limit, offset)
                _history.value = response.history.orEmpty()
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
                _selected.value = repository.getHistory(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteHistory(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteHistory(id)
                loadHistory()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

class HistoryViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                application,
                HistoryRepository(ApiClient.historyApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
