package com.gosuraksha.app.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.profile.model.ProfileResponse
import com.gosuraksha.app.profile.model.UpdateProfileRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = ApiClient.profileApi.getProfile()
                _profile.value = response
            } catch (e: Exception) {
                _message.value = e.message
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
                _message.value = "Profile updated"
                loadProfile()
            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
