package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.LoginRequest
import com.example.siheunggagae.data.model.SignupRequest
import com.example.siheunggagae.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object RateLimited : AuthUiState()
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
                _uiState.value = AuthUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun signup(email: String, password: String, nickname: String, regionSi: String?, regionDong: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.signup(SignupRequest(email, password, nickname, regionSi = regionSi, regionDong = regionDong))
                _uiState.value = if (response.isSuccessful) AuthUiState.Success
                else AuthUiState.Error(response.message())
            } catch (e: Exception) {
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

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(repository) as T
    }
}
