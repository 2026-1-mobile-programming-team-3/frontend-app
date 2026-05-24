package com.example.siheunggagae.ui.util

/**
 * 매칭/요청 상태 영문 코드 → 한국어 라벨 매핑.
 * 서버는 "WAITING", "MATCHING", "PROGRESS", "DONE", "CANCELED" 등을 반환.
 * UI 노출 시 항상 이 함수 경유.
 */
fun matchStatusToKorean(status: String?): String = when (status?.trim()?.uppercase()) {
    "WAITING"               -> "지원자 모집 중"
    "MATCHING"              -> "매칭 진행 중"
    "PROGRESS", "ONGOING"   -> "봉사 진행 중"
    "DONE", "COMPLETED"     -> "봉사 완료"
    "CANCELED", "CANCELLED" -> "취소됨"
    null, ""                -> "상태 없음"
    else                    -> status!!
}

/**
 * 매장 등록 요청 상태 영문 코드 → 한국어.
 */
fun storeRequestStatusToKorean(status: String?): String = when (status?.trim()?.uppercase()) {
    "PENDING"  -> "검토 중"
    "APPROVED" -> "승인됨"
    "REJECTED" -> "거절됨"
    null, ""   -> "상태 없음"
    else       -> status!!
}
