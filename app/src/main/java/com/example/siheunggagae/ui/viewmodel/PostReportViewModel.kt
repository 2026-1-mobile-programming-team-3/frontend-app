package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PostReportUiEvent {
    object Idle : PostReportUiEvent()
    object Loading : PostReportUiEvent()
    data class Success(val message: String) : PostReportUiEvent()
    data class Error(val message: String) : PostReportUiEvent()
}

class PostReportViewModel(private val repository: UserRepository) : ViewModel() {

    private val _event = MutableStateFlow<PostReportUiEvent>(PostReportUiEvent.Idle)
    val event: StateFlow<PostReportUiEvent> = _event

    fun blockUser(targetUserId: Int) {
        _event.value = PostReportUiEvent.Loading
        viewModelScope.launch {
            try {
                val resp = repository.createBlock(targetUserId)
                _event.value = if (resp.isSuccessful || resp.code() == 409) {
                    PostReportUiEvent.Success("작성자를 차단했어요")
                } else {
                    PostReportUiEvent.Error("차단에 실패했어요 (${resp.code()})")
                }
            } catch (e: Exception) {
                _event.value = PostReportUiEvent.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun blockUserAndHide(targetUserId: Int) {
        _event.value = PostReportUiEvent.Loading
        viewModelScope.launch {
            try {
                val resp = repository.createBlock(targetUserId)
                _event.value = if (resp.isSuccessful || resp.code() == 409) {
                    PostReportUiEvent.Success("게시글을 숨기고 작성자를 차단했어요")
                } else {
                    PostReportUiEvent.Error("차단에 실패했어요 (${resp.code()})")
                }
            } catch (e: Exception) {
                _event.value = PostReportUiEvent.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun report(targetUserId: Int, reason: String) {
        if (reason.isBlank()) return
        _event.value = PostReportUiEvent.Loading
        viewModelScope.launch {
            try {
                val resp = repository.createReport(targetUserId, reason)
                _event.value = if (resp.isSuccessful) {
                    PostReportUiEvent.Success("신고가 접수되었어요. 검토 후 조치할게요")
                } else {
                    PostReportUiEvent.Error("신고에 실패했어요 (${resp.code()})")
                }
            } catch (e: Exception) {
                _event.value = PostReportUiEvent.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun resetEvent() {
        _event.value = PostReportUiEvent.Idle
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PostReportViewModel(repository) as T
    }
}
