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

/**
 * 화면 간 이동 시 중복 API 호출(reverseGeocode + getMe) 방지용 인메모리 캐시.
 * 3분 TTL — GPS 이동 반영과 스크린 전환 중복 제거 사이 균형.
 * 위치 권한이 변경되면 invalidate() 호출.
 */
internal object EffectiveCenterCache {
    private const val TTL_MS = 3 * 60 * 1000L

    @Volatile private var cached: EffectiveCenter? = null
    @Volatile private var cachedAt: Long = 0L

    fun get(): EffectiveCenter? {
        val entry = cached ?: return null
        return if (System.currentTimeMillis() - cachedAt < TTL_MS) entry else null
    }

    fun put(center: EffectiveCenter) {
        cached = center
        cachedAt = System.currentTimeMillis()
    }

    fun invalidate() {
        cached = null
        cachedAt = 0L
    }
}

suspend fun resolveEffectiveCenter(
    api: AuthApiService,
    locationProvider: LocationProvider,
): EffectiveCenter {
    EffectiveCenterCache.get()?.let { return it }

    val gps = locationProvider.getLocationOrNull()
    if (gps != null) {
        val rev = runCatching { api.reverseGeocode(gps.latitude, gps.longitude).body() }.getOrNull()
        if (rev?.isInSiheung == true) {
            return EffectiveCenter(
                lat = gps.latitude,
                lng = gps.longitude,
                source = EffectiveCenter.Source.GPS,
                regionDong = rev.label,
            ).also { EffectiveCenterCache.put(it) }
        }
    }
    val regionDong = runCatching { api.getMe().body()?.regionDong }.getOrNull()
    SiheungRegions.coordinatesForDong(regionDong)?.let { (lat, lng) ->
        return EffectiveCenter(
            lat = lat,
            lng = lng,
            source = EffectiveCenter.Source.USER_PROFILE,
            regionDong = regionDong,
        ).also { EffectiveCenterCache.put(it) }
    }
    return EffectiveCenter(
        lat = SiheungRegions.CITY_HALL.first,
        lng = SiheungRegions.CITY_HALL.second,
        source = EffectiveCenter.Source.DEFAULT,
        regionDong = null,
    ).also { EffectiveCenterCache.put(it) }
}
