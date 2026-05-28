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
    initialViewportBounds: DoubleArray? = null,
) : ViewModel() {

    private val _state = MutableStateFlow<PetHotelCompareUi>(PetHotelCompareUi.Loading)
    val state: StateFlow<PetHotelCompareUi> = _state

    private var sort: CompareSortAxis = CompareSortAxis.PRICE
    private var size: PetSize = PetSize.ALL
    private var radius: Int = initialRadius.coerceIn(1000, RADIUS_MAX_M)
    private var raw: List<PetHotelResponse> = emptyList()
    private var fetchJob: Job? = null

    /**
     * 지도에서 진입할 때 함께 전달된 viewport bbox(\[swLat, swLng, neLat, neLng\]).
     * /maps/pet-hotels 는 원형 radius 만 지원하지만, 지도의 viewport 마커 카운트와 결과를 일치시키기
     * 위해 응답을 이 bbox 로 한 번 더 잘라낸다. 사용자가 [expandRadius] 로 범위를 넓히면
     * 의도적으로 더 멀리 보겠다는 신호이므로 클립을 해제한다.
     */
    private var viewportClip: DoubleArray? = initialViewportBounds?.takeIf { it.size >= 4 }

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
        viewportClip = null
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
                raw = clipToViewport(resp.body()?.petHotels.orEmpty())
                recompute()
            } else {
                _state.value = PetHotelCompareUi.Error("주변 펫호텔을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    private fun clipToViewport(items: List<PetHotelResponse>): List<PetHotelResponse> {
        val clip = viewportClip ?: return items
        val swLat = clip[0]
        val swLng = clip[1]
        val neLat = clip[2]
        val neLng = clip[3]
        return items.filter {
            it.latitude in swLat..neLat && it.longitude in swLng..neLng
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
        private val initialViewportBounds: DoubleArray? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PetHotelCompareViewModel(
                repository, userRepository, initialLat, initialLng, initialRadius, initialViewportBounds,
            ) as T
    }
}
