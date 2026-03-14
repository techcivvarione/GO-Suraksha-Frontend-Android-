package com.gosuraksha.app.network

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.CertificatePinner
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.gosuraksha.in/"
    private const val BACKEND_HOST = "api.gosuraksha.in"
    private val CERTIFICATE_PINS = listOf(
        "sha256/8OxN2qOE2YnXCf040tnA++ZD+3cDj+Ly/xPyUUpckyo=",
        "sha256/iFvwVyJSxnQdyaUvUERIf+8qk7gRze3612JMwoO3zdU="
    )

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

    lateinit var scamNetworkApi: ScamNetworkApi
        private set



    fun init(context: Context) {

        val logging = HttpLoggingInterceptor().apply {
            val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            level = if (isDebuggable) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val requestUrlLogger = { chain: okhttp3.Interceptor.Chain ->
            val request = chain.request()
            if (
                request.url.encodedPath.endsWith("/auth/login") ||
                request.url.encodedPath.endsWith("/scam/heatmap") ||
                request.url.encodedPath.endsWith("/scam/radar/live")
            ) {
                Log.d("ApiClient", "Request URL: ${request.url}")
            }
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .certificatePinner(
                CertificatePinner.Builder().apply {
                    CERTIFICATE_PINS.forEach { add(BACKEND_HOST, it) }
                }.build()
            )
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(UnauthorizedInterceptor(context))
            .addInterceptor(requestUrlLogger)
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
        scamNetworkApi = retrofit.create(ScamNetworkApi::class.java)







    }
}
