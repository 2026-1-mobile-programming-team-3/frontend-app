package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.NotificationCategory
import com.example.siheunggagae.data.model.NotificationItem
import com.example.siheunggagae.data.repository.NotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI-layer tab enum (many backend categories → few UI tabs)
enum class NotiTab(val label: String) {
    ALL("전체"), MATCHING("매칭"), SYSTEM("시스템")
}

val NotificationCategory.notiTab: NotiTab
    get() = when (this) {
        NotificationCategory.VOLUNTEER,
        NotificationCategory.MATCH,
        NotificationCategory.REVIEW -> NotiTab.MATCHING
        NotificationCategory.NEWS,
        NotificationCategory.POLICY,
        NotificationCategory.SYSTEM -> NotiTab.SYSTEM
    }

data class NotificationState(
    val items: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null,
    val selectedTab: NotiTab = NotiTab.ALL,
    val isSelectMode: Boolean = false,
    val selectedIds: Set<Int> = emptySet(),
    val deletedCount: Int? = null,
)

class NotificationViewModel(
    private val repository: NotificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState(isLoading = true))
    val state: StateFlow<NotificationState> = _state

    private val buffer = mutableListOf<NotificationItem>()
    private var currentPage = 1
    private var total = 0
    private var loadJob: Job? = null

    init { loadPage() }

    fun selectTab(tab: NotiTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        buffer.clear()
        currentPage = 1
        total = 0
        _state.update { it.copy(isLoading = true, error = null) }
        loadPage()
    }

    fun loadNextPage() {
        if (_state.value.isLoadingMore || !_state.value.hasMore) return
        _state.update { it.copy(isLoadingMore = true) }
        loadPage()
    }

    fun markRead(id: Int) {
        viewModelScope.launch {
            if (id < 0) repository.markLocalRead(id)
            else runCatching { repository.markRead(id) }
            val idx = buffer.indexOfFirst { it.id == id }
            if (idx >= 0) {
                buffer[idx] = buffer[idx].copy(isRead = true)
                _state.update { it.copy(items = buffer.toList()) }
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            runCatching { repository.markAllRead() }
            repository.markAllLocalRead()
            for (i in buffer.indices) buffer[i] = buffer[i].copy(isRead = true)
            _state.update { it.copy(items = buffer.toList()) }
        }
    }

    // ── 선택 모드 ──────────────────────────────────────────────────────────────

    fun enterSelectMode(id: Int) {
        _state.update { it.copy(isSelectMode = true, selectedIds = setOf(id)) }
    }

    fun toggleSelect(id: Int) {
        _state.update { s ->
            val newIds = if (id in s.selectedIds) s.selectedIds - id else s.selectedIds + id
            s.copy(selectedIds = newIds)
        }
    }

    fun exitSelectMode() {
        _state.update { it.copy(isSelectMode = false, selectedIds = emptySet()) }
    }

    fun deleteSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) { exitSelectMode(); return }
        val count = ids.size
        viewModelScope.launch {
            val localIds = ids.filter { it < 0 }
            if (localIds.isNotEmpty()) repository.deleteLocalNotifications(localIds)
            buffer.removeAll { it.id in ids }
            total = maxOf(0, total - count)
            _state.update {
                it.copy(
                    items = buffer.toList(),
                    isSelectMode = false,
                    selectedIds = emptySet(),
                    deletedCount = count,
                )
            }
        }
    }

    fun clearDeletedMessage() {
        _state.update { it.copy(deletedCount = null) }
    }

    private fun loadPage() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val resp = repository.getNotifications(page = currentPage, size = 20)
                val body = resp.body()
                if (resp.isSuccessful && body != null) {
                    total = body.total
                    buffer.addAll(body.items)
                    currentPage++
                    _state.update {
                        it.copy(
                            items = buffer.toList(),
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = buffer.size < total,
                            error = null,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(isLoading = false, isLoadingMore = false, error = "알림을 불러오지 못했습니다")
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _state.update {
                    it.copy(isLoading = false, isLoadingMore = false, error = e.message ?: "네트워크 오류")
                }
            }
        }
    }

    class Factory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationViewModel(repository) as T
    }
}
