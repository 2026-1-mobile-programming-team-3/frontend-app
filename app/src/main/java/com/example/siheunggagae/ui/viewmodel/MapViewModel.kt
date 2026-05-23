package com.example.siheunggagae.ui.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.local.FavoritesCache
import com.example.siheunggagae.data.local.MapFilterStore
import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.model.FavoriteStoreCreateRequest
import com.example.siheunggagae.data.model.StoreCategory
import com.example.siheunggagae.data.model.StoreDetailResponse
import com.example.siheunggagae.data.model.StoreResponse
import com.example.siheunggagae.data.model.StoreViewportItem
import com.example.siheunggagae.data.model.UserRole
import com.example.siheunggagae.data.model.VolunteerMarkerDto
import com.example.siheunggagae.data.network.api.AuthApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    /** [swLat, swLng, neLat, neLng] — 마지막 viewport. 펫호텔 비교 진입 시 동적 radius 계산에 사용. */
    val viewportBounds: DoubleArray? = null,
    val stores: List<StoreResponse> = emptyList(),
    val selectedStore: StoreResponse? = null,
    val selectedStoreDetail: StoreDetailResponse? = null,
    val isDetailLoading: Boolean = false,
    val selectedCategory: StoreCategory = StoreCategory.ALL,
    val isVolunteerMode: Boolean = false,
    val volunteerMarkers: List<VolunteerMarkerDto> = emptyList(),
    val userRole: UserRole = UserRole.USER,
    val totalCount: Int = 0,
    val visibleCategories: Set<String> = MapFilterStore.DEFAULT_CATEGORIES,
    val favoriteStoreIds: Set<Int> = emptySet(),
    // viewport (bbox) 기반 마커 전용
    val viewportStores: List<StoreViewportItem> = emptyList(),
    val currentZoom: Int = 15,
    val truncated: Boolean = false,
)

class MapViewModel(
    private val api: AuthApiService,
    private val locationProvider: LocationProvider,
    private val filterStore: MapFilterStore,
    initialVolunteerMode: Boolean = false,
    private val focusLat: Double = 0.0,
    private val focusLng: Double = 0.0,
    private val focusStoreId: Int = 0,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(isVolunteerMode = initialVolunteerMode))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // viewport 로드 debounce 용
    private var viewportJob: Job? = null
    private var lastBbox: DoubleArray? = null  // [swLat, swLng, neLat, neLng]

    init {
        loadInitial()
        viewModelScope.launch {
            // FavoritesCache 변경 시 stores/selectedStore 자동 동기화
            FavoritesCache.ids.collect { ids ->
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
        viewModelScope.launch {
            filterStore.visibleCategories.collect { cats ->
                _uiState.update { it.copy(visibleCategories = cats) }
            }
        }
        // 캐시 미로드 시에만 API 호출
        if (!FavoritesCache.isLoaded) {
            viewModelScope.launch {
                val ids = runCatching {
                    api.getFavoriteStores().body()?.items
                        ?.mapNotNull { it.storeId }?.toSet() ?: emptySet()
                }.getOrDefault(emptySet())
                FavoritesCache.init(ids)
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
                cameraTarget = if (focusLat != 0.0 && focusLng != 0.0) {
                    focusLat to focusLng
                } else {
                    location?.let { loc -> loc.latitude to loc.longitude }
                },
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
        val bbox = lastBbox
        val zoom = _uiState.value.currentZoom
        if (bbox != null && zoom >= 11) {
            enqueueViewportLoad(bbox[0], bbox[1], bbox[2], bbox[3])
        }
    }

    fun selectStore(store: StoreResponse?) {
        _uiState.update { it.copy(
            selectedStore = store,
            selectedStoreDetail = null,
            isDetailLoading = store != null,
        )}
        if (store != null) {
            viewModelScope.launch {
                val detail = runCatching {
                    api.getStoreDetail(store.resolvedId).body()
                }.getOrNull()
                _uiState.update { it.copy(selectedStoreDetail = detail, isDetailLoading = false) }
            }
        }
    }

    fun toggleFavorite(store: StoreResponse) {
        val storeId = store.resolvedId
        val newFavorited = !store.isFavorited
        // FavoritesCache 업데이트 → collect 블록이 자동으로 stores/selectedStore 반영
        if (newFavorited) FavoritesCache.add(storeId) else FavoritesCache.remove(storeId)
        viewModelScope.launch {
            val ok = runCatching {
                val resp = if (newFavorited) api.addFavoriteStore(FavoriteStoreCreateRequest(storeId))
                           else api.deleteFavoriteStore(storeId)
                Log.d("MapFavorite", "${if (newFavorited) "ADD" else "REMOVE"} storeId=$storeId → HTTP ${resp.code()} ${resp.message()}")
                resp.isSuccessful
                    || (newFavorited && resp.code() == 409)
                    || (!newFavorited && resp.code() == 404)
            }.getOrElse { e ->
                Log.e("MapFavorite", "exception storeId=$storeId", e)
                false
            }
            if (!ok) {
                // 실패 시 cache 원복 → collect 블록이 자동으로 UI 반영
                if (newFavorited) FavoritesCache.remove(storeId) else FavoritesCache.add(storeId)
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
        val bbox = lastBbox
        val zoom = _uiState.value.currentZoom
        if (bbox != null && zoom >= 11) {
            enqueueViewportLoad(bbox[0], bbox[1], bbox[2], bbox[3])
        }
    }

    fun onViewportChange(swLat: Double, swLng: Double, neLat: Double, neLng: Double, zoom: Int) {
        val bbox = doubleArrayOf(swLat, swLng, neLat, neLng)
        lastBbox = bbox
        _uiState.update { it.copy(currentZoom = zoom, viewportBounds = bbox) }
        if (zoom < 11) {
            viewportJob?.cancel()
            _uiState.update { it.copy(viewportStores = emptyList(), truncated = false) }
            return
        }
        enqueueViewportLoad(swLat, swLng, neLat, neLng, debounceMs = 300L)
    }

    private fun enqueueViewportLoad(
        swLat: Double, swLng: Double, neLat: Double, neLng: Double,
        debounceMs: Long = 0L,
    ) {
        viewportJob?.cancel()
        viewportJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)
            val category = _uiState.value.selectedCategory.apiValue
            val response = runCatching {
                api.getStoresByViewport(swLat, swLng, neLat, neLng, category)
            }.getOrNull()
            val body = response?.body()
            _uiState.update { it.copy(
                viewportStores = body?.stores ?: emptyList(),
                truncated = body?.truncated ?: false,
            )}
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
        val favoriteIds = FavoritesCache.ids.value
        val stores = (body?.stores ?: emptyList()).map { s ->
            s.copy(isFavorited = s.resolvedId in favoriteIds)
        }
        _uiState.update { it.copy(stores = stores, totalCount = body?.total ?: 0) }

        // 진입 시 특정 매장을 바로 선택해 바텀시트를 열어야 하는 경우
        if (focusStoreId != 0 && _uiState.value.selectedStore == null) {
            stores.find { it.resolvedId == focusStoreId }?.let { selectStore(it) }
        }
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
        private val focusLat: Double = 0.0,
        private val focusLng: Double = 0.0,
        private val focusStoreId: Int = 0,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MapViewModel(api, locationProvider, filterStore, initialVolunteerMode, focusLat, focusLng, focusStoreId) as T
    }
}
