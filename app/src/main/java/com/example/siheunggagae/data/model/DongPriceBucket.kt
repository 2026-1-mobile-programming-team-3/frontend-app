package com.example.siheunggagae.data.model

/**
 * 시흥시 행정동 1개분 펫호텔 가격 집계.
 * count 는 가격 정보(min_price_krw != null)가 있는 호텔 수만 카운트.
 * 가격 정보가 있는 호텔이 0개인 동은 버킷 자체가 생성되지 않으므로 count >= 1 보장.
 */
data class DongPriceBucket(
    val dong: String,
    val lat: Double,
    val lng: Double,
    val count: Int,
    val minKrw: Int,
    val maxKrw: Int,
    val avgKrw: Int,
)
