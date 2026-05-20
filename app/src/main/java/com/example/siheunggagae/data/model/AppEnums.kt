package com.example.siheunggagae.data.model

/** 매칭 요청 상태 (API: MatchStatus) */
enum class MatchStatus(val label: String) {
    WAITING("모집중"),
    MATCHING("검토중"),
    PROGRESS("진행중"),
    DONE("완료"),
}

/** 봉사 신청 상태 (API: ApplicationStatus) */
enum class ApplicationStatus(val label: String) {
    PENDING("검토중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
}

/** 봉사 신청 처리 액션 */
enum class ApplicationAction { ACCEPT, REJECT }

/**
 * 매장/장소 카테고리.
 * apiValue == null 인 항목은 백엔드 미지원 (UI 필터 전용).
 */
enum class StoreCategory(val label: String, val apiValue: String?) {
    ALL("전체", null),
    CAFE("카페", "CAFE"),
    RESTAURANT("식당", "RESTAURANT"),
    PARK("공원", "PARK"),
    HOSPITAL("동물병원", null),
    GROOMING("미용", null),
}

/** 봉사자 뱃지 등급 (API: VolunteerBadgeTier) — PDF 기준 달성 조건 */
enum class VolunteerBadgeTier(val label: String, val requiredCount: Int) {
    NONE("없음", 0),
    SEED("새싹", 1),
    FLOWER("꽃", 3),
    FRUIT("열매", 8),
    TREE("나무", 15),
}
