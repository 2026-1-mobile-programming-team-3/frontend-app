package com.example.siheunggagae

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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

    // 지도가 파괴됐을 때 composable에서 감지할 수 있도록 노출
    var onMapDestroyed: (() -> Unit)? = null

    // reinit() 지원을 위해 마지막 콜백 보관
    private var lastOnReady: ((KakaoMap) -> Unit)? = null
    val hasBeenInitialized: Boolean get() = lastOnReady != null

    fun init(onReady: (KakaoMap) -> Unit) {
        lastOnReady = onReady
        startMap()
    }

    /** 지도 세션이 무효화된 후 재초기화 */
    fun reinit() = startMap()

    private fun startMap() {
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                kakaoMap = null          // stale 참조 제거
                onMapDestroyed?.invoke() // composable에 알림
            }
            override fun onMapError(e: Exception) { e.printStackTrace() }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                kakaoMap = map
                map.labelManager?.layer?.setClickable(true)
                map.setOnLabelClickListener { _, _, clickedLabel ->
                    val clickedId = clickedLabel.tag as? String
                    clickedId?.let { markerCallbacks[it]?.invoke() }
                    true
                }
                lastOnReady?.invoke(map)
            }
        })
    }

    fun resume() = mapView.resume()
    fun pause() = mapView.pause()

    /** 현재 화면에 보이는 SW/NE 좌표 쌍 반환. 지도 미준비 or 세션 무효 시 null. */
    fun getVisibleBounds(): Pair<LatLng, LatLng>? {
        val map = kakaoMap ?: return null
        return runCatching {
            val sw = map.fromScreenPoint(0, mapView.height) ?: return null
            val ne = map.fromScreenPoint(mapView.width, 0) ?: return null
            sw to ne
        }.getOrNull()
    }

    fun getCurrentZoom(): Int =
        runCatching { kakaoMap?.getCameraPosition()?.zoomLevel }.getOrNull() ?: 15

    fun moveCamera(lat: Double, lng: Double, zoomLevel: Int = 15) {
        runCatching {
            kakaoMap?.moveCamera(
                CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), zoomLevel)
            )
        }
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

    fun addClusterMarker(
        id: String,
        lat: Double,
        lng: Double,
        count: Int,
        onTap: (() -> Unit)? = null,
    ) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return
        val bitmap = createClusterBitmap(count)
        val style = LabelStyles.from(LabelStyle.from(bitmap))
        val label = layer.addLabel(LabelOptions.from(LatLng.from(lat, lng)).setStyles(style))
        label.tag = id
        label.setClickable(onTap != null)
        markers[id] = label
        if (onTap != null) markerCallbacks[id] = onTap
    }

    private fun createClusterBitmap(count: Int, sizePx: Int = 56): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF614B3A.toInt() }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = sizePx * 0.35f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, bgPaint)
        val text = if (count > 99) "99+" else count.toString()
        val textY = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, sizePx / 2f, textY, textPaint)
        return bitmap
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
