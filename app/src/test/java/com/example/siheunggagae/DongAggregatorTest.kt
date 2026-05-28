package com.example.siheunggagae

import com.example.siheunggagae.map.DongAggregator
import com.example.siheunggagae.map.DongAggregator.toManwon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DongAggregatorTest {

    @Test fun `toManwon 53000 returns 5dot3`() {
        assertEquals("5.3", 53000.toManwon())
    }

    @Test fun `toManwon 50000 returns 5 without trailing decimal`() {
        assertEquals("5", 50000.toManwon())
    }

    @Test fun `toManwon 120000 returns 12`() {
        assertEquals("12", 120000.toManwon())
    }

    @Test fun `toManwon 125000 returns 12dot5`() {
        assertEquals("12.5", 125000.toManwon())
    }

    @Test fun `toManwon 1000 returns 0dot1`() {
        assertEquals("0.1", 1000.toManwon())
    }

    @Test fun `toManwon 0 returns 0`() {
        assertEquals("0", 0.toManwon())
    }

    // --- aggregate() ---

    /** 테스트용 더미 동 좌표: A=(0,0), B=(0,10), C=(10,0). 거리상 충분히 떨어짐. */
    private val dummyDongs = mapOf(
        "A" to (0.0 to 0.0),
        "B" to (0.0 to 10.0),
        "C" to (10.0 to 0.0),
    )

    private fun hotel(id: Int, lat: Double, lng: Double, min: Int?, max: Int? = min) =
        com.example.siheunggagae.data.model.PetHotelResponse(
            storeId = id, name = "H$id", address = "addr$id",
            latitude = lat, longitude = lng,
            minPriceKrw = min, maxPriceKrw = max,
        )

    @Test fun `aggregate empty input returns empty`() {
        val result = DongAggregator.aggregate(emptyList(), dummyDongs)
        assertTrue(result.isEmpty())
    }

    @Test fun `aggregate single hotel assigns to nearest dong with count 1 and min equals max`() {
        // (0.1, 0.1) 은 A=(0,0) 에 가장 가까움
        val result = DongAggregator.aggregate(
            listOf(hotel(1, 0.1, 0.1, min = 30_000, max = 30_000)),
            dummyDongs,
        )
        assertEquals(1, result.size)
        val bucket = result.first()
        assertEquals("A", bucket.dong)
        assertEquals(1, bucket.count)
        assertEquals(30_000, bucket.minKrw)
        assertEquals(30_000, bucket.maxKrw)
    }

    @Test fun `aggregate two hotels in different dongs returns two buckets`() {
        val result = DongAggregator.aggregate(
            listOf(
                hotel(1, 0.1, 0.1, min = 30_000, max = 50_000),     // A
                hotel(2, 0.1, 9.9, min = 40_000, max = 60_000),     // B
            ),
            dummyDongs,
        )
        assertEquals(2, result.size)
        val byDong = result.associateBy { it.dong }
        assertEquals(1, byDong["A"]?.count)
        assertEquals(30_000, byDong["A"]?.minKrw)
        assertEquals(50_000, byDong["A"]?.maxKrw)
        assertEquals(1, byDong["B"]?.count)
        assertEquals(40_000, byDong["B"]?.minKrw)
        assertEquals(60_000, byDong["B"]?.maxKrw)
    }

    @Test fun `aggregate three hotels in same dong aggregates min and max`() {
        val result = DongAggregator.aggregate(
            listOf(
                hotel(1, 0.1, 0.1, min = 40_000, max = 60_000),
                hotel(2, 0.2, 0.2, min = 30_000, max = 50_000),
                hotel(3, 0.0, 0.3, min = 55_000, max = 80_000),
            ),
            dummyDongs,
        )
        assertEquals(1, result.size)
        val bucket = result.first()
        assertEquals("A", bucket.dong)
        assertEquals(3, bucket.count)
        assertEquals(30_000, bucket.minKrw)
        assertEquals(80_000, bucket.maxKrw)
    }

    @Test fun `aggregate hotel with null minPrice is skipped`() {
        val result = DongAggregator.aggregate(
            listOf(
                hotel(1, 0.1, 0.1, min = 30_000, max = 50_000),
                hotel(2, 0.2, 0.2, min = null, max = null),
            ),
            dummyDongs,
        )
        assertEquals(1, result.size)
        val bucket = result.first()
        assertEquals(1, bucket.count)
        assertEquals(30_000, bucket.minKrw)
        assertEquals(50_000, bucket.maxKrw)
    }

    @Test fun `aggregate all hotels with null price returns empty`() {
        val result = DongAggregator.aggregate(
            listOf(
                hotel(1, 0.1, 0.1, min = null, max = null),
                hotel(2, 9.9, 0.1, min = null, max = null),
            ),
            dummyDongs,
        )
        assertTrue(result.isEmpty())
    }
}
