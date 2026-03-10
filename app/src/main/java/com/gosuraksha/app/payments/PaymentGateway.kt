package com.gosuraksha.app.payments

import com.gosuraksha.app.core.result.AppResult

interface PaymentGateway {
    suspend fun startSubscription(planId: String): AppResult<Unit>
    suspend fun restorePurchases(): AppResult<Unit>
}
