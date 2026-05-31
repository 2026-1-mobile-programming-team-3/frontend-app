package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.model.MatchListResponse
import com.example.siheunggagae.data.network.api.AuthApiService

class MatchRepository(private val api: AuthApiService) {
    suspend fun getMatchList(
        status: String? = null,
        page: Int? = 1,
        size: Int? = 20,
        sort: String? = null,
        category: String? = null,
        maxDistance: Int? = null,
        lat: Double? = null,
        lng: Double? = null,
    ): Result<MatchListResponse> {
        return try {
            val response = api.getMatches(
                status = status,
                page = page,
                size = size,
                sort = sort,
                category = category,
                maxDistance = maxDistance,
                lat = lat,
                lng = lng,
            )
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(IllegalStateException("Empty body"))
            } else {
                Result.failure(IllegalStateException("HTTP ${response.code()}"))
            }
        } catch (e: Throwable) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun deleteMatch(matchId: Int): Result<Unit> {
        return try {
            val response = api.deleteMatch(matchId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException("HTTP ${response.code()}"))
        } catch (e: Throwable) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }
}