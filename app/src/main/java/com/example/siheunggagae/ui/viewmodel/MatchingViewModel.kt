package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.model.requiresVolunteerRole
import com.example.siheunggagae.data.repository.MatchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class MatchSort { IMMINENT, RECENT, NEAREST }

enum class DistanceFilter(val meters: Int?) {
    KM1(1000), KM3(3000), KM5(5000), ALL(null)
}

enum class Imminence { CRITICAL_6H, TODAY_24H, TOMORROW_D1 }

private val KST = ZoneId.of("Asia/Seoul")
private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")

internal fun computeImminence(
    desiredDate: String?,
    desiredTime: String?,
    now: Instant = Instant.now(),
): Imminence? {
    if (desiredDate.isNullOrBlank() || desiredTime.isNullOrBlank()) return null
    val date = runCatching { LocalDate.parse(desiredDate, DATE_FMT) }.getOrNull() ?: return null
    val time = runCatching { LocalTime.parse(desiredTime, TIME_FMT) }.getOrNull() ?: return null
    val target = LocalDateTime.of(date, time).atZone(KST).toInstant()
    val deltaSec = target.epochSecond - now.epochSecond
    return when {
        deltaSec < 0 -> null
        deltaSec < 6 * 3600 -> Imminence.CRITICAL_6H
        deltaSec < 24 * 3600 -> Imminence.TODAY_24H
        deltaSec < 48 * 3600 -> Imminence.TOMORROW_D1
        else -> null
    }
}

internal fun walkingMinutes(distanceM: Double): Int =
    (distanceM / 67.0).toInt().coerceAtLeast(1)

internal fun diffNewMatchIds(previous: Set<Int>, current: List<Int>): Int =
    current.count { it !in previous }

sealed class MatchingUi {
    object Loading : MatchingUi()
    data class Success(
        val items: List<MatchListItem>,
        val statusTabCounts: Map<String?, Int>,
        val selectedStatus: String?,
        val selectedCategory: MatchCategory?,
        val sort: MatchSort,
        val distance: DistanceFilter,
        val hasMore: Boolean,
        val isRefreshing: Boolean,
        val newCount: Int,
        val showVolunteerWarning: Boolean,
        val isSearchMode: Boolean = false,
        val searchQuery: String = "",
    ) : MatchingUi()
    data class Error(val message: String) : MatchingUi()
}

