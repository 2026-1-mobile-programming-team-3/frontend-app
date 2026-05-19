package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.VolunteerRequestCreate
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VolunteerApplyUiEvent {
    object Idle : VolunteerApplyUiEvent()
    object Submitting : VolunteerApplyUiEvent()
    object Success : VolunteerApplyUiEvent()
    object AlreadyPending : VolunteerApplyUiEvent()
    data class Error(val message: String) : VolunteerApplyUiEvent()
}

class VolunteerApplyViewModel(private val repository: UserRepository) : ViewModel() {

    private val _event = MutableStateFlow<VolunteerApplyUiEvent>(VolunteerApplyUiEvent.Idle)
    val event: StateFlow<VolunteerApplyUiEvent> = _event

    fun submit(message: String) {
        if (message.isBlank()) return
        _event.value = VolunteerApplyUiEvent.Submitting
        viewModelScope.launch {
            try {
                val resp = repository.submitVolunteerRequest(VolunteerRequestCreate(message))
                _event.value = when {
                    resp.isSuccessful  -> VolunteerApplyUiEvent.Success
                    resp.code() == 409 -> VolunteerApplyUiEvent.AlreadyPending
                    else               -> VolunteerApplyUiEvent.Error("신청에 실패했어요 (${resp.code()})")
                }
            } catch (e: Exception) {
                _event.value = VolunteerApplyUiEvent.Error(e.message ?: "네트워크 오류가 발생했어요")
            }
        }
    }

    fun resetEvent() {
        _event.value = VolunteerApplyUiEvent.Idle
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VolunteerApplyViewModel(repository) as T
    }
}
