package com.example.siheunggagae.data.model

enum class UserRole { USER, VOLUNTEER, ADMIN }

data class UserResponse(
    val id: Int,
    val email: String,
    val nickname: String,
    val phone: String?,
    val role: UserRole,
    val profileImageUrl: String?,
    val regionSi: String?,
    val regionDong: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class UserMeResponse(
    val id: Int,
    val email: String,
    val nickname: String,
    val phone: String?,
    val role: UserRole,
    val profileImageUrl: String?,
    val regionSi: String?,
    val regionDong: String?,
    val pets: List<PetResponse> = emptyList(),
    val createdAt: String,
)

data class UserUpdateRequest(
    val nickname: String? = null,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val regionSi: String? = null,
    val regionDong: String? = null,
)

data class AccountDeleteRequest(
    val password: String,
    val reason: String? = null,
)

data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String,
)

data class MessageResponse(
    val message: String,
)

data class VolunteerBadgeInfo(
    val tier: VolunteerBadgeTier,
    val count: Int,
    val nextTier: VolunteerBadgeTier?,
    val nextThreshold: Int?,
    val progressPct: Int,
)

data class ActivityStatsResponse(
    val myMatchCount: Int,
    val volunteerCompletedCount: Int,
    val favoriteCount: Int,
    val badge: VolunteerBadgeInfo,
)

data class VolunteerStatsResponse(
    val totalCount: Int,
    val totalHours: Int,
    val avgRating: Double?,
)

data class MyMatchResponse(
    val id: Int,
    val title: String,
    val status: MatchStatus,
    val scheduledAt: String?,
    val regionDong: String?,
    val petTypes: List<String> = emptyList(),
    val myRating: Double?,
)

data class MyMatchListResponse(
    val content: List<MyMatchResponse>,
    val totalElements: Int,
    val totalPages: Int,
    val page: Int,
    val size: Int,
)

data class FavoriteStoreItem(
    val favoriteId: Int,
    val storeId: Int,
    val name: String,
    val category: StoreCategory,
    val thumbnailUrl: String?,
    val ratingAvg: String,
    val createdAt: String,
)

data class FavoriteStoreListResponse(
    val items: List<FavoriteStoreItem>,
    val total: Int,
    val page: Int,
    val size: Int,
)

data class BlockCreateRequest(val targetUserId: Int)

data class BlockCreatedResponse(
    val blockId: Int,
    val targetUserId: Int,
    val createdAt: String,
)

data class ReportCreateRequest(
    val targetUserId: Int,
    val reason: String,
)

data class ReportCreatedResponse(
    val id: Int,
    val targetUserId: Int,
    val reason: String,
    val createdAt: String,
)

data class BlockListItem(
    val blockId: Int,
    val targetUserId: Int,
    val targetNickname: String?,
    val createdAt: String,
)

data class BlockListResponse(
    val items: List<BlockListItem>,
    val total: Int,
    val page: Int,
    val size: Int,
)

data class VolunteerRequestCreate(val message: String)

data class VolunteerRequestResponse(
    val id: Int,
    val userId: Int,
    val message: String,
    val status: String,
    val createdAt: String,
)
