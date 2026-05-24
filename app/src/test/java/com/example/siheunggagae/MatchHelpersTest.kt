package com.example.siheunggagae

import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.requiresVolunteerRole
import com.example.siheunggagae.ui.viewmodel.diffNewMatchIds
import com.example.siheunggagae.ui.viewmodel.walkingMinutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchHelpersTest {

    @Test fun walkingMinutes_floor_to_minimum_1() {
        assertEquals(1, walkingMinutes(50.0))   // < 67m
        assertEquals(1, walkingMinutes(67.0))
        assertEquals(7, walkingMinutes(500.0))  // 500 / 67 ≈ 7.46 → 7
        assertEquals(29, walkingMinutes(2000.0))
    }

    @Test fun requiresVolunteerRole_VET_and_VOLUNTEER_true() {
        assertTrue(MatchCategory.VET.requiresVolunteerRole())
        assertTrue(MatchCategory.VOLUNTEER.requiresVolunteerRole())
    }

    @Test fun requiresVolunteerRole_others_false() {
        assertFalse(MatchCategory.WALK.requiresVolunteerRole())
        assertFalse(MatchCategory.SHOPPING.requiresVolunteerRole())
        assertFalse(MatchCategory.MOVE.requiresVolunteerRole())
    }

    @Test fun diffNewMatchIds_returns_only_new() {
        val previous = setOf(1, 2, 3)
        val current  = listOf(2, 3, 4, 5)
        assertEquals(2, diffNewMatchIds(previous, current))  // 4, 5
    }

    @Test fun diffNewMatchIds_zero_when_no_new() {
        assertEquals(0, diffNewMatchIds(setOf(1, 2, 3), listOf(1, 2, 3)))
        assertEquals(0, diffNewMatchIds(setOf(1, 2, 3), listOf(2)))
    }
}
