package com.gosuraksha.app.data.repository

import com.gosuraksha.app.core.network.NetworkErrorMapper
import com.gosuraksha.app.data.mapper.toDomain
import com.gosuraksha.app.data.mapper.toDomain as toDomainError
import com.gosuraksha.app.data.remote.HomeRemoteDataSource
import com.gosuraksha.app.domain.model.home.HomeOverview
import com.gosuraksha.app.domain.repository.HomeRepository
import com.gosuraksha.app.domain.result.DomainResult

class HomeRepositoryImpl(
    private val remote: HomeRemoteDataSource
) : HomeRepository {
    override suspend fun getOverview(): DomainResult<HomeOverview> {
        return try {
            val dto = remote.getOverview()
            DomainResult.Success(dto.toDomain())
        } catch (t: Throwable) {
            val appError = NetworkErrorMapper.map(t)
            DomainResult.Failure(appError.toDomainError())
        }
    }
}
