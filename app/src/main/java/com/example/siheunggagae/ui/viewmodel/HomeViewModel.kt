package com.example.siheunggagae.ui.viewmodel

import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    val nearbyStoreCount: Int = 0,
    val allStores: List<StoreResponse> = emptyList(),
    val displayedStores: List<StoreResponse> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val selectedCategory: StoreCategory = StoreCategory.ALL,
    val location: Location? = null,
    val mapCenter: Pair<Double, Double>? = null,
)

class HomeViewModel(
    private val api: AuthApiService,
    private val locationProvider: LocationProvider,
    private val prefs: SharedPreferences,
) : ViewModel() {

    companion object {
        private const val KEY_MANUAL_DONG = "manual_dong"

        val dongCoordinates: Map<String, Pair<Double, Double>> = mapOf(
            "대야동"  to (37.3880 to 126.8030),
            "신천동"  to (37.3730 to 126.7975),
            "신현동"  to (37.3810 to 126.8110),
            "은행동"  to (37.3740 to 126.8130),
            "매화동"  to (37.3615 to 126.8025),
            "도창동"  to (37.3555 to 126.7800),
            "목감동"  to (37.3485 to 126.7890),
            "조남동"  to (37.3335 to 126.7935),
            "포동"    to (37.3665 to 126.7615),
            "군자동"  to (37.3790 to 126.7615),
            "정왕동"  to (37.3400 to 126.7435),
            "능곡동"  to (37.4005 to 126.7940),
            "월곶동"  to (37.4130 to 126.7835),
            "배곧동"  to (37.3600 to 126.7215),
            "장현동"  to (37.3940 to 126.8200),
            "장곡동"  to (37.3765 to 126.8200),
            "연성동"  to (37.3655 to 126.8200),
            "과림동"  to (37.3430 to 126.7645),
        )
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
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

            // 우선순위: 수동 선택(로컬 저장) > GPS > 서버 저장값 > 기본값
            val manualDong = prefs.getString(KEY_MANUAL_DONG, null)

            // 수동 선택 동이 있으면 그 동의 중심 좌표, 없으면 GPS 좌표 사용
            val mapCenter = dongCoordinates[manualDong]

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
                        nearbyStoreCount = dashboard.nearbyStoreCount ?: 0,
                    )
                }
            }

            val lat = location?.latitude ?: 37.3795
            val lng = location?.longitude ?: 126.8025
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
            if (category == StoreCategory.ALL || category.apiValue == null) {
                if (_uiState.value.allStores.isNotEmpty()) {
                    _uiState.update { it.copy(displayedStores = _uiState.value.allStores) }
                } else {
                    val lat = _uiState.value.location?.latitude ?: 37.3795
                    val lng = _uiState.value.location?.longitude ?: 126.8025
                    val stores = runCatching {
                        api.getNearbyStores(lat, lng).body()?.stores ?: emptyList()
                    }.getOrDefault(emptyList())
                    _uiState.update { it.copy(allStores = stores, displayedStores = stores) }
                }
                return@launch
            }
            val stores = runCatching {
                api.getFilteredStores(category = category.apiValue).body()?.stores ?: emptyList()
            }.getOrDefault(emptyList())
            _uiState.update { it.copy(displayedStores = stores) }
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
