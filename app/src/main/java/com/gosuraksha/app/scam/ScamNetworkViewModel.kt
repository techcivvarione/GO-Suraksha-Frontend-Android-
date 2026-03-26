package com.gosuraksha.app.scam

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.data.repository.ScamNetworkRepository
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.scam.location.ScamLookupLocationProvider
import com.gosuraksha.app.scam.model.CheckNumberRequest
import com.gosuraksha.app.scam.model.CheckNumberResponse
import com.gosuraksha.app.scam.model.ReportScamRequest
import com.gosuraksha.app.scam.model.ScamAlertCampaign
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScamNetworkUiState(
    val submittingReport: Boolean = false,
    val reportSuccessMessage: String? = null,
    val reportError: String? = null,
    val checkingNumber: Boolean = false,
    val phoneQuery: String = "",
    val phoneCheckResult: CheckNumberResponse? = null,
    val phoneCheckError: String? = null,
    val loadingTrending: Boolean = false,
    val trendingScams: List<ScamAlertCampaign> = emptyList(),
    val trendingError: String? = null
)

class ScamNetworkViewModel(
    application: Application,
    private val repository: ScamNetworkRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ScamNetworkUiState())
    val uiState: StateFlow<ScamNetworkUiState> = _uiState.asStateFlow()

    private var numberCheckJob: Job? = null

    val alertsPager = Pager(PagingConfig(pageSize = 10, prefetchDistance = 2)) {
        ScamAlertsPagingSource(repository)
    }.flow.cachedIn(viewModelScope)

    init {
        loadTrendingScams()
    }

    fun submitScamReport(request: ReportScamRequest) {
        if (!SessionManager.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(reportError = "Please log in to submit a report.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                submittingReport = true,
                reportSuccessMessage = null,
                reportError = null
            )
            runCatching {
                repository.submitScamReport(request)
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    submittingReport = false,
                    reportSuccessMessage = response.message ?: "Report submitted successfully."
                )
                loadTrendingScams()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    submittingReport = false,
                    reportError = "Unable to submit scam report."
                )
            }
        }
    }

    fun updatePhoneNumberQuery(value: String) {
        _uiState.value = _uiState.value.copy(
            phoneQuery = value,
            phoneCheckError = null
        )
        numberCheckJob?.cancel()
        if (value.filter(Char::isDigit).length < 10) return
        numberCheckJob = viewModelScope.launch {
            delay(500)
            checkPhoneNumber()
        }
    }

    fun checkPhoneNumber(phoneNumber: String = _uiState.value.phoneQuery) {
        if (!SessionManager.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(phoneCheckError = "Please log in to check this number.")
            return
        }
        if (phoneNumber.isBlank()) return

        numberCheckJob?.cancel()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                checkingNumber = true,
                phoneCheckError = null
            )
            val location = ScamLookupLocationProvider.getLastKnownLocation(getApplication())
            runCatching {
                repository.checkNumber(
                    CheckNumberRequest(
                        phone_number = phoneNumber,
                        lat = location?.lat,
                        lng = location?.lng,
                        city = location?.city,
                        state = location?.state,
                        country = location?.country
                    )
                )
            }.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    checkingNumber = false,
                    phoneCheckResult = result
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    checkingNumber = false,
                    phoneCheckError = "Unable to check this number."
                )
            }
        }
    }

    fun loadTrendingScams() {
        if (!SessionManager.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(trendingError = "Please log in to view scam activity.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loadingTrending = true,
                trendingError = null
            )
            runCatching {
                repository.getTrendingScams()
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    loadingTrending = false,
                    trendingScams = response
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    loadingTrending = false,
                    trendingError = "Unable to load trending scam campaigns."
                )
            }
        }
    }

    fun findCampaign(alertId: String?): ScamAlertCampaign? {
        if (alertId.isNullOrBlank()) return null
        return _uiState.value.trendingScams.firstOrNull { it.id == alertId }
    }
}

class ScamNetworkViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScamNetworkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScamNetworkViewModel(
                application,
                ScamNetworkRepository(ApiClient.scamNetworkApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private class ScamAlertsPagingSource(
    private val repository: ScamNetworkRepository
) : PagingSource<Int, ScamAlertCampaign>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScamAlertCampaign> {
        return try {
            val page = params.key ?: 1
            val response = repository.getScamAlerts(
                page = page,
                pageSize = params.loadSize.coerceAtLeast(10)
            )
            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.hasMore) page + 1 else null
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(throwable)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ScamAlertCampaign>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
}
