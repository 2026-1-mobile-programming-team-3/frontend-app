package com.example.siheunggagae

import com.example.siheunggagae.map.DongAggregator.toManwon
import org.junit.Assert.assertEquals
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
}
