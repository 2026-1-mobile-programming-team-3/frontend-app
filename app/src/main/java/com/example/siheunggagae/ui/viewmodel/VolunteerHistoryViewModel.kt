package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MyMatchResponse
import com.example.siheunggagae.data.model.UserRole
import com.example.siheunggagae.data.model.VolunteerStatsResponse
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VolunteerHistoryUiState {
    object Loading : VolunteerHistoryUiState()
    object NotVolunteer : VolunteerHistoryUiState()
    data class Success(
        val stats: VolunteerStatsResponse,
        val matches: List<MyMatchResponse>,
    ) : VolunteerHistoryUiState()
    data class Error(val message: String) : VolunteerHistoryUiState()
}

class VolunteerHistoryViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<VolunteerHistoryUiState>(VolunteerHistoryUiState.Loading)
    val uiState: StateFlow<VolunteerHistoryUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = VolunteerHistoryUiState.Loading
            runCatching {
                val meResp = repository.getMe()
                if (!meResp.isSuccessful) {
                    _uiState.value = VolunteerHistoryUiState.Error("사용자 정보를 불러올 수 없어요")
                    return@launch
                }
                val role = meResp.body()?.role
                if (role != UserRole.VOLUNTEER) {
                    _uiState.value = VolunteerHistoryUiState.NotVolunteer
                    return@launch
                }
                val statsResp = repository.getVolunteerStats()
                val matchesResp = repository.getMyMatches()
                val stats = if (statsResp.isSuccessful) statsResp.body()
                    ?: VolunteerStatsResponse(0, 0.0, null)
                else VolunteerStatsResponse(0, 0.0, null)
                val matches = if (matchesResp.isSuccessful) matchesResp.body()?.content ?: emptyList()
                else emptyList()
                _uiState.value = VolunteerHistoryUiState.Success(stats = stats, matches = matches)
            }.onFailure {
                _uiState.value = VolunteerHistoryUiState.Error("네트워크 오류가 발생했어요")
            }
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VolunteerHistoryViewModel(repository) as T
    }
}
