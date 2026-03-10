package com.gosuraksha.app.domain.usecase

import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.domain.model.home.HomeOverview
import com.gosuraksha.app.domain.repository.HomeRepository
import com.gosuraksha.app.domain.result.DomainResult

class GetHomeOverviewUseCase(
    private val repository: HomeRepository,
    dispatchers: DispatcherProvider
) : UseCase<Unit, DomainResult<HomeOverview>>(dispatchers) {

    override suspend fun execute(params: Unit): DomainResult<HomeOverview> {
        return repository.getOverview()
    }
}
