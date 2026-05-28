# 펫호텔 동(洞) 가격 오버레이 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** PET_HOTEL 카테고리에서 줌아웃 시(zoom ≤ 13) 시흥시 18개 행정동별로 펫호텔 가격 범위(min~max)를 지도 위에 버블로 띄우고, 버블 탭 시 해당 동으로 줌인하면서 자동으로 개별 호텔 마커로 전환되게 한다.

**Architecture:** 클라이언트 사이드 집계. PET_HOTEL 칩 선택 시 기존 `/maps/pet-hotels?radius=15000` 엔드포인트를 시흥시청 좌표 기준 1회 호출 → `DongAggregator`가 각 호텔을 `SiheungRegions`의 최근접 동 중심점에 할당 → 동별 min/max/count 집계 → 새 `MarkerSpec.DongBubble`로 변환 후 `MapViewWrapper.syncMarkers("dong", ...)`로 렌더. 백엔드 수정 0.

**Tech Stack:** Kotlin / Jetpack Compose / Kakao Map SDK 2.12.8 / Retrofit / JUnit 4

**Spec:** `docs/superpowers/specs/2026-05-28-pethotel-dong-price-overlay-design.md`

---

## 파일 구조

| 파일 | 유형 | 책임 |
|---|---|---|
| `app/src/main/java/com/example/siheunggagae/data/model/DongPriceBucket.kt` | NEW | 동 1개분 가격 집계 데이터 컨테이너 |
| `app/src/main/java/com/example/siheunggagae/map/DongAggregator.kt` | NEW | `aggregate()` (호텔 → 동 버킷) + `Int.toManwon()` 포매터. 순수 함수 |
| `app/src/test/java/com/example/siheunggagae/DongAggregatorTest.kt` | NEW | `aggregate()` + `toManwon()` 단위 테스트 |
| `app/src/main/java/com/example/siheunggagae/map/MarkerSpec.kt` | MODIFY | `MarkerSpec.DongBubble` variant 추가 |
| `app/src/main/java/com/example/siheunggagae/MapViewWrapper.kt` | MODIFY | `BitmapKey.DongBubble`, `createDongBubbleBitmap()`, `addSpecInternal()` 분기 |
| `app/src/main/java/com/example/siheunggagae/ui/viewmodel/MapViewModel.kt` | MODIFY | `petHotels`/`dongBuckets` state, `loadPetHotels()`, `selectCategory()`/`refresh()` 분기 |
| `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt` | MODIFY | dong 버블 sync LaunchedEffect, viewport store sync에서 PET_HOTEL 줌아웃 제외, 기존 배너 조건에 `zoom ≥ 14` 추가 |

---

## Task 1: `DongPriceBucket` 데이터 모델

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/data/model/DongPriceBucket.kt`

- [ ] **Step 1: 파일 생성**

```kotlin
package com.example.siheunggagae.data.model

/**
 * 시흥시 행정동 1개분 펫호텔 가격 집계.
 * count 는 가격 정보(min_price_krw != null)가 있는 호텔 수만 카운트.
 * 가격 정보가 있는 호텔이 0개인 동은 버킷 자체가 생성되지 않으므로 count >= 1 보장.
 */
data class DongPriceBucket(
    val dong: String,
    val lat: Double,
    val lng: Double,
    val count: Int,
    val minKrw: Int,
    val maxKrw: Int,
)
```

- [ ] **Step 2: 빌드 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/data/model/DongPriceBucket.kt
git commit -m "feat(model): add DongPriceBucket for per-dong pet hotel price aggregation"
```

---

## Task 2: `DongAggregator.toManwon()` 포매터 (TDD)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/map/DongAggregator.kt`
- Create: `app/src/test/java/com/example/siheunggagae/DongAggregatorTest.kt`

- [ ] **Step 1: 실패하는 테스트 작성**

