package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.data.repository.PetHotelRepository
import com.example.siheunggagae.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class CompareSortAxis { PRICE, DISTANCE, RATING }
enum class PetSize { ALL, SMALL, MEDIUM, LARGE }

sealed class PetHotelCompareUi {
    object Loading : PetHotelCompareUi()
    data class Success(
        val items: List<PetHotelResponse>,
        val absMinPrice: Int,
        val absMaxPrice: Int,
        val sort: CompareSortAxis,
        val size: PetSize,
        val radius: Int,
    ) : PetHotelCompareUi()
    data class Error(val message: String) : PetHotelCompareUi()
}

internal fun matchesSize(planName: String, size: PetSize): Boolean {
    val lower = planName.lowercase()
    return when (size) {
        PetSize.ALL    -> true
        PetSize.SMALL  -> "소형" in planName ||
            "small" in lower ||
            Regex("(?<![A-Za-z])[sS](?![A-Za-z])").containsMatchIn(planName)
        PetSize.MEDIUM -> "중형" in planName ||
            "medium" in lower ||
            Regex("(?<![A-Za-z])[mM](?![A-Za-z])").containsMatchIn(planName)
        PetSize.LARGE  -> "대형" in planName ||
            "large" in lower ||
            Regex("(?<![A-Za-z])[lL](?![A-Za-z])").containsMatchIn(planName)
    }
}

internal fun applySort(
    items: List<PetHotelResponse>,
    axis: CompareSortAxis,
): List<PetHotelResponse> = when (axis) {
    CompareSortAxis.PRICE    -> items.sortedBy { it.minPriceKrw ?: Int.MAX_VALUE }
    CompareSortAxis.DISTANCE -> items.sortedBy { it.distanceM ?: Double.MAX_VALUE }
    CompareSortAxis.RATING   -> items.sortedByDescending { it.ratingAvg ?: -1.0 }
}

internal fun applySizeFilter(
    items: List<PetHotelResponse>,
    size: PetSize,
): List<PetHotelResponse> {
    if (size == PetSize.ALL) return items
    return items.filter { hotel -> hotel.plans.any { matchesSize(it.planName, size) } }
}

class PetHotelCompareViewModel(
    private val repository: PetHotelRepository,
    private val userRepository: UserRepository,
    private val initialLat: Double,
    private val initialLng: Double,
    initialRadius: Int = RADIUS_DEFAULT_M,
) : ViewModel() {

    private val _state = MutableStateFlow<PetHotelCompareUi>(PetHotelCompareUi.Loading)
    val state: StateFlow<PetHotelCompareUi> = _state

    private var sort: CompareSortAxis = CompareSortAxis.PRICE
    private var size: PetSize = PetSize.ALL
    private var radius: Int = initialRadius.coerceIn(1000, RADIUS_MAX_M)
    private var raw: List<PetHotelResponse> = emptyList()
    private var fetchJob: Job? = null

    companion object {
        const val RADIUS_DEFAULT_M = 5000
        const val RADIUS_STEP_M    = 5000
        const val RADIUS_MAX_M     = 50000
    }

    init { fetch() }

    fun setSort(axis: CompareSortAxis) {
        if (sort == axis) return
        sort = axis
        recompute()
    }

    fun setSize(s: PetSize) {
        if (size == s) return
        size = s
        recompute()
    }

    fun expandRadius() {
        val next = (radius + RADIUS_STEP_M).coerceAtMost(RADIUS_MAX_M)
        if (next == radius) return
        radius = next
        fetch()
    }

    fun retry() = fetch()

    fun toggleFavorite(storeId: Int) {
        val current = raw.firstOrNull { it.storeId == storeId }?.isFavorited ?: return
        val nextValue = !current

        // optimistic
        raw = raw.map { if (it.storeId == storeId) it.copy(isFavorited = nextValue) else it }
        recompute()

        viewModelScope.launch {
            val ok = runCatching {
                if (nextValue) {
                    userRepository.addFavoriteStore(storeId).isSuccessful
                } else {
                    userRepository.deleteFavoriteStore(storeId).isSuccessful
                }
            }.getOrDefault(false)

            if (!ok) {
                // 롤백
                raw = raw.map { if (it.storeId == storeId) it.copy(isFavorited = current) else it }
                recompute()
            }
        }
    }

    private fun fetch() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _state.value = PetHotelCompareUi.Loading
            val resp = repository.getNearby(initialLat, initialLng, radius)
            if (resp.isSuccessful) {
                raw = resp.body()?.petHotels.orEmpty()
                recompute()
            } else {
                _state.value = PetHotelCompareUi.Error("주변 펫호텔을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    private fun recompute() {
        val filtered = applySizeFilter(raw, size)
        val sorted = applySort(filtered, sort)
        val absMin = filtered.mapNotNull { it.minPriceKrw }.minOrNull() ?: 0
        val absMax = filtered.mapNotNull { it.maxPriceKrw }.maxOrNull() ?: absMin
        _state.value = PetHotelCompareUi.Success(
            items = sorted,
            absMinPrice = absMin,
            absMaxPrice = absMax,
            sort = sort,
            size = size,
            radius = radius,
        )
    }

    class Factory(
        private val repository: PetHotelRepository,
        private val userRepository: UserRepository,
        private val initialLat: Double,
        private val initialLng: Double,
        private val initialRadius: Int = RADIUS_DEFAULT_M,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PetHotelCompareViewModel(
                repository, userRepository, initialLat, initialLng, initialRadius,
            ) as T
    }
}
