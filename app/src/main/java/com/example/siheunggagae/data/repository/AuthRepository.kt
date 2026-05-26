package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.local.TokenManager
import com.example.siheunggagae.data.model.LoginRequest
import com.example.siheunggagae.data.model.LoginResponse
import com.example.siheunggagae.data.model.LogoutRequest
import com.example.siheunggagae.data.model.SignupRequest
import com.example.siheunggagae.data.model.TokenRefreshRequest
import com.example.siheunggagae.data.model.UserResponse
import com.example.siheunggagae.data.network.FcmTokenManager
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class AuthRepository(
    private val tokenManager: TokenManager,
    private val fcmTokenManager: FcmTokenManager? = null,
) {

    private val api = RetrofitClient.api

    suspend fun signup(request: SignupRequest): Response<UserResponse> =
        api.signup(request)

    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        val response = api.login(request)
        if (response.isSuccessful) {
            response.body()?.let {
                tokenManager.saveTokens(it.accessToken, it.refreshToken, it.expiresIn)
            }
            fcmTokenManager?.registerCurrentDevice()
        }
        return response
    }

    suspend fun refresh(): Boolean {
        val refreshToken = tokenManager.refreshToken ?: return false
        val response = api.refresh(TokenRefreshRequest(refreshToken))
        if (response.isSuccessful) {
            response.body()?.let {
                tokenManager.saveTokens(it.accessToken, it.refreshToken, it.expiresIn)
            }
            return true
        }
        return false
    }

    /**
     * 시작 시점 세션 유효성 검사. accessToken 이 없거나 /me 검증이 401 로 끝나면 false.
     * 만료된 access token 은 TokenAuthenticator 가 자동으로 refresh 를 시도하므로,
     * 여기서 별도 분기 없이 단일 호출로 서버 측 실제 유효성을 확인할 수 있다.
     */
    suspend fun ensureSession(): Boolean {
        if (tokenManager.accessToken == null) return false
        return runCatching { api.getMe().isSuccessful }.getOrDefault(false)
    }

    suspend fun logout() {
        val refreshToken = tokenManager.refreshToken
        // FCM 토큰 삭제 → 이 기기로의 알림 즉시 차단 (다른 계정 로그인 시 크로스 알림 방지)
        runCatching { fcmTokenManager?.deleteDeviceToken() }
        tokenManager.clearTokens()                              // 로컬 즉시 삭제
        if (refreshToken != null) {
            runCatching { api.logout(LogoutRequest(refreshToken)) }  // 서버 무효화 (실패해도 무방)
        }
    }
}
