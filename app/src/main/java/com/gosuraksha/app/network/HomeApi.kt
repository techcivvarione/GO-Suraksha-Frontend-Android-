package com.gosuraksha.app.network

import com.gosuraksha.app.home.model.HomeOverviewResponse
import retrofit2.http.GET

interface HomeApi {

    @GET("home/overview")
    suspend fun getOverview(): HomeOverviewResponse
}
