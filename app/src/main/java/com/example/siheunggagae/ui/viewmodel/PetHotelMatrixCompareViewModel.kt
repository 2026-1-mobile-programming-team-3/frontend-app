package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.data.repository.PetHotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class CompareMode { ONE_ON_ONE, MULTI }

data class CompareInsight(
    val cheapestId: Int?,
    val highestRatedId: Int?,
    val mostOptionsId: Int?,
    val nearestId: Int?,
)

internal fun computeInsight(items: List<PetHotelResponse>): CompareInsight {
    if (items.isEmpty()) return CompareInsight(null, null, null, null)
    val cheapest = items.filter { it.minPriceKrw != null }
        .minByOrNull { it.minPriceKrw!! }?.storeId
    val highest = items.filter { it.ratingAvg != null }
        .maxByOrNull { it.ratingAvg!! }?.storeId
    val nearest = items.filter { it.distanceM != null }
        .minByOrNull { it.distanceM!! }?.storeId
    val mostOpts = items.maxByOrNull { it.planCount }?.storeId
    return CompareInsight(cheapest, highest, mostOpts, nearest)
}

sealed class MatrixCompareUi {
    object Loading : MatrixCompareUi()
    data class Success(
        val items: List<PetHotelResponse>,    // distance-asc sorted
        val mode: CompareMode,
        val selectedAId: Int,
        val selectedBId: Int,
        val insight: CompareInsight,
    ) : MatrixCompareUi()
    data class Error(val message: String) : MatrixCompareUi()
}

class PetHotelMatrixCompareViewModel(
    private val repository: PetHotelRepository,
    private val initialLat: Double,
    private val initialLng: Double,
    initialRadius: Int = PetHotelCompareViewModel.RADIUS_DEFAULT_M,
) : ViewModel() {

    private val _state = MutableStateFlow<MatrixCompareUi>(MatrixCompareUi.Loading)
    val state: StateFlow<MatrixCompareUi> = _state

    private var raw: List<PetHotelResponse> = emptyList()
    private var radius: Int = initialRadius.coerceIn(1000, PetHotelCompareViewModel.RADIUS_MAX_M)
    private var mode: CompareMode = CompareMode.MULTI
    private var selectedA: Int = -1
    private var selectedB: Int = -1

    init { fetch() }

    fun setMode(m: CompareMode) {
        if (mode == m) return
        mode = m
        recompute()
    }

    fun selectA(storeId: Int) {
        if (storeId == selectedB) {
            // swap to prevent duplicate
            val tmp = selectedA
            selectedA = storeId
            selectedB = tmp
        } else {
            selectedA = storeId
        }
        recompute()
    }

    fun selectB(storeId: Int) {
        if (storeId == selectedA) {
            val tmp = selectedB
            selectedB = storeId
            selectedA = tmp
        } else {
            selectedB = storeId
        }
        recompute()
    }

    fun retry() = fetch()

    private fun fetch() {
        viewModelScope.launch {
            _state.value = MatrixCompareUi.Loading
            val resp = repository.getNearby(initialLat, initialLng, radius)
            if (resp.isSuccessful) {
                raw = resp.body()?.petHotels.orEmpty()
                    .sortedBy { it.distanceM ?: Double.MAX_VALUE }
                mode = if (raw.size <= 2) CompareMode.ONE_ON_ONE else CompareMode.MULTI
                selectedA = raw.getOrNull(0)?.storeId ?: -1
                selectedB = raw.getOrNull(1)?.storeId ?: -1
                recompute()
            } else {
                _state.value = MatrixCompareUi.Error("주변 펫호텔을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    private fun recompute() {
        _state.value = MatrixCompareUi.Success(
            items = raw,
            mode = mode,
            selectedAId = selectedA,
            selectedBId = selectedB,
            insight = computeInsight(raw),
        )
    }

    class Factory(
        private val repository: PetHotelRepository,
        private val initialLat: Double,
        private val initialLng: Double,
        private val initialRadius: Int = PetHotelCompareViewModel.RADIUS_DEFAULT_M,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PetHotelMatrixCompareViewModel(repository, initialLat, initialLng, initialRadius) as T
    }
}
