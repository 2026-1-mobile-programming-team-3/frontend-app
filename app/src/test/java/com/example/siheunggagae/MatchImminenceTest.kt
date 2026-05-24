package com.example.siheunggagae

import com.example.siheunggagae.ui.viewmodel.Imminence
import com.example.siheunggagae.ui.viewmodel.computeImminence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class MatchImminenceTest {
    // KST fixed now: 2026-05-24 10:00
    private val now = LocalDateTime.of(2026, 5, 24, 10, 0)
        .atZone(ZoneId.of("Asia/Seoul")).toInstant()

    @Test fun returns_null_for_past() {
        val result = computeImminence("2026-05-24", "09:00", now)
        assertNull(result)
    }

    @Test fun critical_6h_when_within_6h() {
        // 4 hours later
        val result = computeImminence("2026-05-24", "14:00", now)
        assertEquals(Imminence.CRITICAL_6H, result)
    }

    @Test fun today_24h_when_within_24h() {
        // 14 hours later
        val result = computeImminence("2026-05-25", "00:00", now)
        assertEquals(Imminence.TODAY_24H, result)
    }

    @Test fun tomorrow_d1_when_within_48h() {
        // 30 hours later
        val result = computeImminence("2026-05-25", "16:00", now)
        assertEquals(Imminence.TOMORROW_D1, result)
    }

    @Test fun null_when_beyond_48h() {
        // 72 hours later
        val result = computeImminence("2026-05-27", "10:00", now)
        assertNull(result)
    }

    @Test fun null_when_invalid_format() {
        assertNull(computeImminence("invalid", "14:00", now))
        assertNull(computeImminence("2026-05-24", "bad", now))
    }
}
