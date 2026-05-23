package com.example.siheunggagae.data.model

import com.google.gson.annotations.SerializedName

data class PetHotelPlan(
    @SerializedName(value = "plan_name", alternate = ["planName"])
    val planName: String,
    @SerializedName(value = "price_krw", alternate = ["priceKrw"])
    val priceKrw: Int,
    @SerializedName(value = "display_order", alternate = ["displayOrder"])
    val displayOrder: Int? = null,
)

/** Gson does not honour Kotlin default values for absent JSON fields — it sets
 *  object-type fields to null instead.  [plans] is therefore backed by a
 *  nullable private field ([_plans]) and exposed as a non-null [plans] getter
 *  so callers always receive an empty list when the key is missing. */
data class PetHotelResponse(
    @SerializedName(value = "store_id", alternate = ["storeId", "id"])
    val storeId: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName(value = "distance_m", alternate = ["distanceM"])
    val distanceM: Double? = null,
    @SerializedName(value = "thumbnail_url", alternate = ["thumbnailUrl"])
    val thumbnailUrl: String? = null,
    @SerializedName(value = "rating_avg", alternate = ["ratingAvg"])
    val ratingAvg: Double? = null,
    @SerializedName(value = "rating_count", alternate = ["ratingCount"])
    val ratingCount: Int = 0,
    @SerializedName(value = "is_favorited", alternate = ["isFavorited"])
    val isFavorited: Boolean = false,
    @SerializedName(value = "plan_count", alternate = ["planCount"])
    val planCount: Int = 0,
    @SerializedName(value = "min_price_krw", alternate = ["minPriceKrw"])
    val minPriceKrw: Int? = null,
    @SerializedName(value = "max_price_krw", alternate = ["maxPriceKrw"])
    val maxPriceKrw: Int? = null,
    @SerializedName("plans")
    private val _plans: List<PetHotelPlan>? = null,
) {
    val plans: List<PetHotelPlan> get() = _plans ?: emptyList()
}

data class PetHotelListResponse(
    @SerializedName(value = "pet_hotels", alternate = ["petHotels"])
    private val _petHotels: List<PetHotelResponse>? = null,
) {
    val petHotels: List<PetHotelResponse> get() = _petHotels ?: emptyList()
}
