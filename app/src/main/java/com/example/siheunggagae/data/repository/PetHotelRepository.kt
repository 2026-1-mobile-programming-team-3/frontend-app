package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.model.PetHotelListResponse
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class PetHotelRepository {
    private val api = RetrofitClient.api

    suspend fun getNearby(
        lat: Double,
        lng: Double,
        radius: Int = 5000,
    ): Response<PetHotelListResponse> = api.getPetHotels(lat, lng, radius)
}
