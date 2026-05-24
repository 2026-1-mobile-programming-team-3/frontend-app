package com.example.siheunggagae.data.network

import com.example.siheunggagae.BuildConfig
import com.example.siheunggagae.data.network.api.KakaoLocalApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KakaoLocalClient {
    private const val BASE_URL = "https://dapi.kakao.com/"

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_KEY}")
            .build()
        chain.proceed(req)
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: KakaoLocalApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(KakaoLocalApi::class.java)
}
