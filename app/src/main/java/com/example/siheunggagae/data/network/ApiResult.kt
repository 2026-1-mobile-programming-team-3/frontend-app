package com.example.siheunggagae.data.network

import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val type: ApiErrorType, val message: String) : ApiResult<Nothing>()
}

enum class ApiErrorType {
    BadRequest,    // 400
    Unauthorized,  // 401
    Forbidden,     // 403
    NotFound,      // 404
    Conflict,      // 409
    Validation,    // 422
    RateLimited,   // 429
    ServerError,   // 5xx
    Network,       // IOException
}

fun koreanMessage(type: ApiErrorType): String = when (type) {
    ApiErrorType.BadRequest   -> "잘못된 요청입니다"
    ApiErrorType.Unauthorized -> "로그인이 필요합니다"
    ApiErrorType.Forbidden    -> "접근 권한이 없습니다"
    ApiErrorType.NotFound     -> "찾을 수 없습니다"
    ApiErrorType.Conflict     -> "이미 존재하는 정보입니다"
    ApiErrorType.Validation   -> "입력값을 확인해주세요"
    ApiErrorType.RateLimited  -> "요청이 너무 많습니다. 잠시 후 다시 시도해주세요"
    ApiErrorType.ServerError  -> "잠시 후 다시 시도해주세요"
    ApiErrorType.Network      -> "네트워크 연결을 확인해주세요"
}

private fun errorType(code: Int): ApiErrorType = when (code) {
    400  -> ApiErrorType.BadRequest
    401  -> ApiErrorType.Unauthorized
    403  -> ApiErrorType.Forbidden
    404  -> ApiErrorType.NotFound
    409  -> ApiErrorType.Conflict
    422  -> ApiErrorType.Validation
    429  -> ApiErrorType.RateLimited
    in 500..599 -> ApiErrorType.ServerError
    else -> ApiErrorType.ServerError
}

private fun Response<*>.parseErrorDetail(): String? = try {
    val raw = errorBody()?.string() ?: return null
    when (val detail = JSONObject(raw).opt("detail")) {
        is String    -> detail.takeIf { it.isNotBlank() }
        is JSONArray -> buildString {
            for (i in 0 until detail.length()) {
                val msg = detail.getJSONObject(i).optString("msg")
                if (msg.isNotBlank()) { if (isNotEmpty()) append(", "); append(msg) }
            }
        }.ifBlank { null }
        else -> null
    }
} catch (_: Exception) { null }

/**
 * Retrofit Response를 ApiResult로 변환.
 * 422 Validation 에러는 백엔드 detail 메시지를 그대로 사용.
 */
fun <T> Response<T>.toApiResult(): ApiResult<T> {
    if (isSuccessful) {
        return body()?.let { ApiResult.Success(it) }
            ?: ApiResult.Error(ApiErrorType.ServerError, koreanMessage(ApiErrorType.ServerError))
    }
    val type = errorType(code())
    val backendMsg = parseErrorDetail()
    val message = if (type == ApiErrorType.Validation && backendMsg != null) backendMsg
                  else koreanMessage(type)
    return ApiResult.Error(type, message)
}

/**
 * Repository에서 API 호출 시 try/catch 보일러플레이트 제거용 헬퍼.
 *
 * 사용 예:
 * ```
 * suspend fun login(req: LoginRequest) = runApiCall { api.login(req) }
 * ```
 */
suspend fun <T> runApiCall(block: suspend () -> Response<T>): ApiResult<T> = try {
    block().toApiResult()
} catch (_: IOException) {
    ApiResult.Error(ApiErrorType.Network, koreanMessage(ApiErrorType.Network))
} catch (_: Exception) {
    ApiResult.Error(ApiErrorType.ServerError, koreanMessage(ApiErrorType.ServerError))
}
