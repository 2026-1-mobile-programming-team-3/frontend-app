package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.local.TokenManager
import com.example.siheunggagae.data.model.LoginRequest
import com.example.siheunggagae.data.model.LoginResponse
import com.example.siheunggagae.data.model.LogoutRequest
import com.example.siheunggagae.data.model.SignupRequest
import com.example.siheunggagae.data.model.TokenRefreshRequest
import com.example.siheunggagae.data.model.UserResponse
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class AuthRepository(private val tokenManager: TokenManager) {

    private val api = RetrofitClient.api

    suspend fun signup(request: SignupRequest): Response<UserResponse> =
        api.signup(request)

    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        val response = api.login(request)
        if (response.isSuccessful) {
            response.body()?.let {
                tokenManager.saveTokens(it.accessToken, it.refreshToken)
            }
        }
        return response
    }

    suspend fun refresh(): Boolean {
        val refreshToken = tokenManager.refreshToken ?: return false
        val response = api.refresh(TokenRefreshRequest(refreshToken))
        if (response.isSuccessful) {
            response.body()?.let {
                tokenManager.saveTokens(it.accessToken, it.refreshToken)
            }
            return true
        }
        return false
    }

    suspend fun logout() {
        val refreshToken = tokenManager.refreshToken ?: return
        api.logout(LogoutRequest(refreshToken))
        tokenManager.clearTokens()
    }
}
