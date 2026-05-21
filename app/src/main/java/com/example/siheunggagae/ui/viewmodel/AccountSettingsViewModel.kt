package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountSettingsViewModel(private val repository: UserRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun changePassword(
        current: String,
        new: String,
        confirm: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        when {
            current.isBlank() || new.isBlank() -> { onError("비밀번호를 입력해주세요"); return }
            new != confirm -> { onError("새 비밀번호가 일치하지 않아요"); return }
            new.length < 8 -> { onError("비밀번호는 8자 이상이어야 해요"); return }
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = repository.changePassword(current, new)
                if (resp.isSuccessful) onSuccess()
                else onError("비밀번호 변경에 실패했어요 (${resp.code()})")
            } catch (e: Exception) {
                onError(e.message ?: "네트워크 오류")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(
        password: String,
        reason: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (password.isBlank()) { onError("비밀번호를 입력해주세요"); return }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val resp = repository.deleteMe(password, reason?.takeIf { it.isNotBlank() })
                if (resp.isSuccessful) onSuccess()
                else onError("탈퇴에 실패했어요 (${resp.code()})")
            } catch (e: Exception) {
                onError(e.message ?: "네트워크 오류")
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AccountSettingsViewModel(repository) as T
    }
}
