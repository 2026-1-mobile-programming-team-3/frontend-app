package com.example.siheunggagae.data.model

import com.google.gson.annotations.SerializedName

// ── 매장 (Stores) ──────────────────────────────────────────────────────────────

data class StoreResponse(
    // 서버가 camelCase("storeId"), snake_case("store_id"), 또는 단순 "id"로 줄 때 모두 대응
    @SerializedName(value = "store_id", alternate = ["storeId", "id"])
    val storeId: Int = 0,
    val name: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerializedName(value = "distance_m", alternate = ["distanceM"])
    val distanceM: Double? = null,
    @SerializedName(value = "rating_avg", alternate = ["ratingAvg"])
    val ratingAvg: Float? = null,
    @SerializedName(value = "is_favorited", alternate = ["isFavorited"])
    val isFavorited: Boolean = false,
    @SerializedName(value = "is_pet_allowed", alternate = ["isPetAllowed"])
    val isPetAllowed: Boolean? = null,
    @SerializedName(value = "thumbnail_url", alternate = ["thumbnailUrl"])
    val thumbnailUrl: String? = null,
) {
    val resolvedId: Int get() = storeId
}

data class StoreListResponse(
    val stores: List<StoreResponse> = emptyList(),
    val total: Int = 0,
)

data class StoreDetailResponse(
    @SerializedName(value = "store_id", alternate = ["storeId", "id"])
    val storeId: Int? = null,
    val name: String? = null,
    val address: String? = null,
    val phone: String? = null,
    @SerializedName(value = "operating_hours", alternate = ["operatingHours"])
    val operatingHours: String? = null,
    @SerializedName(value = "photo_urls", alternate = ["photoUrls"])
    val photoUrls: List<String>? = null,
    @SerializedName(value = "is_pet_allowed", alternate = ["isPetAllowed"])
    val isPetAllowed: Boolean? = null,
    @SerializedName(value = "rating_avg", alternate = ["ratingAvg"])
    val ratingAvg: Double? = null,
    @SerializedName(value = "review_pet_allowed_rate", alternate = ["reviewPetAllowedRate"])
    val reviewPetAllowedRate: Double? = null,
    @SerializedName(value = "is_favorited", alternate = ["isFavorited"])
    val isFavorited: Boolean? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class StoreReview(
    @SerializedName(value = "review_id", alternate = ["reviewId"])
    val reviewId: Int? = null,
    val nickname: String? = null,
    val rating: Int? = null,
    @SerializedName(value = "is_pet_allowed", alternate = ["isPetAllowed"])
    val isPetAllowed: Boolean? = null,
    val content: String? = null,
    @SerializedName(value = "created_at", alternate = ["createdAt"])
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
    @SerializedName(value = "store_id", alternate = ["storeId", "id"])
    val storeId: Int? = null,
    val name: String? = null,
    val address: String? = null,
    val category: String? = null,
) {
    val resolvedId: Int? get() = storeId
}

data class StoreSearchResponse(
    val results: List<StoreSearchResult>? = null,
)

// ── 봉사 마커 (Volunteer Markers) ──────────────────────────────────────────────

data class VolunteerMarkerDto(
    @SerializedName(value = "request_id", alternate = ["requestId", "id"])
    val requestId: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String? = null,
    val status: String? = null,
)
