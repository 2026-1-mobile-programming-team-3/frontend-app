package com.example.siheunggagae.ui.viewmodel

import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.local.SiheungRegions
import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.model.FavoriteStoreCreateRequest
import com.example.siheunggagae.data.model.NewsItem
import com.example.siheunggagae.data.model.StoreCategory
import com.example.siheunggagae.data.model.StoreResponse
import com.example.siheunggagae.data.model.UserRole
import com.example.siheunggagae.data.model.UserUpdateRequest
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val regionDong: String = "정왕동",
    val userRole: UserRole = UserRole.USER,
    val walkScore: Int = 0,
    val weather: String = "",
    val temperatureCelsius: Int? = null,
    val airQuality: String = "",
    val pendingMatchCount: Int = 0,
    val nearestDDay: Int? = null,
    val volunteerMatchCount: Int = 0,
    val nearbyStoreCount: Int = 0,
    val allStores: List<StoreResponse> = emptyList(),
    val displayedStores: List<StoreResponse> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val selectedCategory: StoreCategory = StoreCategory.ALL,
    val location: Location? = null,
    val isLocationInSiheung: Boolean = false,
    val mapCenter: Pair<Double, Double>? = null,
)

class HomeViewModel(
    private val api: AuthApiService,
    private val locationProvider: LocationProvider,
    private val prefs: SharedPreferences,
) : ViewModel() {

    companion object {
        private const val KEY_MANUAL_DONG = "manual_dong"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val location = locationProvider.getLocationOrNull()
            _uiState.update { it.copy(location = location) }

            val dashboard = runCatching { api.getDashboard().body() }.getOrNull()

            // GPS 역지오코딩으로 현재 동 감지 (시흥시 내일 때만 사용)
            val gpsDong = if (location != null) {
                runCatching {
                    api.reverseGeocode(location.latitude, location.longitude).body()
                }.getOrNull()?.takeIf { it.isInSiheung }?.label
            } else null
            _uiState.update { it.copy(isLocationInSiheung = gpsDong != null) }

            // 우선순위: 수동 선택(로컬 저장) > GPS > 서버 저장값 > 기본값
            val manualDong = prefs.getString(KEY_MANUAL_DONG, null)

            // 수동 선택 동이 있으면 그 동의 중심 좌표, 없으면 GPS 좌표 사용
            val mapCenter = SiheungRegions.coordinatesForDong(manualDong)

            // dashboard 없이 GPS/수동만 성공한 경우에도 동 반영
            if (dashboard == null) {
                val dong = manualDong ?: gpsDong
                if (dong != null) _uiState.update { it.copy(regionDong = dong, mapCenter = mapCenter) }
            }

            if (dashboard != null) {
                val user = dashboard.user
                val serverDong = user?.regionDong
                    ?: runCatching { api.getMe().body()?.regionDong }.getOrNull()
                    ?: "정왕동"
                val dong = manualDong ?: gpsDong ?: serverDong
                val weatherStr = when (dashboard.weather?.condition?.uppercase()) {
                    "CLEAR" -> "맑음"
                    "CLOUDY" -> "흐림"
                    "RAIN" -> "비"
                    "SNOW" -> "눈"
                    else -> dashboard.weather?.condition ?: ""
                }
                val dustStr = when (dashboard.weather?.dustGrade?.uppercase()) {
                    "GOOD" -> "좋음"
                    "MODERATE" -> "보통"
                    "BAD" -> "나쁨"
                    "VERY_BAD" -> "매우나쁨"
                    else -> dashboard.weather?.dustGrade ?: ""
                }
                val authorEntry = dashboard.myMatchSummary?.asAuthor
                val applicantEntry = dashboard.myMatchSummary?.asApplicant

                val dDay = authorEntry?.desiredDate?.let { dateStr ->
                    runCatching {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val target = sdf.parse(dateStr) ?: return@runCatching null
                        val diff = (target.time - Date().time) / (1000 * 60 * 60 * 24)
                        diff.toInt().coerceAtLeast(0)
                    }.getOrNull()
                }
                _uiState.update {
                    it.copy(
                        nickname = user?.nickname ?: "",
                        regionDong = dong,
                        mapCenter = mapCenter,
                        userRole = user?.role ?: UserRole.USER,
                        walkScore = dashboard.walkScore ?: 0,
                        weather = weatherStr,
                        temperatureCelsius = dashboard.weather?.tempC?.toInt(),
                        airQuality = dustStr,
                        pendingMatchCount = authorEntry?.applicationsCount ?: 0,
                        nearestDDay = dDay,
                        volunteerMatchCount = if (applicantEntry != null) 1 else 0,
                        nearbyStoreCount = dashboard.nearbyStoreCount ?: 0,
                    )
                }
            }

            // GPS가 시흥 안일 때만 GPS 사용, 아니면 regionDong 좌표로 폴백
            val (lat, lng) = resolveStoresAnchor(location, gpsDong)
            val stores = runCatching {
                api.getNearbyStores(lat, lng).body()?.stores ?: emptyList()
            }.getOrDefault(emptyList())
            _uiState.update { it.copy(allStores = stores, displayedStores = stores) }

            val news = runCatching {
                api.getNews().body()?.news ?: emptyList()
            }.getOrDefault(emptyList())
            _uiState.update { it.copy(news = news) }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectCategory(category: StoreCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            // allStores 캐시가 비어있으면 위치 기반 nearby 한 번 받아온다.
            // getFilteredStores 는 lat/lng 를 받지 않아 위치 무시 결과를 주므로 사용하지 않는다.
            if (_uiState.value.allStores.isEmpty()) {
                val loc = _uiState.value.location
                val (lat, lng) = if (loc != null && _uiState.value.isLocationInSiheung) {
                    loc.latitude to loc.longitude
                } else {
                    SiheungRegions.coordinatesForDong(_uiState.value.regionDong)
                        ?: SiheungRegions.CITY_HALL
                }
                val stores = runCatching {
                    api.getNearbyStores(lat, lng).body()?.stores ?: emptyList()
                }.getOrDefault(emptyList())
                _uiState.update { it.copy(allStores = stores) }
            }
            val all = _uiState.value.allStores
            val filtered = if (category == StoreCategory.ALL || category.apiValue == null) {
                all
            } else {
                all.filter { it.category == category.apiValue }
            }
            _uiState.update { it.copy(displayedStores = filtered) }
        }
    }

    fun toggleFavorite(store: StoreResponse) {
        viewModelScope.launch {
            if (store.isFavorited) {
                runCatching { api.deleteFavoriteStore(store.storeId) }
            } else {
                runCatching { api.addFavoriteStore(FavoriteStoreCreateRequest(store.storeId)) }
            }
            val toggle: (StoreResponse) -> StoreResponse = { s ->
                if (s.storeId == store.storeId) s.copy(isFavorited = !s.isFavorited) else s
            }
            _uiState.update { state ->
                state.copy(
                    allStores = state.allStores.map(toggle),
                    displayedStores = state.displayedStores.map(toggle),
                )
            }
        }
    }

    /**
     * 매장 조회 기준 좌표 결정.
     * - GPS가 시흥 안(`gpsDong != null`)이면 GPS 좌표 사용
     * - 아니면 현재 regionDong(이미 manualDong → gpsDong → serverDong → 기본값 폴백됨) 좌표
     * - 알 수 없는 동이면 시청(CITY_HALL) 좌표
     */
    private fun resolveStoresAnchor(
        location: Location?,
        gpsDong: String?,
    ): Pair<Double, Double> {
        if (location != null && gpsDong != null) {
            return location.latitude to location.longitude
        }
        return SiheungRegions.coordinatesForDong(_uiState.value.regionDong)
            ?: SiheungRegions.CITY_HALL
    }

    fun resetToCurrentLocation() {
        prefs.edit().remove(KEY_MANUAL_DONG).apply()
        viewModelScope.launch {
            _uiState.update { it.copy(mapCenter = null) }
            loadDashboard()
        }
    }

    fun updateRegionDong(dong: String) {
        prefs.edit().putString(KEY_MANUAL_DONG, dong).apply()
        viewModelScope.launch {
            runCatching { api.updateMe(UserUpdateRequest(regionDong = dong)) }
            _uiState.update { it.copy(regionDong = dong) }
            loadDashboard()
        }
    }

    class Factory(
        private val api: AuthApiService,
        private val locationProvider: LocationProvider,
        private val prefs: SharedPreferences,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(api, locationProvider, prefs) as T
    }
}
