package com.example.siheunggagae.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.location.LocationProvider
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
                val dong = dashboard.regionDong
                    ?: runCatching { api.getMe().body()?.regionDong }.getOrNull()
                    ?: "정왕동"
                _uiState.update {
                    it.copy(
                        nickname = dashboard.nickname,
                        regionDong = dong,
                        userRole = dashboard.role,
                        walkScore = dashboard.walkScore,
                        weather = dashboard.weather ?: "",
                        temperatureCelsius = dashboard.temperatureCelsius,
                        airQuality = dashboard.airQuality ?: "",
                        pendingMatchCount = dashboard.pendingMatchCount,
                        nearestDDay = dashboard.nearestDDay,
                        nearbyStoreCount = dashboard.nearbyStoreCount,
                        allStores = dashboard.stores,
                        displayedStores = dashboard.stores,
                        news = dashboard.news,
                    )
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectCategory(category: StoreCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        if (category == StoreCategory.ALL || category.apiValue == null) {
            _uiState.update { it.copy(displayedStores = _uiState.value.allStores) }
            return
        }
        val location = _uiState.value.location
        if (location == null) {
            val filtered = _uiState.value.allStores.filter { it.category == category.apiValue }
            _uiState.update { it.copy(displayedStores = filtered) }
            return
        }
        viewModelScope.launch {
            val stores = runCatching {
                api.getFilteredStores(
                    category = category.apiValue,
                    lat = location.latitude,
                    lng = location.longitude,
                ).body()?.stores ?: emptyList()
            }.getOrDefault(emptyList())
            _uiState.update { it.copy(displayedStores = stores) }
        }
    }

    fun toggleFavorite(store: StoreResponse) {
        viewModelScope.launch {
            if (store.isFavorite) {
                runCatching { api.removeFavoriteStore(store.id) }
            } else {
                runCatching { api.addFavoriteStore(store.id) }
            }
            val toggle: (StoreResponse) -> StoreResponse = { s ->
                if (s.id == store.id) s.copy(isFavorite = !s.isFavorite) else s
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
