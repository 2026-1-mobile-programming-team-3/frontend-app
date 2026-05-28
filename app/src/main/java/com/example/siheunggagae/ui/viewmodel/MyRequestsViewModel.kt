package com.example.siheunggagae.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MyRequestsUiState {
    object Loading : MyRequestsUiState()
    data class Success(val matches: List<MatchListItem>) : MyRequestsUiState()
    data class Error(val message: String) : MyRequestsUiState()
}

class MyRequestsViewModel(private val api: AuthApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<MyRequestsUiState>(MyRequestsUiState.Loading)
    val uiState: StateFlow<MyRequestsUiState> = _uiState

    // ── [태은-9.2] 탭 상태 저장 변수 (0: 전체, 1: 매칭전, 2: 매칭됨, 3: 종료됨) ──
    var selectedTab by mutableStateOf(0)

    fun fetchMyRequests() {
        viewModelScope.launch {
            _uiState.value = MyRequestsUiState.Loading
            try {
                val response = api.getMyMatches(role = "author", status = null)
                if (response.isSuccessful) {
                    val allItems = response.body()?.items ?: emptyList()
                    val filteredItems = when (selectedTab) {
                        1 -> allItems.filter { it.status == "WAITING" || it.status == "RECRUITING" }
                        2 -> allItems.filter { it.status == "MATCHING" || it.status == "PROGRESS" }
                        3 -> allItems.filter { it.status == "DONE" }
                        else -> allItems
                    }
                    _uiState.value = MyRequestsUiState.Success(filteredItems)
                } else {
                    _uiState.value = MyRequestsUiState.Error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                }
            } catch (e: Exception) {
                _uiState.value = MyRequestsUiState.Error("네트워크 연결 상태를 확인해주세요.")
            }
        }
    }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MyRequestsViewModel(api) as T
    }
}