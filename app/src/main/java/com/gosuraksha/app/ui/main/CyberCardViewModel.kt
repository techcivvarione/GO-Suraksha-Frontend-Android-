package com.gosuraksha.app.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.repository.CyberCardRepository
import com.gosuraksha.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CyberCardViewModel(
    application: Application,
    private val repository: CyberCardRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "GO_SURAKSHA_CYBER_CARD"
    }

    private val _card = MutableStateFlow<CyberCardResponse?>(null)
    val card: StateFlow<CyberCardResponse?> = _card

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Tracks the last known score so we can detect improvements after actions
    private val _previousScore = MutableStateFlow<Int?>(null)

    // Non-null when score improved after a silent refresh — shown as a toast
    private val _improvementMessage = MutableStateFlow<String?>(null)
    val improvementMessage: StateFlow<String?> = _improvementMessage

    fun clearImprovementMessage() {
        _improvementMessage.value = null
    }

    /** Full load with loading spinner — used on first open. */
    fun loadCard() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val fetched = repository.getCyberCard()
                _card.value = fetched
                _previousScore.value = fetched?.score
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cyber card", e)
                _card.value = null
                _error.value = "Unable to load Cyber Card"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Silent background refresh — no loading spinner.
     * Called after the user taps an action (e.g. "Scan password") and returns.
     * If the score improved, sets [improvementMessage] for a toast.
     */
    fun refreshCard() {
        viewModelScope.launch {
            try {
                val before = _previousScore.value
                val fetched = repository.getCyberCard()
                val after = fetched?.score

                if (before != null && after != null && after > before) {
                    val delta = after - before
                    _improvementMessage.value = "+$delta — Your safety score improved"
                }

                _card.value = fetched
                _previousScore.value = after
            } catch (e: Exception) {
                Log.e(TAG, "Silent refresh failed", e)
                // Silent — never shows error to user
            }
        }
    }
}

class CyberCardViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CyberCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CyberCardViewModel(
                application,
                CyberCardRepository(ApiClient.authApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
