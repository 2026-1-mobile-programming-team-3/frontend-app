package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.repository.StoreRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class StoreRequestDetailUi {
    object Loading : StoreRequestDetailUi()
    data class Success(val item: StoreRequestItem) : StoreRequestDetailUi()
    data class Error(val message: String) : StoreRequestDetailUi()
}

sealed class CancelState {
    object Idle : CancelState()
    object Cancelling : CancelState()
    object Cancelled : CancelState()
    data class Failed(val message: String) : CancelState()
}

class StoreRequestDetailViewModel(
    private val repository: StoreRequestRepository,
    private val requestId: Int,
) : ViewModel() {
    private val _state = MutableStateFlow<StoreRequestDetailUi>(StoreRequestDetailUi.Loading)
    val state: StateFlow<StoreRequestDetailUi> = _state

    private val _cancel = MutableStateFlow<CancelState>(CancelState.Idle)
    val cancel: StateFlow<CancelState> = _cancel

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = StoreRequestDetailUi.Loading
            val resp = repository.detail(requestId)
            _state.value = if (resp.isSuccessful) {
                StoreRequestDetailUi.Success(resp.body()!!)
            } else {
                StoreRequestDetailUi.Error("요청을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    fun cancelRequest() {
        viewModelScope.launch {
            _cancel.value = CancelState.Cancelling
            val resp = repository.cancel(requestId)
            _cancel.value = when {
                resp.isSuccessful -> CancelState.Cancelled
                resp.code() == 409 -> CancelState.Failed("이미 처리된 요청은 취소할 수 없어요")
                else -> CancelState.Failed("취소에 실패했어요 (${resp.code()})")
            }
        }
    }

    class Factory(private val repository: StoreRequestRepository, private val requestId: Int) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoreRequestDetailViewModel(repository, requestId) as T
    }
}
