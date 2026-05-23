package com.example.siheunggagae

import com.example.siheunggagae.data.model.PetHotelListResponse
import com.example.siheunggagae.data.model.PetHotelPlan
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.ui.viewmodel.CompareSortAxis
import com.example.siheunggagae.ui.viewmodel.PetSize
import com.example.siheunggagae.ui.viewmodel.applySizeFilter
import com.example.siheunggagae.ui.viewmodel.applySort
import com.example.siheunggagae.ui.viewmodel.matchesSize
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetHotelCompareLogicTest {

    @Test fun matchesSize_korean_explicit() {
        assertTrue(matchesSize("1박 소형견", PetSize.SMALL))
        assertTrue(matchesSize("1박 중형견", PetSize.MEDIUM))
        assertTrue(matchesSize("1박 대형견", PetSize.LARGE))
    }

    @Test fun matchesSize_english_explicit() {
        assertTrue(matchesSize("Small overnight", PetSize.SMALL))
        assertTrue(matchesSize("medium care", PetSize.MEDIUM))
        assertTrue(matchesSize("LARGE 1 day", PetSize.LARGE))
    }

    @Test fun matchesSize_letter_abbreviation() {
        assertTrue(matchesSize("1박 S", PetSize.SMALL))
        assertTrue(matchesSize("1박 M", PetSize.MEDIUM))
        assertTrue(matchesSize("1박 L", PetSize.LARGE))
    }

    @Test fun matchesSize_no_match_returns_false() {
        assertFalse(matchesSize("프리미엄 1박", PetSize.SMALL))
        assertFalse(matchesSize("프리미엄 1박", PetSize.MEDIUM))
        assertFalse(matchesSize("프리미엄 1박", PetSize.LARGE))
    }

    @Test fun matchesSize_all_always_true() {
        assertTrue(matchesSize("any", PetSize.ALL))
        assertTrue(matchesSize("", PetSize.ALL))
    }

    @Test fun matchesSize_letter_must_be_word_boundary() {
        // 'Snack' should not match SMALL just because of 'S'
        assertFalse(matchesSize("Snack", PetSize.SMALL))
        assertFalse(matchesSize("Lunch", PetSize.LARGE))
    }

    private val gson = Gson()

    /**
     * Construct a PetHotelResponse via Gson so the private [_plans] field is
     * populated correctly — matching how the real JSON deserialisation works.
     */
    private fun hotel(
        id: Int,
        min: Int? = null,
        max: Int? = null,
        dist: Double? = null,
        rating: Double? = null,
        plans: List<PetHotelPlan> = emptyList(),
    ): PetHotelResponse {
        val plansJson = plans.joinToString(",") { p ->
            """{"plan_name":"${p.planName}","price_krw":${p.priceKrw}}"""
        }
        val distStr = if (dist != null) """"distance_m":$dist,""" else ""
        val ratingStr = if (rating != null) """"rating_avg":$rating,""" else ""
        val minStr = if (min != null) """"min_price_krw":$min,""" else ""
        val maxStr = if (max != null) """"max_price_krw":$max,""" else ""
        val json = """{
            "pet_hotels":[{
                "store_id":$id,"name":"h$id","address":"",
                "latitude":0.0,"longitude":0.0,
                $distStr$ratingStr${minStr}${maxStr}
                "plans":[$plansJson]
            }]
        }"""
        return gson.fromJson(json, PetHotelListResponse::class.java).petHotels[0]
    }

    @Test fun applySort_price_ascending_nulls_last() {
        val items = listOf(
            hotel(1, min = 60000),
            hotel(2, min = null),
            hotel(3, min = 40000),
        )
        val sorted = applySort(items, CompareSortAxis.PRICE)
        assertEquals(listOf(3, 1, 2), sorted.map { it.storeId })
    }

    @Test fun applySort_distance_ascending_nulls_last() {
        val items = listOf(
            hotel(1, dist = 2000.0),
            hotel(2, dist = null),
            hotel(3, dist = 500.0),
        )
        val sorted = applySort(items, CompareSortAxis.DISTANCE)
        assertEquals(listOf(3, 1, 2), sorted.map { it.storeId })
    }

    @Test fun applySort_rating_descending_nulls_last() {
        val items = listOf(
            hotel(1, rating = 4.2),
            hotel(2, rating = null),
            hotel(3, rating = 4.8),
        )
        val sorted = applySort(items, CompareSortAxis.RATING)
        assertEquals(listOf(3, 1, 2), sorted.map { it.storeId })
    }

    @Test fun applySizeFilter_all_returns_all() {
        val items = listOf(hotel(1), hotel(2))
        val filtered = applySizeFilter(items, PetSize.ALL)
        assertEquals(2, filtered.size)
    }

    @Test fun applySizeFilter_small_keeps_only_hotels_with_small_plan() {
        val items = listOf(
            hotel(1, plans = listOf(PetHotelPlan("1박 소형견", 40000))),
            hotel(2, plans = listOf(PetHotelPlan("1박 대형견", 70000))),
            hotel(3, plans = listOf(
                PetHotelPlan("1박 소형견", 40000),
                PetHotelPlan("1박 중형견", 55000),
            )),
        )
        val filtered = applySizeFilter(items, PetSize.SMALL)
        assertEquals(listOf(1, 3), filtered.map { it.storeId })
    }
}
