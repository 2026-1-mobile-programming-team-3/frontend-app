package com.example.siheunggagae.data.location

import com.example.siheunggagae.data.local.SiheungRegions
import com.example.siheunggagae.data.network.api.AuthApiService

/**
 * Map/Matching/Home 이 "현재 어디를 기준으로 보여줄지" 일관되게 결정하는 단일 진입점.
 *
 * 우선순위:
 *   1. GPS 가 시흥 안 → GPS 좌표
 *   2. GPS 가 시흥 밖이거나 권한 없음 → 사용자 프로필의 region_dong 좌표
 *   3. region_dong 도 없거나 미등록 동 → 시흥시청
 *
 * 사용자가 시흥 밖에 있어도 시흥 안 매장/매칭을 볼 수 있게 하는 핵심 로직.
 */
data class EffectiveCenter(
    val lat: Double,
    val lng: Double,
    val source: Source,
    val regionDong: String?,
) {
    enum class Source { GPS, USER_PROFILE, DEFAULT }

    val isFallback: Boolean get() = source != Source.GPS
    val latLng: Pair<Double, Double> get() = lat to lng
}

suspend fun resolveEffectiveCenter(
    api: AuthApiService,
    locationProvider: LocationProvider,
): EffectiveCenter {
    val gps = locationProvider.getLocationOrNull()
    if (gps != null) {
        val rev = runCatching { api.reverseGeocode(gps.latitude, gps.longitude).body() }.getOrNull()
        if (rev?.isInSiheung == true) {
            return EffectiveCenter(
                lat = gps.latitude,
                lng = gps.longitude,
                source = EffectiveCenter.Source.GPS,
                regionDong = rev.label,
            )
        }
    }
    val regionDong = runCatching { api.getMe().body()?.regionDong }.getOrNull()
    SiheungRegions.coordinatesForDong(regionDong)?.let { (lat, lng) ->
        return EffectiveCenter(
            lat = lat,
            lng = lng,
            source = EffectiveCenter.Source.USER_PROFILE,
            regionDong = regionDong,
        )
    }
    return EffectiveCenter(
        lat = SiheungRegions.CITY_HALL.first,
        lng = SiheungRegions.CITY_HALL.second,
        source = EffectiveCenter.Source.DEFAULT,
        regionDong = null,
    )
}
