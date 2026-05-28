package com.example.siheunggagae

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.example.siheunggagae.map.MarkerProjector
import com.example.siheunggagae.map.MarkerSpec
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
    private val markerVisualKeys = mutableMapOf<String, Any>()
    private val managedIdsByPrefix = mutableMapOf<String, MutableSet<String>>()
    private val bitmapCache = androidx.collection.LruCache<BitmapKey, Bitmap>(200)

    sealed interface BitmapKey {
        data class Single(
            val category: String,
            val name: String,
            val color: Int,
            val selected: Boolean,
            val radius: Float = 22f,
        ) : BitmapKey
        data class Cluster(
            val topCategories: List<String>,
            val count: Int,
        ) : BitmapKey
        data object MyLocation : BitmapKey
        data class DongBubble(
            val dongName: String,
            val count: Int,
            val minKrw: Int,
            val maxKrw: Int,
        ) : BitmapKey
    }

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
        category: String? = null,
        name: String? = null,
        radius: Float = 22f,
        onTap: (() -> Unit)? = null,
    ) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return

        val style = if (markerColor != null) {
            val key = BitmapKey.Single(category ?: "", name ?: "", markerColor, selected = false, radius = radius)
            val bmp = bitmapCache.get(key) ?: createPinBitmap(markerColor, category, name, radius).also {
                bitmapCache.put(key, it)
            }
            LabelStyles.from(LabelStyle.from(bmp))
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

    /**
     * Spec 리스트와 현재 마커 상태를 diff 하여 변경만 적용.
     * `prefix` 그룹 단위로 "이전에 sync 한 ID 집합" 을 추적해, 다음 호출 시 desired 에 없는 것만 제거.
     * 다른 prefix 의 마커·내 위치는 건드리지 않음.
     */
    fun syncMarkers(prefix: String, specs: List<MarkerSpec>) {
        val desired = specs.associateBy { it.id }
        val previouslyManaged = managedIdsByPrefix.getOrPut(prefix) { mutableSetOf() }

        // 1. 제거: 이전엔 관리했지만 이번 desired 에 없는 것
        (previouslyManaged - desired.keys).forEach { id ->
            markers.remove(id)?.remove()
            markerCallbacks.remove(id)
            markerVisualKeys.remove(id)
        }

        // 2. 추가·업데이트
        desired.forEach { (id, spec) ->
            val cachedKey = markerVisualKeys[id]
            if (cachedKey == spec.visualKey) {
                // 비주얼 동일 — onTap 만 갱신 (람다 identity 변화 흡수)
                val newTap = spec.onTapOrNull()
                if (newTap != null) markerCallbacks[id] = newTap else markerCallbacks.remove(id)
                return@forEach
            }
            markers.remove(id)?.remove()
            addSpecInternal(spec)
            markerVisualKeys[id] = spec.visualKey
        }

        // 3. managed set 갱신
        previouslyManaged.clear()
        previouslyManaged.addAll(desired.keys)
    }

    private fun MarkerSpec.onTapOrNull(): (() -> Unit)? = when (this) {
        is MarkerSpec.Single     -> onTap
        is MarkerSpec.Cluster    -> onTap
        is MarkerSpec.DongBubble -> onTap
    }

    private fun addSpecInternal(spec: MarkerSpec) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return
        when (spec) {
            is MarkerSpec.Single -> {
                val key = BitmapKey.Single(spec.category, spec.name, spec.color, spec.isSelected)
                val bmp = bitmapCache.get(key)
                    ?: createSingleBitmap(spec.color, spec.category, spec.name, spec.isSelected)
                        .also { bitmapCache.put(key, it) }
                val style = LabelStyles.from(LabelStyle.from(bmp))
                val label = layer.addLabel(
                    LabelOptions.from(LatLng.from(spec.lat, spec.lng)).setStyles(style)
                )
                label.tag = spec.id
                label.setClickable(spec.onTap != null)
                markers[spec.id] = label
                spec.onTap?.let { markerCallbacks[spec.id] = it }
            }
            is MarkerSpec.Cluster -> {
                val key = BitmapKey.Cluster(spec.topCategories, spec.count)
                val bmp = bitmapCache.get(key)
                    ?: createIconStackClusterBitmap(spec.topCategories, spec.count)
                        .also { bitmapCache.put(key, it) }
                val style = LabelStyles.from(LabelStyle.from(bmp))
                val label = layer.addLabel(
                    LabelOptions.from(LatLng.from(spec.lat, spec.lng)).setStyles(style)
                )
                label.tag = spec.id
                label.setClickable(spec.onTap != null)
                markers[spec.id] = label
                spec.onTap?.let { markerCallbacks[spec.id] = it }
            }
            is MarkerSpec.DongBubble -> {
                val key = BitmapKey.DongBubble(spec.dongName, spec.count, spec.minKrw, spec.maxKrw)
                val bmp = bitmapCache.get(key)
                    ?: createDongBubbleBitmap(spec.dongName, spec.count, spec.minKrw, spec.maxKrw)
                        .also { bitmapCache.put(key, it) }
                val style = LabelStyles.from(LabelStyle.from(bmp))
                val label = layer.addLabel(
                    LabelOptions.from(LatLng.from(spec.lat, spec.lng)).setStyles(style)
                )
                label.tag = spec.id
                label.setClickable(spec.onTap != null)
                markers[spec.id] = label
                spec.onTap?.let { markerCallbacks[spec.id] = it }
            }
        }
    }

    /** Single 마커 — selection 상태에 따라 halo + name chip 분기. 기존 createPinBitmap 의 래퍼. */
    private fun createSingleBitmap(
        color: Int, category: String?, name: String?, selected: Boolean,
    ): Bitmap = if (selected) createSelectedPinBitmap(color, category, name)
                else createPinBitmap(color, category, name)

    fun clearMarkers() {
        markers.values.forEach { it.remove() }
        markers.clear()
        markerCallbacks.clear()
        markerVisualKeys.clear()
        managedIdsByPrefix.clear()
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
        val key = BitmapKey.Cluster(topCategories = emptyList(), count = count)
        val bitmap = bitmapCache.get(key) ?: createClusterBitmap(count).also { bitmapCache.put(key, it) }
        val style = LabelStyles.from(LabelStyle.from(bitmap))
        val label = layer.addLabel(LabelOptions.from(LatLng.from(lat, lng)).setStyles(style))
        label.tag = id
        label.setClickable(onTap != null)
        markers[id] = label
        if (onTap != null) markerCallbacks[id] = onTap
    }

    /**
     * 아이콘 스택 클러스터:
     * - 상위 3 카테고리 핀 (직경 26dp) 좌→우로 겹쳐 쌓기
     * - 우하단 다크 +N 뱃지 (count > topCategories.size 일 때만)
     * - 핀 개수가 1~3 이면 그만큼만, 0 이면 fallback to 기존 단색 원
     */
    private fun createIconStackClusterBitmap(topCategories: List<String>, count: Int): Bitmap {
        if (topCategories.isEmpty()) return createClusterBitmap(count)

        val pinR = 13f                 // 직경 26
        val pinBorder = 2f
        val pinOffsetX = 14f           // 좌우 겹침
        val pinOffsetY = 4f
        val pins = topCategories.take(3)

        val stackW = (pinR + pinBorder) * 2 + pinOffsetX * (pins.size - 1)
        val stackH = (pinR + pinBorder) * 2 + pinOffsetY * (pins.size - 1)

        val badgeText = if (count > pins.size) "+${count - pins.size}" else ""
        val badgePadH = 5f
        val badgePadV = 2f
        val badgePaintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            this.color = android.graphics.Color.WHITE
        }
        val badgeTextW = if (badgeText.isNotEmpty()) badgePaintText.measureText(badgeText) else 0f
        val badgeW = if (badgeText.isNotEmpty()) badgeTextW + badgePadH * 2 else 0f
        val badgeH = if (badgeText.isNotEmpty())
            (badgePaintText.descent() - badgePaintText.ascent()) + badgePadV * 2
        else 0f

        // 캔버스: 핀 스택 + 우하단 badge overflow 여유
        val padding = 4f
        val totalW = (stackW + badgeW * 0.6f + padding * 2).coerceAtLeast(stackW + padding * 2)
        val totalH = (stackH + badgeH * 0.7f + padding * 2).coerceAtLeast(stackH + padding * 2)
        val bitmap = Bitmap.createBitmap(totalW.toInt(), totalH.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        pins.forEachIndexed { idx, cat ->
            val color = when (cat.uppercase()) {
                "CAFE"       -> 0xFF8A6E58.toInt()
                "PARK"       -> 0xFF4CAF50.toInt()
                "HOSPITAL"   -> 0xFFF04268.toInt()
                "GROOMING"   -> 0xFF9C27B0.toInt()
                "RESTAURANT" -> 0xFFF7A35B.toInt()
                "PET_HOTEL"  -> 0xFF614B3A.toInt()
                else         -> 0xFF614B3A.toInt()
            }
            val cx = padding + pinR + pinBorder + idx * pinOffsetX
            val cy = padding + pinR + pinBorder + idx * pinOffsetY
            // 흰 테두리
            canvas.drawCircle(cx, cy, pinR + pinBorder, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = android.graphics.Color.WHITE
            })
            // 카테고리 컬러
            canvas.drawCircle(cx, cy, pinR, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
            })
            // 이모지
            val icon = when (cat.uppercase()) {
                "CAFE"       -> "☕"
                "PARK"       -> "🌳"
                "HOSPITAL"   -> "🏥"
                "GROOMING"   -> "✂"
                "RESTAURANT" -> "🍽"
                "PET_HOTEL"  -> "🏨"
                else         -> "★"
            }
            val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = pinR * 1.1f
                textAlign = Paint.Align.CENTER
                this.color = android.graphics.Color.WHITE
            }
            canvas.drawText(icon, cx, cy + pinR * 0.32f, iconPaint)
        }

        // 우하단 +N 다크 뱃지
        if (badgeText.isNotEmpty()) {
            val lastCx = padding + pinR + pinBorder + (pins.size - 1) * pinOffsetX
            val lastCy = padding + pinR + pinBorder + (pins.size - 1) * pinOffsetY
            val badgeLeft = lastCx + pinR - badgeW * 0.4f
            val badgeTop = lastCy + pinR - badgeH * 0.3f
            val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH)
            // 흰 테두리(외곽)
            canvas.drawRoundRect(
                RectF(badgeLeft - 1.5f, badgeTop - 1.5f, badgeRect.right + 1.5f, badgeRect.bottom + 1.5f),
                badgeH / 2f, badgeH / 2f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE },
            )
            // 다크 본체
            canvas.drawRoundRect(badgeRect, badgeH / 2f, badgeH / 2f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = 0xFF1A1A1A.toInt() })
            // 텍스트
            val tx = (badgeLeft + badgeRect.right) / 2f
            val ty = badgeTop + badgePadV - badgePaintText.ascent()
            canvas.drawText(badgeText, tx, ty, badgePaintText)
        }
        return bitmap
    }

    private fun createClusterBitmap(count: Int, sizePx: Int = 56): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = sizePx / 2f
        canvas.drawCircle(cx, cx, cx, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        })
        canvas.drawCircle(cx, cx, cx - 3.5f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF614B3A.toInt()
        })
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = sizePx * 0.32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val text = if (count > 99) "99+" else count.toString()
        canvas.drawText(text, cx, cx - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
        return bitmap
    }

    fun clearMarkersWithPrefix(prefix: String) {
        val toRemove = markers.keys.filter { it.startsWith(prefix) }
        toRemove.forEach { id ->
            markers.remove(id)?.remove()
            markerCallbacks.remove(id)
            markerVisualKeys.remove(id)
        }
        // syncMarkers tracking 도 함께 정리 (prefix 일부 일치)
        managedIdsByPrefix.values.forEach { set ->
            set.removeAll(toRemove.toSet())
        }
    }

    /**
     * 내 위치 파란 점 — Kakao SDK v2는 빌트인 my-location 이 없어 Label 기반으로 직접 그린다.
     * 같은 ID 로 반복 호출하면 이전 라벨 제거 후 재생성.
     */
    fun updateMyLocation(lat: Double, lng: Double) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return
        val existing = markers[MY_LOCATION_ID]
        if (existing != null) {
            val moved = runCatching { existing.moveTo(LatLng.from(lat, lng)) }.isSuccess
            if (moved) return
            // moveTo 실패(세션 무효 등) → stale label 제거 후 fall-through 해 재생성
            markers.remove(MY_LOCATION_ID)?.remove()
        }
        val bmp = bitmapCache.get(BitmapKey.MyLocation) ?: createMyLocationBitmap().also {
            bitmapCache.put(BitmapKey.MyLocation, it)
        }
        val style = LabelStyles.from(LabelStyle.from(bmp))
        val label = layer.addLabel(LabelOptions.from(LatLng.from(lat, lng)).setStyles(style))
        label.tag = MY_LOCATION_ID
        label.setClickable(false)
        markers[MY_LOCATION_ID] = label
    }

    fun removeMyLocation() {
        markers.remove(MY_LOCATION_ID)?.remove()
    }

    private fun createMyLocationBitmap(sizePx: Int = 48): Bitmap {
        val bm = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        val cx = sizePx / 2f
        // 헤일로 (반투명 파랑)
        c.drawCircle(cx, cx, cx, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x442196F3 })
        // 흰 테두리
        c.drawCircle(cx, cx, cx * 0.48f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        })
        // 본체 파란 점
        c.drawCircle(cx, cx, cx * 0.40f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2196F3.toInt()
        })
        return bm
    }

    /**
     * 동 가격 버블 비트맵 — Brown 카드 디자인.
     * - White bg, 14dp radius, 1.5dp 베이지(#E8D3C2) border
     * - 그림자: Paint.setShadowLayer(14f, 0, 4f, 0x33614B3A)
     * - 상단: 동명(12sp Bold #1E120A) + "${count}곳"(10sp SemiBold #8A6E58)
     * - 하단: "${min}~${max}만" or "${val}만" (14sp ExtraBold #614B3A, letter-spacing -0.4)
     * - 하단 중앙 7px 화살표 tail
     * - min canvas width 96px
     */
    private fun createDongBubbleBitmap(
        dongName: String, count: Int, minKrw: Int, maxKrw: Int,
    ): Bitmap {
        val density = 3f  // ~ xxhdpi 기준 1dp=3px. 캔버스 단위는 px.
        val cornerR = 14f * density
        val borderW = 1.5f * density
        val padH = 12f * density
        val padV = 7f * density
        val tailH = 7f * density
        val tailHalfW = 7f * density
        val rowGap = 1f * density
        val shadowR = 14f
        val shadowDy = 4f

        // 텍스트 페인트
        val dongPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E120A.toInt()
            textSize = 12f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF8A6E58.toInt()
            textSize = 10f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val pricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF614B3A.toInt()
            textSize = 14f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
            letterSpacing = -0.4f / 14f
        }

        val countText = "${count}곳"
        val priceText = formatPriceRange(minKrw, maxKrw)

        // 가로폭: max( 동명+gap+개수, 가격 ) + padH*2
        val topGap = 6f * density
        val topWidth = dongPaint.measureText(dongName) + topGap + countPaint.measureText(countText)
        val priceWidth = pricePaint.measureText(priceText)
        val contentW = maxOf(topWidth, priceWidth)
        val minCanvasW = 96f * density
        val cardW = maxOf(contentW + padH * 2, minCanvasW)

        val dongH = dongPaint.descent() - dongPaint.ascent()
        val priceH = pricePaint.descent() - pricePaint.ascent()
        val cardH = padV + dongH + rowGap + priceH + padV

        val totalH = cardH + tailH

        // 그림자를 위한 여유 공간 (canvas는 shadow를 잘라먹지 않음 - bitmap 사이즈만 넉넉히)
        val shadowPad = shadowR + shadowDy
        val bitmap = Bitmap.createBitmap(
            (cardW + shadowPad * 2).toInt(),
            (totalH + shadowPad * 2).toInt(),
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        canvas.translate(shadowPad, shadowPad)

        // 카드 body path = 라운드 사각 + 아래 화살표
        val bodyPath = Path().apply {
            // 라운드 사각
            addRoundRect(RectF(0f, 0f, cardW, cardH), cornerR, cornerR, Path.Direction.CW)
            // 화살표 (아래쪽으로 삼각형, 카드 하단 중앙)
            val tipX = cardW / 2f
            moveTo(tipX - tailHalfW, cardH - 0.5f)
            lineTo(tipX, cardH + tailH)
            lineTo(tipX + tailHalfW, cardH - 0.5f)
            close()
        }

        // 그림자 + 흰 채움
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            setShadowLayer(shadowR, 0f, shadowDy, 0x33614B3A)
        }
        canvas.drawPath(bodyPath, fillPaint)

        // 보더 (라운드 사각만, 화살표 포함하면 라인이 겹쳐 보임)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE8D3C2.toInt()
            style = Paint.Style.STROKE
            strokeWidth = borderW
        }
        canvas.drawRoundRect(
            RectF(borderW / 2, borderW / 2, cardW - borderW / 2, cardH - borderW / 2),
            cornerR, cornerR, borderPaint,
        )

        // 텍스트
        val dongY = padV - dongPaint.ascent()
        canvas.drawText(dongName, padH, dongY, dongPaint)
        canvas.drawText(countText, cardW - padH, dongY, countPaint)

        val priceY = padV + dongH + rowGap - pricePaint.ascent()
        canvas.drawText(priceText, padH, priceY, pricePaint)

        return bitmap
    }

    /** "5.3~12만" or "5.3만" (min == max 일 때). */
    private fun formatPriceRange(minKrw: Int, maxKrw: Int): String {
        val minStr = krwToManwon(minKrw)
        return if (minKrw == maxKrw) "${minStr}만"
               else "${minStr}~${krwToManwon(maxKrw)}만"
    }

    private fun krwToManwon(krw: Int): String {
        val tenths = (krw + 500) / 1_000
        val whole = tenths / 10
        val frac = tenths % 10
        return if (frac == 0) whole.toString() else "$whole.$frac"
    }

    /** computeMarkerSpecs 에 넘길 projector. 호출 시점의 KakaoMap 좌표 변환을 캡처. */
    fun screenProjector(): MarkerProjector? {
        val map = kakaoMap ?: return null
        return object : MarkerProjector {
            override fun toScreen(lat: Double, lng: Double): Pair<Int, Int>? = runCatching {
                val pt = map.toScreenPoint(com.kakao.vectormap.LatLng.from(lat, lng))
                    ?: return@runCatching null
                pt.x to pt.y
            }.getOrNull()
        }
    }

    /** 카메라 애니메이션. duration ms. */
    fun animateCamera(lat: Double, lng: Double, zoomLevel: Int, durationMs: Int = 400) {
        runCatching {
            kakaoMap?.moveCamera(
                CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), zoomLevel),
                com.kakao.vectormap.camera.CameraAnimation.from(durationMs, true, true),
            )
        }
    }

    /** 여러 좌표를 모두 포함하도록 카메라를 fitBounds. 단일 좌표면 fallback 으로 zoom+2. */
    fun fitMapPoints(
        points: List<Pair<Double, Double>>,
        paddingPx: Int = 80,
        durationMs: Int = 400,
    ) {
        val map = kakaoMap ?: return
        if (points.isEmpty()) return
        if (points.size == 1) {
            val (lat, lng) = points.first()
            val z = (getCurrentZoom() + 2).coerceAtMost(19)
            animateCamera(lat, lng, z, durationMs)
            return
        }
        runCatching {
            val latLngs = points.map { (lat, lng) -> LatLng.from(lat, lng) }
            val sw = LatLng.from(
                latLngs.minOf { it.latitude },
                latLngs.minOf { it.longitude },
            )
            val ne = LatLng.from(
                latLngs.maxOf { it.latitude },
                latLngs.maxOf { it.longitude },
            )
            val bounds = com.kakao.vectormap.LatLngBounds(sw, ne)
            map.moveCamera(
                CameraUpdateFactory.fitMapPoints(bounds, paddingPx),
                com.kakao.vectormap.camera.CameraAnimation.from(durationMs, true, true),
            )
        }
    }

    companion object {
        const val MY_LOCATION_ID = "__my_location__"
    }

    /**
     * 선택된 핀: 본체 원 1.3배 + 핑크 헤일로 + 이름 흰 칩.
     */
    private fun createSelectedPinBitmap(@ColorInt color: Int, category: String?, name: String?): Bitmap {
        val r = 22f * 1.3f             // 본체 반지름 ↑
        val haloR = r + 6f             // 헤일로 외경
        val cx = haloR + 3f
        val cy = haloR + 3f

        val line1 = name?.take(5) ?: ""
        val line2 = if ((name?.length ?: 0) > 5) name!!.drop(5).take(6) else ""
        val lines = listOfNotNull(line1.ifEmpty { null }, line2.ifEmpty { null })

        val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
        }
        val chipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            this.color = 0xFF1E120A.toInt()
        }
        val lineH = chipTextPaint.descent() - chipTextPaint.ascent()
        val chipPadH = 10f
        val chipPadV = 4f
        val chipTextW = lines.maxOfOrNull { chipTextPaint.measureText(it) } ?: 0f
        val chipW = chipTextW + chipPadH * 2
        val chipH = lines.size * lineH + chipPadV * 2

        val totalW = maxOf((cx + haloR + 3f) * 2, chipW + 6f)
        val gap = 6f
        val totalH = cy + haloR + 3f + gap + chipH

        val bitmap = Bitmap.createBitmap(totalW.toInt(), totalH.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val pinCx = totalW / 2f

        // 헤일로 (반투명 핑크)
        canvas.drawCircle(pinCx, cy, haloR, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = 0x40F04268
        })
        // 흰 테두리 원
        canvas.drawCircle(pinCx, cy, r + 4f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
        })
        // 카테고리 컬러 원
        canvas.drawCircle(pinCx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })

        // 카테고리 이모지
        val icon = when (category?.uppercase()) {
            "CAFE"       -> "☕"
            "PARK"       -> "🌳"
            "HOSPITAL"   -> "🏥"
            "GROOMING"   -> "✂"
            "RESTAURANT" -> "🍽"
            "PET_HOTEL"  -> "🏨"
            else         -> "★"
        }
        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = r * 0.95f
            textAlign = Paint.Align.CENTER
            this.color = android.graphics.Color.WHITE
        }
        canvas.drawText(icon, pinCx, cy + r * 0.32f, iconPaint)

        // 이름 칩 (흰 라운드 + shadow + 검은 텍스트)
        if (lines.isNotEmpty()) {
            val chipTop = cy + haloR + 3f + gap
            val chipLeft = pinCx - chipW / 2f
            val chipRect = RectF(chipLeft, chipTop, chipLeft + chipW, chipTop + chipH)
            canvas.drawRoundRect(chipRect, 12f, 12f, chipPaint)
            var y = chipTop + chipPadV - chipTextPaint.ascent()
            lines.forEach { line ->
                canvas.drawText(line, pinCx, y, chipTextPaint)
                y += lineH
            }
        }
        return bitmap
    }

    private fun createPinBitmap(
        @ColorInt color: Int,
        category: String? = null,
        name: String? = null,
        radius: Float = 22f,
    ): Bitmap {
        val r  = radius   // 원 반지름
        val cx = r + 3f
        val cy = r + 3f

        // 매장명을 5자 기준으로 2줄 분리
        val line1 = name?.take(5) ?: ""
        val line2 = if ((name?.length ?: 0) > 5) name!!.drop(5).take(6) else ""

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            this.color = 0xFF1E120A.toInt()
        }
        val lineH   = textPaint.descent() - textPaint.ascent()
        val textGap = 4f
        val lines   = listOfNotNull(line1.ifEmpty { null }, line2.ifEmpty { null })
        val textBlockH = lines.size * lineH + (lines.size - 1).coerceAtLeast(0) * textGap

        val totalW = maxOf((cx + r + 3f) * 2, lines.maxOfOrNull { textPaint.measureText(it) + 8f } ?: 0f)
        val totalH = cy + r + 3f + textGap + textBlockH + 4f

        val bitmap = Bitmap.createBitmap(totalW.toInt(), totalH.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val pinCx = totalW / 2f

        // 흰 테두리 원
        canvas.drawCircle(pinCx, cy, r + 3f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE })
        // 카테고리 컬러 원
        canvas.drawCircle(pinCx, cy, r,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })

        // 카테고리 이모지 or 별
        val icon = when (category?.uppercase()) {
            "CAFE"       -> "☕"
            "PARK"       -> "🌳"
            "HOSPITAL"   -> "🏥"
            "GROOMING"   -> "✂"
            "RESTAURANT" -> "🍽"
            "PET_HOTEL"  -> "🏨"
            else         -> "★"
        }
        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = r * 0.95f
            textAlign = Paint.Align.CENTER
            this.color = android.graphics.Color.WHITE
        }
        canvas.drawText(icon, pinCx, cy + r * 0.32f, iconPaint)

        // 매장명 텍스트 (원 아래) — 흰 테두리 → 검정 채움 순서로 2회 드로우
        val strokePaint = Paint(textPaint).apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            strokeJoin = Paint.Join.ROUND
            this.color = android.graphics.Color.WHITE
        }
        textPaint.style = Paint.Style.FILL
        var nameY = cy + r + 3f + textGap - textPaint.ascent()
        lines.forEach { line ->
            canvas.drawText(line, pinCx, nameY, strokePaint)
            canvas.drawText(line, pinCx, nameY, textPaint)
            nameY += lineH + textGap
        }

        return bitmap
    }
}
