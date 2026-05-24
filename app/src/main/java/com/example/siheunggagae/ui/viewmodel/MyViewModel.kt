package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.ActivityStatsResponse
import com.example.siheunggagae.data.model.UserMeResponse
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MyUiState {
    object Loading : MyUiState()
    data class Success(val user: UserMeResponse, val stats: ActivityStatsResponse) : MyUiState()
    data class Error(val message: String) : MyUiState()
}

class MyViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MyUiState>(MyUiState.Loading)
    val uiState: StateFlow<MyUiState> = _uiState

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = MyUiState.Loading
            try {
                val meResp = repository.getMe()
                val statsResp = repository.getActivityStats()
                if (meResp.isSuccessful && statsResp.isSuccessful) {
                    _uiState.value = MyUiState.Success(meResp.body()!!, statsResp.body()!!)
                } else {
                    _uiState.value = MyUiState.Error("정보를 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                _uiState.value = MyUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun silentRefresh() {
        viewModelScope.launch {
            try {
                val meResp = repository.getMe()
                val statsResp = repository.getActivityStats()
                if (meResp.isSuccessful && statsResp.isSuccessful) {
                    _uiState.value = MyUiState.Success(meResp.body()!!, statsResp.body()!!)
                }
            } catch (_: Exception) { }
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MyViewModel(repository) as T
    }
}
