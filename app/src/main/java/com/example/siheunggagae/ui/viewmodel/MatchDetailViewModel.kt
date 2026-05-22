package com.example.siheunggagae.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MatchDetailUiState {
    object Loading : MatchDetailUiState()
    data class Success(val detail: MatchDetailResponse) : MatchDetailUiState()
    data class Error(val message: String) : MatchDetailUiState()
    object DeleteSuccess : MatchDetailUiState()
}

class MatchDetailViewModel(private val api: AuthApiService) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchDetailUiState>(MatchDetailUiState.Loading)
    val uiState: StateFlow<MatchDetailUiState> = _uiState

    var isApplied by mutableStateOf(false)
    var myApplicationId by mutableStateOf<Int?>(null)
    var currentUserId by mutableStateOf<Int?>(null)

    // 👈 [1단계 추가] 글 작성자가 화면 하단에서 볼 수 있는 전체 지원자 명단 상태 정의
    var applicantList by mutableStateOf<List<com.example.siheunggagae.data.model.ApplicationItem>>(emptyList())

    fun fetchDetail(matchId: Int) {
        viewModelScope.launch {
            _uiState.value = MatchDetailUiState.Loading
            isApplied = false // 진입 시 초기화
            myApplicationId = null
            applicantList = emptyList() // 👈 진입 시 초기화
            try {
                val response = api.getMatchDetail(matchId)
                if (response.isSuccessful && response.body() != null) {
                    val detail = response.body()!!

                    // [순서 변경] 화면을 Success로 바꾸기 전에 내 정보와 신청자 목록을 미리 조회합니다!
                    val meResponse = api.getMe()
                    val appsResponse = api.getApplications(matchId)

                    if (meResponse.isSuccessful && appsResponse.isSuccessful) {
                        val myUserId = meResponse.body()?.id
                        currentUserId = myUserId

                        // 👈 [1단계 추가] 서버가 내려준 지원자 목록 데이터 전체를 뷰모델 변수에 저장합니다.
                        applicantList = appsResponse.body()?.items ?: emptyList()

                        // [변경] 내가 신청한 이력이 리스트에 있는지 찾고 객체를 통째로 꺼냅니다.
                        val myApp = appsResponse.body()?.items?.find {
                            it.applicant?.applicantId == myUserId
                        }

                        // [변경] 찾은 결과에 따라 신청 여부와 신청 ID를 동기화합니다.
                        isApplied = myApp != null
                        myApplicationId = myApp?.applicationId
                    }

                    // 모든 데이터와 신청 상태(isApplied) 세팅이 완료된 '후'에 화면을 Success로 전환합니다!
                    _uiState.value = MatchDetailUiState.Success(detail)

                } else {
                    _uiState.value = MatchDetailUiState.Error("데이터를 불러오지 못했습니다. (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = MatchDetailUiState.Error("네트워크 오류 발생")
            }
        }
    }

    fun deleteMatch(matchId: Int) {
        viewModelScope.launch {
            _uiState.value = MatchDetailUiState.Loading
            try {
                val response = api.deleteMatch(matchId)
                if (response.isSuccessful) {
                    _uiState.value = MatchDetailUiState.DeleteSuccess
                } else {
                    _uiState.value = MatchDetailUiState.Error("삭제에 실패했습니다. (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = MatchDetailUiState.Error("네트워크 오류 발생")
            }
        }
    }

    fun applyForMatch(matchId: Int, message: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val body = com.example.siheunggagae.data.model.ApplicationCreateRequest(message = message)
                val response = api.applyToMatch(matchId, body)

                if (response.isSuccessful) {
                    isApplied = true // 성공 시 즉시 활성화 제한 상태 유지

                    myApplicationId = response.body()?.applicationId

                    onResult(true, "봉사 신청이 완료되었습니다.")
                } else {
                    if (response.code() == 409) {
                        isApplied = true // 이미 신청된 상태임이 확인되었으므로 버튼을 회색(비활성화)으로 동기화
                        onResult(false, "이미 신청한 매칭입니다.")
                    } else {
                        onResult(false, "신청에 실패했습니다. (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                onResult(false, "네트워크 오류가 발생했습니다.")
            }
        }
    }
    fun updateMatchStatus(matchId: Int, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // 팀원이 정의한 MatchStatusUpdateRequest 모델 클래스를 생성합니다.
                val requestBody = com.example.siheunggagae.data.model.MatchStatusUpdateRequest(status = status)
                val response = api.updateMatchStatus(matchId, requestBody)

                if (response.isSuccessful) {
                    // 상태 변경에 성공하면, 상세 데이터를 다시 호출(fetchDetail)하여
                    // 화면의 상단 배너 칩을 '검토중'으로 실시간 동기화합니다.
                    fetchDetail(matchId)
                    onComplete() // 성공 후 화면 이동 등 후속 처리를 위한 콜백 실행
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리 구역
            }
        }
    }
    fun resetState() {
        _uiState.value = MatchDetailUiState.Loading
    }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MatchDetailViewModel(api) as T
    }
}