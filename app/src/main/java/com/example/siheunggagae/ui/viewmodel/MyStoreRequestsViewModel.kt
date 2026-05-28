package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.repository.StoreRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MyStoreRequestsUiState {
    object Loading : MyStoreRequestsUiState()
    data class Success(
        val items: List<StoreRequestItem>,
        val total: Int,
        val page: Int,
        val hasMore: Boolean,
        val filter: StoreRequestStatus?,
    ) : MyStoreRequestsUiState()
    data class Error(val message: String) : MyStoreRequestsUiState()
}

class MyStoreRequestsViewModel(
    private val repository: StoreRequestRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<MyStoreRequestsUiState>(MyStoreRequestsUiState.Loading)
    val state: StateFlow<MyStoreRequestsUiState> = _state
    private var currentFilter: StoreRequestStatus? = null

    init { refresh() }

    fun setFilter(status: StoreRequestStatus?) {
        if (currentFilter == status) return
        currentFilter = status
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = MyStoreRequestsUiState.Loading
            val resp = repository.list(status = currentFilter, page = 1, size = 20)
            val body = resp.body()
            if (resp.isSuccessful && body != null) {
                _state.value = MyStoreRequestsUiState.Success(
                    items = body.items,
                    total = body.total,
                    page = 1,
                    hasMore = body.items.size >= 20,
                    filter = currentFilter,
                )
            } else {
                _state.value = MyStoreRequestsUiState.Error("목록을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    fun silentRefresh() {
        viewModelScope.launch {
            try {
                val resp = repository.list(status = currentFilter, page = 1, size = 20)
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    _state.value = MyStoreRequestsUiState.Success(
                        items = body.items,
                        total = body.total,
                        page = 1,
                        hasMore = body.items.size >= 20,
                        filter = currentFilter,
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun loadMore() {
        val s = _state.value as? MyStoreRequestsUiState.Success ?: return
        if (!s.hasMore) return
        viewModelScope.launch {
            val next = s.page + 1
            val resp = repository.list(status = currentFilter, page = next, size = 20)
            val body = resp.body()
            if (resp.isSuccessful && body != null) {
                _state.value = s.copy(
                    items = s.items + body.items,
                    page = next,
                    hasMore = body.items.size >= 20,
                )
            }
        }
    }

    class Factory(private val repository: StoreRequestRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MyStoreRequestsViewModel(repository) as T
    }
}
