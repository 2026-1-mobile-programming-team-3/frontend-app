package com.example.siheunggagae

import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.MatchListResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MatchModelsTest {
    private val gson = Gson()

    @Test
    fun deserializes_new_fields() {
        val json = """
            {"items":[{
              "match_id": 7, "title": "산책", "address": "정왕동",
              "latitude": 37.3752, "longitude": 126.7281,
              "desired_date": "2026-05-25", "desired_time": "14:00",
              "status": "RECRUITING",
              "category": "WALK",
              "author_user_id": 14,
              "distance_m": 350.0
            }]}
        """.trimIndent()
        val parsed = gson.fromJson(json, MatchListResponse::class.java)
        val item = parsed.items!![0]
        assertEquals(MatchCategory.WALK, item.category)
        assertEquals(14, item.authorUserId)
        assertEquals(350.0, item.distanceM!!, 0.01)
    }

    @Test
    fun missing_new_fields_default_null() {
        val json = """
            {"items":[{"match_id": 1, "title": "X", "status": "RECRUITING"}]}
        """.trimIndent()
        val item = gson.fromJson(json, MatchListResponse::class.java).items!![0]
        assertNull(item.category)
        assertNull(item.authorUserId)
        assertNull(item.distanceM)
    }

    @Test
    fun camelCase_alternate_keys_work() {
        val json = """
            {"items":[{"match_id":1,"authorUserId":42,"distanceM":120.5}]}
        """.trimIndent()
        val item = gson.fromJson(json, MatchListResponse::class.java).items!![0]
        assertEquals(42, item.authorUserId)
        assertEquals(120.5, item.distanceM!!, 0.01)
    }
}
