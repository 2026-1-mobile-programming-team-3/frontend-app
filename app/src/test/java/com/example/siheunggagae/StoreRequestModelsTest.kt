package com.example.siheunggagae

import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.model.StoreRequestType
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class StoreRequestModelsTest {
    private val gson = Gson()

    @Test
    fun storeRequestType_serializes_to_uppercase() {
        assertEquals("\"ADD\"", gson.toJson(StoreRequestType.ADD))
        assertEquals("\"UPDATE\"", gson.toJson(StoreRequestType.UPDATE))
    }

    @Test
    fun storeRequestStatus_serializes_to_uppercase() {
        assertEquals("\"PENDING\"", gson.toJson(StoreRequestStatus.PENDING))
        assertEquals("\"APPROVED\"", gson.toJson(StoreRequestStatus.APPROVED))
        assertEquals("\"REJECTED\"", gson.toJson(StoreRequestStatus.REJECTED))
    }
}
