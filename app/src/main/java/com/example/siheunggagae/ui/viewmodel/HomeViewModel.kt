package com.example.siheunggagae.ui.viewmodel

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
)

class HomeViewModel(
    private val api: AuthApiService,
    private val locationProvider: LocationProvider,
) : ViewModel() {

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
            if (dashboard != null) {
                val user = dashboard.user
                val dong = user?.regionDong
                    ?: runCatching { api.getMe().body()?.regionDong }.getOrNull()
                    ?: "정왕동"
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
                runCatching { api.removeFavoriteStore(store.storeId) }
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

    fun updateRegionDong(dong: String) {
        viewModelScope.launch {
            runCatching { api.updateMe(UserUpdateRequest(regionDong = dong)) }
            _uiState.update { it.copy(regionDong = dong) }
            loadDashboard()
        }
    }

    class Factory(
        private val api: AuthApiService,
        private val locationProvider: LocationProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(api, locationProvider) as T
    }
}
