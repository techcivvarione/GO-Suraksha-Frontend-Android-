package com.gosuraksha.app.data.remote

import com.gosuraksha.app.data.remote.dto.home.HomeOverviewDto
import com.gosuraksha.app.network.HomeApi

class HomeRemoteDataSource(
    private val api: HomeApi
) {
    // Unwrap the response envelope — HomeRepositoryImpl receives HomeOverviewDto directly
    suspend fun getOverview(): HomeOverviewDto = api.getOverview().data
}
