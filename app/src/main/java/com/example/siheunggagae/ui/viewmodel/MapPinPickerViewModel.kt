package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PinPickerUi(
    val lat: Double,
    val lng: Double,
    val address: String? = null,
    val resolving: Boolean = false,
)

class MapPinPickerViewModel(
    initialLat: Double,
    initialLng: Double,
) : ViewModel() {
    private val api = RetrofitClient.api
    private var resolveJob: Job? = null

    private val _state = MutableStateFlow(PinPickerUi(lat = initialLat, lng = initialLng))
    val state: StateFlow<PinPickerUi> = _state

    init { scheduleResolve(initialLat, initialLng) }

    fun onCameraIdle(lat: Double, lng: Double) {
        if (_state.value.lat == lat && _state.value.lng == lng) return
        _state.value = _state.value.copy(lat = lat, lng = lng, address = null)
        scheduleResolve(lat, lng)
    }

    private fun scheduleResolve(lat: Double, lng: Double) {
        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            delay(200)
            _state.value = _state.value.copy(resolving = true)
            try {
                val resp = api.reverseGeocode(lat, lng)
                if (resp.isSuccessful) {
                    _state.value = _state.value.copy(
                        address = resp.body()?.formattedAddress,
                        resolving = false,
                    )
                } else {
                    _state.value = _state.value.copy(address = null, resolving = false)
                }
            } catch (_: Throwable) {
                _state.value = _state.value.copy(address = null, resolving = false)
            }
        }
    }

    class Factory(private val lat: Double, private val lng: Double) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MapPinPickerViewModel(lat, lng) as T
    }
}
