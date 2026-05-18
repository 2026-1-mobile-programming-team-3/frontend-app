package com.example.siheunggagae.data.model

enum class UserRole { USER, VOLUNTEER, ADMIN }

data class UserResponse(
    val id: Int,
    val email: String,
    val nickname: String,
    val phone: String?,
    val role: UserRole,
    val profileImageUrl: String?,
    val regionSi: String?,
    val regionDong: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class UserMeResponse(
    val id: Int,
    val email: String,
    val nickname: String,
    val phone: String?,
    val role: UserRole,
    val profileImageUrl: String?,
    val regionSi: String?,
    val regionDong: String?,
    val pets: List<PetResponse> = emptyList(),
    val createdAt: String,
)

data class UserUpdateRequest(
    val nickname: String? = null,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val regionSi: String? = null,
    val regionDong: String? = null,
)

data class AccountDeleteRequest(
    val password: String,
    val reason: String? = null,
)

data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String,
)

data class MessageResponse(
    val message: String,
)
