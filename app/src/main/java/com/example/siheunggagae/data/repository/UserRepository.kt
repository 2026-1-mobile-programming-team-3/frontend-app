package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.model.ActivityStatsResponse
import com.example.siheunggagae.data.model.UserMeResponse
import com.example.siheunggagae.data.model.UserUpdateRequest
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class UserRepository {
    private val api = RetrofitClient.api

    suspend fun getMe(): Response<UserMeResponse> = api.getMe()

    suspend fun getActivityStats(): Response<ActivityStatsResponse> = api.getActivityStats()

    suspend fun updateMe(request: UserUpdateRequest): Response<UserMeResponse> =
        api.updateMe(request)
}
