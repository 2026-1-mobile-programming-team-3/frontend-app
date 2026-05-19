package com.example.siheunggagae

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles

class MapViewWrapper(private val mapView: MapView) {

    private var kakaoMap: KakaoMap? = null
    private val markers = mutableMapOf<String, Label>()
    private val markerCallbacks = mutableMapOf<String, () -> Unit>()

    fun init(onReady: (KakaoMap) -> Unit) {
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {}
            override fun onMapError(e: Exception) { e.printStackTrace() }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                // 기본 레이어 클릭 활성화 (레이어 + 라벨 둘 다 필요)
                map.labelManager?.layer?.setClickable(true)
                map.setOnLabelClickListener { _, _, clickedLabel ->
                    val clickedId = clickedLabel.tag as? String
                    clickedId?.let { markerCallbacks[it]?.invoke() }
                    true
                }
                onReady(map)
            }
        })
    }

    fun resume() = mapView.resume()
    fun pause() = mapView.pause()

    fun moveCamera(lat: Double, lng: Double, zoomLevel: Int = 15) {
        kakaoMap?.moveCamera(
            CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), zoomLevel)
        )
    }

    fun addMarker(
        id: String,
        lat: Double,
        lng: Double,
        @ColorInt markerColor: Int? = null,
        onTap: (() -> Unit)? = null,
    ) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return

        val style = if (markerColor != null) {
            LabelStyles.from(LabelStyle.from(createCircleBitmap(markerColor)))
        } else {
            LabelStyles.from(LabelStyle.from())
        }

        val label = layer.addLabel(
            LabelOptions.from(LatLng.from(lat, lng)).setStyles(style)
        )
        label.tag = id
        label.setClickable(true)
        markers[id] = label
        if (onTap != null) markerCallbacks[id] = onTap
    }

    fun removeMarker(id: String) {
        markers.remove(id)?.remove()
        markerCallbacks.remove(id)
    }

    fun clearMarkers() {
        markers.values.forEach { it.remove() }
        markers.clear()
        markerCallbacks.clear()
    }

    fun clearMarkersWithPrefix(prefix: String) {
        val toRemove = markers.keys.filter { it.startsWith(prefix) }
        toRemove.forEach { id ->
            markers.remove(id)?.remove()
            markerCallbacks.remove(id)
        }
    }

    private fun createCircleBitmap(@ColorInt color: Int, sizePx: Int = 48): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        val radius = sizePx / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        return bitmap
    }
}
