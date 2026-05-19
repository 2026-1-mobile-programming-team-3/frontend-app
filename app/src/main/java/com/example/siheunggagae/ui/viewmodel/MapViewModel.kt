package com.example.siheunggagae.ui.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.local.MapFilterStore
import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.model.FavoriteStoreCreateRequest
import com.example.siheunggagae.data.model.StoreCategory
import com.example.siheunggagae.data.model.StoreResponse
import com.example.siheunggagae.data.model.UserRole
import com.example.siheunggagae.data.model.VolunteerMarkerDto
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val isLoading: Boolean = false,
    val location: Location? = null,
    val cameraTarget: Pair<Double, Double>? = null,
    val cameraSerial: Int = 0,
    val stores: List<StoreResponse> = emptyList(),
    val selectedStore: StoreResponse? = null,
    val selectedCategory: StoreCategory = StoreCategory.ALL,
    val isVolunteerMode: Boolean = false,
    val volunteerMarkers: List<VolunteerMarkerDto> = emptyList(),
    val userRole: UserRole = UserRole.USER,
    val totalCount: Int = 0,
    val visibleCategories: Set<String> = MapFilterStore.DEFAULT_CATEGORIES,
    val favoriteStoreIds: Set<Int> = emptySet(),
)

class MapViewModel(
    private val api: AuthApiService,
    private val locationProvider: LocationProvider,
    private val filterStore: MapFilterStore,
    initialVolunteerMode: Boolean = false,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(isVolunteerMode = initialVolunteerMode))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadInitial()
        loadFavoriteIds()
        viewModelScope.launch {
            filterStore.visibleCategories.collect { cats ->
                _uiState.update { it.copy(visibleCategories = cats) }
            }
        }
    }

    private fun loadFavoriteIds() {
        viewModelScope.launch {
            val ids = runCatching {
                api.getFavoriteStores().body()?.items
                    ?.mapNotNull { it.storeId }?.toSet() ?: emptySet()
            }.getOrDefault(emptySet())
            _uiState.update { state ->
                state.copy(
                    favoriteStoreIds = ids,
                    stores = state.stores.map { it.copy(isFavorited = it.resolvedId in ids) },
                    selectedStore = state.selectedStore?.let {
                        it.copy(isFavorited = it.resolvedId in ids)
                    },
                )
            }
        }
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userRole = runCatching {
                api.getMe().body()?.role ?: UserRole.USER
            }.getOrDefault(UserRole.USER)
            _uiState.update { it.copy(userRole = userRole) }

            val location = locationProvider.getLocationOrNull()
            _uiState.update { it.copy(
                location = location,
                cameraTarget = location?.let { loc -> loc.latitude to loc.longitude },
                cameraSerial = it.cameraSerial + 1,
            )}

            if (location != null) {
                loadStores(location.latitude, location.longitude)
            }

            if (_uiState.value.isVolunteerMode) {
                loadVolunteerMarkers()
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectCategory(category: StoreCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        val location = _uiState.value.location ?: return
        viewModelScope.launch {
            loadStores(location.latitude, location.longitude, category)
        }
    }

    fun selectStore(store: StoreResponse?) {
        _uiState.update { it.copy(selectedStore = store) }
    }

    fun toggleFavorite(store: StoreResponse) {
        val storeId = store.resolvedId
        val newFavorited = !store.isFavorited
        val update: (List<StoreResponse>) -> List<StoreResponse> = { list ->
            list.map { if (it.resolvedId == storeId) it.copy(isFavorited = newFavorited) else it }
        }
        _uiState.update { it.copy(
            stores = update(it.stores),
            selectedStore = it.selectedStore?.let { s ->
                if (s.resolvedId == storeId) s.copy(isFavorited = newFavorited) else s
            },
            favoriteStoreIds = if (newFavorited) it.favoriteStoreIds + storeId
                               else it.favoriteStoreIds - storeId,
        )}
        viewModelScope.launch {
            val ok = runCatching {
                val resp = if (newFavorited) api.addFavoriteStore(FavoriteStoreCreateRequest(storeId))
                           else api.deleteFavoriteStore(storeId)
                Log.d("MapFavorite", "${if (newFavorited) "ADD" else "REMOVE"} storeId=$storeId → HTTP ${resp.code()} ${resp.message()}")
                // 409: 이미 즐겨찾기됨 → ADD 목표 달성으로 간주
                // 404: 즐겨찾기 없음  → REMOVE 목표 달성으로 간주
                resp.isSuccessful
                    || (newFavorited && resp.code() == 409)
                    || (!newFavorited && resp.code() == 404)
            }.getOrElse { e ->
                Log.e("MapFavorite", "exception storeId=$storeId", e)
                false
            }
            if (!ok) {
                // 실패 시 원복
                val revert: (List<StoreResponse>) -> List<StoreResponse> = { list ->
                    list.map { if (it.resolvedId == storeId) it.copy(isFavorited = !newFavorited) else it }
                }
                _uiState.update { it.copy(
                    stores = revert(it.stores),
                    selectedStore = it.selectedStore?.let { s ->
                        if (s.resolvedId == storeId) s.copy(isFavorited = !newFavorited) else s
                    },
                    favoriteStoreIds = if (!newFavorited) it.favoriteStoreIds + storeId
                                       else it.favoriteStoreIds - storeId,
                )}
            }
        }
    }

    fun moveToCurrentLocation() {
        viewModelScope.launch {
            val location = locationProvider.getLocationOrNull() ?: return@launch
            _uiState.update { it.copy(
                location = location,
                cameraTarget = location.latitude to location.longitude,
                cameraSerial = it.cameraSerial + 1,
            )}
        }
    }

    fun refresh(lat: Double, lng: Double) {
        viewModelScope.launch {
            loadStores(lat, lng, _uiState.value.selectedCategory)
        }
    }

    fun applyFilter(visibleApiCategories: Set<String>) {
        viewModelScope.launch { filterStore.saveCategories(visibleApiCategories) }
    }

    fun toggleVolunteerMode() {
        val newMode = !_uiState.value.isVolunteerMode
        _uiState.update { it.copy(isVolunteerMode = newMode) }
        if (newMode && _uiState.value.volunteerMarkers.isEmpty()) {
            viewModelScope.launch { loadVolunteerMarkers() }
        }
    }

    private suspend fun loadStores(
        lat: Double,
        lng: Double,
        category: StoreCategory = _uiState.value.selectedCategory,
    ) {
        val response = runCatching {
            if (category == StoreCategory.ALL || category.apiValue == null) {
                api.getNearbyStores(lat, lng)
            } else {
                api.getFilteredStores(category = category.apiValue)
            }
        }.getOrNull()
        val body = response?.body()
        val favoriteIds = _uiState.value.favoriteStoreIds
        _uiState.update { it.copy(
            stores = (body?.stores ?: emptyList()).map { s ->
                s.copy(isFavorited = s.resolvedId in favoriteIds)
            },
            totalCount = body?.total ?: 0,
        )}
    }

    private suspend fun loadVolunteerMarkers() {
        val markers = runCatching {
            api.getVolunteerMarkers().body() ?: emptyList()
        }.getOrDefault(emptyList())
        _uiState.update { it.copy(volunteerMarkers = markers) }
    }

    class Factory(
        private val api: AuthApiService,
        private val locationProvider: LocationProvider,
        private val filterStore: MapFilterStore,
        private val initialVolunteerMode: Boolean = false,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MapViewModel(api, locationProvider, filterStore, initialVolunteerMode) as T
    }
}
