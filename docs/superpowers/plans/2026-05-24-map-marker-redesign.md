# Map Marker Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 지도 마커·클러스터링을 깜빡임 없이 부드럽게 동작시키고, 클러스터 탭 → 줌인 + 선택 마커 강조 + 아이콘 스택 클러스터로 시각·동작 모두 개선.

**Architecture:** `MarkerSpec` sealed interface가 비주얼 식별자(`visualKey`)를 들고, `MapViewWrapper.syncMarkers(prefix, specs)` 가 기존 마커와 id-set diff 해서 비주얼이 같은 마커는 건드리지 않는다. 클러스터링은 픽셀 거리 그리디 (Kakao SDK 비의존, 순수 함수 → 테스트 가능). 비트맵은 LRU 200 캐시. 기존 `addMarker`/`clearMarkers`/`clearMarkersWithPrefix` API는 유지(4개 화면 사용처 보호).

**Tech Stack:** Kotlin · Jetpack Compose · Kakao Vector Map SDK 2.12.8 · JUnit 4 · `androidx.collection.LruCache`

**Spec:** `docs/superpowers/specs/2026-05-24-map-marker-redesign-design.md`

---

## File Structure

| 파일 | 역할 | 새 파일? |
|---|---|---|
| `app/src/main/java/com/example/siheunggagae/map/MarkerSpec.kt` | `MarkerSpec` sealed interface, `BitmapKey`, `MarkerProjector` 인터페이스 | 새 |
| `app/src/main/java/com/example/siheunggagae/map/MarkerClustering.kt` | `computeMarkerSpecs(stores, projector, zoom, selectedId)` 순수 함수 | 새 |
| `app/src/main/java/com/example/siheunggagae/MapViewWrapper.kt` | `syncMarkers`, bitmap LRU 캐시, my-location moveTo, `fitMapPoints`, 클러스터 비트맵(아이콘 스택), 선택 핀(halo + name chip) | 수정 |
| `app/src/main/java/com/example/siheunggagae/ui/viewmodel/MapViewModel.kt` | viewport 응답 short-circuit | 수정 |
| `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt` | `syncMarkers` 통합, 클러스터 탭 = bbox fit, 봉사 마커 클러스터링 | 수정 |
| `app/src/test/java/com/example/siheunggagae/MarkerClusteringTest.kt` | 알고리즘 단위 테스트 | 새 |

---

## Task 1: 순수 클러스터링 알고리즘 + 테스트

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/map/MarkerSpec.kt`
- Create: `app/src/main/java/com/example/siheunggagae/map/MarkerClustering.kt`
- Create: `app/src/test/java/com/example/siheunggagae/MarkerClusteringTest.kt`

알고리즘을 Kakao SDK 의존성 없는 순수 함수로 만들면 단위 테스트 가능. `MarkerProjector` 인터페이스로 lat/lng → screen px 변환을 추상화.

- [ ] **Step 1: MarkerSpec / Projector 타입 정의 (최소 형태)**

Create `app/src/main/java/com/example/siheunggagae/map/MarkerSpec.kt`:

```kotlin
package com.example.siheunggagae.map

import com.example.siheunggagae.data.model.StoreViewportItem

/** lat/lng -> 화면 픽셀 좌표 변환. null = 화면 밖 또는 변환 실패. */
interface MarkerProjector {
    fun toScreen(lat: Double, lng: Double): Pair<Int, Int>?
}

sealed interface MarkerSpec {
    val id: String
    val lat: Double
    val lng: Double
    /** equals/hashCode 비교 시 비주얼 식별. onTap 등 비-비주얼 필드는 제외. */
    val visualKey: Any

    data class Single(
        override val id: String,
        override val lat: Double,
        override val lng: Double,
        val category: String,
        val name: String,
        val color: Int,
        val isSelected: Boolean,
        val onTap: (() -> Unit)? = null,
    ) : MarkerSpec {
        override val visualKey: Any = SingleVisual(id, lat, lng, category, name, color, isSelected)
        data class SingleVisual(
            val id: String, val lat: Double, val lng: Double,
            val category: String, val name: String, val color: Int, val isSelected: Boolean,
        )
    }

    data class Cluster(
        override val id: String,
        override val lat: Double,
        override val lng: Double,
        /** 가장 많은 순으로 정렬된 상위 3 카테고리 (혹은 더 적게). */
        val topCategories: List<String>,
        val count: Int,
        /** 탭 시 bbox 계산용 — (lat, lng) 쌍. */
        val memberCoords: List<Pair<Double, Double>>,
        val onTap: (() -> Unit)? = null,
    ) : MarkerSpec {
        override val visualKey: Any = ClusterVisual(id, lat, lng, topCategories, count)
        data class ClusterVisual(
            val id: String, val lat: Double, val lng: Double,
            val topCategories: List<String>, val count: Int,
        )
    }
}

