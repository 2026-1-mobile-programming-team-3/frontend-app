package com.example.siheunggagae.map

import com.example.siheunggagae.data.model.DongPriceBucket
import com.example.siheunggagae.data.model.PetHotelResponse

object DongAggregator {

    /**
     * KRW → 만원 단위 문자열 (천 단위까지). trailing ".0" 제거.
     * 예: 53_000 → "5.3", 50_000 → "5", 120_000 → "12", 125_000 → "12.5".
     */
    fun Int.toManwon(): String {
        val tenths = (this + 500) / 1_000     // 0.1만 단위로 반올림
        val whole = tenths / 10
        val frac = tenths % 10
        return if (frac == 0) whole.toString() else "$whole.$frac"
    }

    /**
     * 호텔 리스트 → 동별 가격 버킷.
     *
     * - 각 호텔은 dongCenters 중 가장 가까운 동에 할당 (Haversine 거리)
     * - minPriceKrw == null 인 호텔은 가격 정보 없음 → 스킵
     * - 가격 정보가 있는 호텔이 0개인 동은 버킷 미생성
     * - minKrw = 동 내 호텔들의 min(minPriceKrw)
     *   maxKrw = 동 내 호텔들의 max(maxPriceKrw ?: minPriceKrw)
     */
    fun aggregate(
        hotels: List<PetHotelResponse>,
        dongCenters: Map<String, Pair<Double, Double>>,
    ): List<DongPriceBucket> {
        if (hotels.isEmpty() || dongCenters.isEmpty()) return emptyList()

        data class Acc(var min: Int, var max: Int, var sum: Long, var count: Int)
        val byDong = linkedMapOf<String, Acc>()

        for (h in hotels) {
            val minPrice = h.minPriceKrw ?: continue
            val maxPrice = h.maxPriceKrw ?: minPrice
            val nearest = dongCenters.minByOrNull { (_, c) ->
                haversineMeters(h.latitude, h.longitude, c.first, c.second)
            } ?: continue
            val acc = byDong.getOrPut(nearest.key) { Acc(minPrice, maxPrice, 0L, 0) }
            acc.min = minOf(acc.min, minPrice)
            acc.max = maxOf(acc.max, maxPrice)
            acc.sum += minPrice
            acc.count += 1
        }

        return byDong.map { (dong, acc) ->
            val (lat, lng) = dongCenters.getValue(dong)
            DongPriceBucket(dong = dong, lat = lat, lng = lng,
                count = acc.count, minKrw = acc.min, maxKrw = acc.max,
                avgKrw = (acc.sum / acc.count).toInt())
        }
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2).let { it * it }
        return 2 * r * Math.asin(Math.sqrt(a))
    }
}
