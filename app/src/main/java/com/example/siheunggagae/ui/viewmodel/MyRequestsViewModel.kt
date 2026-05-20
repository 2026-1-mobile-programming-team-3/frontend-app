package com.example.siheunggagae.ui.viewmodel

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

    fun fetchMyRequests() {
        viewModelScope.launch {
            _uiState.value = MyRequestsUiState.Loading
            try {
                // 사용자가 작성한 요청을 조회하기 위해 role 파라미터를 "author"로 지정합니다.
                val response = api.getMyMatches(role = "author")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = MyRequestsUiState.Success(response.body()!!.items ?: emptyList())
                } else {
                    _uiState.value = MyRequestsUiState.Error("데이터를 불러오지 못했습니다. (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = MyRequestsUiState.Error("네트워크 오류가 발생했습니다.")
            }
        }
    }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MyRequestsViewModel(api) as T
    }
}