`app/src/test/java/com/example/siheunggagae/DongAggregatorTest.kt`:
```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.map.DongAggregator.toManwon
import org.junit.Assert.assertEquals
import org.junit.Test

class DongAggregatorTest {

    @Test fun `toManwon 53000 returns 5dot3`() {
        assertEquals("5.3", 53000.toManwon())
    }

    @Test fun `toManwon 50000 returns 5 without trailing decimal`() {
        assertEquals("5", 50000.toManwon())
    }

    @Test fun `toManwon 120000 returns 12`() {
        assertEquals("12", 120000.toManwon())
    }

    @Test fun `toManwon 125000 returns 12dot5`() {
        assertEquals("12.5", 125000.toManwon())
    }

    @Test fun `toManwon 1000 returns 0dot1`() {
        assertEquals("0.1", 1000.toManwon())
    }

    @Test fun `toManwon 0 returns 0`() {
        assertEquals("0", 0.toManwon())
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.DongAggregatorTest"`
Expected: FAIL — `Unresolved reference: DongAggregator`

- [ ] **Step 3: 최소 구현**

`app/src/main/java/com/example/siheunggagae/map/DongAggregator.kt`:
```kotlin
package com.example.siheunggagae.map

import com.example.siheunggagae.data.model.DongPriceBucket
import com.example.siheunggagae.data.model.PetHotelResponse

object DongAggregator {

    /**
     * KRW → 만원 단위 문자열 (천 단위까지). trailing ".0" 제거.
     * 예: 53_000 → "5.3", 50_000 → "5", 120_000 → "12", 125_000 → "12.5".
     */
    fun Int.toManwon(): String {
        val tenths = (this + 500) / 1_000     // 0.1만 단위로 반올림
        val whole = tenths / 10
        val frac = tenths % 10
        return if (frac == 0) whole.toString() else "$whole.$frac"
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.DongAggregatorTest"`
Expected: 6 tests PASS

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/map/DongAggregator.kt \
        app/src/test/java/com/example/siheunggagae/DongAggregatorTest.kt
git commit -m "feat(map): add DongAggregator.toManwon price formatter"
```

---

## Task 3: `DongAggregator.aggregate()` 집계 로직 (TDD)

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/map/DongAggregator.kt`
- Modify: `app/src/test/java/com/example/siheunggagae/DongAggregatorTest.kt`

- [ ] **Step 1: 실패하는 테스트 작성 — `DongAggregatorTest` 끝에 추가**

