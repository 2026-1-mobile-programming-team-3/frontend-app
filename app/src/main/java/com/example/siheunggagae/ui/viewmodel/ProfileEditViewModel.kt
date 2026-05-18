package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.local.TokenManager
import com.example.siheunggagae.data.model.ConflictResponse
import com.example.siheunggagae.data.model.PydanticErrorResponse
import com.example.siheunggagae.data.model.UserMeResponse
import com.example.siheunggagae.data.model.UserUpdateRequest
import com.example.siheunggagae.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileEditUiState {
    object Loading : ProfileEditUiState()
    data class Loaded(val user: UserMeResponse) : ProfileEditUiState()
    object Saving : ProfileEditUiState()
    object SaveSuccess : ProfileEditUiState()
    data class NicknameConflict(val message: String) : ProfileEditUiState()
    data class FieldErrors(val errors: Map<String, String>) : ProfileEditUiState()
    data class Error(val message: String) : ProfileEditUiState()
}

class ProfileEditViewModel(
    private val repository: UserRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Loading)
    val uiState: StateFlow<ProfileEditUiState> = _uiState

    private val _localImageUri = MutableStateFlow(tokenManager.localProfileImageUri.value)
    val localImageUri: StateFlow<String?> = _localImageUri

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = ProfileEditUiState.Loading
            try {
                val resp = repository.getMe()
                _uiState.value = if (resp.isSuccessful && resp.body() != null) {
                    ProfileEditUiState.Loaded(resp.body()!!)
                } else {
                    ProfileEditUiState.Error("사용자 정보를 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileEditUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun setLocalImageUri(uri: String?) {
        tokenManager.setLocalProfileImageUri(uri)
        _localImageUri.value = uri
    }

    fun save(
        nickname: String,
        phone: String?,
        regionSi: String?,
        regionDong: String?,
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileEditUiState.Saving
            try {
                val resp = repository.updateMe(
                    UserUpdateRequest(
                        nickname = nickname.takeIf { it.isNotBlank() },
                        phone = phone?.takeIf { it.isNotBlank() },
                        profileImageUrl = null,
                        regionSi = regionSi?.takeIf { it.isNotBlank() },
                        regionDong = regionDong?.takeIf { it.isNotBlank() },
                    )
                )
                val body = resp.errorBody()?.string()
                _uiState.value = when (resp.code()) {
                    in 200..299 -> ProfileEditUiState.SaveSuccess
                    409 -> {
                        val detail = runCatching {
                            Gson().fromJson(body, ConflictResponse::class.java).detail
                        }.getOrDefault("이미 사용 중인 닉네임입니다.")
                        ProfileEditUiState.NicknameConflict(detail)
                    }
                    422 -> ProfileEditUiState.FieldErrors(parseFieldErrors(body))
                    else -> ProfileEditUiState.Error("저장에 실패했습니다")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileEditUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun clearSaveResult() {
        loadCurrentUser()
    }

    private fun parseFieldErrors(errorBody: String?): Map<String, String> {
        if (errorBody.isNullOrBlank()) return emptyMap()
        return try {
            val resp = Gson().fromJson(errorBody, PydanticErrorResponse::class.java)
            resp.detail.associate { (it.loc.lastOrNull() ?: "unknown") to it.msg }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    class Factory(
        private val repository: UserRepository,
        private val tokenManager: TokenManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileEditViewModel(repository, tokenManager) as T
    }
}