internal data class ClusterAccumulator(
    var sx: Double,
    var sy: Double,
    var sumLat: Double,
    var sumLng: Double,
    val members: MutableList<StoreViewportItem>,
) {
    fun centroidLat(): Double = sumLat / members.size
    fun centroidLng(): Double = sumLng / members.size
}
```

- [ ] **Step 2: Run a smoke build to confirm it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL (no other files reference MarkerSpec yet).

- [ ] **Step 3: 알고리즘 테스트 작성 (failing)**

Create `app/src/test/java/com/example/siheunggagae/MarkerClusteringTest.kt`:

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.data.model.StoreViewportItem
import com.example.siheunggagae.map.MarkerProjector
import com.example.siheunggagae.map.MarkerSpec
import com.example.siheunggagae.map.computeMarkerSpecs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 단위 좌표를 1° = 100px 로 단순 매핑하는 fake projector.
 * 예: lat=37.0, lng=126.0 → (12600, 3700) (lng → x, lat → y).
 */
private class FakeProjector(
    private val originLat: Double = 0.0,
    private val originLng: Double = 0.0,
    private val pxPerDegree: Double = 100.0,
) : MarkerProjector {
    override fun toScreen(lat: Double, lng: Double): Pair<Int, Int>? {
        val x = ((lng - originLng) * pxPerDegree).toInt()
        val y = ((lat - originLat) * pxPerDegree).toInt()
        return x to y
    }
}

private fun store(id: Int, lat: Double, lng: Double, cat: String = "CAFE", name: String = "S$id") =
    StoreViewportItem(storeId = id, name = name, latitude = lat, longitude = lng, category = cat)

class MarkerClusteringTest {

    @Test fun `zoom less than 11 returns empty`() {
        val stores = listOf(store(1, 37.0, 126.0), store(2, 37.1, 126.1))
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 10, selectedId = null)
        assertTrue(result.isEmpty())
    }

    @Test fun `zoom 16 always returns singles`() {
        val stores = listOf(
            store(1, 37.0, 126.0),
            store(2, 37.0001, 126.0001), // 1 px 이내
        )
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 16, selectedId = null)
        assertEquals(2, result.size)
        assertTrue(result.all { it is MarkerSpec.Single })
    }

    @Test fun `nearby stores within 80px form one cluster`() {
        // 1° = 100px, 0.5° = 50px → 80px 이내
        val stores = listOf(
            store(1, 37.0, 126.0),
            store(2, 37.0, 126.5),
            store(3, 37.5, 126.0),
        )
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 13, selectedId = null)
        assertEquals(1, result.size)
        val c = result.first() as MarkerSpec.Cluster
        assertEquals(3, c.count)
        assertEquals(listOf(37.0 to 126.0, 37.0 to 126.5, 37.5 to 126.0), c.memberCoords)
    }

    @Test fun `distant stores stay separate singles`() {
        val stores = listOf(
            store(1, 37.0, 126.0),  // (12600, 3700)
            store(2, 38.0, 127.0),  // (12700, 3800) — 0.1*100=10px? no, |dx|=100, |dy|=100 → dist≈141px
        )
        // diff |dx|=100, |dy|=100 → distance ≈ 141 px > 80 → separate
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 13, selectedId = null)
        assertEquals(2, result.size)
        assertTrue(result.all { it is MarkerSpec.Single })
    }

    @Test fun `grid-edge case — close stores across decimal boundary still cluster`() {
        // 현재 알고리즘 결함: lat 37.40 vs 37.41 같은 격자 외라서 분리됨.
        // 픽셀 거리 기반에서는 같은 클러스터.
        val stores = listOf(
            store(1, 37.40, 126.50),
            store(2, 37.41, 126.50),
        )
        // 거리: |dy| = 0.01 * 100 = 1 px → cluster
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 12, selectedId = null)
        assertEquals(1, result.size)
        assertTrue(result.first() is MarkerSpec.Cluster)
    }

    @Test fun `single store at zoom 13 is Single`() {
        val stores = listOf(store(1, 37.0, 126.0))
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 13, selectedId = null)
        assertEquals(1, result.size)
        assertTrue(result.first() is MarkerSpec.Single)
    }

    @Test fun `off-screen stores excluded`() {
        val projector = object : MarkerProjector {
            override fun toScreen(lat: Double, lng: Double): Pair<Int, Int>? =
                if (lat < 38.0) (lng * 100).toInt() to (lat * 100).toInt() else null
        }
        val stores = listOf(
            store(1, 37.0, 126.0),
            store(2, 39.0, 126.0),  // off-screen
        )
        val result = computeMarkerSpecs(stores, projector, zoom = 13, selectedId = null)
        assertEquals(1, result.size)
        assertEquals(1, (result.first() as MarkerSpec.Single).id.let { it.removePrefix("store_").toInt() })
    }

    @Test fun `top categories sorted by frequency`() {
        val stores = listOf(
            store(1, 37.0, 126.0, "CAFE"),
            store(2, 37.001, 126.0, "CAFE"),
            store(3, 37.002, 126.0, "CAFE"),
            store(4, 37.003, 126.0, "HOSPITAL"),
            store(5, 37.004, 126.0, "HOSPITAL"),
            store(6, 37.005, 126.0, "PARK"),
            store(7, 37.006, 126.0, "RESTAURANT"),
        )
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 12, selectedId = null)
        assertEquals(1, result.size)
        val c = result.first() as MarkerSpec.Cluster
        assertEquals(listOf("CAFE", "HOSPITAL", "PARK"), c.topCategories) // 최대 3
        assertEquals(7, c.count)
    }

    @Test fun `selected store marks Single as isSelected`() {
        val stores = listOf(store(1, 37.0, 126.0), store(2, 38.5, 127.5))
        val result = computeMarkerSpecs(stores, FakeProjector(), zoom = 16, selectedId = 2)
        val singles = result.filterIsInstance<MarkerSpec.Single>()
        assertEquals(2, singles.size)
        assertTrue(singles.first { it.id == "store_2" }.isSelected)
        assertTrue(!singles.first { it.id == "store_1" }.isSelected)
    }
}
```

