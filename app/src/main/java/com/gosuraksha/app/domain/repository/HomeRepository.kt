package com.gosuraksha.app.domain.repository

import com.gosuraksha.app.domain.model.home.HomeOverview
import com.gosuraksha.app.domain.result.DomainResult

interface HomeRepository {
    suspend fun getOverview(): DomainResult<HomeOverview>
}
