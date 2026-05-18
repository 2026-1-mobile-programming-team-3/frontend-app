package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.model.ReverseGeocodeResponse
import com.example.siheunggagae.data.network.api.AuthApiService

class GeoRepository(
    private val api: AuthApiService,
    private val locationProvider: LocationProvider,
) {

    /**
     * 현재 GPS 좌표 → 행정구역 레이블 ("정왕동").
     * 권한 거부 or GPS 실패 시 → 사용자 프로필의 region_dong 폴백.
     * 둘 다 없으면 null 반환.
     */
    suspend fun getCurrentRegionLabel(): String? {
        val fromGps = reverseGeocodeCurrentLocation()
        if (fromGps != null) return fromGps.label

        return runCatching { api.getMe().body()?.regionDong }.getOrNull()
    }

    /**
     * 현재 GPS 좌표 → 전체 역지오코딩 응답.
     * 권한 거부 or GPS 실패 시 null.
     */
    suspend fun reverseGeocodeCurrentLocation(): ReverseGeocodeResponse? {
        val location = locationProvider.getLocationOrNull() ?: return null
        return reverseGeocode(location.latitude, location.longitude)
    }

    /** 알려진 좌표로 직접 역지오코딩. */
    suspend fun reverseGeocode(lat: Double, lng: Double): ReverseGeocodeResponse? =
        runCatching { api.reverseGeocode(lat, lng).body() }.getOrNull()

    /** 현재 권한 허용 여부. */
    fun hasLocationPermission(): Boolean = locationProvider.hasPermission()
}
