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

    // 1번 개선: 단일 변수(Int?)에서 다중 선택 배열 변수(List<Int>)로 전면 교정
    var selectedPetIds: List<Int> = emptyList()
    var selectedCategory: MatchCategory? = null
    var desiredDate: String? = null
    var desiredTime: String? = null
    var title: String = ""
    var content: String = ""
    var address: String = ""
    var latitude: Float = 37.3801f
    var longitude: Float = 126.8029f
    // 사용자가 갤러리에서 선택한 사진의 content:// URI 목록. 백엔드의 multipart upload 추가 전까지는
    // 작성 직후 본인 기기 메모리에만 살아 있고, 백엔드가 echo 해주지 않는 한 디테일 화면에서는 비어 보인다.
    var selectedImageUris: List<String> = emptyList()

    fun setCategory(category: MatchCategory) {
        selectedCategory = category
    }

    init { fetchMyPets() }

    fun loadMatchDetail(matchId: Int) {
        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            try {
                val response = api.getMatchDetail(matchId)
                val data = response.body()
                if (response.isSuccessful && data != null) {
                    title = data.title ?: ""
                    content = data.content ?: ""
                    address = data.address ?: ""
                    desiredDate = data.desiredDate
                    desiredTime = data.desiredTime

                    // 하이브리드 파싱: 신규 스펙(pets) 리스트가 있으면 매핑하고, 구버전 단일 객체(pet)만 있으면 리스트로 변환해 결합
                    selectedPetIds = data.pets?.mapNotNull { it.petId } ?: listOfNotNull(data.pet?.petId)
                    selectedImageUris = data.imageUrls.orEmpty()

                    _uiState.value = RequestUiState.Idle
                } else {
                    _uiState.value = RequestUiState.Error("데이터를 불러올 수 없습니다.")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = RequestUiState.Error("네트워크 오류")
            }
        }
    }

    fun updateRequest(matchId: Int) {
        // 1번 개선: selectedPetId == null 검증을 selectedPetIds.isEmpty() 배열 검증으로 변경
        if (selectedPetIds.isEmpty() || title.isBlank() || content.isBlank() || address.isBlank() || desiredDate == null || desiredTime == null) {
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
                    desiredTime = desiredTime,
                    petIds = selectedPetIds, // DTO에 리스트 대입
                    imageUrls = selectedImageUris.ifEmpty { null },
                )
                val response = api.updateMatch(matchId, updateBody)
                if (response.isSuccessful) _uiState.value = RequestUiState.Success
                else _uiState.value = RequestUiState.Error("수정에 실패했습니다.")
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = RequestUiState.Error("네트워크 오류")
            }
        }
    }

    fun fetchMyPets() {
        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            try {
                val response = api.getMe()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _uiState.value = RequestUiState.PetsLoaded(body.pets)
                } else {
                    _uiState.value = RequestUiState.Error("반려동물 정보를 불러오지 못했습니다.")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = RequestUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun submitRequest() {
        val category = selectedCategory
        if (category == null) {
            _uiState.value = RequestUiState.Error("카테고리를 선택해 주세요.")
            return
        }
        // 1번 개선: selectedPetId == null 검증을 selectedPetIds.isEmpty() 배열 검증으로 변경
        if (selectedPetIds.isEmpty() || title.isBlank() || content.isBlank() || address.isBlank() || desiredDate == null || desiredTime == null) {
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
                    desiredTime = desiredTime,
                    petIds = selectedPetIds, // DTO에 리스트 대입
                    imageUrls = selectedImageUris.ifEmpty { null },
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
                if (e is kotlinx.coroutines.CancellationException) throw e
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