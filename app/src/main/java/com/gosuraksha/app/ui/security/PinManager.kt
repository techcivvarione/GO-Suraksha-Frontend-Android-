package com.gosuraksha.app.ui.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class PinManager(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_pin_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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
}
