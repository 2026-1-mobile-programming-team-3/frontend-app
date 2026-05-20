package com.example.siheunggagae.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.local.LocalNotificationStore
import com.example.siheunggagae.data.model.NotificationCategory
import com.example.siheunggagae.data.model.PetCreate
import com.example.siheunggagae.data.model.PetGender
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.model.PetSpecies
import com.example.siheunggagae.data.model.PetUpdate
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PetAddUiState {
    object Loading : PetAddUiState()
    object Idle : PetAddUiState()
    object Saving : PetAddUiState()
    object SaveSuccess : PetAddUiState()
    data class Error(val message: String) : PetAddUiState()
}

class PetAddViewModel(
    application: Application,
    private val repository: UserRepository,
    val petId: Int?,
    private val localNotificationStore: LocalNotificationStore? = null,
) : AndroidViewModel(application) {

    val isEditMode get() = petId != null

    private val notePrefs =
        application.getSharedPreferences("pet_notes", Context.MODE_PRIVATE)

    private val photoPrefs =
        application.getSharedPreferences("pet_photos", Context.MODE_PRIVATE)

    // 수정 모드일 때 저장된 값을 동기로 읽어둠
    val savedNote: String =
        if (petId != null) notePrefs.getString("note_$petId", "") ?: "" else ""

    val savedPhotoUri: String? =
        if (petId != null) photoPrefs.getString("photo_$petId", null) else null

    private val _localPhotoUri = MutableStateFlow(savedPhotoUri)
    val localPhotoUri: StateFlow<String?> = _localPhotoUri

    fun setLocalPhotoUri(uri: String?) {
        _localPhotoUri.value = uri
    }

    private val _uiState = MutableStateFlow<PetAddUiState>(
        if (petId != null) PetAddUiState.Loading else PetAddUiState.Idle
    )
    val uiState: StateFlow<PetAddUiState> = _uiState

    private val _initialPet = MutableStateFlow<PetResponse?>(null)
    val initialPet: StateFlow<PetResponse?> = _initialPet

    init {
        if (petId != null) loadPet(petId)
    }

    private fun loadPet(petId: Int) {
        viewModelScope.launch {
            try {
                val resp = repository.getMe()
                if (resp.isSuccessful && resp.body() != null) {
                    _initialPet.value = resp.body()!!.pets.find { it.id == petId }
                    _uiState.value = PetAddUiState.Idle
                } else {
                    _uiState.value = PetAddUiState.Error("반려동물 정보를 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                _uiState.value = PetAddUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun save(
        name: String,
        species: PetSpecies,
        breed: String?,
        age: Int?,
        gender: PetGender?,
        isNeutered: Boolean,
        note: String?,
    ) {
        viewModelScope.launch {
            _uiState.value = PetAddUiState.Saving
            try {
                val resp = if (petId == null) {
                    repository.addPet(
                        PetCreate(
                            name = name,
                            species = species,
                            breed = breed?.takeIf { it.isNotBlank() },
                            age = age,
                            weightKg = null,
                            isNeutered = isNeutered,
                            gender = gender,
                            photoUrl = null,
                        )
                    )
                } else {
                    repository.updatePet(
                        petId,
                        PetUpdate(
                            name = name,
                            breed = breed?.takeIf { it.isNotBlank() },
                            age = age,
                            weightKg = null,
                            isNeutered = isNeutered,
                            gender = gender,
                            photoUrl = null,
                        )
                    )
                }
                if (resp.isSuccessful) {
                    val id = petId ?: resp.body()?.id
                    if (id != null) {
                        if (!note.isNullOrBlank()) {
                            notePrefs.edit().putString("note_$id", note).apply()
                        } else {
                            notePrefs.edit().remove("note_$id").apply()
                        }
                        val photoUri = _localPhotoUri.value
                        if (!photoUri.isNullOrBlank()) {
                            photoPrefs.edit().putString("photo_$id", photoUri).apply()
                        } else {
                            photoPrefs.edit().remove("photo_$id").apply()
                        }
                    }
                    // 알림함에 시스템 알림 추가
                    val notifTitle = if (petId == null) "반려동물 등록됨" else "반려동물 수정됨"
                    val notifBody  = if (petId == null) "${name}이(가) 등록되었습니다."
                                     else "${name} 정보가 수정되었습니다."
                    localNotificationStore?.add(NotificationCategory.SYSTEM, notifTitle, notifBody)
                    _uiState.value = PetAddUiState.SaveSuccess
                } else {
                    _uiState.value = PetAddUiState.Error("저장에 실패했습니다")
                }
            } catch (e: Exception) {
                _uiState.value = PetAddUiState.Error(e.message ?: "네트워크 오류")
            }
        }
    }

    fun clearError() {
        _uiState.value = PetAddUiState.Idle
    }

    class Factory(
        private val application: Application,
        private val repository: UserRepository,
        private val petId: Int?,
        private val localNotificationStore: LocalNotificationStore? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PetAddViewModel(application, repository, petId, localNotificationStore) as T
    }
}