```kotlin
    // --- aggregate() ---

    /** 테스트용 더미 동 좌표: A=(0,0), B=(0,10), C=(10,0). 거리상 충분히 떨어짐. */
    private val dummyDongs = mapOf(
        "A" to (0.0 to 0.0),
        "B" to (0.0 to 10.0),
        "C" to (10.0 to 0.0),
    )

    private fun hotel(id: Int, lat: Double, lng: Double, min: Int?, max: Int? = min) =
        com.example.siheunggagae.data.model.PetHotelResponse(
            storeId = id, name = "H$id", address = "addr$id",
            latitude = lat, longitude = lng,
            minPriceKrw = min, maxPriceKrw = max,
        )

    @Test fun `aggregate empty input returns empty`() {
        val result = DongAggregator.aggregate(emptyList(), dummyDongs)
        assertTrue(result.isEmpty())
    }

    @Test fun `aggregate single hotel assigns to nearest dong with count 1 and min equals max`() {
        // (0.1, 0.1) 은 A=(0,0) 에 가장 가까움
        val result = DongAggregator.aggregate(
            listOf(hotel(1, 0.1, 0.1, min = 30_000, max = 30_000)),
            dummyDongs,
        )
        assertEquals(1, result.size)
        val bucket = result.first()
        assertEquals("A", bucket.dong)
        assertEquals(1, bucket.count)
        assertEquals(30_000, bucket.minKrw)
        assertEquals(30_000, bucket.maxKrw)
    }

    @Test fun `aggregate two hotels in different dongs returns two buckets`() {
        val result = DongAggregator.aggregate(
            listOf(
                hotel(1, 0.1, 0.1, min = 30_000, max = 50_000),     // A
                hotel(2, 0.1, 9.9, min = 40_000, max = 60_000),     // B
            ),
            dummyDongs,
        )
        assertEquals(2, result.size)
        val byDong = result.associateBy { it.dong }
        assertEquals(1, byDong["A"]?.count)
        assertEquals(30_000, byDong["A"]?.minKrw)
        assertEquals(50_000, byDong["A"]?.maxKrw)
        assertEquals(1, byDong["B"]?.count)
        assertEquals(40_000, byDong["B"]?.minKrw)
        assertEquals(60_000, byDong["B"]?.maxKrw)
    }

    @Test fun `aggregate three hotels in same dong aggregates min and max`() {
        val result = DongAggregator.aggregate(
            listOf(
                hotel(1, 0.1, 0.1, min = 40_000, max = 60_000),
                hotel(2, 0.2, 0.2, min = 30_000, max = 50_000),
                hotel(3, 0.0, 0.3, min = 55_000, max = 80_000),
            ),
            dummyDongs,
        )
        assertEquals(1, result.size)
        val bucket = result.first()
        assertEquals("A", bucket.dong)
        assertEquals(3, bucket.count)
        assertEquals(30_000, bucket.minKrw)
        assertEquals(80_000, bucket.maxKrw)
    }

    @Test fun `aggregate hotel with null minPrice is skipped`() {
        val result = DongAggregator.aggregate(
            listOf(
                hotel(1, 0.1, 0.1, min = 30_000, max = 50_000),
                hotel(2, 0.2, 0.2, min = null, max = null),
            ),
            dummyDongs,
        )
        assertEquals(1, result.size)
        val bucket = result.first()
        assertEquals(1, bucket.count)
        assertEquals(30_000, bucket.minKrw)
        assertEquals(50_000, bucket.maxKrw)
    }

    @Test fun `aggregate all hotels with null price returns empty`() {
        val result = DongAggregator.aggregate(
            listOf(
                hotel(1, 0.1, 0.1, min = null, max = null),
                hotel(2, 9.9, 0.1, min = null, max = null),
            ),
            dummyDongs,
        )
        assertTrue(result.isEmpty())
    }
```

테스트 파일 상단에 `import org.junit.Assert.assertTrue` 추가.

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.DongAggregatorTest"`
Expected: FAIL — `Unresolved reference: aggregate`

- [ ] **Step 3: 구현 추가 — `DongAggregator` object 안에 추가**

```kotlin
    /**
     * 호텔 리스트 → 동별 가격 버킷.
     *
     * - 각 호텔은 dongCenters 중 가장 가까운 동에 할당 (Haversine 거리)
     * - minPriceKrw == null 인 호텔은 가격 정보 없음 → 스킵
     * - 가격 정보가 있는 호텔이 0개인 동은 버킷 미생성
     * - minKrw = 동 내 호텔들의 min(minPriceKrw)
     *   maxKrw = 동 내 호텔들의 max(maxPriceKrw ?: minPriceKrw)
     */
    fun aggregate(
        hotels: List<PetHotelResponse>,
        dongCenters: Map<String, Pair<Double, Double>>,
    ): List<DongPriceBucket> {
        if (hotels.isEmpty() || dongCenters.isEmpty()) return emptyList()

        data class Acc(var min: Int, var max: Int, var count: Int)
        val byDong = linkedMapOf<String, Acc>()

        for (h in hotels) {
            val minPrice = h.minPriceKrw ?: continue
            val maxPrice = h.maxPriceKrw ?: minPrice
            val nearest = dongCenters.minByOrNull { (_, c) ->
                haversineMeters(h.latitude, h.longitude, c.first, c.second)
            } ?: continue
            val acc = byDong.getOrPut(nearest.key) { Acc(minPrice, maxPrice, 0) }
            acc.min = minOf(acc.min, minPrice)
            acc.max = maxOf(acc.max, maxPrice)
            acc.count += 1
        }

        return byDong.map { (dong, acc) ->
            val (lat, lng) = dongCenters.getValue(dong)
            DongPriceBucket(dong = dong, lat = lat, lng = lng,
                count = acc.count, minKrw = acc.min, maxKrw = acc.max)
        }
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2).let { it * it }
        return 2 * r * Math.asin(Math.sqrt(a))
    }
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.DongAggregatorTest"`
Expected: 12 tests PASS (6 from Task 2 + 6 new)

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/map/DongAggregator.kt \
        app/src/test/java/com/example/siheunggagae/DongAggregatorTest.kt
