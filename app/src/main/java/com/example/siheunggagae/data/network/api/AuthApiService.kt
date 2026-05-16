package com.example.siheunggagae.data.network.api

import com.example.siheunggagae.data.model.AccountDeleteRequest
import com.example.siheunggagae.data.model.LoginRequest
import com.example.siheunggagae.data.model.LoginResponse
import com.example.siheunggagae.data.model.LogoutRequest
import com.example.siheunggagae.data.model.MessageResponse
import com.example.siheunggagae.data.model.PasswordChangeRequest
import com.example.siheunggagae.data.model.PetCreate
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.model.PetUpdate
import com.example.siheunggagae.data.model.SignupRequest
import com.example.siheunggagae.data.model.TokenRefreshRequest
import com.example.siheunggagae.data.model.TokenRefreshResponse
import com.example.siheunggagae.data.model.UserMeResponse
import com.example.siheunggagae.data.model.UserResponse
import com.example.siheunggagae.data.model.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────────

    @POST("api/v1/auth/signup")
    suspend fun signup(@Body body: SignupRequest): Response<UserResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: TokenRefreshRequest): Response<TokenRefreshResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<MessageResponse>

    // ── Users ─────────────────────────────────────────────────────────────────────

    @GET("api/v1/users/me")
    suspend fun getMe(): Response<UserMeResponse>

    @PATCH("api/v1/users/me")
    suspend fun updateMe(@Body body: UserUpdateRequest): Response<UserMeResponse>

    @DELETE("api/v1/users/me")
    suspend fun deleteMe(@Body body: AccountDeleteRequest): Response<MessageResponse>

    @PUT("api/v1/users/me/password")
    suspend fun changePassword(@Body body: PasswordChangeRequest): Response<MessageResponse>

    // ── Pets ──────────────────────────────────────────────────────────────────────

    @POST("api/v1/users/me/pets")
    suspend fun addPet(@Body body: PetCreate): Response<PetResponse>

    @PATCH("api/v1/users/me/pets/{petId}")
    suspend fun updatePet(
        @Path("petId") petId: Int,
        @Body body: PetUpdate,
    ): Response<PetResponse>

    @DELETE("api/v1/users/me/pets/{petId}")
    suspend fun deletePet(@Path("petId") petId: Int): Response<MessageResponse>
}
