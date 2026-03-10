package com.gosuraksha.app.domain.usecase

data class HomeUseCases(
    val getOverview: GetHomeOverviewUseCase
)

interface HomeUseCaseProvider {
    fun homeUseCases(): HomeUseCases
}
