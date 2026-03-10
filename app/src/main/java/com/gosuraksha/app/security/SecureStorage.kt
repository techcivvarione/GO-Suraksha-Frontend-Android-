package com.gosuraksha.app.security

interface SecureStorage {
    fun putString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
    fun clear()
}
