package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchCreateRequest
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RequestUiState {
    object Idle : RequestUiState()
    object Loading : RequestUiState()
    data class PetsLoaded(val pets: List<PetResponse>) : RequestUiState()
    object Success : RequestUiState()
    data class Error(val message: String) : RequestUiState()
}

class RequestViewModel(private val api: AuthApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<RequestUiState>(RequestUiState.Idle)
    val uiState: StateFlow<RequestUiState> = _uiState

    var selectedPetId: Int? = null
    var desiredDate: String? = null
    var title: String = ""
    var content: String = ""
    var address: String = ""
    // 위치 기반 서비스가 아니므로 임의의 좌표(시흥시청)로 고정 (또는 카카오맵 API 연동 필요)
    var latitude: Float = 37.3801f
    var longitude: Float = 126.8029f

    init {
        fetchMyPets()
    }

    fun fetchMyPets() {
        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            try {
                val response = api.getMe()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = RequestUiState.PetsLoaded(response.body()!!.pets)
                } else {
                    _uiState.value = RequestUiState.Error("반려동물 정보를 불러오지 못했습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = RequestUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun submitRequest() {
        if (selectedPetId == null || title.isBlank() || content.isBlank() || address.isBlank() || desiredDate == null) {
            _uiState.value = RequestUiState.Error("필수 입력 항목을 모두 채워주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            try {
                val requestBody = MatchCreateRequest(
                    title = title,
                    content = content,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    desiredDate = desiredDate,
                    petId = selectedPetId
                )
                val response = api.createMatch(requestBody)
                if (response.isSuccessful) {
                    _uiState.value = RequestUiState.Success
                } else {
                    _uiState.value = RequestUiState.Error("요청 등록에 실패했습니다. (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = RequestUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun resetState() {
        _uiState.value = RequestUiState.Idle
    }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RequestViewModel(api) as T
        }
    }
}