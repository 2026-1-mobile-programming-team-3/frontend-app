package com.example.siheunggagae.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StatusLabelsTest {

    @Test fun matchStatus_known_codes() {
        assertEquals("지원자 모집 중", matchStatusToKorean("WAITING"))
        assertEquals("매칭 진행 중", matchStatusToKorean("MATCHING"))
        assertEquals("봉사 진행 중", matchStatusToKorean("PROGRESS"))
        assertEquals("봉사 진행 중", matchStatusToKorean("ONGOING"))
        assertEquals("봉사 완료", matchStatusToKorean("DONE"))
        assertEquals("봉사 완료", matchStatusToKorean("COMPLETED"))
        assertEquals("취소됨", matchStatusToKorean("CANCELED"))
        assertEquals("취소됨", matchStatusToKorean("CANCELLED"))
    }

    @Test fun matchStatus_null_or_blank_returns_default() {
        assertEquals("상태 없음", matchStatusToKorean(null))
        assertEquals("상태 없음", matchStatusToKorean(""))
        assertEquals("상태 없음", matchStatusToKorean("   "))
    }

    @Test fun matchStatus_case_insensitive() {
        assertEquals("지원자 모집 중", matchStatusToKorean("waiting"))
        assertEquals("지원자 모집 중", matchStatusToKorean("Waiting"))
    }

    @Test fun matchStatus_unknown_passes_through() {
        assertEquals("FOO_BAR", matchStatusToKorean("FOO_BAR"))
    }

    @Test fun storeRequestStatus_known_codes() {
        assertEquals("검토 중", storeRequestStatusToKorean("PENDING"))
        assertEquals("승인됨", storeRequestStatusToKorean("APPROVED"))
        assertEquals("거절됨", storeRequestStatusToKorean("REJECTED"))
    }

    @Test fun storeRequestStatus_unknown_passes_through() {
        assertEquals("XYZ", storeRequestStatusToKorean("XYZ"))
    }
}
