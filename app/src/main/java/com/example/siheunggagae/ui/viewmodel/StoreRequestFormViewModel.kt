package com.example.siheunggagae.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.StorePricingPlanInput
import com.example.siheunggagae.data.model.StoreRequestPayload
import com.example.siheunggagae.data.model.StoreRequestSubmitRequest
import com.example.siheunggagae.data.model.StoreRequestType
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.data.repository.StoreRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class PlanInput(val planName: String, val priceKrw: Int)

data class StoreRequestFormState(
    val name: String = "",
    val category: String? = null,                // CAFE/RESTAURANT/PARK/PET_HOTEL
    val isPetAllowed: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",
    val phone: String = "",
    val operatingHours: String = "",
    val plans: List<PlanInput> = emptyList(),
    // photoStubUrls: source of truth for which photos will be submitted (ordered).
    // photoUrisByUrl: local preview Uri keyed by stub URL — populated only for newly-added photos.
    val photoStubUrls: List<String> = emptyList(),
    val photoUrisByUrl: Map<String, Uri> = emptyMap(),
    // proofUrls: source of truth for which proofs will be submitted (ordered).
    // proofUrisByUrl: local preview Uri keyed by stub URL — populated only for newly-added proofs.
    val proofUrls: List<String> = emptyList(),
    val proofUrisByUrl: Map<String, Uri> = emptyMap(),
    val message: String = "",
)

fun StoreRequestFormState.validateForSubmit(): Pair<Boolean, Map<String, String>> {
    val errors = mutableMapOf<String, String>()
    if (name.isBlank()) errors["name"] = "매장명을 입력해주세요"
    if (category.isNullOrBlank()) errors["category"] = "카테고리를 선택해주세요"
    if (latitude == null || longitude == null) errors["location"] = "지도에서 위치를 선택해주세요"
    if (proofUrls.isEmpty()) errors["proof"] = "증빙 자료를 1개 이상 첨부해주세요"
    if (category == "PET_HOTEL") {
        when {
            plans.isEmpty() ->
                errors["plans"] = "가격 플랜을 1개 이상 추가해주세요"
            plans.any { it.planName.isBlank() || it.priceKrw <= 0 } ->
                errors["plans"] = "플랜명과 가격(0보다 큰 값)을 입력해주세요"
            plans.map { it.planName }.toSet().size != plans.size ->
                errors["plans"] = "플랜명이 중복되었어요"
        }
    }
    return errors.isEmpty() to errors
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Submitting : SubmitState()
    data class Submitted(val requestId: Int) : SubmitState()
    data class Failed(val message: String) : SubmitState()
}

