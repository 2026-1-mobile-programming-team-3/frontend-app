package com.example.siheunggagae.data.model

data class SignupRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val phone: String? = null,
    val regionSi: String? = null,
    val regionDong: String? = null,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
)

data class TokenRefreshRequest(
    val refreshToken: String,
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
)

data class LogoutRequest(
    val refreshToken: String,
)
