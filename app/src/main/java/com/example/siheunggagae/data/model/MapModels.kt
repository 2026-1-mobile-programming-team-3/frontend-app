package com.example.siheunggagae.data.model

data class StoreResponse(
    val id: Int,
    val name: String,
    val category: String,
    val lat: Double,
    val lng: Double,
    val distanceM: Double?,
    val rating: Float?,
    val isFavorite: Boolean = false,
)

data class StoreListResponse(
    val stores: List<StoreResponse>,
    val total: Int,
)

data class VolunteerMarkerDto(
    val id: Int,
    val lat: Double,
    val lng: Double,
    val title: String?,
)
