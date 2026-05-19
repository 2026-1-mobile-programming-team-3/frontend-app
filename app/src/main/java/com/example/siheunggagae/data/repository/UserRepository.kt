package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.model.ActivityStatsResponse
import com.example.siheunggagae.data.model.BlockCreateRequest
import com.example.siheunggagae.data.model.BlockCreatedResponse
import com.example.siheunggagae.data.model.BlockListResponse
import com.example.siheunggagae.data.model.ReportCreateRequest
import com.example.siheunggagae.data.model.ReportCreatedResponse
import com.example.siheunggagae.data.model.FavoriteStoreListResponse
import com.example.siheunggagae.data.model.MyMatchListResponse
import com.example.siheunggagae.data.model.VolunteerRequestCreate
import com.example.siheunggagae.data.model.VolunteerRequestResponse
import com.example.siheunggagae.data.model.VolunteerStatsResponse
import com.example.siheunggagae.data.model.MessageResponse
import com.example.siheunggagae.data.model.PetCreate
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.model.PetUpdate
import com.example.siheunggagae.data.model.UserMeResponse
import com.example.siheunggagae.data.model.UserUpdateRequest
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class UserRepository {
    private val api = RetrofitClient.api

    suspend fun getMe(): Response<UserMeResponse> = api.getMe()

    suspend fun getActivityStats(): Response<ActivityStatsResponse> = api.getActivityStats()

    suspend fun getVolunteerStats(): Response<VolunteerStatsResponse> = api.getVolunteerStats()

    suspend fun getMyMatches(page: Int = 1, size: Int = 20): Response<MyMatchListResponse> =
        api.getMyMatches(role = "applicant", status = "DONE", page = page, size = size)

    suspend fun updateMe(request: UserUpdateRequest): Response<UserMeResponse> =
        api.updateMe(request)

    suspend fun addPet(body: PetCreate): Response<PetResponse> = api.addPet(body)

    suspend fun updatePet(petId: Int, body: PetUpdate): Response<PetResponse> =
        api.updatePet(petId, body)

    suspend fun deletePet(petId: Int): Response<MessageResponse> = api.deletePet(petId)

    suspend fun getFavoriteStores(page: Int = 1, size: Int = 20): Response<FavoriteStoreListResponse> =
        api.getFavoriteStores(page, size)

    suspend fun deleteFavoriteStore(storeId: Int): Response<Unit> =
        api.deleteFavoriteStore(storeId)

    suspend fun submitVolunteerRequest(body: VolunteerRequestCreate): Response<VolunteerRequestResponse> =
        api.submitVolunteerRequest(body)

    suspend fun getBlocks(page: Int = 1, size: Int = 50): Response<BlockListResponse> =
        api.getBlocks(page, size)

    suspend fun createBlock(targetUserId: Int): Response<BlockCreatedResponse> =
        api.createBlock(BlockCreateRequest(targetUserId))

    suspend fun deleteBlock(blockId: Int): Response<Unit> =
        api.deleteBlock(blockId)

    suspend fun createReport(targetUserId: Int, reason: String): Response<ReportCreatedResponse> =
        api.createReport(ReportCreateRequest(targetUserId, reason))
}