class MatchingViewModel(
    private val repository: MatchRepository,
    private val isCurrentUserVolunteer: () -> Boolean,
    private val getCurrentLocation: suspend () -> Pair<Double, Double>?,
) : ViewModel() {

    private val _state = MutableStateFlow<MatchingUi>(MatchingUi.Loading)
    val state: StateFlow<MatchingUi> = _state

    private var raw: List<MatchListItem> = emptyList()
    private var page: Int = 1
    private var lastIdSet: Set<Int> = emptySet()
    private var newCount: Int = 0
    private var fetchJob: Job? = null

    private var selectedStatus: String? = null
    private var selectedCategory: MatchCategory? = null
    private var sort: MatchSort = MatchSort.IMMINENT
    private var distance: DistanceFilter = DistanceFilter.KM5

    private var isSearchMode: Boolean = false
    private var searchQuery: String = ""
    private var searchSavedStatus: String? = null
    private var searchSavedCategory: MatchCategory? = null
    private var searchSavedDistance: DistanceFilter = DistanceFilter.KM5

    init { fetch(reset = true) }

    fun setStatus(status: String?) {
        if (selectedStatus == status) return
        selectedStatus = status
        fetch(reset = true)
    }

    fun setCategory(category: MatchCategory?) {
        if (selectedCategory == category) return
        selectedCategory = category
        fetch(reset = true)
    }

    fun setSort(s: MatchSort) {
        if (sort == s) return
        sort = s
        fetch(reset = true)
    }

    fun setDistance(d: DistanceFilter) {
        if (distance == d) return
        distance = d
        fetch(reset = true)
    }

    fun refresh() = fetch(reset = true, isRefresh = true)

    fun loadMore() {
        val s = _state.value as? MatchingUi.Success ?: return
        if (!s.hasMore || s.isRefreshing) return
        fetch(reset = false)
    }

    fun dismissNewCount() {
        newCount = 0
        recompute()
    }

    fun enterSearch() {
        if (isSearchMode) return
        isSearchMode = true
        searchQuery = ""
        searchSavedStatus = selectedStatus
        searchSavedCategory = selectedCategory
        searchSavedDistance = distance
        selectedStatus = null
        selectedCategory = null
        distance = DistanceFilter.ALL
        fetch(reset = true)
    }

    fun exitSearch() {
        if (!isSearchMode) return
        isSearchMode = false
        searchQuery = ""
        selectedStatus = searchSavedStatus
        selectedCategory = searchSavedCategory
        distance = searchSavedDistance
        fetch(reset = true)
    }

    fun updateSearch(query: String) {
        searchQuery = query
        recompute()
    }

    private fun fetch(reset: Boolean, isRefresh: Boolean = false) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (reset && !isRefresh) {
                val current = _state.value as? MatchingUi.Success
                if (current == null) {
                    _state.value = MatchingUi.Loading
                } else {
                    // Loading으로 전환하면 selectedStatus가 null이 돼
                    // 탭 애니메이션이 "전체"를 경유하는 버그가 생김.
                    // 대신 탭 선택만 즉시 반영하고 목록만 비운다.
                    raw = emptyList()
                    _state.value = current.copy(
                        items = emptyList(),
                        selectedStatus = selectedStatus,
                        selectedCategory = selectedCategory,
                        sort = sort,
                        distance = distance,
                        hasMore = false,
                    )
                }
            }
            if (isRefresh) recompute(refreshing = true)
            val location = getCurrentLocation()
            val lat = location?.first
            val lng = location?.second
            val targetPage = if (reset) 1 else (page + 1)

            // 백엔드 검증: lat/lng 한쪽만 보내면 422. 위치 없으면 nearest/max_distance 도 비활성.
            val safeSort = if (sort == MatchSort.NEAREST && location == null) MatchSort.IMMINENT else sort
            val safeMaxDistance = if (location == null) null else distance.meters

            val result = repository.getMatchList(
                status = selectedStatus,
                page = targetPage,
                size = 20,
                sort = safeSort.name.lowercase(),
                category = selectedCategory?.name,
                maxDistance = safeMaxDistance,
                lat = lat,
                lng = lng,
            )
            result.onSuccess { resp ->
                val newItems = resp.items.orEmpty()
                val incomingIds = newItems.mapNotNull { it.matchId }
                if (reset) {
                    newCount = if (lastIdSet.isNotEmpty()) diffNewMatchIds(lastIdSet, incomingIds) else 0
                    raw = newItems
                    page = 1
                    lastIdSet = incomingIds.toSet()
                } else {
                    raw = raw + newItems
                    page = targetPage
                    lastIdSet = (lastIdSet + incomingIds).toSet()
                }
                recompute(hasMore = newItems.size >= 20)
            }.onFailure { err ->
                _state.value = MatchingUi.Error(err.message ?: "매칭을 불러오지 못했어요")
            }
        }
    }

    private fun recompute(hasMore: Boolean? = null, refreshing: Boolean = false) {
        val current = _state.value as? MatchingUi.Success
        val counts = computeStatusTabCounts(raw)
        val showWarn = selectedCategory?.let {
            it.requiresVolunteerRole() && !isCurrentUserVolunteer()
        } ?: false
        val displayItems = if (isSearchMode && searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            raw.filter { item ->
                item.title?.lowercase()?.contains(q) == true ||
                item.authorNickname?.lowercase()?.contains(q) == true ||
                item.address?.lowercase()?.contains(q) == true
            }
        } else raw
        _state.value = MatchingUi.Success(
            items = displayItems,
            statusTabCounts = counts,
            selectedStatus = selectedStatus,
            selectedCategory = selectedCategory,
            sort = sort,
            distance = distance,
            hasMore = hasMore ?: current?.hasMore ?: true,
            isRefreshing = refreshing,
            newCount = newCount,
            showVolunteerWarning = showWarn,
            isSearchMode = isSearchMode,
            searchQuery = searchQuery,
        )
    }

    private fun computeStatusTabCounts(items: List<MatchListItem>): Map<String?, Int> {
        val byStatus = items.groupBy { it.status }
        return mapOf(
            null to items.size,
            "WAITING" to (byStatus["WAITING"]?.size ?: 0),
            "MATCHING" to (byStatus["MATCHING"]?.size ?: 0),
            "PROGRESS" to (byStatus["PROGRESS"]?.size ?: 0),
            "DONE" to (byStatus["DONE"]?.size ?: 0),
        )
    }

    class Factory(
        private val repository: MatchRepository,
        private val isCurrentUserVolunteer: () -> Boolean,
        private val getCurrentLocation: suspend () -> Pair<Double, Double>?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MatchingViewModel(repository, isCurrentUserVolunteer, getCurrentLocation) as T
    }
}
