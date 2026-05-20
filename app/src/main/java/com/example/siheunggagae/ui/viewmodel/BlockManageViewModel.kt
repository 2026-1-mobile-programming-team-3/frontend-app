package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.BlockListItem
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BlockManageUiState {
    object Loading : BlockManageUiState()
    data class Success(val items: List<BlockListItem>) : BlockManageUiState()
    data class Error(val message: String) : BlockManageUiState()
}

class BlockManageViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockManageUiState>(BlockManageUiState.Loading)
    val uiState: StateFlow<BlockManageUiState> = _uiState

    init { fetchBlocks() }

    fun fetchBlocks() {
        viewModelScope.launch {
            _uiState.value = BlockManageUiState.Loading
            try {
                val resp = repository.getBlocks()
                if (resp.isSuccessful) {
                    _uiState.value = BlockManageUiState.Success(resp.body()?.items ?: emptyList())
                } else {
                    _uiState.value = BlockManageUiState.Error("목록을 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                _uiState.value = BlockManageUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun unblock(blockId: Int) {
        val current = _uiState.value as? BlockManageUiState.Success ?: return
        val removed = current.items.find { it.blockId == blockId } ?: return
        // 낙관적 업데이트
        _uiState.value = BlockManageUiState.Success(current.items.filter { it.blockId != blockId })
        viewModelScope.launch {
            try {
                val resp = repository.deleteBlock(blockId)
                if (!resp.isSuccessful) revert(removed)
            } catch (_: Exception) {
                revert(removed)
            }
        }
    }

    private fun revert(item: BlockListItem) {
        val current = _uiState.value as? BlockManageUiState.Success ?: return
        _uiState.value = BlockManageUiState.Success(
            (current.items + item).sortedByDescending { it.createdAt }
        )
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BlockManageViewModel(repository) as T
    }
}
