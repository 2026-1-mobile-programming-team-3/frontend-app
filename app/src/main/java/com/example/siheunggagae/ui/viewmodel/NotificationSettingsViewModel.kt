package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 서버 key 상수 — Swagger 기준 (백엔드 key 변경 시 여기만 수정)
object NotifKey {
    const val MATCH  = "match"
    const val NEWS   = "news"
    const val REVIEW = "review"
}

sealed class NotificationSettingsUiState {
    object Loading : NotificationSettingsUiState()
    data class Success(
        val matchEnabled: Boolean,
        val newsEnabled: Boolean,
        val reviewEnabled: Boolean,
    ) : NotificationSettingsUiState()
    data class Error(val message: String) : NotificationSettingsUiState()
}

class NotificationSettingsViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationSettingsUiState>(NotificationSettingsUiState.Loading)
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState

    init { load() }

    fun load() {
        _uiState.value = NotificationSettingsUiState.Loading
        viewModelScope.launch {
            try {
                val resp = repository.getNotificationSettings()
                if (resp.isSuccessful) {
                    val settings = resp.body()?.settings ?: emptyMap()
                    _uiState.value = NotificationSettingsUiState.Success(
                        matchEnabled  = settings[NotifKey.MATCH]  ?: true,
                        newsEnabled   = settings[NotifKey.NEWS]   ?: true,
                        reviewEnabled = settings[NotifKey.REVIEW] ?: false,
                    )
                } else {
                    _uiState.value = NotificationSettingsUiState.Error("설정을 불러오지 못했어요 (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = NotificationSettingsUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun toggle(category: String, enabled: Boolean) {
        val prev = _uiState.value as? NotificationSettingsUiState.Success ?: return
        _uiState.value = when (category) {
            NotifKey.MATCH  -> prev.copy(matchEnabled  = enabled)
            NotifKey.NEWS   -> prev.copy(newsEnabled   = enabled)
            NotifKey.REVIEW -> prev.copy(reviewEnabled = enabled)
            else -> return
        }
        viewModelScope.launch {
            try {
                repository.updateNotificationSettings(mapOf(category to enabled))
            } catch (e: Exception) {
                _uiState.value = prev
            }
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationSettingsViewModel(repository) as T
    }
}
