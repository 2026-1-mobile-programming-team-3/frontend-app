package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchReviewRequest
import com.example.siheunggagae.data.model.MatchReviewResponse
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReviewUiState {
    object Idle : ReviewUiState()
    object Loading : ReviewUiState()
    // 🌟 [수정] 조회 모드 대응을 위해 성공 상태 시 후기 데이터를 담을 수 있도록 구조 확장
    data class Success(val review: MatchReviewResponse? = null) : ReviewUiState()
    data class Error(val message: String) : ReviewUiState()
}

class MatchReviewViewModel(private val api: AuthApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Idle)
    val uiState: StateFlow<ReviewUiState> = _uiState

    // ── [태은-8.5] 후기 등록 POST API 호출 ──
    fun submitReview(matchId: Int, rating: Int, content: String, imageUris: List<String>, onComplete: () -> Unit) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.value = ReviewUiState.Loading
            try {
                val requestBody = MatchReviewRequest(
                    proofImageUrls = imageUris,
                    rating = rating,
                    content = content
                )
                val response = api.submitMatchReview(matchId, requestBody)

                if (response.isSuccessful) {
                    _uiState.value = ReviewUiState.Success()
                    onComplete()
                } else {
                    val errorMsg = if (response.code() == 409) "이미 후기를 작성한 매칭입니다." else "등록에 실패했습니다."
                    _uiState.value = ReviewUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = ReviewUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    // ── 🌟 [신규 추가] 작성된 후기 단건 조회 GET API 호출 ──
    fun loadReview(matchId: Int) {
        viewModelScope.launch {
            _uiState.value = ReviewUiState.Loading
            try {
                val response = api.getMatchReview(matchId)
                if (response.isSuccessful && response.body() != null) {
                    // 성공 시 UI State에 후기 엔티티를 탑재하여 화면에 뿌려줌
                    _uiState.value = ReviewUiState.Success(response.body())
                } else {
                    _uiState.value = ReviewUiState.Error("후기 정보를 불러오지 못했습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = ReviewUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    // ── 🌟 [신규 추가] 후기 내용 수정 PATCH API 호출 ──
    fun modifyReview(matchId: Int, rating: Int, content: String, onComplete: () -> Unit) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.value = ReviewUiState.Loading
            try {
                val requestBody = MatchReviewRequest(
                    proofImageUrls = emptyList(), // 필요시 이미지 전달 구조 확장 가능
                    rating = rating,
                    content = content
                )
                val response = api.updateMatchReview(matchId, requestBody)

                if (response.isSuccessful) {
                    // 수정한 데이터 버전을 다시 갱신 반영
                    _uiState.value = ReviewUiState.Success(response.body())
                    onComplete()
                } else {
                    _uiState.value = ReviewUiState.Error("후기 수정에 실패했습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = ReviewUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun resetState() {
        _uiState.value = ReviewUiState.Idle
    }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MatchReviewViewModel(api) as T
    }
}