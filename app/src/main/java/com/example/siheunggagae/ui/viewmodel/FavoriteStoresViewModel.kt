package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.FavoriteStoreItem
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FavoriteStoresUiState {
    object Loading : FavoriteStoresUiState()
    data class Success(val items: List<FavoriteStoreItem>) : FavoriteStoresUiState()
    data class Error(val message: String) : FavoriteStoresUiState()
}

class FavoriteStoresViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoriteStoresUiState>(FavoriteStoresUiState.Loading)
    val uiState: StateFlow<FavoriteStoresUiState> = _uiState

    init { fetchStores() }

    fun fetchStores() {
        viewModelScope.launch {
            _uiState.value = FavoriteStoresUiState.Loading
            try {
                val resp = repository.getFavoriteStores()
                if (resp.isSuccessful) {
                    _uiState.value = FavoriteStoresUiState.Success(resp.body()?.items ?: emptyList())
                } else {
                    _uiState.value = FavoriteStoresUiState.Error("목록을 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                _uiState.value = FavoriteStoresUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun removeFavorite(storeId: Int) {
        val current = _uiState.value as? FavoriteStoresUiState.Success ?: return
        val removed = current.items.find { it.storeId == storeId } ?: return

        // 낙관적 업데이트: 즉시 목록에서 제거
        _uiState.value = FavoriteStoresUiState.Success(current.items.filter { it.storeId != storeId })

        viewModelScope.launch {
            try {
                val resp = repository.deleteFavoriteStore(storeId)
                if (!resp.isSuccessful) {
                    revert(removed)
                }
            } catch (e: Exception) {
                revert(removed)
            }
        }
    }

    private fun revert(item: FavoriteStoreItem) {
        val current = _uiState.value as? FavoriteStoresUiState.Success ?: return
        val restored = (current.items + item).sortedBy { it.createdAt }
        _uiState.value = FavoriteStoresUiState.Success(restored)
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FavoriteStoresViewModel(repository) as T
    }
}
