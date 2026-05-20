package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.VolunteerBadgeInfo
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VolunteerBadgeViewModel(private val repository: UserRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _badge = MutableStateFlow<VolunteerBadgeInfo?>(null)
    val badge: StateFlow<VolunteerBadgeInfo?> = _badge

    init { load() }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { repository.getActivityStats() }.onSuccess { resp ->
                if (resp.isSuccessful) _badge.value = resp.body()?.badge
            }
            _isLoading.value = false
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VolunteerBadgeViewModel(repository) as T
    }
}
