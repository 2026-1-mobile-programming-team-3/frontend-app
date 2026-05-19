package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.model.MatchListResponse
import com.example.siheunggagae.data.network.api.AuthApiService

class MatchRepository(private val api: AuthApiService) {
    suspend fun getMatchList(status: String? = null, page: Int = 1): Result<MatchListResponse> {
        return try {
            val response = api.getMatches(status = status, page = page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("목록을 불러오지 못했습니다. (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}