package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.PetCreate
import com.example.siheunggagae.data.model.PetGender
import com.example.siheunggagae.data.model.PetSpecies
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PetAddUiState {
    object Idle : PetAddUiState()
    object Loading : PetAddUiState()
    object Success : PetAddUiState()
    data class Error(val message: String) : PetAddUiState()
}

class PetAddViewModel(private val api: AuthApiService) : ViewModel() {
    private val _uiState = MutableStateFlow<PetAddUiState>(PetAddUiState.Idle)
    val uiState: StateFlow<PetAddUiState> = _uiState

    fun addPet(
        name: String, species: PetSpecies, breed: String?,
        ageStr: String, weightStr: String,
        isNeutered: Boolean, gender: PetGender
    ) {
        if (name.isBlank()) {
            _uiState.value = PetAddUiState.Error("이름은 필수 입력입니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PetAddUiState.Loading
            try {
                val age = ageStr.toIntOrNull()
                val weightKg = weightStr.toFloatOrNull()

                val request = PetCreate(
                    name = name,
                    species = species,
                    breed = breed?.ifBlank { null },
                    age = age,
                    weightKg = weightKg,
                    isNeutered = isNeutered,
                    gender = gender
                )

                val response = api.addPet(request)
                if (response.isSuccessful) {
                    _uiState.value = PetAddUiState.Success
                } else {
                    _uiState.value = PetAddUiState.Error("등록 실패 (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = PetAddUiState.Error("네트워크 오류 발생")
            }
        }
    }

    fun resetState() { _uiState.value = PetAddUiState.Idle }

    class Factory(private val api: AuthApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PetAddViewModel(api) as T
    }
}