- [ ] **Step 4: 테스트 실행해 컴파일 실패 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.MarkerClusteringTest"`
Expected: FAIL — `computeMarkerSpecs` unresolved reference.

- [ ] **Step 5: 알고리즘 구현 (`MarkerClustering.kt`)**

Create `app/src/main/java/com/example/siheunggagae/map/MarkerClustering.kt`:

```kotlin
package com.example.siheunggagae.map

import com.example.siheunggagae.data.model.StoreViewportItem

private const val CLUSTER_RADIUS_PX = 80
private const val CLUSTER_RADIUS_SQ = CLUSTER_RADIUS_PX * CLUSTER_RADIUS_PX

/**
 * Viewport 매장 → MarkerSpec 리스트 (개별 + 클러스터).
 *
 * - zoom < 11: empty
 * - zoom >= 16: 항상 개별
 * - 그 외: 픽셀 거리 80px 그리디 클러스터
 * - 화면 밖(projector null) 매장 제외
 */
fun computeMarkerSpecs(
    stores: List<StoreViewportItem>,
    projector: MarkerProjector,
    zoom: Int,
    selectedId: Int?,
): List<MarkerSpec> {
    if (zoom < 11) return emptyList()

    if (zoom >= 16) {
        return stores.mapNotNull { s ->
            projector.toScreen(s.latitude, s.longitude) ?: return@mapNotNull null
            MarkerSpec.Single(
                id = "store_${s.storeId}",
                lat = s.latitude,
                lng = s.longitude,
                category = s.category,
                name = s.name,
                color = colorFor(s.category),
                isSelected = (s.storeId == selectedId),
            )
        }
    }

    val accumulators = mutableListOf<ClusterAccumulator>()
    for (s in stores) {
        val (sx, sy) = projector.toScreen(s.latitude, s.longitude) ?: continue
        val host = accumulators.firstOrNull { acc ->
            val dx = acc.sx - sx
            val dy = acc.sy - sy
            (dx * dx + dy * dy) < CLUSTER_RADIUS_SQ
        }
        if (host != null) {
            host.members += s
            host.sumLat += s.latitude
            host.sumLng += s.longitude
            // 평균 픽셀 위치 갱신 (centroid drift)
            val n = host.members.size
            host.sx = ((host.sx * (n - 1)) + sx) / n
            host.sy = ((host.sy * (n - 1)) + sy) / n
        } else {
            accumulators += ClusterAccumulator(
                sx = sx.toDouble(),
                sy = sy.toDouble(),
                sumLat = s.latitude,
                sumLng = s.longitude,
                members = mutableListOf(s),
            )
        }
    }

    return accumulators.map { acc ->
        if (acc.members.size == 1) {
            val s = acc.members.first()
            MarkerSpec.Single(
                id = "store_${s.storeId}",
                lat = s.latitude,
                lng = s.longitude,
                category = s.category,
                name = s.name,
                color = colorFor(s.category),
                isSelected = (s.storeId == selectedId),
            )
        } else {
            val tops = acc.members
                .groupingBy { it.category }
                .eachCount()
                .entries.sortedByDescending { it.value }
                .take(3)
                .map { it.key }
            // 클러스터 id 는 멤버 id 정렬 해시 — 멤버 동일하면 id 동일 → diff 효율↑
            val sortedIds = acc.members.map { it.storeId }.sorted()
            val id = "cluster_" + sortedIds.joinToString("-")
            MarkerSpec.Cluster(
                id = id,
                lat = acc.centroidLat(),
                lng = acc.centroidLng(),
                topCategories = tops,
                count = acc.members.size,
                memberCoords = acc.members.map { it.latitude to it.longitude },
            )
        }
    }
}

private fun colorFor(category: String): Int = when (category.uppercase()) {
    "CAFE"       -> 0xFF8A6E58.toInt()
    "PARK"       -> 0xFF4CAF50.toInt()
    "HOSPITAL"   -> 0xFFF04268.toInt()
    "GROOMING"   -> 0xFF9C27B0.toInt()
    "RESTAURANT" -> 0xFFF7A35B.toInt()
    "PET_HOTEL"  -> 0xFF614B3A.toInt()
    else         -> 0xFF614B3A.toInt()
}
```

