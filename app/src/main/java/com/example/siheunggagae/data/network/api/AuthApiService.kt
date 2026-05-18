package com.example.siheunggagae.data.network.api

import com.example.siheunggagae.data.model.AccountDeleteRequest
import com.example.siheunggagae.data.model.ReverseGeocodeResponse
import com.example.siheunggagae.data.model.ChatMessageCreateRequest
import com.example.siheunggagae.data.model.DeviceRegisterRequest
import com.example.siheunggagae.data.model.DeviceRegisteredResponse
import com.example.siheunggagae.data.model.MarkAllReadResponse
import com.example.siheunggagae.data.model.NotificationCategory
import com.example.siheunggagae.data.model.NotificationListResponse
import com.example.siheunggagae.data.model.NotificationReadResponse
import com.example.siheunggagae.data.model.NotificationSettingsResponse
import com.example.siheunggagae.data.model.NotificationSettingsUpdateRequest
import com.example.siheunggagae.data.model.UnreadCountResponse
import com.example.siheunggagae.data.model.ChatMessageCreatedResponse
import com.example.siheunggagae.data.model.ChatMessageListResponse
import com.example.siheunggagae.data.model.ChatThreadListResponse
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
import retrofit2.http.Query

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

    // ── Chat ──────────────────────────────────────────────────────────────────────

    @GET("api/v1/matches/{matchId}/chats")
    suspend fun getChatThreads(
        @Path("matchId") matchId: Int,
    ): Response<ChatThreadListResponse>

    @GET("api/v1/matches/{matchId}/applications/{applicationId}/messages")
    suspend fun getChatMessages(
        @Path("matchId") matchId: Int,
        @Path("applicationId") applicationId: Int,
        @Query("before_id") beforeId: Int? = null,
        @Query("size") size: Int = 50,
    ): Response<ChatMessageListResponse>

    @POST("api/v1/matches/{matchId}/applications/{applicationId}/messages")
    suspend fun sendChatMessage(
        @Path("matchId") matchId: Int,
        @Path("applicationId") applicationId: Int,
        @Body body: ChatMessageCreateRequest,
    ): Response<ChatMessageCreatedResponse>

    // ── Maps ──────────────────────────────────────────────────────────────────────

    @GET("api/v1/maps/stores")
    suspend fun getNearbyStores(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int = 2000,
    ): Response<com.example.siheunggagae.data.model.StoreListResponse>

    @GET("api/v1/maps/stores/filter")
    suspend fun getFilteredStores(
        @Query("category") category: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int = 2000,
    ): Response<com.example.siheunggagae.data.model.StoreListResponse>

    @GET("api/v1/maps/volunteers")
    suspend fun getVolunteerMarkers(): Response<List<com.example.siheunggagae.data.model.VolunteerMarkerDto>>

    // ── Geo ───────────────────────────────────────────────────────────────────────

    @GET("api/v1/geo/reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
    ): Response<ReverseGeocodeResponse>

    // ── Notifications ─────────────────────────────────────────────────────────────

    @POST("api/v1/notifications/devices")
    suspend fun registerDevice(
        @Body body: DeviceRegisterRequest,
    ): Response<DeviceRegisteredResponse>

    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("category") category: NotificationCategory? = null,
        @Query("is_read") isRead: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
    ): Response<NotificationListResponse>

    @GET("api/v1/notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @PATCH("api/v1/notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: Int,
    ): Response<NotificationReadResponse>

    @PATCH("api/v1/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<MarkAllReadResponse>

    @GET("api/v1/users/me/notification-settings")
    suspend fun getNotificationSettings(): Response<NotificationSettingsResponse>

    @PATCH("api/v1/users/me/notification-settings")
    suspend fun updateNotificationSettings(
        @Body body: NotificationSettingsUpdateRequest,
    ): Response<NotificationSettingsResponse>
}
