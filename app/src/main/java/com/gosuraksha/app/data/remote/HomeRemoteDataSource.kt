package com.gosuraksha.app.data.remote

import com.gosuraksha.app.network.HomeApi

class HomeRemoteDataSource(
    private val api: HomeApi
) {
    suspend fun getOverview() = api.getOverview()
}
