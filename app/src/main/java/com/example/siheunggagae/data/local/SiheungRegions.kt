package com.example.siheunggagae.data.local

/**
 * 시흥시 18개 행정동의 중심 좌표.
 *
 * Map/Matching/Home 모두에서 "사용자가 시흥 밖에 있을 때 어디를 기준으로 보여줄지" 결정에 사용.
 * 기존엔 HomeViewModel 안에 박혀 있던 것을 공용 util 로 이동.
 *
 * 새 동이 추가되면 여기 갱신. 다른 시(市)로 확장하면 동적 로드 구조 필요.
 */
object SiheungRegions {

    /** 시흥시청 — 모든 fallback 의 마지막 보루. */
    val CITY_HALL: Pair<Double, Double> = 37.3799 to 126.8030

    val dongCoordinates: Map<String, Pair<Double, Double>> = mapOf(
        "대야동"  to (37.3880 to 126.8030),
        "신천동"  to (37.3730 to 126.7975),
        "신현동"  to (37.3810 to 126.8110),
        "은행동"  to (37.3740 to 126.8130),
        "매화동"  to (37.3615 to 126.8025),
        "도창동"  to (37.3555 to 126.7800),
        "목감동"  to (37.3485 to 126.7890),
        "조남동"  to (37.3335 to 126.7935),
        "포동"    to (37.3665 to 126.7615),
        "군자동"  to (37.3790 to 126.7615),
        "정왕동"  to (37.3400 to 126.7435),
        "능곡동"  to (37.4005 to 126.7940),
        "월곶동"  to (37.4130 to 126.7835),
        "배곧동"  to (37.3600 to 126.7215),
        "장현동"  to (37.3940 to 126.8200),
        "장곡동"  to (37.3765 to 126.8200),
        "연성동"  to (37.3655 to 126.8200),
        "과림동"  to (37.3430 to 126.7645),
    )

    /** 동네명 → 좌표. 미등록 동이면 null. */
    fun coordinatesForDong(dong: String?): Pair<Double, Double>? =
        dong?.let { dongCoordinates[it] }
}
