package com.gosuraksha.app.security

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.secureTokenStore by preferencesDataStore(name = "secure_token_store")

class EncryptedTokenStorage(
    private val context: Context
) : SecureTokenStorage {

    private val tokenKey = stringPreferencesKey("access_token_enc")
    private val syncPrefs by lazy {
        context.getSharedPreferences("secure_token_store_sync", Context.MODE_PRIVATE)
    }
    private var cachedToken: String? = null

    override suspend fun setToken(token: String) {
        val sanitizedToken = token.takeIf { it.isNotBlank() } ?: return
        val encrypted = encrypt(sanitizedToken)
        context.secureTokenStore.edit { prefs ->
            prefs[tokenKey] = encrypted
        }
        syncPrefs.edit().putString(tokenKey.name, encrypted).commit()
        cachedToken = sanitizedToken
    }

    override suspend fun getToken(): String? {
        val stored = context.secureTokenStore.data.firstOrNull()?.get(tokenKey)
            ?: syncPrefs.getString(tokenKey.name, null)
            ?: return null
        return decrypt(stored)?.also { cachedToken = it }
    }

    suspend fun getAccessToken(): String? = getToken()

    override suspend fun clearToken() {
        context.secureTokenStore.edit { prefs ->
            prefs.remove(tokenKey)
        }
        clearTokenSync()
    }

    override fun getTokenSync(): String? {
        cachedToken?.let { return it }
        val stored = syncPrefs.getString(tokenKey.name, null) ?: return null
        return decrypt(stored)?.also { cachedToken = it }
    }

    fun getAccessTokenSync(): String? = getTokenSync()

    fun saveTokenSync(token: String) {
        runBlocking { setToken(token) }
    }

    override fun clearTokenSync() {
        runBlocking {
            context.secureTokenStore.edit { prefs ->
                prefs.remove(tokenKey)
            }
        }
        syncPrefs.edit().remove(tokenKey.name).commit()
        cachedToken = null
    }

    private fun encrypt(plainText: String?): String {
        val payload = plainText?.takeIf { it.isNotBlank() }?.toByteArray(Charsets.UTF_8) ?: ByteArray(0)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(payload)
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String? {
        return try {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            if (combined.size <= IV_SIZE) return null
            val iv = combined.copyOfRange(0, IV_SIZE)
            val cipherText = combined.copyOfRange(IV_SIZE, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH, iv))
            val plain = cipher.doFinal(cipherText)
            String(plain, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existing?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEY_STORE)
        val spec = android.security.keystore.KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "gosuraksha_token_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 12
        private const val TAG_LENGTH = 128
    }
}
