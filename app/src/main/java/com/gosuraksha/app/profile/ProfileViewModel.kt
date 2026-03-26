package com.gosuraksha.app.profile

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.datatransport.BuildConfig
import com.google.gson.Gson
import com.gosuraksha.app.core.result.AppError
import com.gosuraksha.app.core.result.AppResult
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.data.local.TokenLocalDataSource
import com.gosuraksha.app.data.mapper.requireData
import com.gosuraksha.app.data.mapper.toDomain
import com.gosuraksha.app.data.remote.AuthRemoteDataSource
import com.gosuraksha.app.data.repository.AuthRepositoryImpl
import com.gosuraksha.app.data.repository.ProfileRepository
import com.gosuraksha.app.domain.model.User
import com.gosuraksha.app.domain.repository.AuthRepository
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.network.QuotaResponse
import com.gosuraksha.app.profile.model.ProfileResponse
import com.gosuraksha.app.profile.model.UpdateProfileRequest
import com.gosuraksha.app.profile.model.UpgradePlanRequest
import com.gosuraksha.app.security.EncryptedTokenStorage
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ProfileViewModel(
    application: Application,
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val tokenLocalDataSource: TokenLocalDataSource
) : AndroidViewModel(application) {

    private companion object {
        private const val TAG = "ProfileViewModel"
        private const val MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L
        private val allowedMimeTypes = setOf("image/jpeg", "image/png")
    }

    private val gson = Gson()

    private val _profile = MutableStateFlow(SessionManager.user.value?.toProfileResponse())
    val profile: StateFlow<ProfileResponse?> = _profile

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _avatarImageUri = MutableStateFlow<String?>(null)
    val avatarImageUri: StateFlow<String?> = _avatarImageUri

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    // ── Scan quota (null = not yet loaded or failed) ──────────────────────────
    private val _quota = MutableStateFlow<QuotaResponse?>(null)
    val quota: StateFlow<QuotaResponse?> = _quota

    init {
        _profile.value?.let { setProfileState(it) }
    }

    fun loadProfile() {
        if (!hasActiveSession()) return

        viewModelScope.launch {
            _loading.value = true
            try {
                fetchProfile()
                loadQuota()   // load quota alongside profile
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load profile", e)
                _message.value = "error_generic"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadQuota() {
        if (!hasActiveSession()) return
        viewModelScope.launch {
            try {
                _quota.value = repository.getQuota()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load quota", e)
                // quota failure is non-fatal — UI shows nothing
            }
        }
    }

    fun updateProfile(name: String) {
        if (!hasActiveSession()) {
            _message.value = AppError.Unauthorized.toMessage()
            return
        }

        viewModelScope.launch {
            try {
                _loading.value = true
                val request = UpdateProfileRequest(name = name.trim())
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Update profile request body=${gson.toJson(request)}")
                }
                val response = repository.updateProfile(request)
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(TAG, "Update profile failed: $errorBody")
                    throw IllegalStateException("Failed to update profile")
                }
                _message.value = "profile_updated"
                fetchProfile()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update profile", e)
                _message.value = e.message ?: "profile_update_failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun setAvatarImageUri(uri: String?) {
        _avatarImageUri.value = uri
    }

    fun uploadProfilePhoto(context: Context, uri: Uri) {
        if (!hasActiveSession()) {
            _message.value = AppError.Unauthorized.toMessage()
            return
        }

        viewModelScope.launch {
            try {
                _loading.value = true
                val imageUrl = uploadProfilePhotoInternal(context, uri)
                if (!imageUrl.isNullOrBlank()) {
                    val cacheBustedUrl = imageUrl.withCacheBust()
                    fetchProfile()
                    _avatarImageUri.value = cacheBustedUrl
                    syncSessionProfile(imageUrl = imageUrl)
                    _message.value = "profile_updated"
                } else {
                    _message.value = "error_generic"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload profile photo", e)
                _message.value = e.message ?: "profile_update_failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun completeProfileSetup(
        context: Context,
        name: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val token = SessionManager.accessToken.value
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Profile setup token present=${!token.isNullOrBlank()}")
                }
                if (token.isNullOrBlank()) {
                    throw IllegalStateException("Session expired. Please login again")
                }
                val uploadedImageUrl = imageUri?.let { uploadProfilePhotoInternal(context, it) }
                val request = UpdateProfileRequest(
                    name = name.trim(),
                    profile_image_url = uploadedImageUrl?.takeIf { it.isNotBlank() }
                )
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Profile setup request body=${gson.toJson(request)}")
                }
                val response = repository.updateProfile(request)
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e(TAG, "Profile setup failed: $errorBody")
                    throw IllegalStateException("Failed to update profile")
                }
                fetchProfile()
                syncSessionProfile(name = name, imageUrl = uploadedImageUrl)
                _message.value = "profile_updated"
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to complete profile setup", e)
                val message = e.message ?: "Something went wrong. Please try again"
                _message.value = message
                onError(message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun upgradeToPremium() {
        if (!hasActiveSession()) {
            _message.value = AppError.Unauthorized.toMessage()
            return
        }

        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.upgradePlan(
                    UpgradePlanRequest(plan = "GO_PRO")
                )
                if (response.isSuccessful) {
                    _message.value = "profile_updated"
                    fetchProfile()
                    refreshSessionFromAuthMe()
                    loadQuota()   // refresh quota limits after plan change
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

    fun deleteAccount(
        username: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (_isDeleting.value) return
        if (!hasActiveSession()) {
            onError(AppError.Unauthorized.toMessage())
            return
        }

        viewModelScope.launch {
            _isDeleting.value = true
            try {
                when (val result = authRepository.deleteAccount(username)) {
                    is AppResult.Success -> {
                        SessionManager.markAccountDeleted()
                        SessionManager.clear("Account deleted")
                        tokenLocalDataSource.clearToken()
                        resetUiState()
                        onSuccess()
                    }
                    is AppResult.Failure -> {
                        onError(result.error.toMessage())
                    }
                }
            } finally {
                _isDeleting.value = false
            }
        }
    }

    private suspend fun fetchProfile() {
        if (!hasActiveSession()) {
            _profile.value = null
            _avatarImageUri.value = null
            return
        }

        val response = repository.getProfile()
        if (response.isSuccessful) {
            val body = try {
                response.requireData("Profile response body missing")
            } catch (_: IllegalStateException) {
                _message.value = "error_generic"
                return
            }
            run {
                val profileId = runCatching { body.id }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                if (profileId == null) {
                    Log.e(TAG, "Profile response missing id")
                    _message.value = "error_generic"
                    return
                }

                val normalizedProfile = ProfileResponse(
                    id = profileId,
                    name = body.name,
                    email = body.email,
                    phone = body.phone,
                    profile_image_url = body.profile_image_url,
                    role = body.role,
                    created_at = body.created_at,
                    updated_at = body.updated_at,
                    plan = body.plan?.ifBlank { "FREE" } ?: "FREE"
                )
                setProfileState(normalizedProfile)
                syncSessionProfile(
                    name = normalizedProfile.name,
                    phone = normalizedProfile.phone,
                    imageUrl = normalizedProfile.profile_image_url
                )
            }
        } else {
            _message.value = "error_server"
        }
    }

    private suspend fun uploadProfilePhotoInternal(context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType !in allowedMimeTypes) {
            throw IllegalStateException("Please select a JPG or PNG image.")
        }
        val validatedMimeType = mimeType ?: throw IllegalStateException("Unable to read selected image.")

        val sizeBytes = context.queryFileSize(uri)
        if (sizeBytes != null && sizeBytes > MAX_FILE_SIZE_BYTES) {
            throw IllegalStateException("Image must be smaller than 5 MB.")
        }

        val extension = when (validatedMimeType) {
            "image/png" -> "png"
            else -> "jpg"
        }
        val tempFile = File.createTempFile("profile_upload_", ".$extension", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IllegalStateException("Unable to read selected image.")

            val requestFile = tempFile.asRequestBody(validatedMimeType.toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
            val response = repository.uploadProfilePhoto(imagePart)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                Log.e(TAG, "Profile photo upload failed: $errorBody")
                throw IllegalStateException("Something went wrong. Please try again")
            }
            return response.requireData("Profile photo upload response body missing").profile_image_url
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun refreshSessionFromAuthMe() {
        val accessToken = SessionManager.accessToken.value ?: return
        runCatching {
            repository.getMe().toDomain()
        }.onSuccess { refreshedUser ->
            SessionManager.setSession(refreshedUser, accessToken)
        }.onFailure {
            Log.e(TAG, "Failed to refresh session after plan change", it)
        }
    }

    private fun resetUiState() {
        _profile.value = null
        _avatarImageUri.value = null
        _message.value = null
        _loading.value = false
    }

    private fun setProfileState(profile: ProfileResponse) {
        if (profile.id.isBlank()) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Skipping invalid profile state assignment. id='${profile.id}'")
            }
            return
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Setting profile state with id=${profile.id}")
        }
        _profile.value = profile
        _avatarImageUri.value = profile.profile_image_url
    }

    private fun hasActiveSession(): Boolean = !SessionManager.accessToken.value.isNullOrBlank()

    private fun syncSessionProfile(
        name: String? = _profile.value?.name,
        phone: String? = _profile.value?.phone,
        imageUrl: String? = _profile.value?.profile_image_url,
        planStr: String? = _profile.value?.plan
    ) {
        val currentUser = SessionManager.user.value ?: return
        val resolvedPlan = planStr?.uppercase()?.let {
            when (it) {
                "GO_ULTRA" -> com.gosuraksha.app.domain.model.Plan.GO_ULTRA
                "GO_PRO"   -> com.gosuraksha.app.domain.model.Plan.GO_PRO
                else       -> com.gosuraksha.app.domain.model.Plan.FREE
            }
        } ?: currentUser.plan

        SessionManager.setUser(
            currentUser.copy(
                name            = name?.ifBlank { currentUser.name } ?: currentUser.name,
                phone           = phone ?: currentUser.phone,
                profileImageUrl = imageUrl ?: currentUser.profileImageUrl,
                plan            = resolvedPlan
            )
        )
    }

    private fun Context.queryFileSize(uri: Uri): Long? {
        val cursor: Cursor? = contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && it.moveToFirst()) {
                return it.getLong(sizeIndex)
            }
        }
        return null
    }

    private fun String.withCacheBust(): String {
        val separator = if (contains("?")) "&" else "?"
        return "$this${separator}t=${System.currentTimeMillis()}"
    }
}

class ProfileViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                application,
                ProfileRepository(ApiClient.profileApi, ApiClient.authApi),
                AuthRepositoryImpl(
                    AuthRemoteDataSource(ApiClient.authApi),
                    TokenLocalDataSource(EncryptedTokenStorage(application.applicationContext))
                ),
                TokenLocalDataSource(EncryptedTokenStorage(application.applicationContext))
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun User.toProfileResponse(): ProfileResponse = ProfileResponse(
    id = id,
    name = name,
    email = email,
    phone = phone,
    profile_image_url = profileImageUrl,
    role = role,
    created_at = null,
    updated_at = null,
    plan = when (plan) {
        com.gosuraksha.app.domain.model.Plan.GO_ULTRA -> "GO_ULTRA"
        com.gosuraksha.app.domain.model.Plan.GO_PRO -> "GO_PRO"
        else -> "FREE"
    }
)

private fun AppError.toMessage(): String = when (this) {
    AppError.Network -> "Something went wrong. Please try again"
    AppError.Timeout -> "Something went wrong. Please try again"
    AppError.Unauthorized -> "Session expired. Please login again"
    AppError.Forbidden -> "Something went wrong. Please try again"
    AppError.NotFound -> "Something went wrong. Please try again"
    AppError.Server -> "Something went wrong. Please try again"
    is AppError.Validation -> message.ifBlank { "Something went wrong. Please try again" }
    is AppError.Unknown -> "Something went wrong. Please try again"
}



