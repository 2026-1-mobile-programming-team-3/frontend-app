package com.example.siheunggagae.data.model

import com.google.gson.annotations.SerializedName

enum class PetSpecies { DOG, CAT, OTHER }
enum class PetGender { MALE, FEMALE, UNKNOWN }

data class PetCreate(
    val name: String,
    val species: PetSpecies,
    val breed: String? = null,
    val age: Int? = null,
    @SerializedName("weight_kg") val weightKg: Float? = null,
    @SerializedName("is_neutered") val isNeutered: Boolean,
    val gender: PetGender? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    val note: String? = null,
    val birthDate: String? = null
)

data class PetResponse(
    val id: Int,
    val name: String,
    val species: PetSpecies,
    val breed: String?,
    val age: Int?,
    @SerializedName("weight_kg") val weightKg: Float?,
    @SerializedName("is_neutered") val isNeutered: Boolean,
    val gender: PetGender?,
    @SerializedName("photo_url") val photoUrl: String?,
    val note: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

data class PetUpdate(
    val name: String? = null,
    val breed: String? = null,
    val age: Int? = null,
    @SerializedName("weight_kg") val weightKg: Float? = null,
    @SerializedName("is_neutered") val isNeutered: Boolean? = null,
    val gender: PetGender? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    val note: String? = null,
)