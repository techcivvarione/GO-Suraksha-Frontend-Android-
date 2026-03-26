package com.gosuraksha.app.data.repository

import com.gosuraksha.app.network.AuthApi
import com.gosuraksha.app.ui.main.CyberCardResponse

class CyberCardRepository(
    private val api: AuthApi
) {
    suspend fun getCyberCard(): CyberCardResponse = api.getCyberCard().data
}
