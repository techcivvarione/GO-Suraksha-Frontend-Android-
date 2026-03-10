package com.gosuraksha.app.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "https://api.civvarione.com/"

    private lateinit var retrofit: Retrofit

    lateinit var authApi: AuthApi
        private set

    lateinit var analyzeApi: AnalyzeApi
        private set

    lateinit var homeApi: HomeApi
        private set

    lateinit var newsApi: NewsApi
        private set

    lateinit var profileApi: ProfileApi
        private set

    lateinit var securityApi: SecurityApi
        private set
    lateinit var historyApi: HistoryApi
        private set

    lateinit var alertsApi: AlertsApi
        private set
    lateinit var riskApi: RiskApi
        private set

    lateinit var trustedContactsApi: TrustedContactsApi
        private set

    lateinit var qrApi: QrApi
        private set



    fun init(context: Context) {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val loginUrlLogger = { chain: okhttp3.Interceptor.Chain ->
            val request = chain.request()
            if (request.url.encodedPath.endsWith("/auth/login")) {
                Log.d("ApiClient", "Login request URL: ${request.url}")
            }
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(UnauthorizedInterceptor(context))
            .addInterceptor(loginUrlLogger)
            .addInterceptor(logging)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        analyzeApi = retrofit.create(AnalyzeApi::class.java)
        homeApi = retrofit.create(HomeApi::class.java)
        newsApi = retrofit.create(NewsApi::class.java)
        profileApi = retrofit.create(ProfileApi::class.java)
        securityApi = retrofit.create(SecurityApi::class.java)
        historyApi = retrofit.create(HistoryApi::class.java)
        alertsApi = retrofit.create(AlertsApi::class.java)
        riskApi = retrofit.create(RiskApi::class.java)
        trustedContactsApi = retrofit.create(TrustedContactsApi::class.java)
        qrApi = retrofit.create(QrApi::class.java)







    }
}
