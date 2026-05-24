package com.example.siheunggagae

import com.example.siheunggagae.data.model.StoreViewportItem
import com.example.siheunggagae.map.MarkerProjector
import com.example.siheunggagae.map.MarkerSpec
import com.example.siheunggagae.map.computeMarkerSpecs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 단위 좌표를 1° = 100px 로 단순 매핑하는 fake projector.
 * 예: lat=37.0, lng=126.0 → (12600, 3700) (lng → x, lat → y).
 */
private class FakeProjector(
    private val originLat: Double = 0.0,
    private val originLng: Double = 0.0,
    private val pxPerDegree: Double = 100.0,
) : MarkerProjector {
    override fun toScreen(lat: Double, lng: Double): Pair<Int, Int>? {
        val x = ((lng - originLng) * pxPerDegree).toInt()
        val y = ((lat - originLat) * pxPerDegree).toInt()
        return x to y
    }
}

private fun store(id: Int, lat: Double, lng: Double, cat: String = "CAFE", name: String = "S$id") =
    StoreViewportItem(storeId = id, name = name, latitude = lat, longitude = lng, category = cat)

class MarkerClusteringTest {

    @Test fun `zoom less than 11 returns empty`() {
        val stores = listOf(store(1, 37.0, 126.0), store(2, 37.1, 126.1))
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 10, selectedId = null)
        assertTrue(result.isEmpty())
    }

    @Test fun `zoom 16 always returns singles`() {
        val stores = listOf(
            store(1, 37.0, 126.0),
            store(2, 37.0001, 126.0001),
        )
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 16, selectedId = null)
        assertEquals(2, result.size)
        assertTrue(result.all { it is MarkerSpec.Single })
    }

    @Test fun `nearby stores within 80px form one cluster`() {
        // 1° = 100px, 0.5° = 50px → 80px 이내
        val stores = listOf(
            store(1, 37.0, 126.0),
            store(2, 37.0, 126.5),
            store(3, 37.5, 126.0),
        )
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 13, selectedId = null)
        assertEquals(1, result.size)
        val c = result.first() as MarkerSpec.Cluster
        assertEquals(3, c.count)
        assertEquals(listOf(37.0 to 126.0, 37.0 to 126.5, 37.5 to 126.0), c.memberCoords)
    }

    @Test fun `distant stores stay separate singles`() {
        val stores = listOf(
            store(1, 37.0, 126.0),
            store(2, 38.0, 127.0),
        )
        // |dx|=100, |dy|=100 → distance ≈ 141 px > 80 → separate
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 13, selectedId = null)
        assertEquals(2, result.size)
        assertTrue(result.all { it is MarkerSpec.Single })
    }

    @Test fun `grid-edge case — close stores across decimal boundary still cluster`() {
        val stores = listOf(
            store(1, 37.40, 126.50),
            store(2, 37.41, 126.50),
        )
        // 거리: |dy| = 0.01 * 100 = 1 px → cluster
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 12, selectedId = null)
        assertEquals(1, result.size)
        assertTrue(result.first() is MarkerSpec.Cluster)
    }

    @Test fun `single store at zoom 13 is Single`() {
        val stores = listOf(store(1, 37.0, 126.0))
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 13, selectedId = null)
        assertEquals(1, result.size)
        assertTrue(result.first() is MarkerSpec.Single)
    }

    @Test fun `off-screen stores excluded`() {
        val projector = object : MarkerProjector {
            override fun toScreen(lat: Double, lng: Double): Pair<Int, Int>? =
                if (lat < 38.0) (lng * 100).toInt() to (lat * 100).toInt() else null
        }
        val stores = listOf(
            store(1, 37.0, 126.0),
            store(2, 39.0, 126.0),
        )
        val result = computeMarkerSpecs(stores, projector, zoom = 13, selectedId = null)
        assertEquals(1, result.size)
        assertEquals(1, (result.first() as MarkerSpec.Single).id.removePrefix("store_").toInt())
    }

    @Test fun `top categories sorted by frequency`() {
        val stores = listOf(
            store(1, 37.0, 126.0, "CAFE"),
            store(2, 37.001, 126.0, "CAFE"),
            store(3, 37.002, 126.0, "CAFE"),
            store(4, 37.003, 126.0, "HOSPITAL"),
            store(5, 37.004, 126.0, "HOSPITAL"),
            store(6, 37.005, 126.0, "PARK"),
            store(7, 37.006, 126.0, "RESTAURANT"),
        )
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 12, selectedId = null)
        assertEquals(1, result.size)
        val c = result.first() as MarkerSpec.Cluster
        assertEquals(listOf("CAFE", "HOSPITAL", "PARK"), c.topCategories)
        assertEquals(7, c.count)
    }

    @Test fun `selected store marks Single as isSelected`() {
        val stores = listOf(store(1, 37.0, 126.0), store(2, 38.5, 127.5))
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 16, selectedId = 2)
        val singles = result.filterIsInstance<MarkerSpec.Single>()
        assertEquals(2, singles.size)
        val s2 = singles.first { s -> s.id == "store_2" }
        val s1 = singles.first { s -> s.id == "store_1" }
        assertTrue(s2.isSelected)
        assertTrue(!s1.isSelected)
    }
}