- [ ] **Step 6: 테스트 실행해 통과 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.MarkerClusteringTest"`
Expected: 9 tests passed.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/siheunggagae/map/MarkerSpec.kt \
        app/src/main/java/com/example/siheunggagae/map/MarkerClustering.kt \
        app/src/test/java/com/example/siheunggagae/MarkerClusteringTest.kt
git commit -m "feat(map): 픽셀 거리 클러스터링 알고리즘 + 단위 테스트

기존 격자(소수점) 기반에서 픽셀 거리 80px 그리디 방식으로 교체.
MarkerSpec sealed interface + 순수 함수로 SDK 의존성 분리."
```

---

## Task 2: MapViewWrapper — Bitmap LRU 캐시 + Projector 노출

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/MapViewWrapper.kt`

비트맵 생성 캐시(LRU 200) + Kakao map 좌표 변환을 외부에서 쓸 수 있게 노출.

- [ ] **Step 1: import 추가 + LRU cache 필드 + BitmapKey 정의 (MapViewWrapper.kt 최상단 클래스 본문 진입부)**

Find:
```kotlin
class MapViewWrapper(private val mapView: MapView) {

    private var kakaoMap: KakaoMap? = null
    private val markers = mutableMapOf<String, Label>()
    private val markerCallbacks = mutableMapOf<String, () -> Unit>()
```

Replace with:
```kotlin
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
        ) : BitmapKey
        data class Cluster(
            val topCategories: List<String>,
            val countBucket: Int,
        ) : BitmapKey
        data object MyLocation : BitmapKey
        data object Volunteer : BitmapKey
        data class VolunteerCluster(val countBucket: Int) : BitmapKey
    }

    private fun bucketCount(n: Int): Int = when {
        n < 10 -> n
        n < 50 -> 10
        n < 100 -> 50
        else -> 100
    }
```

- [ ] **Step 2: Projector 노출 — 클래스 끝부분(`companion object` 위)에 `screenProjector()` 추가**

Find the line:
```kotlin
    companion object {
        const val MY_LOCATION_ID = "__my_location__"
    }
```

Insert above it:
```kotlin
    /** computeMarkerSpecs 에 넘길 projector. 호출 시점의 KakaoMap 좌표 변환을 캡처. */
    fun screenProjector(): com.example.siheunggagae.map.MarkerProjector? {
        val map = kakaoMap ?: return null
        return object : com.example.siheunggagae.map.MarkerProjector {
            override fun toScreen(lat: Double, lng: Double): Pair<Int, Int>? = runCatching {
                val pt = map.toScreenPoint(com.kakao.vectormap.LatLng.from(lat, lng))
                    ?: return null
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
            val bounds = com.kakao.vectormap.LatLngBounds(latLngs)
            map.moveCamera(
                CameraUpdateFactory.fitMapPoints(bounds, paddingPx),
                com.kakao.vectormap.camera.CameraAnimation.from(durationMs, true, true),
            )
        }
    }
```

- [ ] **Step 3: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: 기존 `createPinBitmap`/`createClusterBitmap`/`createMyLocationBitmap` 호출을 캐시 경유로**

In `addMarker(...)` find:
```kotlin
        val style = if (markerColor != null) {
            LabelStyles.from(LabelStyle.from(createPinBitmap(markerColor, category, name)))
        } else {
            LabelStyles.from(LabelStyle.from())
        }
```

Replace with:
```kotlin
        val style = if (markerColor != null) {
            val key = BitmapKey.Single(category ?: "", name ?: "", markerColor, selected = false)
            val bmp = bitmapCache.get(key) ?: createPinBitmap(markerColor, category, name).also {
                bitmapCache.put(key, it)
            }
            LabelStyles.from(LabelStyle.from(bmp))
        } else {
            LabelStyles.from(LabelStyle.from())
        }
```

In `addClusterMarker(...)` find:
```kotlin
        val bitmap = createClusterBitmap(count)
        val style = LabelStyles.from(LabelStyle.from(bitmap))
```

Replace with:
```kotlin
        val key = BitmapKey.Cluster(topCategories = emptyList(), countBucket = bucketCount(count))
        val bitmap = bitmapCache.get(key) ?: createClusterBitmap(count).also { bitmapCache.put(key, it) }
        val style = LabelStyles.from(LabelStyle.from(bitmap))
```

