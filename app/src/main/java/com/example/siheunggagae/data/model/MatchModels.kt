package com.example.siheunggagae.data.model

import com.google.gson.annotations.SerializedName

// ── 매칭 요청 (Match Requests) ─────────────────────────────────────────────────

data class MatchCreateRequest(
    val title: String,
    val content: String,
    val category: MatchCategory,
    val latitude: Float,
    val longitude: Float,
    val address: String? = null,
    val desiredDate: String? = null,
    @SerializedName("desired_time") val desiredTime: String? = null,
    @SerializedName("pet_ids") val petIds: List<Int>? = null, // 1번 개선: 단일 petId에서 다중 petIds 배열로 확장
)

data class MatchCreateResponse(
    val matchId: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
)

data class MatchUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    val category: MatchCategory? = null,
    val latitude: Float? = null,
    val longitude: Float? = null,
    val address: String? = null,
    val desiredDate: String? = null,
    @SerializedName("desired_time") val desiredTime: String? = null,
    @SerializedName("pet_ids") val petIds: List<Int>? = null, // 1번 개선: 단일 petId에서 다중 petIds 배열로 확장
)

enum class MatchCategory { WALK, VET, SHOPPING, MOVE, VOLUNTEER, OTHER }

fun MatchCategory.requiresVolunteerRole(): Boolean = this == MatchCategory.VOLUNTEER

data class MatchListItem(
    @SerializedName("match_id") val matchId: Int? = null,
    val title: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("desired_date") val desiredDate: String? = null,
    @SerializedName("desired_time") val desiredTime: String? = null,
    val status: String? = null,
    @SerializedName("author_nickname") val authorNickname: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("applications_count") val applicationsCount: Int? = null,
    @SerializedName("matched_applicant_nickname") val matchedApplicantNickname: String? = null,
    @SerializedName("unread_message_count") val unreadMessageCount: Int = 0,
    @SerializedName("my_application_status") val myApplicationStatus: String? = null,
    @SerializedName("received_rating") val receivedRating: Int? = null,
    val category: MatchCategory? = null,
    @SerializedName(value = "author_user_id", alternate = ["authorUserId"])
    val authorUserId: Int? = null,
    @SerializedName(value = "distance_m", alternate = ["distanceM"])
    val distanceM: Double? = null,
)

data class MatchListResponse(
    val items: List<MatchListItem>? = null,
    val total: Int? = null,
    val page: Int? = null,
    val size: Int? = null,
)

data class MatchAuthor(
    val userId: Int? = null,
    val nickname: String? = null,
)

data class MatchPet(
    val petId: Int? = null,
    val name: String? = null,
    val species: String? = null,
    val isNeutered: Boolean? = null,
)

data class MatchDetailResponse(
    val matchId: Int? = null,
    val author: MatchAuthor? = null,
    val pet: MatchPet? = null, // 하이브리드 스펙 유지
    @SerializedName("pets") val pets: List<MatchPet>? = null, // 1번 개선: 여러 마리 정보 반환을 수용할 수 있는 복수 배열 추가
    val title: String? = null,
    val content: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val desiredDate: String? = null,
    @SerializedName("desired_time") val desiredTime: String? = null,
    @SerializedName("is_reviewed") val isReviewed: Boolean? = null,
    val status: String? = null,
    val applicationsCount: Int? = null,
    val createdAt: String? = null,
    val category: MatchCategory? = null,
)

data class MatchStatusUpdateRequest(
    val status: String,
)

data class MatchStatusUpdateResponse(
    val matchId: Int? = null,
    val status: String? = null,
    val updatedAt: String? = null,
)

// ── 봉사 신청 (Applications) ───────────────────────────────────────────────────

data class ApplicationCreateRequest(
    val message: String? = null,
)

data class ApplicationCreateResponse(
    @SerializedName("application_id") val applicationId: Int? = null,
    @SerializedName("match_id") val matchId: Int? = null,
    @SerializedName("applicant_id") val applicantId: Int? = null,
    val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class ApplicationActionRequest(
    val action: String,
)

data class ApplicationActionResponse(
    @SerializedName("application_id") val applicationId: Int? = null,
    val status: String? = null,
    @SerializedName("match_status") val matchStatus: String? = null,
)

data class ApplicationApplicant(
    @SerializedName("applicant_id") val applicantId: Int? = null,
    val nickname: String? = null,
)

data class ApplicationItem(
    @SerializedName("application_id") val applicationId: Int? = null,
    val applicant: ApplicationApplicant? = null,
    val message: String? = null,
    val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class ApplicationListResponse(
    val items: List<ApplicationItem>? = null,
    val total: Int? = null,
)

data class MatchCancelResponse(
    @SerializedName("application_id") val applicationId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("match_status") val matchStatus: String,
    @SerializedName("reopened_to_applicant_ids") val reopenedToApplicantIds: List<Int>
)

// ── 봉사 완료 후기 (Reviews) ───────────────────────────────────────────────────

data class MatchReviewRequest(
    val proofImageUrls: List<String>? = null,
    val rating: Int,
    val content: String,
)

data class MatchReviewResponse(
    @SerializedName("review_id") val reviewId: Int? = null,
    @SerializedName("match_id") val matchId: Int? = null,
    @SerializedName("rating") val rating: Int? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("proof_image_urls") val proofImageUrls: List<String>? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)