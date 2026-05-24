package com.example.siheunggagae.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryVisualTest {

    @Test fun visualFor_known_categories_distinct() {
        val cafe = CategoryVisual.forCategory("CAFE")
        val park = CategoryVisual.forCategory("PARK")
        val hospital = CategoryVisual.forCategory("HOSPITAL")
        assertNotEquals(cafe.colorInt, park.colorInt)
        assertNotEquals(park.colorInt, hospital.colorInt)
        assertTrue(cafe.gradient.size >= 2)
    }

    @Test fun visualFor_unknown_returns_default() {
        val default = CategoryVisual.forCategory("UNKNOWN_XYZ")
        assertNotNull(default)
        assertEquals(CategoryVisual.DEFAULT, default)
    }

    @Test fun visualFor_null_returns_default() {
        assertEquals(CategoryVisual.DEFAULT, CategoryVisual.forCategory(null))
    }

    @Test fun visualFor_case_insensitive() {
        assertEquals(CategoryVisual.forCategory("CAFE"), CategoryVisual.forCategory("cafe"))
        assertEquals(CategoryVisual.forCategory("CAFE"), CategoryVisual.forCategory("Cafe"))
    }

    @Test fun korean_label_present() {
        assertEquals("카페", CategoryVisual.forCategory("CAFE").koreanLabel)
        assertEquals("공원", CategoryVisual.forCategory("PARK").koreanLabel)
    }
}
