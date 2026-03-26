package com.gosuraksha.app.core.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.gosuraksha.app.domain.model.Plan
import com.gosuraksha.app.domain.model.User
import com.gosuraksha.app.security.EncryptedTokenStorage
import com.gosuraksha.app.security.SecureTokenStorage
import java.security.KeyStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {

    private const val PREFS_NAME = "gosuraksha_session_secure"
    private const val KEY_USER = "session_user"
    private const val KEY_ACCOUNT_DELETED = "account_deleted"
    private const val TAG = "SessionManager"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null
    private var tokenStorage: SecureTokenStorage? = null

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken

    private val _sessionExpired = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpired = _sessionExpired.asSharedFlow()

    fun initialize(context: Context) {
        if (prefs != null && tokenStorage != null) return
        val appContext = context.applicationContext
        tokenStorage = EncryptedTokenStorage(appContext)
        prefs = createEncryptedPrefs(appContext)
        restoreUserSnapshot()
        updateAccessToken(tokenStorage?.getTokenSync())
    }

    fun setSession(user: User, accessToken: String) {
        _user.value = user
        updateAccessToken(accessToken)
        persistUserSnapshot(user)
    }

    fun setUser(user: User) {
        _user.value = user
        if (_accessToken.value.isNullOrBlank()) {
            updateAccessToken(tokenStorage?.getTokenSync())
        }
        persistUserSnapshot(user)
    }

    fun updateAccessToken(accessToken: String?) {
        _accessToken.value = accessToken
    }

    fun saveToken(accessToken: String) {
        updateAccessToken(accessToken)
    }

    fun markAccountDeleted() {
        prefs?.edit()?.putBoolean(KEY_ACCOUNT_DELETED, true)?.apply()
    }

    fun consumeAccountDeletedFlag(): Boolean {
        val wasDeleted = prefs?.getBoolean(KEY_ACCOUNT_DELETED, false) == true
        if (wasDeleted) {
            prefs?.edit()?.remove(KEY_ACCOUNT_DELETED)?.apply()
        }
        return wasDeleted
    }

    fun clear(reason: String = "Session cleared") {
        Log.w(TAG, reason)
        _user.value = null
        _accessToken.value = null
        prefs?.edit()?.remove(KEY_USER)?.apply()
        tokenStorage?.clearTokenSync()
    }

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }

    fun isLoggedIn(): Boolean = !_accessToken.value.isNullOrBlank() && _user.value != null

    fun isPaid(): Boolean = _user.value?.plan == Plan.GO_PRO || _user.value?.plan == Plan.GO_ULTRA

    fun isUltra(): Boolean = _user.value?.plan == Plan.GO_ULTRA

    private fun restoreUserSnapshot() {
        val storedUserJson = prefs?.getString(KEY_USER, null)
        if (!storedUserJson.isNullOrBlank()) {
            runCatching {
                gson.fromJson(storedUserJson, User::class.java)
            }.onSuccess { restoredUser ->
                _user.value = restoredUser
            }.onFailure {
                prefs?.edit()?.remove(KEY_USER)?.apply()
            }
        }
    }

    private fun persistUserSnapshot(user: User) {
        prefs?.edit()?.putString(KEY_USER, gson.toJson(user))?.apply()
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Encrypted prefs corrupted, resetting", e)

            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            runCatching {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                keyStore.deleteEntry("master_key")
                keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }.onFailure {
                Log.e(TAG, "Failed to delete corrupted keystore entry", it)
            }

            val recreatedKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                recreatedKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
