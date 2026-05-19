package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MatchingUiState {
    object Loading : MatchingUiState()
    data class Success(val matches: List<MatchListItem>) : MatchingUiState()
    data class Error(val message: String) : MatchingUiState()
}

class MatchingViewModel(private val repository: MatchRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchingUiState>(MatchingUiState.Loading)
    val uiState: StateFlow<MatchingUiState> = _uiState

    init {
        fetchMatches() // 뷰모델이 생성될 때 자동으로 전체 목록 불러오기
    }

    fun fetchMatches(status: String? = null) {
        viewModelScope.launch {
            _uiState.value = MatchingUiState.Loading
            // status가 "ALL"이면 null로 변환해서 전체 조회
            val apiStatus = if (status == "ALL") null else status

            repository.getMatchList(status = apiStatus)
                .onSuccess { response ->
                    _uiState.value = MatchingUiState.Success(response.items ?: emptyList())
                }
                .onFailure { error ->
                    _uiState.value = MatchingUiState.Error(error.message ?: "네트워크 오류 발생")
                }
        }
    }

    class Factory(private val repository: MatchRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MatchingViewModel(repository) as T
        }
    }
}