package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.MatchCreateRequest
import com.example.siheunggagae.data.model.MatchUpdateRequest
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.network.ApiError
import com.example.siheunggagae.data.network.api.AuthApiService
import com.example.siheunggagae.data.network.parseApiError
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
    var selectedCategory: MatchCategory? = null
    var desiredDate: String? = null
    var desiredTime: String? = null // 👈 1. 시간 저장을 위한 변수 추가!
    var title: String = ""
    var content: String = ""
    var address: String = ""
    var latitude: Float = 37.3801f
    var longitude: Float = 126.8029f

    fun setCategory(category: MatchCategory) {
        selectedCategory = category
    }

    init { fetchMyPets() }

    // 1. 기존 데이터 불러오기 (수정 모드)
    fun loadMatchDetail(matchId: Int) {
        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            try {
                val response = api.getMatchDetail(matchId)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    title = data.title ?: ""
                    content = data.content ?: ""
                    address = data.address ?: ""
                    desiredDate = data.desiredDate
                    desiredTime = data.desiredTime // 👈 2. 기존 시간 데이터 받아오기
                    selectedPetId = data.pet?.petId

                    _uiState.value = RequestUiState.Idle
                } else {
                    _uiState.value = RequestUiState.Error("데이터를 불러올 수 없습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = RequestUiState.Error("네트워크 오류")
            }
        }
    }

    // 2. 데이터 수정 요청
    fun updateRequest(matchId: Int) {
        // 시간 검증 추가
        if (selectedPetId == null || title.isBlank() || content.isBlank() || address.isBlank() || desiredDate == null || desiredTime == null) {
            _uiState.value = RequestUiState.Error("필수 입력 항목을 모두 채워주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            try {
                val updateBody = MatchUpdateRequest(
                    title = title,
                    content = content,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    desiredDate = desiredDate,
                    desiredTime = desiredTime, // 👈 3. 수정 요청 바디에 시간 추가!
                    petId = selectedPetId
                )
                val response = api.updateMatch(matchId, updateBody)
                if (response.isSuccessful) _uiState.value = RequestUiState.Success
                else _uiState.value = RequestUiState.Error("수정에 실패했습니다.")
            } catch (e: Exception) {
                _uiState.value = RequestUiState.Error("네트워크 오류")
            }
        }
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

    // 3. 신규 데이터 등록 요청
    fun submitRequest() {
        val category = selectedCategory
        if (category == null) {
            _uiState.value = RequestUiState.Error("카테고리를 선택해 주세요.")
            return
        }
        // 시간 검증 추가
        if (selectedPetId == null || title.isBlank() || content.isBlank() || address.isBlank() || desiredDate == null || desiredTime == null) {
            _uiState.value = RequestUiState.Error("필수 입력 항목을 모두 채워주세요.")
            return
        }
        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            try {
                val requestBody = MatchCreateRequest(
                    title = title, content = content,
                    category = category,
                    latitude = latitude, longitude = longitude,
                    address = address, desiredDate = desiredDate,
                    desiredTime = desiredTime, // 👈 4. 생성 요청 바디에 시간 추가!
                    petId = selectedPetId
                )
                val response = api.createMatch(requestBody)
                if (response.isSuccessful) {
                    _uiState.value = RequestUiState.Success
                } else {
                    val err = parseApiError(response.errorBody())
                    val message = when (err) {
                        is ApiError.Structured -> when (err.errorCode) {
                            "VOLUNTEER_ROLE_REQUIRED" -> "봉사자 자격이 필요한 카테고리예요. 자격 신청 후 다시 시도해 주세요."
                            else -> err.message.ifBlank { "요청 등록에 실패했습니다." }
                        }
                        is ApiError.Plain -> err.message
                        ApiError.Unknown -> "요청 등록에 실패했습니다. (${response.code()})"
                    }
                    _uiState.value = RequestUiState.Error(message)
                }
            } catch (e: Exception) {
                _uiState.value = RequestUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun resetState() { _uiState.value = RequestUiState.Idle }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RequestViewModel(api) as T
    }
}