package com.example.siheunggagae.ui.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.local.FavoritesCache
import com.example.siheunggagae.data.local.MapFilterStore
import com.example.siheunggagae.data.local.SiheungRegions
import com.example.siheunggagae.data.location.EffectiveCenter
import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.location.resolveEffectiveCenter
import com.example.siheunggagae.data.model.DongPriceBucket
import com.example.siheunggagae.data.model.FavoriteStoreCreateRequest
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.data.model.StoreCategory
import com.example.siheunggagae.data.model.StoreDetailResponse
import com.example.siheunggagae.data.model.StoreResponse
import com.example.siheunggagae.data.model.StoreViewportItem
import com.example.siheunggagae.data.model.UserRole
import com.example.siheunggagae.data.model.VolunteerMarkerDto
import com.example.siheunggagae.data.network.api.AuthApiService
import com.example.siheunggagae.map.DongAggregator
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
    val petHotels: List<PetHotelResponse> = emptyList(),
    val dongBuckets: List<DongPriceBucket> = emptyList(),
    /** GPS 가 시흥 밖이라 fallback 좌표를 쓰고 있는 상태. UI 배너 노출 트리거. */
    val centerFallback: EffectiveCenter? = null,
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

            // focus 좌표가 명시되면 그대로 사용(매장 상세에서 진입). 아니면 GPS-시흥 fallback resolver.
            val effective = if (focusLat != 0.0 && focusLng != 0.0) {
                null
            } else {
                resolveEffectiveCenter(api, locationProvider)
            }
            val centerLat = effective?.lat ?: focusLat
            val centerLng = effective?.lng ?: focusLng

            _uiState.update { it.copy(
                location = location,
                cameraTarget = centerLat to centerLng,
                cameraSerial = it.cameraSerial + 1,
                centerFallback = effective?.takeIf { it.isFallback },
            )}

            loadStores(centerLat, centerLng)

            if (_uiState.value.isVolunteerMode) {
                loadVolunteerMarkers()
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /** 사용자가 직접 동네를 선택해서 그 좌표로 카메라 이동. fallback 배너는 그대로 유지. */
    fun moveToDong(dong: String) {
        val (lat, lng) = SiheungRegions.coordinatesForDong(dong) ?: return
        _uiState.update { it.copy(
            cameraTarget = lat to lng,
            cameraSerial = it.cameraSerial + 1,
        )}
        viewModelScope.launch { loadStores(lat, lng, _uiState.value.selectedCategory) }
    }

    /** fallback 배너 닫기 — UI 에서만 숨김. 다음 진입 때 다시 평가. */
    fun dismissFallbackBanner() {
        _uiState.update { it.copy(centerFallback = null) }
    }

    fun selectCategory(category: StoreCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        val (lat, lng) = _uiState.value.cameraTarget ?: return
        viewModelScope.launch {
            loadStores(lat, lng, category)
        }
        val bbox = lastBbox
        val zoom = _uiState.value.currentZoom
        if (bbox != null && zoom >= 11) {
            enqueueViewportLoad(bbox[0], bbox[1], bbox[2], bbox[3])
        }
        if (category == StoreCategory.PET_HOTEL && _uiState.value.petHotels.isEmpty()) {
            viewModelScope.launch { loadPetHotels() }
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

    /**
     * 현재 카메라가 보고 있는 영역을 다시 로드.
     * lastBbox 중심 > cameraTarget > 초기 GPS 순으로 fallback.
     */
    fun refresh() {
        val bbox = lastBbox
        val (lat, lng) = when {
            bbox != null -> ((bbox[0] + bbox[2]) / 2.0) to ((bbox[1] + bbox[3]) / 2.0)
            else -> _uiState.value.cameraTarget
                ?: _uiState.value.location?.let { it.latitude to it.longitude }
                ?: return
        }
        viewModelScope.launch {
            loadStores(lat, lng, _uiState.value.selectedCategory)
        }
        if (bbox != null && _uiState.value.currentZoom >= 11) {
            enqueueViewportLoad(bbox[0], bbox[1], bbox[2], bbox[3])
        }
        if (_uiState.value.selectedCategory == StoreCategory.PET_HOTEL) {
            viewModelScope.launch { loadPetHotels() }
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
            val newStores = body?.stores ?: emptyList()
            val newTruncated = body?.truncated ?: false
            _uiState.update { current ->
                val sameStores = current.viewportStores.size == newStores.size &&
                    current.viewportStores.zip(newStores).all { (a, b) -> a.storeId == b.storeId }
                if (sameStores && current.truncated == newTruncated) current
                else current.copy(viewportStores = newStores, truncated = newTruncated)
            }
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
        // 카테고리별 별도 엔드포인트(getFilteredStores)는 lat/lng 를 받지 않아 위치를 무시하므로,
        // 항상 위치 기반 nearby 결과를 받아 클라이언트에서 카테고리로 필터링한다.
        val response = runCatching { api.getNearbyStores(lat, lng) }.getOrNull()
        val body = response?.body()
        val favoriteIds = FavoritesCache.ids.value
        val all = (body?.stores ?: emptyList()).map { s ->
            s.copy(isFavorited = s.resolvedId in favoriteIds)
        }
        val stores = if (category == StoreCategory.ALL || category.apiValue == null) {
            all
        } else {
            all.filter { it.category == category.apiValue }
        }
        val total = if (category == StoreCategory.ALL || category.apiValue == null) {
            body?.total ?: stores.size
        } else {
            stores.size
        }
        _uiState.update { it.copy(stores = stores, totalCount = total) }

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

    /**
     * 시흥시청 중심 15km 반경 펫호텔 1회 fetch + DongAggregator 로 동별 집계.
     * 실패 시 silent — 기존 캐시 유지 (없으면 emptyList 유지).
     */
    private suspend fun loadPetHotels() {
        val (cityLat, cityLng) = SiheungRegions.CITY_HALL
        val hotels = runCatching {
            api.getPetHotels(cityLat, cityLng, radius = 15_000).body()?.petHotels.orEmpty()
        }.getOrDefault(emptyList())
        if (hotels.isEmpty() && _uiState.value.petHotels.isNotEmpty()) {
            // 네트워크 실패 등으로 빈 응답 — 기존 캐시 유지
            return
        }
        val buckets = DongAggregator.aggregate(hotels, SiheungRegions.dongCoordinates)
        _uiState.update { it.copy(petHotels = hotels, dongBuckets = buckets) }
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