In `updateMyLocation(...)` find:
```kotlin
        markers.remove(MY_LOCATION_ID)?.remove()
        val style = LabelStyles.from(LabelStyle.from(createMyLocationBitmap()))
        val label = layer.addLabel(LabelOptions.from(LatLng.from(lat, lng)).setStyles(style))
        label.tag = MY_LOCATION_ID
        label.setClickable(false)
        markers[MY_LOCATION_ID] = label
```

Replace with:
```kotlin
        val existing = markers[MY_LOCATION_ID]
        if (existing != null) {
            runCatching { existing.moveTo(LatLng.from(lat, lng)) }
            return
        }
        val bmp = bitmapCache.get(BitmapKey.MyLocation) ?: createMyLocationBitmap().also {
            bitmapCache.put(BitmapKey.MyLocation, it)
        }
        val style = LabelStyles.from(LabelStyle.from(bmp))
        val label = layer.addLabel(LabelOptions.from(LatLng.from(lat, lng)).setStyles(style))
        label.tag = MY_LOCATION_ID
        label.setClickable(false)
        markers[MY_LOCATION_ID] = label
```

- [ ] **Step 5: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/siheunggagae/MapViewWrapper.kt
git commit -m "perf(map): MapViewWrapper 비트맵 LRU 캐시 + 내 위치 moveTo + 카메라 animate/fit 헬퍼

같은 카테고리·이름 핀은 재렌더 안 함. 내 위치 핀은
remove+add 대신 Label.moveTo 호출. fitMapPoints/animateCamera 헬퍼 추가."
```

---

## Task 3: MapViewWrapper — `syncMarkers` diff API + 아이콘 스택 클러스터·선택 핀 비트맵

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/MapViewWrapper.kt`

새 `syncMarkers(prefix, specs)` 메서드 추가. 클러스터 비트맵 구현을 아이콘 스택으로 교체하고, 선택된 마커용 헤일로 + 이름 칩 버전 비트맵을 분기.

- [ ] **Step 1: `syncMarkers` 메서드 추가 (`clearMarkers()` 위에 삽입)**

Find:
```kotlin
    fun clearMarkers() {
        markers.values.forEach { it.remove() }
        markers.clear()
        markerCallbacks.clear()
    }
```

Insert above:
```kotlin
    /**
     * Spec 리스트와 현재 마커 상태를 diff 하여 변경만 적용.
     * `prefix` 그룹 단위로 "이전에 sync 한 ID 집합" 을 추적해, 다음 호출 시 desired 에 없는 것만 제거.
     * 다른 prefix 의 마커·내 위치는 건드리지 않음.
     */
    fun syncMarkers(prefix: String, specs: List<com.example.siheunggagae.map.MarkerSpec>) {
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

    private fun com.example.siheunggagae.map.MarkerSpec.onTapOrNull(): (() -> Unit)? = when (this) {
        is com.example.siheunggagae.map.MarkerSpec.Single  -> onTap
        is com.example.siheunggagae.map.MarkerSpec.Cluster -> onTap
    }

    private fun addSpecInternal(spec: com.example.siheunggagae.map.MarkerSpec) {
        val map = kakaoMap ?: return
        val layer = map.labelManager?.layer ?: return
        when (spec) {
            is com.example.siheunggagae.map.MarkerSpec.Single -> {
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
            is com.example.siheunggagae.map.MarkerSpec.Cluster -> {
                val key = BitmapKey.Cluster(spec.topCategories, bucketCount(spec.count))
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
        }
    }

    /** Single 마커 — selection 상태에 따라 halo + name chip 분기. 기존 createPinBitmap 의 래퍼. */
    private fun createSingleBitmap(
        color: Int, category: String?, name: String?, selected: Boolean,
    ): Bitmap = if (selected) createSelectedPinBitmap(color, category, name)
                else createPinBitmap(color, category, name)
```

- [ ] **Step 2: `createSelectedPinBitmap` 구현 추가 (`createPinBitmap` 함수 위에 삽입)**

Find:
```kotlin
    private fun createPinBitmap(@ColorInt color: Int, category: String? = null, name: String? = null): Bitmap {
```

Insert above:
```kotlin
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
            setShadowLayer(2f, 0f, 1f, 0x33000000)
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
```

- [ ] **Step 3: 기존 `createClusterBitmap` 위에 새 `createIconStackClusterBitmap` 추가**

Find:
```kotlin
    private fun createClusterBitmap(count: Int, sizePx: Int = 56): Bitmap {
```

Insert above:
```kotlin
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
        val totalH = (stackH + badgeH * 0.4f + padding * 2).coerceAtLeast(stackH + padding * 2)
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
```

