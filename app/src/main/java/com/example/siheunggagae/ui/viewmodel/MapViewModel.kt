package com.example.siheunggagae.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.location.LocationProvider
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
    val stores: List<StoreResponse> = emptyList(),
    val selectedStore: StoreResponse? = null,
    val selectedCategory: StoreCategory = StoreCategory.ALL,
    val isVolunteerMode: Boolean = false,
    val volunteerMarkers: List<VolunteerMarkerDto> = emptyList(),
    val userRole: UserRole = UserRole.USER,
    val totalCount: Int = 0,
)

class MapViewModel(
    private val api: AuthApiService,
    private val locationProvider: LocationProvider,
    initialVolunteerMode: Boolean = false,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(isVolunteerMode = initialVolunteerMode))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userRole = runCatching {
                api.getMe().body()?.role ?: UserRole.USER
            }.getOrDefault(UserRole.USER)
            _uiState.update { it.copy(userRole = userRole) }

            val location = locationProvider.getLocationOrNull()
            _uiState.update { it.copy(location = location) }

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

    fun moveToCurrentLocation(onMoved: (lat: Double, lng: Double) -> Unit) {
        viewModelScope.launch {
            val location = locationProvider.getLocationOrNull() ?: return@launch
            _uiState.update { it.copy(location = location) }
            onMoved(location.latitude, location.longitude)
        }
    }

    fun refresh(lat: Double, lng: Double) {
        viewModelScope.launch {
            loadStores(lat, lng, _uiState.value.selectedCategory)
        }
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
        _uiState.update { it.copy(
            stores = body?.stores ?: emptyList(),
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
        private val initialVolunteerMode: Boolean = false,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MapViewModel(api, locationProvider, initialVolunteerMode) as T
    }
}
