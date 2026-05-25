package com.example.siheunggagae.data.model

data class ReverseGeocodeResponse(
    val si: String?,
    val sido: String?,
    val dong: String?,
    val formattedAddress: String,
    val isInSiheung: Boolean,
    val label: String,
)

data class GeoSearchResult(
    val placeName: String = "",
    val roadAddress: String? = null,
    val address: String? = null,
    val category: String? = null,
    val phone: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val distanceMeters: Double? = null,
)

data class GeoSearchResponse(
    val results: List<GeoSearchResult> = emptyList(),
    val totalCount: Int = 0,
    val isEnd: Boolean = true,
)
