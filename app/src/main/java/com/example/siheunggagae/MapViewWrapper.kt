package com.example.siheunggagae

import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

class MapViewWrapper(private val mapView: MapView) {

    private var kakaoMap: KakaoMap? = null
    private val markers = mutableMapOf<String, com.kakao.vectormap.label.Label>()

    fun init(onReady: (KakaoMap) -> Unit) {
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}
            override fun onMapError(e: Exception) {
                e.printStackTrace()
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                onReady(map)
            }
        })
    }

    fun moveCamera(lat: Double, lng: Double, zoomLevel: Int = 15) {
        kakaoMap?.moveCamera(
            CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), zoomLevel)
        )
    }

    fun addMarker(id: String, lat: Double, lng: Double, onTap: (() -> Unit)? = null) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return
        val label = layer.addLabel(
            LabelOptions.from(LatLng.from(lat, lng))
                .setStyles(LabelStyles.from(LabelStyle.from()))
        )
        markers[id] = label
        onTap?.let { callback ->
            map.setOnLabelClickListener { _, _, clickedLabel ->
                if (clickedLabel == label) callback()
                true
            }
        }
    }

    fun removeMarker(id: String) {
        markers.remove(id)?.remove()
    }

    fun clearMarkers() {
        markers.values.forEach { it.remove() }
        markers.clear()
    }
}