- [ ] **Step 4: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/siheunggagae/MapViewWrapper.kt
git commit -m "feat(map): syncMarkers diff API + 아이콘 스택 클러스터·선택 핀 비트맵

MarkerSpec 기반 diff 동기화로 동일 비주얼은 재생성 안 함.
클러스터는 상위 3 카테고리 핀 스택 + 다크 +N 뱃지로 시각 정보 강화.
선택된 핀은 1.3배 확대 + 핑크 헤일로 + 이름 흰 칩."
```

---

## Task 4: MapViewModel — Viewport 응답 동일성 short-circuit

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/MapViewModel.kt:254-271`

같은 매장 id 셋이 들어오면 state 재발행 안 함 → Compose recomposition 차단.

- [ ] **Step 1: `enqueueViewportLoad` 내부의 state update 부분만 교체**

Find:
```kotlin
            val body = response?.body()
            _uiState.update { it.copy(
                viewportStores = body?.stores ?: emptyList(),
                truncated = body?.truncated ?: false,
            )}
        }
    }
```

Replace with:
```kotlin
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
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/MapViewModel.kt
git commit -m "perf(map): viewport 응답 동일하면 state 재발행 안 함

같은 매장 id set/truncated 가 다시 들어와도 StateFlow emit 안 함.
LaunchedEffect 재실행 차단 → 마커 깜빡임 1차 방어."
```

---

## Task 5: MapScreen — `syncMarkers` 통합 + 클러스터 탭 = bbox fit + 봉사 마커 클러스터링

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt`

기존 매장·봉사 마커 LaunchedEffect 를 `syncMarkers` 한 번 호출로 통합. 클러스터 탭에 bbox fit 동작 연결. 기존 grid 클러스터 함수 (`computeViewportMarkers`) 와 `ViewportMarker` data class 는 신규 알고리즘으로 교체되므로 제거.

- [ ] **Step 1: import 추가 (파일 상단)**

Find:
```kotlin
import com.example.siheunggagae.data.model.VolunteerMarkerDto
import com.example.siheunggagae.data.model.toStoreResponse
```

Insert above:
```kotlin
import com.example.siheunggagae.map.MarkerSpec
import com.example.siheunggagae.map.computeMarkerSpecs
```

- [ ] **Step 2: viewport 마커 동기화 LaunchedEffect 교체 (line 261-287)**

Find:
```kotlin
    // viewport 마커 동기화 — zoom 11+ 에서 bbox 기반 데이터, 줌에 따라 클러스터링
    LaunchedEffect(mapReady, uiState.viewportStores, uiState.visibleCategories, uiState.currentZoom) {
        if (!mapReady) return@LaunchedEffect
        mapWrapper.clearMarkersWithPrefix("store_")
        mapWrapper.clearMarkersWithPrefix("cluster_")
        val filtered = uiState.viewportStores.filter { it.category in uiState.visibleCategories }
        computeViewportMarkers(filtered, uiState.currentZoom).forEach { marker ->
            if (marker.singleStore != null) {
                val color = categoryColors[marker.singleStore.category] ?: defaultMarkerColor
                mapWrapper.addMarker(
                    id = marker.id,
                    lat = marker.lat,
                    lng = marker.lng,
                    markerColor = color,
                    category = marker.singleStore.category,
                    name = marker.singleStore.name,
                    onTap = { viewModel.selectStore(marker.singleStore.toStoreResponse()) },
                )
            } else {
                mapWrapper.addClusterMarker(
                    id = marker.id,
                    lat = marker.lat,
                    lng = marker.lng,
                    count = marker.count,
                )
            }
        }
    }
```

Replace with:
```kotlin
    // viewport 마커 동기화 — picture-on-update 가 아니라 spec diff 기반 sync
    LaunchedEffect(
        mapReady,
        uiState.viewportStores,
        uiState.visibleCategories,
        uiState.currentZoom,
        uiState.selectedStore?.resolvedId,
    ) {
        if (!mapReady) return@LaunchedEffect
        val projector = mapWrapper.screenProjector() ?: return@LaunchedEffect
        val filtered = uiState.viewportStores.filter { it.category in uiState.visibleCategories }
        val byId = filtered.associateBy { it.storeId }
        val specs = computeMarkerSpecs(filtered, projector, uiState.currentZoom, uiState.selectedStore?.resolvedId)
            .map { spec ->
                when (spec) {
                    is MarkerSpec.Single -> {
                        val storeIdInt = spec.id.removePrefix("store_").toIntOrNull()
                        spec.copy(onTap = {
                            byId[storeIdInt]?.toStoreResponse()?.let { viewModel.selectStore(it) }
                        })
                    }
                    is MarkerSpec.Cluster -> spec.copy(onTap = {
                        mapWrapper.fitMapPoints(spec.memberCoords, paddingPx = 120)
                    })
                }
            }
        mapWrapper.syncMarkers("store", specs)
    }
