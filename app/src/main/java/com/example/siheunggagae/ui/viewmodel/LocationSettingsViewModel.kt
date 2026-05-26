package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.UserUpdateRequest
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationSettingsViewModel(private val repository: UserRepository) : ViewModel() {

    private val _regionDong = MutableStateFlow<String?>(null)
    val regionDong: StateFlow<String?> = _regionDong

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    init { loadRegion() }

    fun reload() { loadRegion() }

    private fun loadRegion() {
        viewModelScope.launch {
            runCatching {
                val resp = repository.getMe()
                if (resp.isSuccessful) {
                    _regionDong.value = resp.body()?.regionDong
                }
            }
        }
    }

    fun updateRegionDong(
        dong: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (dong.isBlank()) { onError("동 이름을 입력해주세요"); return }
        _isUpdating.value = true
        viewModelScope.launch {
            runCatching {
                val resp = repository.updateMe(UserUpdateRequest(regionDong = dong.trim()))
                if (resp.isSuccessful) {
                    _regionDong.value = dong.trim()
                    onSuccess()
                } else {
                    onError("위치 설정에 실패했어요 (${resp.code()})")
                }
            }.onFailure { e ->
                onError(if (e.message.isNullOrBlank()) "연결에 실패했어요. 잠시 후 다시 시도해주세요." else e.message!!)
            }
            _isUpdating.value = false
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LocationSettingsViewModel(repository) as T
    }
}
