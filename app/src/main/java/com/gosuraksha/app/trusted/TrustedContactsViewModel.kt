package com.gosuraksha.app.trusted

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.repository.TrustedContactsRepository
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.trusted.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrustedContactsViewModel(
    application: Application,
    private val repository: TrustedContactsRepository
) :
    AndroidViewModel(application) {

    private val _contacts = MutableStateFlow<List<TrustedContact>>(emptyList())
    val contacts: StateFlow<List<TrustedContact>> = _contacts

    private val _alerts = MutableStateFlow<List<TrustedAlert>>(emptyList())
    val alerts: StateFlow<List<TrustedAlert>> = _alerts

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadContacts() {
        viewModelScope.launch {
            try {
                _error.value = null
                _loading.value = true

                val response = repository.listTrustedContacts()

                _contacts.value = response.data ?: emptyList()

            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message ?: "error_trusted_contacts_load_failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addContact(name: String, email: String?, phone: String?) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.addTrustedContact(
                    AddTrustedContactRequest(name, email, phone)
                )
                loadContacts()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.deleteTrustedContact(id)
                loadContacts()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadAlerts() {
        viewModelScope.launch {
            try {
                _error.value = null

                val response = repository.getTrustedAlerts()

                _alerts.value = response.alerts ?: emptyList()

            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message ?: "error_trusted_alerts_load_failed"
            }
        }
    }
}

class TrustedContactsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrustedContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrustedContactsViewModel(
                application,
                TrustedContactsRepository(ApiClient.trustedContactsApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
