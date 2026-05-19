package com.example.siheunggagae.data.model

// ── 매장 (Stores) ──────────────────────────────────────────────────────────────

data class StoreResponse(
    val storeId: Int = 0,
    val name: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distanceM: Double? = null,
    val ratingAvg: Float? = null,
    val isFavorited: Boolean = false,
    val isPetAllowed: Boolean? = null,
    val thumbnailUrl: String? = null,
)

data class StoreListResponse(
    val stores: List<StoreResponse> = emptyList(),
    val total: Int = 0,
)

data class StoreDetailResponse(
    val storeId: Int? = null,
    val name: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val operatingHours: String? = null,
    val photoUrls: List<String>? = null,
    val isPetAllowed: Boolean? = null,
    val ratingAvg: Double? = null,
    val reviewPetAllowedRate: Double? = null,
    val isFavorited: Boolean? = null,
)

data class StoreReview(
    val reviewId: Int? = null,
    val nickname: String? = null,
    val rating: Int? = null,
    val isPetAllowed: Boolean? = null,
    val content: String? = null,
    val createdAt: String? = null,
)

data class StoreReviewListResponse(
    val reviews: List<StoreReview>? = null,
)

data class StoreReviewCreateRequest(
    val rating: Int,
    val isPetAllowed: Boolean,
    val content: String,
)

data class StoreReviewCreateResponse(
    val reviewId: Int? = null,
    val rating: Int? = null,
    val isPetAllowed: Boolean? = null,
    val content: String? = null,
    val createdAt: String? = null,
)

data class StoreSearchResult(
    val storeId: Int? = null,
    val id: Int? = null,          // 서버 필드명 차이 대비 fallback
    val name: String? = null,
    val address: String? = null,
    val category: String? = null,
) {
    val resolvedId: Int? get() = storeId ?: id
}

data class StoreSearchResponse(
    val results: List<StoreSearchResult>? = null,
)

// ── 봉사 마커 (Volunteer Markers) ──────────────────────────────────────────────

data class VolunteerMarkerDto(
    val requestId: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String? = null,
    val status: String? = null,
)
