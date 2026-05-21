package com.example.siheunggagae.data.network.api

import com.example.siheunggagae.data.model.AccountDeleteRequest
import com.example.siheunggagae.data.model.ActivityStatsResponse
import com.example.siheunggagae.data.model.ApplicationActionRequest
import com.example.siheunggagae.data.model.ApplicationActionResponse
import com.example.siheunggagae.data.model.ApplicationCreateRequest
import com.example.siheunggagae.data.model.ApplicationCreateResponse
import com.example.siheunggagae.data.model.ApplicationListResponse
import com.example.siheunggagae.data.model.BlockCreateRequest
import com.example.siheunggagae.data.model.BlockCreatedResponse
import com.example.siheunggagae.data.model.BlockListResponse
import com.example.siheunggagae.data.model.CalendarResponse
import com.example.siheunggagae.data.model.ChatReportCreateRequest
import com.example.siheunggagae.data.model.ChatReportCreateResponse
import com.example.siheunggagae.data.model.ChatMessageCreateRequest
import com.example.siheunggagae.data.model.ChatMessageCreatedResponse
import com.example.siheunggagae.data.model.ChatMessageListResponse
import com.example.siheunggagae.data.model.ChatThreadListResponse
import com.example.siheunggagae.data.model.DailyCalendarResponse
import com.example.siheunggagae.data.model.DashboardResponse
import com.example.siheunggagae.data.model.DeviceRegisterRequest
import com.example.siheunggagae.data.model.DeviceRegisteredResponse
import com.example.siheunggagae.data.model.FavoriteStoreCreateRequest
import com.example.siheunggagae.data.model.FavoriteStoreCreateResponse
import com.example.siheunggagae.data.model.FavoriteStoreListResponse
import com.example.siheunggagae.data.model.LoginRequest
import com.example.siheunggagae.data.model.LoginResponse
import com.example.siheunggagae.data.model.LogoutRequest
import com.example.siheunggagae.data.model.MarkAllReadResponse
import com.example.siheunggagae.data.model.MatchCreateRequest
import com.example.siheunggagae.data.model.MatchCreateResponse
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.data.model.MatchListResponse
import com.example.siheunggagae.data.model.MatchReviewRequest
import com.example.siheunggagae.data.model.MatchReviewResponse
import com.example.siheunggagae.data.model.MatchStatusUpdateRequest
import com.example.siheunggagae.data.model.MatchStatusUpdateResponse
import com.example.siheunggagae.data.model.MatchUpdateRequest
import com.example.siheunggagae.data.model.MessageResponse
import com.example.siheunggagae.data.model.MyMatchListResponse
import com.example.siheunggagae.data.model.NewsDetailResponse
import com.example.siheunggagae.data.model.NewsListResponse
import com.example.siheunggagae.data.model.NotificationCategory
import com.example.siheunggagae.data.model.NotificationListResponse
import com.example.siheunggagae.data.model.NotificationReadResponse
import com.example.siheunggagae.data.model.NotificationSettingsResponse
import com.example.siheunggagae.data.model.NotificationSettingsUpdateRequest
import com.example.siheunggagae.data.model.PasswordChangeRequest
import com.example.siheunggagae.data.model.PetCreate
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.model.PetUpdate
import com.example.siheunggagae.data.model.ReportCreateRequest
import com.example.siheunggagae.data.model.ReportCreatedResponse
import com.example.siheunggagae.data.model.ReverseGeocodeResponse
import com.example.siheunggagae.data.model.SignupRequest
import com.example.siheunggagae.data.model.StoreDetailResponse
import com.example.siheunggagae.data.model.StoreListResponse
import com.example.siheunggagae.data.model.StoreReviewCreateRequest
import com.example.siheunggagae.data.model.StoreReviewCreateResponse
import com.example.siheunggagae.data.model.StoreReviewListResponse
import com.example.siheunggagae.data.model.StoreSearchResponse
import com.example.siheunggagae.data.model.TokenRefreshRequest
import com.example.siheunggagae.data.model.TokenRefreshResponse
import com.example.siheunggagae.data.model.UnreadCountResponse
import com.example.siheunggagae.data.model.UserMeResponse
import com.example.siheunggagae.data.model.UserResponse
import com.example.siheunggagae.data.model.UserUpdateRequest
import com.example.siheunggagae.data.model.VolunteerMarkerDto
import com.example.siheunggagae.data.model.VolunteerRequestCreate
import com.example.siheunggagae.data.model.VolunteerRequestResponse
import com.example.siheunggagae.data.model.VolunteerStatsResponse
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

    @GET("api/v1/users/me/activity-stats")
    suspend fun getActivityStats(): Response<ActivityStatsResponse>

    @GET("api/v1/users/me/volunteer-stats")
    suspend fun getVolunteerStats(): Response<VolunteerStatsResponse>

    @GET("api/v1/users/me/matches")
    suspend fun getMyMatches(
        @Query("role") role: String = "applicant",
        @Query("status") status: String = "DONE",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
    ): Response<MyMatchListResponse>

    @PATCH("api/v1/users/me")
    suspend fun updateMe(@Body body: UserUpdateRequest): Response<UserMeResponse>

    @DELETE("api/v1/users/me")
    suspend fun deleteMe(@Body body: AccountDeleteRequest): Response<MessageResponse>

    @PUT("api/v1/users/me/password")
    suspend fun changePassword(@Body body: PasswordChangeRequest): Response<MessageResponse>

    @POST("api/v1/users/me/volunteer-request")
    suspend fun submitVolunteerRequest(
        @Body body: VolunteerRequestCreate,
    ): Response<VolunteerRequestResponse>

    // ── Pets ──────────────────────────────────────────────────────────────────────

    @POST("api/v1/users/me/pets")
    suspend fun addPet(@Body body: PetCreate): Response<PetResponse>

    @PATCH("api/v1/users/me/pets/{petId}")
    suspend fun updatePet(
        @Path("petId") petId: Int,
        @Body body: PetUpdate,
    ): Response<PetResponse>

    // MessageResponse -> Unit 으로 변경  (204 No Content 처리)
    @DELETE("api/v1/users/me/pets/{petId}")

    suspend fun deletePet(@Path("petId") petId: Int): Response<Unit>

    // ── Blocks ────────────────────────────────────────────────────────────────────

    @GET("api/v1/users/me/blocks")
    suspend fun getBlocks(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50,
    ): Response<BlockListResponse>

    @POST("api/v1/users/me/blocks")
    suspend fun createBlock(
        @Body body: BlockCreateRequest,
    ): Response<BlockCreatedResponse>

    @DELETE("api/v1/users/me/blocks/{blockId}")
    suspend fun deleteBlock(
        @Path("blockId") blockId: Int,
    ): Response<Unit>

    // ── Favorites ─────────────────────────────────────────────────────────────────

    @GET("api/v1/users/me/favorites/stores")
    suspend fun getFavoriteStores(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
    ): Response<FavoriteStoreListResponse>

    @POST("api/v1/users/me/favorites/stores")
    suspend fun addFavoriteStore(@Body body: FavoriteStoreCreateRequest): Response<FavoriteStoreCreateResponse>

    @DELETE("api/v1/users/me/favorites/stores/{storeId}")
    suspend fun deleteFavoriteStore(
        @Path("storeId") storeId: Int,
    ): Response<Unit>

    // ── Matches ───────────────────────────────────────────────────────────────────

    @GET("api/v1/matches")
    suspend fun getMatches(
        @Query("status") status: String? = null,
        @Query("region") region: String? = null,
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): Response<MatchListResponse>

    @POST("api/v1/matches")
    suspend fun createMatch(@Body body: MatchCreateRequest): Response<MatchCreateResponse>

    @GET("api/v1/matches/{matchId}")
    suspend fun getMatchDetail(@Path("matchId") matchId: Int): Response<MatchDetailResponse>

    @PATCH("api/v1/matches/{matchId}")
    suspend fun updateMatch(
        @Path("matchId") matchId: Int,
        @Body body: MatchUpdateRequest,
    ): Response<MessageResponse>

    @DELETE("api/v1/matches/{matchId}")
    suspend fun deleteMatch(@Path("matchId") matchId: Int): Response<MessageResponse>

    @PATCH("api/v1/matches/{matchId}/status")
    suspend fun updateMatchStatus(
        @Path("matchId") matchId: Int,
        @Body body: MatchStatusUpdateRequest,
    ): Response<MatchStatusUpdateResponse>

    // ── Applications ──────────────────────────────────────────────────────────────

    @GET("api/v1/matches/{matchId}/applications")
    suspend fun getApplications(@Path("matchId") matchId: Int): Response<ApplicationListResponse>

    @POST("api/v1/matches/{matchId}/applications")
    suspend fun applyToMatch(
        @Path("matchId") matchId: Int,
        @Body body: ApplicationCreateRequest,
    ): Response<ApplicationCreateResponse>

    @PATCH("api/v1/matches/{matchId}/applications/{applicationId}")
    suspend fun respondToApplication(
        @Path("matchId") matchId: Int,
        @Path("applicationId") applicationId: Int,
        @Body body: ApplicationActionRequest,
    ): Response<ApplicationActionResponse>

    @POST("api/v1/matches/{matchId}/review")
    suspend fun submitMatchReview(
        @Path("matchId") matchId: Int,
        @Body body: MatchReviewRequest,
    ): Response<MatchReviewResponse>

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

    // ── Home ─────────────────────────────────────────────────────────────────────

    @GET("api/v1/home/dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>

    // ── News ──────────────────────────────────────────────────────────────────────

    @GET("api/v1/news")
    suspend fun getNews(): Response<NewsListResponse>

    @GET("api/v1/news/{newsId}")
    suspend fun getNewsDetail(@Path("newsId") newsId: String): Response<NewsDetailResponse>

    @GET("api/v1/news/calendar")
    suspend fun getCalendar(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): Response<CalendarResponse>

    @GET("api/v1/news/calendar/daily")
    suspend fun getDailyCalendar(@Query("date") date: String): Response<DailyCalendarResponse>

    // ── Maps ──────────────────────────────────────────────────────────────────────

    @GET("api/v1/maps/stores")
    suspend fun getNearbyStores(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int = 2000,
    ): Response<StoreListResponse>

    @GET("api/v1/maps/stores/search")
    suspend fun searchStores(@Query("keyword") keyword: String): Response<StoreSearchResponse>

    @GET("api/v1/maps/stores/filter")
    suspend fun getFilteredStores(
        @Query("category") category: String? = null,
        @Query("is_pet_allowed") isPetAllowed: Boolean? = null,
    ): Response<StoreListResponse>

    @GET("api/v1/maps/stores/{storeId}")
    suspend fun getStoreDetail(@Path("storeId") storeId: Int): Response<StoreDetailResponse>

    @GET("api/v1/maps/stores/{storeId}/reviews")
    suspend fun getStoreReviews(@Path("storeId") storeId: Int): Response<StoreReviewListResponse>

    @POST("api/v1/maps/stores/{storeId}/reviews")
    suspend fun createStoreReview(
        @Path("storeId") storeId: Int,
        @Body body: StoreReviewCreateRequest,
    ): Response<StoreReviewCreateResponse>

    @DELETE("api/v1/maps/stores/{storeId}/reviews/{reviewId}")
    suspend fun deleteStoreReview(
        @Path("storeId") storeId: Int,
        @Path("reviewId") reviewId: Int,
    ): Response<MessageResponse>

    @GET("api/v1/maps/volunteers")
    suspend fun getVolunteerMarkers(): Response<List<VolunteerMarkerDto>>

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

    // ── Reports ───────────────────────────────────────────────────────────────────

    @POST("api/v1/reports")
    suspend fun createReport(
        @Body body: ReportCreateRequest,
    ): Response<ReportCreatedResponse>

    @POST("api/v1/reports/chat")
    suspend fun reportChatUser(@Body body: ChatReportCreateRequest): Response<ChatReportCreateResponse>
}
