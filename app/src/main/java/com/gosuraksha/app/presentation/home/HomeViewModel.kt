package com.gosuraksha.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosuraksha.app.data.repository.NewsRepository
import com.gosuraksha.app.domain.model.home.HomeOverview
import com.gosuraksha.app.domain.result.DomainError
import com.gosuraksha.app.domain.result.DomainResult
import com.gosuraksha.app.domain.usecase.HomeUseCases
import com.gosuraksha.app.presentation.state.UiState
import com.gosuraksha.app.ui.home.BannerAction
import com.gosuraksha.app.ui.home.BannerItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val useCases: HomeUseCases,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _overviewState = MutableStateFlow<UiState<HomeOverview>>(UiState.Idle)
    val overviewState: StateFlow<UiState<HomeOverview>> = _overviewState

    private val _bannerItems = MutableStateFlow<List<BannerItem>>(defaultFeatureBanners())
    val bannerItems: StateFlow<List<BannerItem>> = _bannerItems

    init {
        loadOverview()
    }

    fun loadOverview() {
        viewModelScope.launch {
            _overviewState.value = UiState.Loading
            when (val result = useCases.getOverview(Unit)) {
                is DomainResult.Success -> _overviewState.value = UiState.Success(result.data)
                is DomainResult.Failure -> _overviewState.value = UiState.Error(result.error.toMessage())
            }
        }
    }

    fun loadBannerItems(languageCode: String = "en") {
        viewModelScope.launch {
            val featureBanners = defaultFeatureBanners()
            val newsBanners = runCatching {
                newsRepository.getNews(languageCode)
                    .news
                    .asSequence()
                    .filter { it.title.isNotBlank() }
                    .distinctBy { it.title.trim().lowercase() }
                    .take(3)
                    .map { news ->
                        BannerItem.NewsBanner(
                            id = "news_${news.title.hashCode()}",
                            title = news.title.trim(),
                            category = news.category.ifBlank { "Security Update" },
                            imageUrl = news.image?.takeIf { it.isNotBlank() }
                        )
                    }
                    .toList()
            }.getOrDefault(emptyList())

            _bannerItems.value = newsBanners + featureBanners
        }
    }
}

class HomeViewModelFactory(
    private val useCases: HomeUseCases,
    private val newsRepository: NewsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(useCases, newsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun defaultFeatureBanners(): List<BannerItem> {
    return listOf(
        BannerItem.FeatureBanner(
            id = "feature_qr_safety",
            title = "QR Safety",
            subtitle = "Scan QR before you pay",
            action = BannerAction.OPEN_QR_SCAN
        ),
        BannerItem.FeatureBanner(
            id = "feature_scam_detection",
            title = "Scam Detection",
            subtitle = "Got a suspicious message? Check now",
            action = BannerAction.OPEN_THREAT_SCAN
        ),
        BannerItem.FeatureBanner(
            id = "feature_cyber_sos",
            title = "Cyber SOS",
            subtitle = "Lost money? Act immediately",
            action = BannerAction.OPEN_CYBER_SOS
        ),
        BannerItem.FeatureBanner(
            id = "feature_safety_score",
            title = "Safety Score",
            subtitle = "Check your Cyber Safety Score",
            action = BannerAction.OPEN_RISK_SCORE
        ),
        BannerItem.FeatureBanner(
            id = "feature_family_protection",
            title = "Family Protection",
            subtitle = "Protect your family from scams",
            action = BannerAction.OPEN_FAMILY
        )
    )
}

private fun DomainError.toMessage(): String {
    return when (this) {
        DomainError.Network -> "error_network"
        DomainError.Timeout -> "error_timeout"
        DomainError.Unauthorized -> "error_unauthorized"
        DomainError.ScanLimitReached -> "error_generic"
        DomainError.Forbidden -> "error_forbidden"
        DomainError.NotFound -> "error_not_found"
        DomainError.Server -> "error_server"
        is DomainError.Unknown -> message ?: "error_generic"
    }
}
