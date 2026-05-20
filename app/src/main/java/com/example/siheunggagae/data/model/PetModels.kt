package com.example.siheunggagae.data.model

enum class PetSpecies { DOG, CAT, OTHER }
enum class PetGender { MALE, FEMALE, UNKNOWN }

data class PetCreate(
    val name: String,
    val species: PetSpecies,
    val breed: String? = null,
    val age: Int? = null,
    val weightKg: Float? = null,
    val isNeutered: Boolean,
    val gender: PetGender? = null,
    val photoUrl: String? = null,
    val note: String? = null,
)

data class PetResponse(
    val id: Int,
    val name: String,
    val species: PetSpecies,
    val breed: String?,
    val age: Int?,
    val weightKg: Float?,
    val isNeutered: Boolean,
    val gender: PetGender?,
    val photoUrl: String?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String,
)

data class PetUpdate(
    val name: String? = null,
    val breed: String? = null,
    val age: Int? = null,
    val weightKg: Float? = null,
    val isNeutered: Boolean? = null,
    val gender: PetGender? = null,
    val photoUrl: String? = null,
    val note: String? = null,
)
