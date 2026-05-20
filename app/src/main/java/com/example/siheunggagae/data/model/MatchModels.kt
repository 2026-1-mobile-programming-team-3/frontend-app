package com.example.siheunggagae.data.model

// ── 매칭 요청 (Match Requests) ─────────────────────────────────────────────────

data class MatchCreateRequest(
    val title: String,
    val content: String,
    val latitude: Float,
    val longitude: Float,
    val address: String? = null,
    val desiredDate: String? = null,
    val petId: Int? = null,
)

data class MatchCreateResponse(
    val matchId: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
)

data class MatchUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    val latitude: Float? = null,
    val longitude: Float? = null,
    val address: String? = null,
    val desiredDate: String? = null,
    val petId: Int? = null,
)

data class MatchListItem(
    val matchId: Int? = null,
    val title: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val desiredDate: String? = null,
    val status: String? = null,
    val authorNickname: String? = null,
    val createdAt: String? = null,
    val applicationsCount: Int? = null,
    val matchedApplicantNickname: String? = null,
    val unreadMessageCount: Int = 0,
    val myApplicationStatus: String? = null,
    val receivedRating: Int? = null,
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
    val pet: MatchPet? = null,
    val title: String? = null,
    val content: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val desiredDate: String? = null,
    val status: String? = null,
    val applicationsCount: Int? = null,
    val createdAt: String? = null,
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
    val applicationId: Int? = null,
    val matchId: Int? = null,
    val applicantId: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
)

data class ApplicationActionRequest(
    val action: String,
)

data class ApplicationActionResponse(
    val applicationId: Int? = null,
    val status: String? = null,
    val matchStatus: String? = null,
)

data class ApplicationApplicant(
    val applicantId: Int? = null,
    val nickname: String? = null,
)

data class ApplicationItem(
    val applicationId: Int? = null,
    val applicant: ApplicationApplicant? = null,
    val message: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
)

data class ApplicationListResponse(
    val items: List<ApplicationItem>? = null,
    val total: Int? = null,
)

// ── 봉사 완료 후기 (Reviews) ───────────────────────────────────────────────────

data class MatchReviewRequest(
    val proofImageUrls: List<String>? = null,
    val rating: Int,
    val content: String,
)

data class MatchReviewResponse(
    val reviewId: Int? = null,
    val matchId: Int? = null,
    val rating: Int? = null,
    val createdAt: String? = null,
)
