package com.example.siheunggagae

import com.example.siheunggagae.data.model.StoreDetailResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoreDetailModelsTest {
    private val gson = Gson()

    @Test
    fun deserializes_category_and_plans() {
        val json = """
            {
              "store_id": 42,
              "name": "해피포우 펫호텔",
              "category": "PET_HOTEL",
              "plans": [
                {"plan_name": "소형견 1박", "price_krw": 30000, "display_order": 0},
                {"plan_name": "중형견 1박", "price_krw": 40000, "display_order": 1}
              ]
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, StoreDetailResponse::class.java)
        assertEquals("PET_HOTEL", parsed.category)
        assertEquals(2, parsed.plans.size)
        assertEquals("소형견 1박", parsed.plans[0].planName)
        assertEquals(30000, parsed.plans[0].priceKrw)
        assertEquals(0, parsed.plans[0].displayOrder)
    }

    @Test
    fun plans_defaults_to_empty_list_when_absent() {
        val json = """{"store_id": 1, "name": "카페"}"""
        val parsed = gson.fromJson(json, StoreDetailResponse::class.java)
        assertNull(parsed.category)
        assertEquals(emptyList<Any>(), parsed.plans)
    }

    @Test
    fun plans_defaults_to_empty_list_when_null() {
        val json = """{"store_id": 1, "name": "카페", "plans": null}"""
        val parsed = gson.fromJson(json, StoreDetailResponse::class.java)
        assertEquals(emptyList<Any>(), parsed.plans)
    }
}