```

- [ ] **Step 3: 봉사요청 마커 LaunchedEffect 교체 (line 289-304)**

Find:
```kotlin
    // 봉사요청 마커 동기화
    LaunchedEffect(mapReady, uiState.isVolunteerMode, uiState.volunteerMarkers) {
        if (!mapReady) return@LaunchedEffect
        mapWrapper.clearMarkersWithPrefix("vol_")
        if (uiState.isVolunteerMode) {
            uiState.volunteerMarkers.forEach { vol ->
                mapWrapper.addMarker(
                    id = "vol_${vol.requestId}",
                    lat = vol.latitude,
                    lng = vol.longitude,
                    markerColor = volunteerMarkerColor,
                    onTap = { onNavigate(Screen.MatchingPublicDetail.createRoute(vol.requestId)) },
                )
            }
        }
    }
```

Replace with:
```kotlin
    // 봉사요청 마커 동기화 (같은 픽셀 거리 클러스터링 사용)
    LaunchedEffect(mapReady, uiState.isVolunteerMode, uiState.volunteerMarkers, uiState.currentZoom) {
        if (!mapReady) return@LaunchedEffect
        if (!uiState.isVolunteerMode) {
            mapWrapper.syncMarkers("vol", emptyList())
            return@LaunchedEffect
        }
        val projector = mapWrapper.screenProjector() ?: return@LaunchedEffect
        // 봉사요청을 StoreViewportItem 형태로 어댑팅 — 카테고리 "VOLUNTEER" 단일
        val asItems = uiState.volunteerMarkers.map { v ->
            com.example.siheunggagae.data.model.StoreViewportItem(
                storeId = v.requestId,
                name = v.title ?: "",
                latitude = v.latitude,
                longitude = v.longitude,
                category = "VOLUNTEER",
            )
        }
        val specs = computeMarkerSpecs(asItems, projector, uiState.currentZoom, selectedId = null)
            .map { spec ->
                when (spec) {
                    is MarkerSpec.Single -> {
                        val volId = spec.id.removePrefix("store_").toIntOrNull() ?: return@map spec
                        spec.copy(
                            id = "vol_$volId",
                            color = volunteerMarkerColor,
                            onTap = { onNavigate(Screen.MatchingPublicDetail.createRoute(volId)) },
                        )
                    }
                    is MarkerSpec.Cluster -> spec.copy(
                        id = "volcluster_" + spec.id.removePrefix("cluster_"),
                        onTap = { mapWrapper.fitMapPoints(spec.memberCoords, paddingPx = 120) },
                    )
                }
            }
        mapWrapper.syncMarkers("vol", specs)
    }
```

- [ ] **Step 4: 기존 `ViewportMarker` 및 `computeViewportMarkers` 함수 제거 (line 1358-1391)**

Find and delete:
```kotlin
// ─── Viewport 마커 클러스터링 ──────────────────────────────────────────────────

private data class ViewportMarker(
    val id: String,
    val lat: Double,
    val lng: Double,
    val count: Int,
    val singleStore: StoreViewportItem?,  // null = 클러스터
)

/**
 * zoom >= 14: 개별 마커
 * zoom 11~13: 소수점 자리 수 기반 그리드 클러스터링
 *   zoom 11~12 → 1자리(≈11km 격자), zoom 13 → 2자리(≈1km 격자)
 */
private fun computeViewportMarkers(stores: List<StoreViewportItem>, zoom: Int): List<ViewportMarker> {
    if (zoom >= 14) {
        return stores.map { ViewportMarker("store_${it.storeId}", it.latitude, it.longitude, 1, it) }
    }
    val decimals = if (zoom <= 12) 1 else 2
    return stores
        .groupBy { "${"%.${decimals}f".format(it.latitude)},${"%.${decimals}f".format(it.longitude)}" }
        .map { (key, group) ->
            val centerLat = group.sumOf { it.latitude } / group.size
            val centerLng = group.sumOf { it.longitude } / group.size
            ViewportMarker(
                id = "cluster_$key",
                lat = centerLat,
                lng = centerLng,
                count = group.size,
                singleStore = if (group.size == 1) group.first() else null,
            )
        }
}
```

(섹션 헤더 `// ─── Viewport 마커 클러스터링 …` 까지 같이 제거)

- [ ] **Step 5: 카메라 이동을 animate 로 부드럽게 (선택 마커 이동)**

Find:
```kotlin
    // 매장 선택 시 카메라 이동 + 시트는 peek 으로 — StoreDetailSheet 모달이 위에 뜨므로 시트를 hide 할 필요 없음.
    LaunchedEffect(uiState.selectedStore) {
        val store = uiState.selectedStore
        if (store != null && mapReady) {
            mapWrapper.moveCamera(store.latitude, store.longitude)
            sheetState.bottomSheetState.partialExpand()
        }
    }
```

