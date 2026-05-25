package com.example.siheunggagae.data.network.api

import com.example.siheunggagae.data.model.KakaoLocalSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoLocalApi {
    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Query("query") query: String,
        @Query("x") x: Double? = null,
        @Query("y") y: Double? = null,
        @Query("radius") radius: Int? = null,
        @Query("size") size: Int = 15,
        @Query("page") page: Int = 1,
    ): Response<KakaoLocalSearchResponse>
}
