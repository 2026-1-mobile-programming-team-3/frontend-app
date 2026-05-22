package com.example.siheunggagae.data.model

import com.google.gson.annotations.SerializedName

enum class StoreRequestType { ADD, UPDATE }
enum class StoreRequestStatus { PENDING, APPROVED, REJECTED }

data class StorePricingPlanInput(
    @SerializedName(value = "plan_name", alternate = ["planName"])
    val planName: String,
    @SerializedName(value = "price_krw", alternate = ["priceKrw"])
    val priceKrw: Int,
    @SerializedName(value = "display_order", alternate = ["displayOrder"])
    val displayOrder: Int? = null,
)

data class StoreRequestPayload(
    val name: String? = null,
    val address: String? = null,
    val category: String? = null,
    @SerializedName(value = "is_pet_allowed", alternate = ["isPetAllowed"])
    val isPetAllowed: Boolean? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    @SerializedName(value = "operating_hours", alternate = ["operatingHours"])
    val operatingHours: String? = null,
    @SerializedName(value = "photo_urls", alternate = ["photoUrls"])
    val photoUrls: List<String>? = null,
    val plans: List<StorePricingPlanInput>? = null,
)

data class StoreRequestSubmitRequest(
    val type: StoreRequestType,
    @SerializedName(value = "target_store_id", alternate = ["targetStoreId"])
    val targetStoreId: Int? = null,
    val payload: StoreRequestPayload,
    @SerializedName(value = "proof_urls", alternate = ["proofUrls"])
    val proofUrls: List<String>? = null,
    val message: String? = null,
)

data class StoreRequestSubmitResponse(
    @SerializedName(value = "request_id", alternate = ["requestId", "id"])
    val requestId: Int,
    val type: StoreRequestType,
    @SerializedName(value = "target_store_id", alternate = ["targetStoreId"])
    val targetStoreId: Int?,
    val status: StoreRequestStatus,
    @SerializedName(value = "created_at", alternate = ["createdAt"])
    val createdAt: String,
)

data class StoreRequestItem(
    @SerializedName(value = "request_id", alternate = ["requestId", "id"])
    val requestId: Int,
    val type: StoreRequestType,
    @SerializedName(value = "target_store_id", alternate = ["targetStoreId"])
    val targetStoreId: Int?,
    val payload: StoreRequestPayload,
    // 응답에서는 서버가 항상 배열로 내려주므로 non-null. 요청(SubmitRequest.proofUrls)에서는 null=생략 의미라 nullable인 점 주의.
    @SerializedName(value = "proof_urls", alternate = ["proofUrls"])
    val proofUrls: List<String>,
    val message: String?,
    val status: StoreRequestStatus,
    @SerializedName(value = "review_note", alternate = ["reviewNote"])
    val reviewNote: String?,
    @SerializedName(value = "processed_at", alternate = ["processedAt"])
    val processedAt: String?,
    @SerializedName(value = "created_at", alternate = ["createdAt"])
    val createdAt: String,
)

data class StoreRequestListResponse(
    val items: List<StoreRequestItem>,
    val total: Int,
    val page: Int,
    val size: Int,
)
