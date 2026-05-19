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

// ── 봉사자 역할 전환 요청 ──────────────────────────────────────────────────────

data class VolunteerRequestCreate(
    val message: String,
)

data class VolunteerRequestResponse(
    val id: Int? = null,
    val userId: Int? = null,
    val message: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
)

// ── 활동 통계 & 봉사 뱃지 ─────────────────────────────────────────────────────

data class VolunteerStatsResponse(
    val totalCount: Int? = null,
    val totalHours: Double? = null,
    val avgRating: Double? = null,
)

data class VolunteerBadge(
    val tier: String? = null,
    val count: Int? = null,
    val nextTier: String? = null,
    val nextThreshold: Int? = null,
    val progressPct: Int? = null,
)

data class ActivityStatsResponse(
    val myMatchCount: Int? = null,
    val volunteerCompletedCount: Int? = null,
    val favoriteCount: Int? = null,
    val badge: VolunteerBadge? = null,
)

// ── 즐겨찾기 매장 ─────────────────────────────────────────────────────────────

data class FavoriteStoreCreateRequest(
    val storeId: Int,
)

data class FavoriteStoreCreateResponse(
    val favoriteId: Long? = null,
    val storeId: Long? = null,
    val createdAt: String? = null,
)

data class FavoriteStoreItem(
    val favoriteId: Int? = null,
    val storeId: Int? = null,
    val name: String? = null,
    val category: String? = null,
    val thumbnailUrl: String? = null,
    val ratingAvg: Double? = null,
    val createdAt: String? = null,
)

data class FavoriteStoreListResponse(
    val items: List<FavoriteStoreItem>? = null,
    val total: Int? = null,
    val page: Int? = null,
    val size: Int? = null,
)

// ── 사용자 차단 ───────────────────────────────────────────────────────────────

data class BlockCreateRequest(
    val targetUserId: Int,
)

data class BlockCreateResponse(
    val blockId: Int? = null,
    val targetUserId: Int? = null,
    val createdAt: String? = null,
)

data class BlockItem(
    val blockId: Int? = null,
    val targetUserId: Int? = null,
    val targetNickname: String? = null,
    val createdAt: String? = null,
)

data class BlockListResponse(
    val items: List<BlockItem>? = null,
    val total: Int? = null,
)

// ── 신고 ──────────────────────────────────────────────────────────────────────

data class ReportCreateRequest(
    val targetUserId: Int,
    val reason: String,
)

data class ReportCreateResponse(
    val id: Int? = null,
    val targetUserId: Int? = null,
    val reason: String? = null,
    val createdAt: String? = null,
)

data class ChatReportCreateRequest(
    val chatId: Int,
    val targetUserId: Int,
    val messageId: Int,
    val reason: String,
)

data class ChatReportCreateResponse(
    val reportId: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
)
