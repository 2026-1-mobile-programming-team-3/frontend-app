package com.example.siheunggagae.ui.viewmodel

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

    fun fetchDetail(matchId: Int) {
        viewModelScope.launch {
            _uiState.value = MatchDetailUiState.Loading
            try {
                val response = api.getMatchDetail(matchId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = MatchDetailUiState.Success(response.body()!!)
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

    fun resetState() {
        _uiState.value = MatchDetailUiState.Loading
    }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MatchDetailViewModel(api) as T
    }
}