git commit -m "feat(map): add DongAggregator.aggregate (pet hotels -> per-dong price buckets)"
```

---

## Task 4: `MarkerSpec.DongBubble` variant 추가

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/map/MarkerSpec.kt`

- [ ] **Step 1: `MarkerSpec` sealed interface에 variant 추가 — `Cluster` 데이터 클래스 다음에**

기존 파일의 `Cluster` 정의 직후(닫는 `}` 와 `}` 사이가 아니라 sealed interface 의 마지막 멤버 위치)에 추가:

```kotlin
    data class DongBubble(
        override val id: String,
        override val lat: Double,
        override val lng: Double,
        val dongName: String,
        val count: Int,
        val minKrw: Int,
        val maxKrw: Int,
        val onTap: (() -> Unit)? = null,
    ) : MarkerSpec {
        override val visualKey: Any = DongBubbleVisual(id, lat, lng, dongName, count, minKrw, maxKrw)
        data class DongBubbleVisual(
            val id: String, val lat: Double, val lng: Double,
            val dongName: String, val count: Int, val minKrw: Int, val maxKrw: Int,
        )
    }
```

수정 후 `MarkerSpec.kt` 의 sealed interface 안에는 `Single`, `Cluster`, `DongBubble` 세 variant 가 있어야 함.

- [ ] **Step 2: 빌드 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL — `MapViewWrapper.addSpecInternal()` 가 sealed exhaustive 가 깨지면서 빨간 줄이 뜰 수 있음. 이건 Task 5에서 분기 추가하며 해결되므로 일단 무시. 만약 컴파일이 실패하면 Task 5로 합쳐서 진행.

> **Note:** `addSpecInternal(spec: MarkerSpec)` 의 `when (spec)` 이 exhaustive 라면 컴파일 실패함. 그러면 Task 5의 분기를 먼저 stub(`is MarkerSpec.DongBubble -> {}`)으로 채워서 빌드 통과시킨 뒤 진행.

- [ ] **Step 3: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/map/MarkerSpec.kt
git commit -m "feat(map): add MarkerSpec.DongBubble variant"
```

---

## Task 5: `MapViewWrapper`의 dong 버블 비트맵 렌더링

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/MapViewWrapper.kt`

이 task는 비주얼 렌더링이라 단위 테스트 없음. 빌드 통과 + 다음 task의 통합 후 디바이스/에뮬레이터로 시각 확인.

- [ ] **Step 1: `BitmapKey` sealed interface에 variant 추가**

기존 `BitmapKey.MyLocation` 다음에:
```kotlin
        data class DongBubble(
            val dongName: String,
            val count: Int,
            val minKrw: Int,
            val maxKrw: Int,
        ) : BitmapKey
```

- [ ] **Step 2: `createDongBubbleBitmap()` 함수 추가 — `createMyLocationBitmap()` 다음 위치**

```kotlin
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

        val totalW = cardW
        val totalH = cardH + tailH

        // 그림자를 위한 여유 공간 (canvas는 shadow를 잘라먹지 않음 - bitmap 사이즈만 넉넉히)
        val shadowPad = shadowR + shadowDy
        val bitmap = Bitmap.createBitmap(
            (totalW + shadowPad * 2).toInt(),
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
```

