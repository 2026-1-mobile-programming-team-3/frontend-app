package com.example.siheunggagae.data.network

import com.example.siheunggagae.data.local.TokenManager
import com.example.siheunggagae.data.network.api.AuthApiService
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://backend-production-f6c0.up.railway.app/"

object RetrofitClient {

    private lateinit var tokenManager: TokenManager
    private var onSessionExpired: () -> Unit = {}

    fun init(tokenManager: TokenManager, onSessionExpired: () -> Unit = {}) {
        this.tokenManager = tokenManager
        this.onSessionExpired = onSessionExpired
    }

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private val okHttpClient: OkHttpClient by lazy {
        val authenticator = TokenAuthenticator(tokenManager) { onSessionExpired() }
        val authInterceptor = AuthInterceptor {
            if (::tokenManager.isInitialized) tokenManager.accessToken else null
        }
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .authenticator(authenticator)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApiService::class.java)
    }
}
