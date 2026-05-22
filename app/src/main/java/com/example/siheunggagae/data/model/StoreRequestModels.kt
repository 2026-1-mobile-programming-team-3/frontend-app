package com.example.siheunggagae.data.model

enum class StoreRequestType { ADD, UPDATE }
enum class StoreRequestStatus { PENDING, APPROVED, REJECTED }

data class StorePricingPlanInput(
    val plan_name: String,
    val price_krw: Int,
    val display_order: Int? = null,
)

data class StoreRequestPayload(
    val name: String? = null,
    val address: String? = null,
    val category: String? = null,
    val is_pet_allowed: Boolean? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val operating_hours: String? = null,
    val photo_urls: List<String>? = null,
    val plans: List<StorePricingPlanInput>? = null,
)

data class StoreRequestSubmitRequest(
    val type: StoreRequestType,
    val target_store_id: Int? = null,
    val payload: StoreRequestPayload,
    val proof_urls: List<String>? = null,
    val message: String? = null,
)

data class StoreRequestSubmitResponse(
    val request_id: Int,
    val type: StoreRequestType,
    val target_store_id: Int?,
    val status: StoreRequestStatus,
    val created_at: String,
)

data class StoreRequestItem(
    val request_id: Int,
    val type: StoreRequestType,
    val target_store_id: Int?,
    val payload: StoreRequestPayload,
    val proof_urls: List<String>,
    val message: String?,
    val status: StoreRequestStatus,
    val review_note: String?,
    val processed_at: String?,
    val created_at: String,
)

data class StoreRequestListResponse(
    val items: List<StoreRequestItem>,
    val total: Int,
    val page: Int,
    val size: Int,
)
