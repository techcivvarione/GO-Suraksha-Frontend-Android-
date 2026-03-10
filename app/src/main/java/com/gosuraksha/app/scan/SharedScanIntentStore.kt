package com.gosuraksha.app.scan

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SharedScanPayload(
    val uri: Uri,
    val mimeType: String
)

object SharedScanIntentStore {
    private val _pending = MutableStateFlow<SharedScanPayload?>(null)
    val pending: StateFlow<SharedScanPayload?> = _pending

    fun publish(payload: SharedScanPayload) {
        _pending.value = payload
    }

    fun consume() {
        _pending.value = null
    }
}
