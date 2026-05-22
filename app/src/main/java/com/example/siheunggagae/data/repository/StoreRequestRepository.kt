package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.model.StoreRequestListResponse
import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.model.StoreRequestSubmitRequest
import com.example.siheunggagae.data.model.StoreRequestSubmitResponse
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class StoreRequestRepository {
    private val api = RetrofitClient.api

    suspend fun submit(body: StoreRequestSubmitRequest): Response<StoreRequestSubmitResponse> =
        api.submitStoreRequest(body)

    suspend fun list(
        status: StoreRequestStatus? = null,
        page: Int = 1,
        size: Int = 20,
    ): Response<StoreRequestListResponse> =
        api.getMyStoreRequests(status = status?.name, page = page, size = size)

    suspend fun detail(requestId: Int): Response<StoreRequestItem> =
        api.getStoreRequest(requestId)

    suspend fun cancel(requestId: Int): Response<Unit> =
        api.cancelStoreRequest(requestId)
}
