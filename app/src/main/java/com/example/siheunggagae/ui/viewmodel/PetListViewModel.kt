package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PetListUiState {
    object Loading : PetListUiState()
    data class Success(val pets: List<PetResponse>) : PetListUiState()
    data class Error(val message: String) : PetListUiState()
}

class PetListViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<PetListUiState>(PetListUiState.Loading)
    val uiState: StateFlow<PetListUiState> = _uiState

    init {
        fetchPets()
    }

    fun fetchPets() {
        viewModelScope.launch {
            _uiState.value = PetListUiState.Loading
            try {
                val resp = repository.getMe()
                _uiState.value = if (resp.isSuccessful && resp.body() != null) {
                    PetListUiState.Success(resp.body()!!.pets)
                } else {
                    PetListUiState.Error("반려동물 정보를 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                _uiState.value = PetListUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun silentRefresh() {
        viewModelScope.launch {
            try {
                val resp = repository.getMe()
                if (resp.isSuccessful && resp.body() != null) {
                    _uiState.value = PetListUiState.Success(resp.body()!!.pets)
                }
            } catch (_: Exception) { }
        }
    }

    fun deletePet(petId: Int) {
        viewModelScope.launch {
            try {
                val resp = repository.deletePet(petId)
                if (resp.isSuccessful) {
                    fetchPets()
                }
            } catch (e: Exception) {
                // 삭제 실패 시 현재 상태 유지
            }
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PetListViewModel(repository) as T
    }
}
