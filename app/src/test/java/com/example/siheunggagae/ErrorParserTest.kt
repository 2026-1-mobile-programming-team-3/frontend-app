package com.example.siheunggagae

import com.example.siheunggagae.data.network.ApiError
import com.example.siheunggagae.data.network.parseApiError
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorParserTest {

    private fun body(json: String): ResponseBody =
        json.toResponseBody("application/json".toMediaType())

    @Test fun string_detail_returns_plain() {
        val parsed = parseApiError(body("""{"detail":"Bad request"}"""))
        assertTrue(parsed is ApiError.Plain)
        assertEquals("Bad request", (parsed as ApiError.Plain).message)
    }

    @Test fun object_detail_with_error_code_returns_structured() {
        val json = """{"detail":{"error_code":"VOLUNTEER_ROLE_REQUIRED","message":"봉사자 전용"}}"""
        val parsed = parseApiError(body(json))
        assertTrue(parsed is ApiError.Structured)
        val s = parsed as ApiError.Structured
        assertEquals("VOLUNTEER_ROLE_REQUIRED", s.errorCode)
        assertEquals("봉사자 전용", s.message)
    }

    @Test fun empty_body_returns_unknown() {
        assertTrue(parseApiError(null) is ApiError.Unknown)
    }

    @Test fun malformed_json_returns_plain_raw() {
        val parsed = parseApiError(body("not json"))
        assertTrue(parsed is ApiError.Plain)
    }
}
