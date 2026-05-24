package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.LoginRequest
import com.example.siheunggagae.data.model.ConflictResponse
import com.example.siheunggagae.data.model.PydanticErrorResponse
import com.example.siheunggagae.data.model.SignupRequest
import com.example.siheunggagae.data.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object RateLimited : AuthUiState()
    object EmailConflict : AuthUiState()
    data class FieldErrors(val errors: Map<String, String>) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.login(LoginRequest(email, password))
                _uiState.value = when (response.code()) {
                    in 200..299 -> AuthUiState.Success
                    401, 422    -> AuthUiState.Error("이메일 또는 비밀번호가 올바르지 않습니다")
                    429         -> AuthUiState.RateLimited
                    else        -> AuthUiState.Error("로그인에 실패했습니다")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = AuthUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun signup(
        email: String,
        password: String,
        nickname: String,
        phone: String? = null,
        regionSi: String? = null,
        regionDong: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.signup(
                    SignupRequest(
                        email = email,
                        password = password,
                        nickname = nickname,
                        phone = phone,
                        regionSi = regionSi,
                        regionDong = regionDong,
                    )
                )
                when (response.code()) {
                    in 200..299 -> {
                        val loginResp = repository.login(LoginRequest(email, password))
                        _uiState.value = if (loginResp.isSuccessful) AuthUiState.Success
                        else AuthUiState.Error("자동 로그인에 실패했습니다")
                    }
                    409 -> _uiState.value = parse409(response.errorBody()?.string())
                    422 -> _uiState.value = AuthUiState.FieldErrors(
                        parseFieldErrors(response.errorBody()?.string())
                    )
                    429 -> _uiState.value = AuthUiState.RateLimited
                    else -> _uiState.value = AuthUiState.Error("회원가입에 실패했습니다")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = AuthUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    private fun parse409(errorBody: String?): AuthUiState {
        val detail = runCatching {
            Gson().fromJson(errorBody, ConflictResponse::class.java).detail
        }.getOrDefault("")
        return if (detail.contains("닉네임")) {
            AuthUiState.FieldErrors(mapOf("nickname" to detail))
        } else {
            AuthUiState.EmailConflict
        }
    }

    private fun parseFieldErrors(errorBody: String?): Map<String, String> {
        if (errorBody.isNullOrBlank()) return emptyMap()
        return try {
            val resp = Gson().fromJson(errorBody, PydanticErrorResponse::class.java)
            resp.detail.associate { item ->
                (item.loc.lastOrNull() ?: "unknown") to item.msg
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(repository) as T
    }
}