Replace with:
```kotlin
    // 매장 선택 시 카메라 이동 + 시트는 peek 으로 — StoreDetailSheet 모달이 위에 뜨므로 시트를 hide 할 필요 없음.
    LaunchedEffect(uiState.selectedStore) {
        val store = uiState.selectedStore
        if (store != null && mapReady) {
            val z = mapWrapper.getCurrentZoom().coerceAtLeast(16)
            mapWrapper.animateCamera(store.latitude, store.longitude, z, durationMs = 350)
            sheetState.bottomSheetState.partialExpand()
        }
    }
```

- [ ] **Step 6: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: 기존 알고리즘 테스트가 깨지지 않는지 전체 unit test 실행**

Run: `./gradlew :app:testDebugUnitTest`
Expected: 모든 테스트 통과 (특히 PetHotelCompareLogicTest, MarkerClusteringTest).

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt
git commit -m "feat(map): MapScreen syncMarkers 통합 + 클러스터 탭 bbox fit + 봉사 마커 클러스터링

기존 격자 클러스터 함수·ViewportMarker 제거.
viewport·봉사 마커 모두 픽셀 거리 클러스터링 + diff sync 경유.
클러스터 탭 시 멤버 좌표로 fitMapPoints 부드러운 줌인.
선택 매장 카메라 이동도 350ms animate."
```

---

## Task 6: 디바이스 시각 검증

**Files:** (코드 변경 없음 — 동작·시각 검증만)

빌드된 APK 를 실기기 또는 에뮬레이터에서 실행해 spec 의 시각·동작 요구가 충족되는지 확인.

- [ ] **Step 1: 디버그 APK 빌드**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL — `app/build/outputs/apk/debug/app-debug.apk` 생성.

- [ ] **Step 2: 디바이스/에뮬레이터에 설치**

Run: `./gradlew :app:installDebug`
Expected: 설치 성공. (디바이스 없으면 "단계 X 만 수동 검증 필요" 라고 보고.)

- [ ] **Step 3: 검증 체크리스트 — 사용자에게 다음을 보고**

다음 시나리오를 손으로 테스트해 결과를 기록:

1. **클러스터 시각** — 줌 12~13 에서 시청 근처에서 클러스터가 아이콘 스택 + +N 뱃지로 보이는지
2. **클러스터 탭** — 클러스터 탭하면 카메라가 부드럽게 그 영역으로 줌인 + 매장들이 펼쳐지는지
3. **선택 마커 강조** — 핀 탭하면 1.3배 확대 + 핑크 헤일로 + 이름 흰 칩 표시되는지
4. **깜빡임** — 손으로 천천히 줌 인/아웃 할 때 마커가 깜빡이지 않고 부드럽게 유지되는지 (특히 같은 매장이 가만히 있는 경우)
5. **카테고리 필터 변경** — 필터 시트에서 한 카테고리만 끄고 켤 때 다른 카테고리 마커가 깜빡이지 않는지
6. **내 위치 핀** — GPS 권한 켠 채로 이동(또는 모의 위치) 시 파란 점이 사라졌다 나오지 않고 자연스럽게 이동하는지
7. **봉사 모드** (VOLUNTEER 계정만) — 봉사 마커 토글 시 클러스터링 적용되는지
8. **다른 화면 회귀** — HomeScreen 미니맵, PlaceDetailScreen 핀, MatchingPublicDetailScreen 핀 모두 기존대로 렌더되는지

각 항목 PASS/FAIL 보고.

- [ ] **Step 4: 회귀 발견 시 — 새 task 로 fix 추가**

검증 중 깨진 항목이 있으면 plan 에 fix task 를 append 하고 다시 구현 흐름으로.

---

## Self-Review 결과

✅ **Spec coverage**:
- 깜빡임 #1 → Task 1·4·5 (알고리즘·VM short-circuit·syncMarkers diff)
- 클러스터 탭 #2 → Task 5 step 2 (fitMapPoints onTap)
- 점프성 클러스터 #3·#4 → Task 1 (픽셀 거리 알고리즘)
- 클러스터 시각 정보 부족 #5 → Task 3 step 3 (아이콘 스택)
- 비트맵 alloc #6 → Task 2 (LRU 캐시)
- 선택 마커 표시 #7 → Task 3 step 2 (createSelectedPinBitmap)
- 내 위치 깜빡임 #8 → Task 2 step 4 (moveTo)
- 봉사 마커 클러스터링 #9 → Task 5 step 3

✅ **Type consistency**: `MarkerSpec.Single.copy(onTap = …)`, `MarkerSpec.Cluster.copy(onTap = …)`, `syncMarkers(prefix, specs)`, `screenProjector()`, `fitMapPoints(points, paddingPx)`, `animateCamera(lat, lng, zoom, durationMs)` 모두 정의·사용 일관.

✅ **No placeholders**: 모든 step 에 실 코드/명령어 포함.
