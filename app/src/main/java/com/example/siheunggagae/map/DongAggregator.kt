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
}
