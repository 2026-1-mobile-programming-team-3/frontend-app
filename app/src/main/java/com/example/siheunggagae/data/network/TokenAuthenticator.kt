package com.example.siheunggagae.data.network

import com.example.siheunggagae.data.local.TokenManager
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject

private const val REFRESH_URL =
    "https://backend-production-f6c0.up.railway.app/api/v1/auth/refresh"

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val onSessionExpired: () -> Unit,
) : Authenticator {

    // auth 요청과 분리된 전용 클라이언트 — 인터셉터/Authenticator 없음
    private val refreshClient = OkHttpClient()

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        // auth 관련 경로(login, signup, refresh)는 재시도하지 않음
        if ("/auth/" in response.request.url.encodedPath) return null

        // 이미 한 번 재시도했는데도 401이면 무한루프 방지를 위해 포기
        if (response.priorResponse != null) return null

        val staleToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        val currentToken = tokenManager.accessToken

        // 다른 스레드가 이미 갱신했다면 새 토큰으로 바로 재시도
        if (currentToken != null && currentToken != staleToken) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        val refreshToken = tokenManager.refreshToken ?: run {
            tokenManager.clearTokens()
            onSessionExpired()
            return null
        }

        return try {
            val body = """{"refresh_token":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaType())
            val refreshResp = refreshClient.newCall(
                Request.Builder().url(REFRESH_URL).post(body).build()
            ).execute()

            if (refreshResp.isSuccessful) {
                val json = JSONObject(refreshResp.body?.string() ?: "{}")
                val newAccess  = json.optString("access_token").takeIf { it.isNotEmpty() }
                val newRefresh = json.optString("refresh_token").takeIf { it.isNotEmpty() }
                val expiresIn  = json.optInt("expires_in", 3600)

                if (newAccess != null && newRefresh != null) {
                    tokenManager.saveTokens(newAccess, newRefresh, expiresIn)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .build()
                } else {
                    tokenManager.clearTokens()
                    onSessionExpired()
                    null
                }
            } else {
                tokenManager.clearTokens()
                onSessionExpired()
                null
            }
        } catch (_: Exception) {
            // 네트워크 오류 시 토큰을 지우지 않음 (연결 복구 후 재시도 가능)
            null
        }
    }
}