> **Note:** `krwToManwon` 은 `DongAggregator.toManwon` 과 동일 로직이지만 `MapViewWrapper` 가 `map` 패키지에 의존하지 않게 하기 위해 별도 private 함수로 둠. (둘 다 6줄짜리 순수 함수라 DRY 위반보다 의존 방향 단순화가 더 가치 있음.)

- [ ] **Step 3: `addSpecInternal()` 의 `when (spec)` 에 `DongBubble` 분기 추가**

기존 `is MarkerSpec.Cluster -> { ... }` 다음에:
```kotlin
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
```

- [ ] **Step 4: 빌드 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

> **Note (anchor):** Kakao Label은 기본적으로 비트맵 중심을 lat/lng 에 앵커링한다. 우리 버블은 아래 화살표가 있어 "화살표 끝"이 동 중심에 오는 게 이상적이지만, 기존 다른 마커들도 모두 기본 앵커를 쓰므로 일관성 + v1 단순성을 위해 그대로 둔다. 시각 검증(Task 7 Step 6)에서 위치가 어색하면 후속 PR에서 `LabelOptions.from(...).setRank()` 직전에 `LabelStyles.from(...)` 에 transition/anchor 설정 추가.

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/MapViewWrapper.kt
git commit -m "feat(map): render DongBubble markers via createDongBubbleBitmap"
```

---

## Task 6: `MapViewModel` 상태 + `loadPetHotels` + 분기

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/MapViewModel.kt`

- [ ] **Step 1: `MapUiState` 데이터 클래스에 필드 2개 추가 — `centerFallback: EffectiveCenter? = null` 위에 추가**

```kotlin
    val petHotels: List<com.example.siheunggagae.data.model.PetHotelResponse> = emptyList(),
    val dongBuckets: List<com.example.siheunggagae.data.model.DongPriceBucket> = emptyList(),
```

- [ ] **Step 2: imports 정리 — 파일 상단**

```kotlin
import com.example.siheunggagae.data.local.SiheungRegions
import com.example.siheunggagae.data.model.DongPriceBucket
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.map.DongAggregator
```

(이미 `SiheungRegions` import 있으면 중복 제거)

- [ ] **Step 3: `loadPetHotels()` private suspend 함수 추가 — `loadVolunteerMarkers()` 다음 위치**

```kotlin
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
```

- [ ] **Step 4: `selectCategory()` 분기 추가 — 함수 끝의 `if (bbox != null && zoom >= 11) { enqueueViewportLoad(...) }` 다음에 추가**

```kotlin
        if (category == StoreCategory.PET_HOTEL && _uiState.value.petHotels.isEmpty()) {
            viewModelScope.launch { loadPetHotels() }
        }
```

- [ ] **Step 5: `refresh()` 분기 추가 — 함수 끝**

```kotlin
        if (_uiState.value.selectedCategory == StoreCategory.PET_HOTEL) {
            viewModelScope.launch { loadPetHotels() }
        }
```

- [ ] **Step 6: 빌드 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: 기존 단위 테스트 회귀 확인**

Run: `./gradlew :app:testDebugUnitTest`
Expected: ALL PASS (신규 12개 + 기존 모두)

- [ ] **Step 8: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/MapViewModel.kt
git commit -m "feat(map): fetch pet hotels and aggregate per-dong on PET_HOTEL select"
```

---

## Task 7: `MapScreen` 와이어링 (sync + 필터 + 배너)

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt`

- [ ] **Step 1: 동 버블 sync `LaunchedEffect` 추가 — 봉사요청 마커 sync(`LaunchedEffect(mapReady, uiState.isVolunteerMode, ...)`) 직전에 추가**

