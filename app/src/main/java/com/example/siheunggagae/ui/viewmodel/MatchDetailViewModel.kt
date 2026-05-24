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
    var isReviewWritten by mutableStateOf(false) // 👈 하단 바 후기 분기용 변수
    var myApplicationStatus by mutableStateOf<String?>(null)

    // 글 작성자가 화면 하단에서 볼 수 있는 전체 지원자 명단 상태 변수
    var applicantList by mutableStateOf<List<com.example.siheunggagae.data.model.ApplicationItem>>(emptyList())

    fun fetchDetail(matchId: Int) {
        viewModelScope.launch {
            _uiState.value = MatchDetailUiState.Loading
            isApplied = false
            myApplicationId = null
            myApplicationStatus = null
            applicantList = emptyList()
            isReviewWritten = false // 초기화
            try {
                val response = api.getMatchDetail(matchId)
                if (response.isSuccessful && response.body() != null) {
                    val detail = response.body()!!

                    // ─── 🌟 [신규 매핑 완료] 백엔드가 추가해 준 후기 작성 여부 값을 프론트 상태 변수에 동기화 ───
                    isReviewWritten = detail.isReviewed ?: false

                    val meResponse = api.getMe()
                    val appsResponse = api.getApplications(matchId)

                    if (meResponse.isSuccessful && appsResponse.isSuccessful) {
                        val myUserId = meResponse.body()?.id
                        currentUserId = myUserId

                        applicantList = appsResponse.body()?.items ?: emptyList()

                        val myApp = appsResponse.body()?.items?.find {
                            it.applicant?.applicantId == myUserId
                        }

                        val rawStatus = myApp?.status?.trim()?.uppercase()

                        isApplied = myApp != null && (rawStatus == "PENDING" || rawStatus == "ACCEPTED")
                        myApplicationId = myApp?.applicationId
                        myApplicationStatus = rawStatus
                    }

                    _uiState.value = MatchDetailUiState.Success(detail)

                } else {
                    _uiState.value = MatchDetailUiState.Error("데이터를 불러오지 못했습니다. (${response.code()})")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.value = MatchDetailUiState.Error("네트워크 오류 발생")
            }
        }
    }

    fun acceptApplication(matchId: Int, applicationId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val body = com.example.siheunggagae.data.model.ApplicationActionRequest(action = "ACCEPT")
                val response = api.respondToApplication(matchId, applicationId, body)
                if (response.isSuccessful) {
                    fetchDetail(matchId)
                    onComplete()
                }
            } catch (e: Exception) {}
        }
    }

    fun rejectApplication(matchId: Int, applicationId: Int) {
        viewModelScope.launch {
            try {
                val body = com.example.siheunggagae.data.model.ApplicationActionRequest(action = "REJECT")
                val response = api.respondToApplication(matchId, applicationId, body)
                if (response.isSuccessful) {
                    fetchDetail(matchId)
                }
            } catch (e: Exception) {}
        }
    }

    fun cancelAcceptedMatching(matchId: Int, applicationId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.cancelMatching(matchId, applicationId)
                if (response.isSuccessful) {
                    fetchDetail(matchId)
                    onComplete()
                }
            } catch (e: Exception) {}
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
                if (e is kotlinx.coroutines.CancellationException) throw e
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
                    isApplied = true
                    myApplicationId = response.body()?.applicationId
                    myApplicationStatus = "PENDING"
                    onResult(true, "봉사 신청이 완료되었습니다.")
                } else {
                    if (response.code() == 409) {
                        fetchDetail(matchId)
                        onResult(false, "이미 신청한 매칭입니다.")
                    } else {
                        onResult(false, "신청에 실패했습니다. (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                onResult(false, "네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun updateMatchStatus(matchId: Int, status: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val requestBody = com.example.siheunggagae.data.model.MatchStatusUpdateRequest(status = status)
                val response = api.updateMatchStatus(matchId, requestBody)

                if (response.isSuccessful) {
                    fetchDetail(matchId)
                    onComplete()
                }
            } catch (e: Exception) {
                // 예외 처리
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