class StoreRequestFormViewModel(
    private val repository: StoreRequestRepository,
    val mode: StoreRequestType,
    val targetStoreId: Int?,
    val sourceRequestId: Int?,
) : ViewModel() {

    private val _form = MutableStateFlow(StoreRequestFormState())
    val form: StateFlow<StoreRequestFormState> = _form

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors

    private val _submit = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submit: StateFlow<SubmitState> = _submit

    init {
        if (sourceRequestId != null) {
            // Re-submission from a rejected/canceled request — prefill from that request's payload
            viewModelScope.launch {
                val resp = repository.detail(sourceRequestId)
                if (resp.isSuccessful) {
                    val item = resp.body() ?: return@launch
                    val p = item.payload
                    _form.value = _form.value.copy(
                        name = p.name.orEmpty(),
                        category = p.category,
                        address = p.address.orEmpty(),
                        isPetAllowed = p.isPetAllowed ?: true,
                        latitude = p.latitude,
                        longitude = p.longitude,
                        phone = p.phone.orEmpty(),
                        operatingHours = p.operatingHours.orEmpty(),
                        plans = p.plans.orEmpty().map { PlanInput(it.planName, it.priceKrw) },
                        photoStubUrls = p.photoUrls.orEmpty(),
                        proofUrls = item.proofUrls,
                        message = item.message.orEmpty(),
                    )
                }
            }
        } else if (mode == StoreRequestType.UPDATE && targetStoreId != null) {
            // UPDATE — fetch the existing store and prefill what we can.
            // Note: StoreDetailResponse does NOT carry `category`; user must re-select category in the form.
            viewModelScope.launch {
                val resp = RetrofitClient.api.getStoreDetail(targetStoreId)
                if (resp.isSuccessful) {
                    val d = resp.body() ?: return@launch
                    _form.value = _form.value.copy(
                        name = d.name.orEmpty(),
                        address = d.address.orEmpty(),
                        isPetAllowed = d.isPetAllowed ?: true,
                        latitude = d.latitude,
                        longitude = d.longitude,
                        phone = d.phone.orEmpty(),
                        operatingHours = d.operatingHours.orEmpty(),
                        photoStubUrls = d.photoUrls.orEmpty(),
                        // category is intentionally left null — user must select.
                        // proofUrls intentionally NOT carried over from previous data.
                    )
                }
            }
        }
    }

    fun setName(v: String) { _form.value = _form.value.copy(name = v) }
    fun setCategory(v: String) { _form.value = _form.value.copy(category = v) }
    fun setIsPetAllowed(v: Boolean) { _form.value = _form.value.copy(isPetAllowed = v) }
    fun setPhone(v: String) { _form.value = _form.value.copy(phone = v) }
    fun setHours(v: String) { _form.value = _form.value.copy(operatingHours = v) }
    fun setMessage(v: String) { _form.value = _form.value.copy(message = v.take(1000)) }

    fun applyPickedLocation(lat: Double, lng: Double, address: String?) {
        _form.value = _form.value.copy(
            latitude = lat,
            longitude = lng,
            address = address ?: _form.value.address,
        )
    }

    fun addPlan() {
        _form.value = _form.value.copy(plans = _form.value.plans + PlanInput("", 0))
    }
    fun updatePlanName(index: Int, name: String) {
        val list = _form.value.plans.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(planName = name)
            _form.value = _form.value.copy(plans = list)
        }
    }
    fun updatePlanPrice(index: Int, price: Int) {
        val list = _form.value.plans.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(priceKrw = price)
            _form.value = _form.value.copy(plans = list)
        }
    }
    fun removePlan(index: Int) {
        val list = _form.value.plans.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _form.value = _form.value.copy(plans = list)
        }
    }

    private fun makeStubUrl(): String = "https://placeholder.local/uploads/" + UUID.randomUUID()

    fun addPhotos(uris: List<Uri>) {
        val current = _form.value
        val remaining = 5 - current.photoStubUrls.size
        if (remaining <= 0) return
        val toAdd = uris.take(remaining)
        val newStubs = current.photoStubUrls + toAdd.map { makeStubUrl() }
        val newUriMap = current.photoUrisByUrl + newStubs.takeLast(toAdd.size).zip(toAdd).toMap()
        _form.value = current.copy(photoStubUrls = newStubs, photoUrisByUrl = newUriMap)
    }
    fun removePhoto(index: Int) {
        val current = _form.value
        val stubs = current.photoStubUrls.toMutableList()
        if (index !in stubs.indices) return
        val removedUrl = stubs.removeAt(index)
        val newUriMap = current.photoUrisByUrl - removedUrl
        _form.value = current.copy(photoStubUrls = stubs, photoUrisByUrl = newUriMap)
    }

    fun addProofs(uris: List<Uri>) {
        val current = _form.value
        val remaining = 10 - current.proofUrls.size
        if (remaining <= 0) return
        val toAdd = uris.take(remaining)
        val newUrls = current.proofUrls + toAdd.map { makeStubUrl() }
        val newUriMap = current.proofUrisByUrl + newUrls.takeLast(toAdd.size).zip(toAdd).toMap()
        _form.value = current.copy(proofUrls = newUrls, proofUrisByUrl = newUriMap)
    }
    fun removeProof(index: Int) {
        val current = _form.value
        val urls = current.proofUrls.toMutableList()
        if (index !in urls.indices) return
        val removedUrl = urls.removeAt(index)
        val newUriMap = current.proofUrisByUrl - removedUrl
        _form.value = current.copy(proofUrls = urls, proofUrisByUrl = newUriMap)
    }

    fun submit() {
        val (ok, errs) = _form.value.validateForSubmit()
        _errors.value = errs
        if (!ok) return
        val f = _form.value
        val body = StoreRequestSubmitRequest(
            type = mode,
            targetStoreId = if (mode == StoreRequestType.UPDATE) targetStoreId else null,
            payload = StoreRequestPayload(
                name = f.name,
                address = f.address,
                category = f.category,
                isPetAllowed = f.isPetAllowed,
                latitude = f.latitude,
                longitude = f.longitude,
                phone = f.phone.ifBlank { null },
                operatingHours = f.operatingHours.ifBlank { null },
                photoUrls = f.photoStubUrls.ifEmpty { null },
                plans = if (f.category == "PET_HOTEL")
                    f.plans.mapIndexed { i, p ->
                        StorePricingPlanInput(p.planName, p.priceKrw, i)
                    } else null,
            ),
            proofUrls = f.proofUrls.ifEmpty { null },
            message = f.message.ifBlank { null },
        )
        viewModelScope.launch {
            _submit.value = SubmitState.Submitting
            val resp = repository.submit(body)
            _submit.value = if (resp.isSuccessful) {
                SubmitState.Submitted(resp.body()!!.requestId)
            } else when (resp.code()) {
                409 -> SubmitState.Failed("이미 검토 중인 요청이 있어요")
                429 -> SubmitState.Failed("잠시 후 다시 시도해주세요")
                422 -> SubmitState.Failed("입력 정보를 다시 확인해주세요")
                else -> SubmitState.Failed("제출에 실패했어요 (${resp.code()})")
            }
        }
    }

    class Factory(
        private val repository: StoreRequestRepository,
        private val mode: StoreRequestType,
        private val targetStoreId: Int?,
        private val sourceRequestId: Int?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoreRequestFormViewModel(repository, mode, targetStoreId, sourceRequestId) as T
    }
}
