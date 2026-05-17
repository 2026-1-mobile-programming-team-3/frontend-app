package com.example.siheunggagae.data.model

data class ReverseGeocodeResponse(
    val si: String?,
    val sido: String?,
    val dong: String?,
    val formattedAddress: String,
    val isInSiheung: Boolean,
    val label: String,
)
