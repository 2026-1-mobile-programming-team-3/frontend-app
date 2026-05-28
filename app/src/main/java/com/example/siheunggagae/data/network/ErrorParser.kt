package com.example.siheunggagae.data.network

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import retrofit2.Response

sealed class ApiError {
    data class Plain(val message: String) : ApiError()
    data class Structured(val errorCode: String, val message: String) : ApiError()
    object Unknown : ApiError()
}

/**
 * FastAPI/Pydantic 스타일 에러 응답 파서.
 *
 * detail 이 문자열이면 Plain, 객체({error_code, message}) 이면 Structured.
 * 봉사자 전용 카테고리 작성 시 백엔드가 VOLUNTEER_ROLE_REQUIRED 를 Structured 로 반환.
 */
fun parseApiError(errorBody: ResponseBody?): ApiError {
    val raw = errorBody?.string()?.takeIf { it.isNotBlank() } ?: return ApiError.Unknown
    return runCatching {
        val root = JsonParser.parseString(raw)
        val detail = root.asJsonObject?.get("detail") ?: return ApiError.Plain(raw)
        when {
            detail.isJsonPrimitive -> ApiError.Plain(detail.asString)
            detail.isJsonObject -> {
                val obj = detail.asJsonObject
                val code = obj["error_code"]?.takeIf { it.isJsonPrimitive }?.asString
                val msg = obj["message"]?.takeIf { it.isJsonPrimitive }?.asString
                if (code != null) ApiError.Structured(code, msg ?: "")
                else ApiError.Plain(msg ?: raw)
            }
            else -> ApiError.Plain(raw)
        }
    }.getOrElse { ApiError.Plain(raw) }
}

/** Retrofit Response 에서 에러 추출 ergonomic helper. */
fun <T> Response<T>.toApiError(): ApiError = parseApiError(errorBody())
