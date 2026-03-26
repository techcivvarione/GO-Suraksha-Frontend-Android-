package com.gosuraksha.app

import android.app.Application
import com.gosuraksha.app.core.session.SessionManager
import com.gosuraksha.app.domain.usecase.AuthUseCaseProvider
import com.gosuraksha.app.domain.usecase.AuthUseCases
import com.gosuraksha.app.domain.usecase.HomeUseCaseProvider
import com.gosuraksha.app.domain.usecase.HomeUseCases
import com.gosuraksha.app.domain.usecase.ScanUseCaseProvider
import com.gosuraksha.app.domain.usecase.ScanUseCases
import com.gosuraksha.app.infra.AppContainer
import com.gosuraksha.app.network.ApiClient

class GoSurakshaApp : Application(), AuthUseCaseProvider, HomeUseCaseProvider, ScanUseCaseProvider {

    private lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        ApiClient.init(applicationContext)
        SessionManager.initialize(applicationContext)
        container = AppContainer(applicationContext)
    }

    override fun authUseCases(): AuthUseCases = container.authUseCases

    override fun homeUseCases(): HomeUseCases = container.homeUseCases

    override fun scanUseCases(): ScanUseCases = container.scanUseCases
}
