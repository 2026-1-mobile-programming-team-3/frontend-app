package com.example.siheunggagae

import com.example.siheunggagae.ui.viewmodel.PlanInput
import com.example.siheunggagae.ui.viewmodel.StoreRequestFormState
import com.example.siheunggagae.ui.viewmodel.validateForSubmit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class StoreRequestFormValidationTest {

    private val baseValid = StoreRequestFormState(
        name = "배곧 펫호텔",
        category = "PET_HOTEL",
        isPetAllowed = true,
        latitude = 37.3752,
        longitude = 126.7281,
        address = "경기 시흥시 배곧동",
        plans = listOf(PlanInput("1박 소형견", 40000)),
        proofUrls = listOf("https://placeholder.local/x"),
    )

    @Test fun valid_form_passes() {
        val (ok, _) = baseValid.validateForSubmit()
        assertTrue(ok)
    }

    @Test fun blank_name_fails() {
        val (ok, errors) = baseValid.copy(name = "").validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("name"))
    }

    @Test fun missing_category_fails() {
        val (ok, errors) = baseValid.copy(category = null).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("category"))
    }

    @Test fun missing_location_fails() {
        val (ok, errors) = baseValid.copy(latitude = null, longitude = null).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("location"))
    }

    @Test fun empty_proof_fails_strict() {
        val (ok, errors) = baseValid.copy(proofUrls = emptyList()).validateForSubmit()
        assertFalse(ok)
        assertEquals("증빙 자료를 1개 이상 첨부해주세요", errors["proof"])
    }

    @Test fun pet_hotel_without_plans_fails() {
        val (ok, errors) = baseValid.copy(plans = emptyList()).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("plans"))
    }

    @Test fun non_pet_hotel_with_empty_plans_passes() {
        val (ok, _) = baseValid.copy(category = "CAFE", plans = emptyList()).validateForSubmit()
        assertTrue(ok)
    }

    @Test fun duplicate_plan_name_fails() {
        val (ok, errors) = baseValid.copy(
            plans = listOf(
                PlanInput("1박", 40000),
                PlanInput("1박", 50000),
            ),
        ).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("plans"))
    }

    @Test fun zero_price_plan_fails() {
        val (ok, errors) = baseValid.copy(
            plans = listOf(PlanInput("1박", 0)),
        ).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("plans"))
    }
}
