package com.gosuraksha.app.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.profile.model.ProfileResponse
import com.gosuraksha.app.profile.model.UpgradePlanRequest
import com.gosuraksha.app.profile.model.UpdateProfileRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // Temporary UI-only avatar state (not persisted to backend)
    private val _avatarImageUri = MutableStateFlow<String?>(null)
    val avatarImageUri: StateFlow<String?> = _avatarImageUri

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.profileApi.getProfile()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _profile.value = body.copy(
                            plan = body.plan?.ifBlank { "FREE" } ?: "FREE"
                        )
                    } else {
                        _message.value = "error_generic"
                    }
                } else {
                    _message.value = "error_server"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load profile", e)
                _message.value = "error_generic"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                ApiClient.profileApi.updateProfile(
                    UpdateProfileRequest(name, phone)
                )
                _message.value = "profile_updated"
                loadProfile()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update profile", e)
                _message.value = "profile_update_failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun setAvatarImageUri(uri: String?) {
        _avatarImageUri.value = uri
    }

    fun upgradeToPremium() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.profileApi.upgradePlan(
                    UpgradePlanRequest(plan = "GO_PRO")
                )
                if (response.isSuccessful) {
                    _message.value = "profile_updated"
                    loadProfile()
                } else {
                    _message.value = "error_server"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upgrade plan", e)
                _message.value = "error_generic"
            } finally {
                _loading.value = false
            }
        }
    }
}
