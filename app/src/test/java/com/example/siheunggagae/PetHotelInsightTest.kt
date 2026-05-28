package com.example.siheunggagae

import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.ui.viewmodel.computeInsight
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PetHotelInsightTest {

    private val gson = Gson()

    private fun hotel(
        id: Int,
        min: Int? = null,
        max: Int? = null,
        rating: Double? = null,
        dist: Double? = null,
        plans: Int = 0,
    ): PetHotelResponse {
        val plansJson = (1..plans).joinToString(",") {
            "{\"plan_name\":\"p$it\",\"price_krw\":${min ?: 0}}"
        }
        val json = """
            {"store_id":$id,"name":"h$id","address":"","latitude":0.0,"longitude":0.0,
             ${min?.let { "\"min_price_krw\":$it," } ?: ""}
             ${max?.let { "\"max_price_krw\":$it," } ?: ""}
             ${rating?.let { "\"rating_avg\":$it," } ?: ""}
             ${dist?.let { "\"distance_m\":$it," } ?: ""}
             "plan_count":$plans,
             "plans":[$plansJson]}
        """.trimIndent()
        return gson.fromJson(json, PetHotelResponse::class.java)
    }

    @Test fun cheapest_picks_min_minPrice() {
        val items = listOf(hotel(1, min = 50000), hotel(2, min = 40000), hotel(3, min = 60000))
        val i = computeInsight(items)
        assertEquals(2, i.cheapestId)
    }

    @Test fun highest_rated_excludes_null_ratings() {
        val items = listOf(hotel(1, rating = null), hotel(2, rating = 4.5), hotel(3, rating = 4.8))
        val i = computeInsight(items)
        assertEquals(3, i.highestRatedId)
    }

    @Test fun nearest_excludes_null_distances() {
        val items = listOf(hotel(1, dist = 2000.0), hotel(2, dist = null), hotel(3, dist = 500.0))
        val i = computeInsight(items)
        assertEquals(3, i.nearestId)
    }

    @Test fun most_options_picks_max_plan_count() {
        val items = listOf(hotel(1, plans = 3), hotel(2, plans = 5), hotel(3, plans = 2))
        val i = computeInsight(items)
        assertEquals(2, i.mostOptionsId)
    }

    @Test fun all_null_categories_return_null() {
        val items = listOf(hotel(1), hotel(2))
        val i = computeInsight(items)
        assertNull(i.cheapestId)
        assertNull(i.highestRatedId)
        assertNull(i.nearestId)
        // plan_count default 0 — maxByOrNull returns first item
        assertEquals(1, i.mostOptionsId)
    }
}
