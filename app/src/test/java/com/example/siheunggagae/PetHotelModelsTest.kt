package com.example.siheunggagae

import com.example.siheunggagae.data.model.PetHotelListResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PetHotelModelsTest {
    private val gson = Gson()

    @Test
    fun deserializes_snake_case_payload() {
        val json = """
            {
              "pet_hotels": [{
                "store_id": 207,
                "name": "배곧 펫호텔",
                "address": "경기도 시흥시 배곧동",
                "latitude": 37.3752, "longitude": 126.7281,
                "distance_m": 320.5,
                "thumbnail_url": "https://example.com/x.jpg",
                "rating_avg": 4.6, "rating_count": 12,
                "is_favorited": false,
                "plan_count": 3,
                "min_price_krw": 40000, "max_price_krw": 75000,
                "plans": [
                  {"plan_name":"1박 소형견","price_krw":40000,"display_order":0},
                  {"plan_name":"1박 중형견","price_krw":55000,"display_order":1},
                  {"plan_name":"1박 대형견","price_krw":75000,"display_order":2}
                ]
              }]
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, PetHotelListResponse::class.java)
        assertEquals(1, parsed.petHotels.size)
        val h = parsed.petHotels[0]
        assertEquals(207, h.storeId)
        assertEquals(320.5, h.distanceM!!, 0.01)
        assertEquals("https://example.com/x.jpg", h.thumbnailUrl)
        assertEquals(40000, h.minPriceKrw)
        assertEquals(75000, h.maxPriceKrw)
        assertEquals(3, h.planCount)
        assertEquals(3, h.plans.size)
        assertEquals("1박 소형견", h.plans[0].planName)
        assertEquals(40000, h.plans[0].priceKrw)
    }

    @Test
    fun empty_pet_hotels_when_root_key_absent() {
        val parsed = gson.fromJson("{}", PetHotelListResponse::class.java)
        assertEquals(emptyList<Any>(), parsed.petHotels)
    }

    @Test
    fun missing_optional_fields_default() {
        val json = """
            {
              "pet_hotels": [{
                "store_id": 1, "name": "X", "address": "Y",
                "latitude": 0.0, "longitude": 0.0
              }]
            }
        """.trimIndent()
        val h = gson.fromJson(json, PetHotelListResponse::class.java).petHotels[0]
        assertNull(h.distanceM)
        assertNull(h.thumbnailUrl)
        assertEquals(0, h.ratingCount)
        assertEquals(false, h.isFavorited)
        assertEquals(emptyList<Any>(), h.plans)
    }
}
