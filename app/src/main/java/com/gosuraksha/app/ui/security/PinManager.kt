package com.gosuraksha.app.ui.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.crypto.AEADBadTagException

class PinManager(context: Context) {

    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    fun isPinSet(): Boolean {
        return prefs.contains("pin_hash")
    }

    fun savePin(pin: String) {
        val hashed = hash(pin)
        prefs.edit().putString("pin_hash", hashed).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString("pin_hash", null) ?: return false
        return stored == hash(pin)
    }

    fun clearPin() {
        prefs.edit().remove("pin_hash").apply()
    }

    private fun hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val prefsName = "gosuraksha_secure_prefs"
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            EncryptedSharedPreferences.create(
                context,
                prefsName,
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            if (e is AEADBadTagException || e is GeneralSecurityException || e.cause is AEADBadTagException) {
                Log.e("PIN_MANAGER", "Encrypted prefs corrupted, resetting", e)
                Log.e("GO_SURAKSHA_SECURITY", "Encrypted storage reset due to keystore mismatch")
                context.deleteSharedPreferences(prefsName)
                EncryptedSharedPreferences.create(
                    context,
                    prefsName,
                    masterKeyAlias,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } else {
                throw e
            }
        }
    }
}
