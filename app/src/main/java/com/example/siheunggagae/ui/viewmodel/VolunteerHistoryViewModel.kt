package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MyMatchResponse
import com.example.siheunggagae.data.model.VolunteerStatsResponse
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VolunteerHistoryUiState {
    object Loading : VolunteerHistoryUiState()
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
            val stats = runCatching { repository.getVolunteerStats() }
                .getOrNull()
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?: VolunteerStatsResponse(totalCount = 0, totalHours = null, avgRating = null)

            runCatching {
                val resp = repository.getVolunteerHistory()
                val matches = if (resp.isSuccessful) resp.body()?.content.orEmpty() else emptyList()
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
