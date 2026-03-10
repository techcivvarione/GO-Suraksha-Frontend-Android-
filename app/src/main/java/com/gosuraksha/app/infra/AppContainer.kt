package com.gosuraksha.app.infra

import android.content.Context
import com.gosuraksha.app.core.dispatchers.DefaultDispatcherProvider
import com.gosuraksha.app.core.dispatchers.DispatcherProvider
import com.gosuraksha.app.data.local.TokenLocalDataSource
import com.gosuraksha.app.data.remote.AuthRemoteDataSource
import com.gosuraksha.app.data.remote.HomeRemoteDataSource
import com.gosuraksha.app.data.remote.ScanRemoteDataSource
import com.gosuraksha.app.data.repository.AuthRepositoryImpl
import com.gosuraksha.app.data.repository.HomeRepositoryImpl
import com.gosuraksha.app.data.repository.ScanRepositoryImpl
import com.gosuraksha.app.data.repository.SessionRepositoryImpl
import com.gosuraksha.app.domain.usecase.AuthUseCases
import com.gosuraksha.app.domain.usecase.HomeUseCases
import com.gosuraksha.app.domain.usecase.GetHomeOverviewUseCase
import com.gosuraksha.app.domain.usecase.LoginUseCase
import com.gosuraksha.app.domain.usecase.RestoreSessionUseCase
import com.gosuraksha.app.domain.usecase.SignupUseCase
import com.gosuraksha.app.domain.usecase.ScanUseCases
import com.gosuraksha.app.domain.usecase.AnalyzeTextUseCase
import com.gosuraksha.app.domain.usecase.ExplainScanUseCase
import com.gosuraksha.app.domain.usecase.ScanAiImageUseCase
import com.gosuraksha.app.network.ApiClient
import com.gosuraksha.app.security.EncryptedTokenStorage

class AppContainer(
    private val context: Context,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider
) {
    private val tokenStorage = EncryptedTokenStorage(context)
    private val tokenLocalDataSource = TokenLocalDataSource(tokenStorage)
    private val authRemoteDataSource = AuthRemoteDataSource(ApiClient.authApi)
    private val homeRemoteDataSource = HomeRemoteDataSource(ApiClient.homeApi)
    private val scanRemoteDataSource = ScanRemoteDataSource(ApiClient.analyzeApi)

    private val authRepository = AuthRepositoryImpl(authRemoteDataSource, tokenLocalDataSource)
    private val sessionRepository = SessionRepositoryImpl(tokenLocalDataSource)
    private val homeRepository = HomeRepositoryImpl(homeRemoteDataSource)
    private val scanRepository = ScanRepositoryImpl(scanRemoteDataSource)

    val authUseCases: AuthUseCases by lazy {
        AuthUseCases(
            login = LoginUseCase(authRepository, dispatchers),
            signup = SignupUseCase(authRepository, dispatchers),
            restoreSession = RestoreSessionUseCase(authRepository, sessionRepository, dispatchers)
        )
    }

    val homeUseCases: HomeUseCases by lazy {
        HomeUseCases(
            getOverview = GetHomeOverviewUseCase(homeRepository, dispatchers)
        )
    }

    val scanUseCases: ScanUseCases by lazy {
        ScanUseCases(
            analyze = AnalyzeTextUseCase(scanRepository, dispatchers),
            explain = ExplainScanUseCase(scanRepository, dispatchers),
            scanAiImage = ScanAiImageUseCase(scanRepository, dispatchers)
        )
    }
}
