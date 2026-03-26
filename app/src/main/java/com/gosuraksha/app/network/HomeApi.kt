package com.gosuraksha.app.network

import com.gosuraksha.app.data.remote.dto.auth.ApiResponse
import com.gosuraksha.app.data.remote.dto.home.HomeOverviewDto
import retrofit2.http.GET

interface HomeApi {

    @GET("home/overview")
    suspend fun getOverview(): ApiResponse<HomeOverviewDto>
}
