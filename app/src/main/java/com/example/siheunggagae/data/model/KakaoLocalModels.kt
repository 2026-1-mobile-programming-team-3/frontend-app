package com.example.siheunggagae.data.model

import com.google.gson.annotations.SerializedName

data class KakaoLocalSearchResponse(
    val documents: List<KakaoLocalDocument> = emptyList(),
    val meta: KakaoLocalMeta? = null,
)

data class KakaoLocalDocument(
    @SerializedName("place_name") val placeName: String = "",
    @SerializedName("address_name") val addressName: String? = null,
    @SerializedName("road_address_name") val roadAddressName: String? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("x") val longitude: String = "",
    @SerializedName("y") val latitude: String = "",
    @SerializedName("place_url") val placeUrl: String? = null,
)

data class KakaoLocalMeta(
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("pageable_count") val pageableCount: Int = 0,
    @SerializedName("is_end") val isEnd: Boolean = true,
)
