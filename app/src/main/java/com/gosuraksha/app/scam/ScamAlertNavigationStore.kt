package com.gosuraksha.app.scam

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ScamAlertDeepLink(
    val route: String,
    val alertId: String? = null
)

object ScamAlertNavigationStore {
    const val EXTRA_SCAM_ROUTE = "scam_route"
    const val EXTRA_SCAM_ALERT_ID = "scam_alert_id"

    private val _pending = MutableStateFlow<ScamAlertDeepLink?>(null)
    val pending: StateFlow<ScamAlertDeepLink?> = _pending

    fun publish(route: String, alertId: String? = null) {
        _pending.value = ScamAlertDeepLink(route = route, alertId = alertId)
    }

    fun consume() {
        _pending.value = null
    }
}
