package com.gosuraksha.app.ui.home

sealed class BannerItem {
    data class NewsBanner(
        val id: String,
        val title: String,
        val category: String,
        val imageUrl: String?
    ) : BannerItem()

    data class FeatureBanner(
        val id: String,
        val title: String,
        val subtitle: String,
        val action: BannerAction
    ) : BannerItem()
}

enum class BannerAction {
    OPEN_QR_SCAN,
    OPEN_THREAT_SCAN,
    OPEN_CYBER_SOS,
    OPEN_RISK_SCORE,
    OPEN_FAMILY,
    OPEN_NEWS
}
