package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchReviewRequest
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReviewUiState {
    object Idle : ReviewUiState()
    object Loading : ReviewUiState()
    object Success : ReviewUiState()
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
                // 명세서 규격에 맞춰 Request DTO 조립 (이미지 서버가 없으므로 현재는 로컬 갤러리 URI 리스트 탑재)
                val requestBody = MatchReviewRequest(
                    proofImageUrls = imageUris,
                    rating = rating,
                    content = content
                )
                val response = api.submitMatchReview(matchId, requestBody)

                if (response.isSuccessful) {
                    _uiState.value = ReviewUiState.Success
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

    fun resetState() {
        _uiState.value = ReviewUiState.Idle
    }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MatchReviewViewModel(api) as T
    }
}