```kotlin
    // 동 가격 버블 sync — PET_HOTEL 카테고리 + 줌아웃(<=13) 일 때만 표시.
    LaunchedEffect(mapReady, uiState.selectedCategory, uiState.currentZoom, uiState.dongBuckets) {
        if (!mapReady) return@LaunchedEffect
        val show = uiState.selectedCategory == StoreCategory.PET_HOTEL && uiState.currentZoom <= 13
        if (!show) {
            mapWrapper.syncMarkers("dong", emptyList())
            return@LaunchedEffect
        }
        val specs = uiState.dongBuckets.map { bucket ->
            MarkerSpec.DongBubble(
                id = "dong_${bucket.dong}",
                lat = bucket.lat,
                lng = bucket.lng,
                dongName = bucket.dong,
                count = bucket.count,
                minKrw = bucket.minKrw,
                maxKrw = bucket.maxKrw,
                onTap = { mapWrapper.animateCamera(bucket.lat, bucket.lng, zoomLevel = 16) },
            )
        }
        mapWrapper.syncMarkers("dong", specs)
    }
```

- [ ] **Step 2: 기존 viewport store sync에서 PET_HOTEL 줌아웃 시 펫호텔 제외**

`LaunchedEffect(mapReady, uiState.viewportStores, uiState.visibleCategories, uiState.currentZoom, uiState.selectedStore?.resolvedId)` 내부의 `val filtered = uiState.viewportStores.filter { it.category in uiState.visibleCategories }` 줄을 다음으로 교체:

```kotlin
        val hideHotelMarkers = uiState.selectedCategory == StoreCategory.PET_HOTEL && uiState.currentZoom <= 13
        val filtered = uiState.viewportStores.filter {
            it.category in uiState.visibleCategories &&
                !(hideHotelMarkers && it.category == "PET_HOTEL")
        }
```

- [ ] **Step 3: `PetHotelCompareBanner` 의 `AnimatedVisibility` 조건에 `zoom >= 14` 추가**

기존:
```kotlin
                    AnimatedVisibility(
                        visible = isPetHotelOnly && petHotelCount > 0,
                        ...
```
변경:
```kotlin
                    AnimatedVisibility(
                        visible = isPetHotelOnly && petHotelCount > 0 && uiState.currentZoom >= 14,
                        ...
```

- [ ] **Step 4: 빌드 확인**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 단위 테스트 회귀 확인**

Run: `./gradlew :app:testDebugUnitTest`
Expected: ALL PASS

- [ ] **Step 6: 디바이스/에뮬레이터 시각 검증 체크리스트**

빌드한 APK 실행 후 수동 확인:
- [ ] 지도 화면에서 PET_HOTEL 칩 선택 → 잠시 후 (네트워크 호출 후) 동 버블이 줌아웃 상태(<=13)에서 보이는가
- [ ] 버블 디자인이 spec(Brown 카드, 14dp radius, 베이지 보더, 동명+개수+가격 2단)과 일치하는가
- [ ] 가격 표기가 "5.3~12만" 또는 "5만" 식으로 천 단위까지 표시되는가
- [ ] 줌인(>=14) → 버블 사라지고 개별 펫호텔 마커가 보이는가
- [ ] 줌아웃(<=13) → 버블 다시 보이고 개별 마커 사라지는가
- [ ] 버블 탭 → 해당 동 중심으로 줌 16 으로 카메라 이동 + 자동으로 개별 마커 표시되는가
- [ ] 다른 카테고리 칩(CAFE 등) 선택 → 버블 즉시 사라지는가
- [ ] 줌 <= 13 일 때 "주변 펫호텔 N곳" 배너 안 보이는가 (줌 14 이상에서만 노출)
- [ ] 새로고침 FAB → 펫호텔 데이터 재호출되어 버블이 갱신되는가

- [ ] **Step 7: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt
git commit -m "feat(map): wire dong price bubbles into MapScreen with zoom-based switch"
```

---

## 종료 후

모든 task 완료 후:

- [ ] 전체 회귀 빌드 + 테스트: `./gradlew :app:assembleDebug :app:testDebugUnitTest`
- [ ] superpowers:finishing-a-development-branch 스킬로 머지/PR 결정
