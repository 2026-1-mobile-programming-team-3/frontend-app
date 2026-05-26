package com.example.siheunggagae.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.model.VolunteerStatsResponse
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VolunteerHistoryUiState {
    object Loading : VolunteerHistoryUiState()
    data class Success(
        val stats: VolunteerStatsResponse,
        val matches: List<MatchListItem>,
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
            // /activity-stats 가 정확한 봉사 완료 카운트를 줌 (volunteer-stats는 0 반환 버그)
            val activityStats = runCatching { repository.getActivityStats() }
                .getOrNull()?.takeIf { it.isSuccessful }?.body()
            val volunteerStats = runCatching { repository.getVolunteerStats() }
                .getOrNull()?.takeIf { it.isSuccessful }?.body()
            val stats = VolunteerStatsResponse(
                totalCount = activityStats?.volunteerCompletedCount ?: volunteerStats?.totalCount ?: 0,
                totalHours = null,
                avgRating = volunteerStats?.avgRating,
            )

            runCatching {
                val resp = repository.getVolunteerHistory()
                val items = resp.body()?.items.orEmpty()
                Log.d("VolHist", "HTTP ${resp.code()} | items=${items.size} | statuses=${items.map { it.status }}")
                val matches = items.filter { it.status == "DONE" }
                val avgRating = matches.mapNotNull { it.receivedRating?.toDouble() }
                    .takeIf { it.isNotEmpty() }?.average()
                _uiState.value = VolunteerHistoryUiState.Success(
                    stats = stats.copy(avgRating = avgRating),
                    matches = matches,
                )
            }.onFailure { e ->
                Log.e("VolHist", "exception: ${e.message}", e)
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
