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

    private val _pendingInvites = MutableStateFlow<List<PendingInvite>>(emptyList())
    val pendingInvites: StateFlow<List<PendingInvite>> = _pendingInvites

    private val _familyMembers = MutableStateFlow<List<FamilyMemberDashboardItem>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMemberDashboardItem>> = _familyMembers

    private val _capabilities = MutableStateFlow(FamilyProtectionCapabilities())
    val capabilities: StateFlow<FamilyProtectionCapabilities> = _capabilities

    private val _dashboardMode = MutableStateFlow<String?>(null)
    val dashboardMode: StateFlow<String?> = _dashboardMode

    private val _ownSecureNow = MutableStateFlow<List<SecureNowItem>>(emptyList())
    val ownSecureNow: StateFlow<List<SecureNowItem>> = _ownSecureNow

    private val _familySecureNow = MutableStateFlow<List<SecureNowItem>>(emptyList())
    val familySecureNow: StateFlow<List<SecureNowItem>> = _familySecureNow

    private val _notifications = MutableStateFlow(NotificationFeedResponse())
    val notifications: StateFlow<NotificationFeedResponse> = _notifications

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

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
                if (phone.isNullOrBlank()) {
                    repository.addTrustedContact(AddTrustedContactRequest(name, email, phone))
                } else {
                    repository.inviteTrustedContact(
                        InviteTrustedContactRequest(
                            name = name,
                            phone = phone,
                            relationship = null,
                            add_to_family = true
                        )
                    )
                    _statusMessage.value = "Invite sent"
                    loadPendingInvites()
                    loadNotifications()
                }
                loadDashboard()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun sendInvite(name: String, phone: String, addToFamily: Boolean) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.inviteTrustedContact(
                    InviteTrustedContactRequest(
                        name = name,
                        phone = phone,
                        relationship = null,
                        add_to_family = addToFamily
                    )
                )
                _statusMessage.value = "Invite sent"
                loadDashboard()
                loadNotifications()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to send invite"
            }
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.deleteTrustedContact(id)
                loadContacts()
                loadDashboard()
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

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _error.value = null
                _loading.value = true
                val dashboard = repository.getFamilyDashboard()
                _familyMembers.value = dashboard.members
                _capabilities.value = dashboard.capabilities ?: FamilyProtectionCapabilities()
                _dashboardMode.value = dashboard.mode
                loadContacts()
                loadPendingInvites()
                loadSecureNow()
                loadNotifications()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to load family dashboard"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadPendingInvites() {
        viewModelScope.launch {
            runCatching { repository.getPendingInvites() }
                .onSuccess {
                    _pendingInvites.value = it.invites
                    loadNotifications()
                }
                .onFailure { _error.value = it.message ?: "Unable to load invites" }
        }
    }

    fun respondToInvite(inviteId: String, action: String, addToFamily: Boolean = true) {
        viewModelScope.launch {
            try {
                _error.value = null
                repository.respondToInvite(
                    InviteActionRequest(
                        invite_id = inviteId,
                        action = action,
                        add_to_family = addToFamily
                    )
                )
                _statusMessage.value = if (action == "ACCEPT") "Invite accepted" else "Invite rejected"
                loadDashboard()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to update invite"
            }
        }
    }

    fun loadSecureNow() {
        viewModelScope.launch {
            runCatching { repository.getSecureNow() }
                .onSuccess {
                    _capabilities.value = it.capabilities ?: _capabilities.value
                    _ownSecureNow.value = it.own_items
                    _familySecureNow.value = it.family_items
                }
                .onFailure { _error.value = it.message ?: "Unable to load secure now items" }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            runCatching { repository.getNotifications() }
                .onSuccess { _notifications.value = it }
                .onFailure { _error.value = it.message ?: "Unable to load notifications" }
        }
    }

    fun completeSecureNow(itemId: String) {
        viewModelScope.launch {
            try {
                repository.completeSecureNow(itemId)
                _statusMessage.value = "Task completed"
                loadSecureNow()
                loadNotifications()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to update task"
            }
        }
    }

    fun setPrimaryContact(contactId: String) {
        viewModelScope.launch {
            try {
                repository.setPrimaryContact(contactId)
                _statusMessage.value = "Primary contact updated"
                loadContacts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to update primary contact"
            }
        }
    }

    fun triggerManualAlert() {
        viewModelScope.launch {
            try {
                repository.triggerManualAlert(ManualAlertRequest())
                _statusMessage.value = "Alert sent manually"
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to send alert"
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
