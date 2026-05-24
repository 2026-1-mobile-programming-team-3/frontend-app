# 지도 마커·클러스터링 리디자인

**작성일**: 2026-05-24
**대상**: `ui/screen/MapScreen.kt`, `MapViewWrapper.kt`, `ui/viewmodel/MapViewModel.kt`
**관련 화면**: 지도 탭 (Map)

## 1. 배경 — 현재 문제

사용자 지적과 코드 분석에서 드러난 결함:

| # | 문제 | 위치 |
|---|---|---|
| 1 | 줌 변경·viewport 갱신마다 모든 마커가 destroy → recreate 되어 깜빡임 | `MapScreen.kt:261-287` `clearMarkersWithPrefix("store_") + "cluster_"` 후 전체 add |
| 2 | 클러스터 마커 탭이 아무 동작도 안 함 | `MapScreen.kt:279-285` `addClusterMarker(...)` 호출에서 `onTap` 누락 → `MapViewWrapper.kt:138` `setClickable(false)` |
| 3 | 클러스터링이 위경도 소수점 격자 기반 → zoom 11–12 ≈11km / zoom 13 ≈1km / zoom 14+ 개별로 점프 | `MapScreen.kt:1373-1391` `computeViewportMarkers` |
| 4 | 격자 경계 artifact — `lat=37.40 / 37.41` 두 매장이 다른 셀로 강제 분리 | 동일 |
| 5 | 클러스터 마커가 단순 카운트 숫자만 표시 — 카테고리 정보·시각 위계 없음 | `MapViewWrapper.kt:143-162` |
| 6 | 비트맵을 매 addMarker 마다 새로 alloc — 매장 100 개면 Canvas 100 번 | `MapViewWrapper.kt:99-103, 212-278` |
| 7 | 선택된 마커가 시각적으로 평상시와 동일 | 마커 렌더 경로에 selection state 미반영 |
| 8 | 내 위치 핀이 위치 업데이트마다 remove → addLabel | `MapViewWrapper.kt:179-184` |
| 9 | 봉사요청 마커는 클러스터링 없음 | `MapScreen.kt:289-304` |

본 spec은 #1–#9 를 모두 해결한다. 바텀시트 카운트 vs viewport 카운트 불일치는 의미가 다르므로 스코프 외(별도 결정).

## 2. 디자인 결정

### 2.1 마커 시각

**클러스터 마커 (아이콘 스택)**

상위 3 카테고리의 핀 비트맵이 좌→우로 약간씩 겹쳐 쌓인 모양. 각 핀은 직경 26dp, 화이트 2dp 테두리, 카테고리 컬러 배경, 화이트 카테고리 이모지. 우하단에 다크(#1A1A1A) `+N` 카운트 뱃지(2dp 화이트 테두리, 폰트 12sp ExtraBold). 카운트 ≤ 3 이면 핀만(뱃지 생략).

핀 정렬:
- 첫 핀(가장 많은 카테고리): `left=0, top=4dp`, `z=3`
- 둘째 핀: `left=14dp, top=0`, `z=2`
- 셋째 핀: `left=28dp, top=8dp`, `z=1`
- 뱃지: 첫·셋째 핀 그룹의 우하단

전체 크기 약 56dp × 46dp.

**개별 매장 마커 (기존 디자인 유지)**

`createPinBitmap` 기존 로직 그대로. 카테고리 컬러 원 + 이모지 + 이름 2줄. 변경 없음.

**선택된 마커 (Halo + 확대 + 이름 칩)**

선택된 매장의 개별 마커는:
- 본체 원 1.3× 확대 (반지름 22f → 28.6f)
- 본체 바깥쪽에 핑크 헤일로 — `Color(0xFFF04268, alpha=0.25)` 6dp 폭
- 매장 이름은 기존처럼 본체 아래에 표시하되, 흰 칩 배경(반경 12dp, padding 3×10dp, shadow 2dp)에 담아 가독성 강화

클러스터는 selection state 없음 (탭 시 줌인되므로 잠시 후 사라짐).

### 2.2 클러스터 탭 동작

**부드러운 줌인** — Kakao SDK `CameraAnimation` 사용:

```
onClusterTap(cluster):
    members = cluster.members  // List<StoreViewportItem>
    if members.size == 1: selectStore(members[0]); return
    bbox = computeBoundingBox(members, paddingFraction=0.10)
    if bboxSpan(bbox) < epsilon:  // 모든 매장이 거의 같은 좌표
        animateCamera(center=mean, zoom=currentZoom+2, duration=400ms)
    else:
        animateCamera(fitBounds=bbox, duration=400ms)
```

기존 `moveCamera` (`CameraUpdateFactory.newCenterPosition`) 외에 `fitMapPoints(LatLng[], padding)` 변형도 wrapper에 추가.

### 2.3 클러스터링 알고리즘 — 픽셀 거리 그리디

```
computeViewportMarkers(stores, projector, zoom):
    if zoom < 11: return []
    if zoom >= 16: return stores.map(::single)   // 항상 개별
    THRESHOLD_PX = 80
    result = mutableListOf<Cluster>()
    for store in stores:
        sx, sy = projector.toScreen(store.lat, store.lng) ?: continue
        host = result.firstOrNull { (it.sx - sx).pow(2) + (it.sy - sy).pow(2) < THRESHOLD_PX² }
        if host != null:
            host.add(store, sx, sy)        // host.sx, host.sy 는 가중 평균으로 업데이트
        else:
            result += Cluster.from(store, sx, sy)
    return result.map { it.toMarker() }
```

- 픽셀 거리 80px = 손가락 터치 영역 + 여유. supercluster 기본값(60px) 보다 보수적.
- `projector` 는 `MapViewWrapper` 가 `KakaoMap.toScreenPoint(LatLng)` 를 노출.
- 좌→픽셀 투영이 실패한(화면 밖) 매장은 클러스터 후보에서 제외.

**클러스터 → 멤버 목록**: 클러스터 마커가 자기 멤버 리스트를 가지고 있어야 탭 시 bbox 계산 가능. `ViewportMarker` 데이터 구조에 `members: List<StoreViewportItem>` 추가.

### 2.4 깜빡임 방지 — 다층 방어

**Layer 1 · ViewModel 단** — viewport 응답 동일성 체크:

```kotlin
private fun applyViewportResponse(stores: List<StoreViewportItem>) {
    val current = _uiState.value.viewportStores
    if (stores.size == current.size &&
        stores.zip(current).all { (a, b) -> a.storeId == b.storeId }) {
        return  // 동일 → 재발행 안 함
    }
    _uiState.update { it.copy(viewportStores = stores) }
}
```

**Layer 2 · Compose Effect 키 정리**:

기존:
```kotlin
LaunchedEffect(mapReady, uiState.viewportStores, uiState.visibleCategories, uiState.currentZoom)
```

신규:
```kotlin
LaunchedEffect(mapReady, uiState.viewportStores, uiState.visibleCategories,
               uiState.currentZoom, uiState.selectedStore?.resolvedId) {
    if (!mapReady) return@LaunchedEffect
    val filtered = uiState.viewportStores.filter { it.category in uiState.visibleCategories }
    val specs = computeViewportMarkers(filtered, mapWrapper, uiState.currentZoom)
        .map { it.toSpec(selectedId = uiState.selectedStore?.resolvedId) }
    mapWrapper.syncMarkers("store", specs)
}
```

`currentZoom` 은 클러스터 알고리즘에 필요하므로 유지하되, viewport 데이터 자체가 안 바뀌면 zoom 변화에도 다음 레이어가 diff 처리.

**Layer 3 · Wrapper diff (`syncMarkers`)**:

```kotlin
fun syncMarkers(prefix: String, specs: List<MarkerSpec>) {
    val desired = specs.associateBy { "${prefix}_${it.id}" }
    val existing = markers.keys.filter { it.startsWith("${prefix}_") }.toSet()

    // 제거: 존재하지만 desired 에 없는 것
    (existing - desired.keys).forEach { id ->
        markers.remove(id)?.remove()
        markerCallbacks.remove(id)
        markerSpecs.remove(id)
    }
    // 추가/업데이트
    desired.forEach { (id, spec) ->
        val cachedKey = markerSpecs[id]
        if (cachedKey == spec.visualKey) {
            // 비주얼은 동일 — 콜백만 최신화 (람다 identity 변화 흡수)
            spec.onTap?.let { markerCallbacks[id] = it }
            return@forEach
        }
        markers.remove(id)?.remove()  // 비주얼 변경 → 재생성
        addMarkerInternal(id, spec)
        markerSpecs[id] = spec.visualKey
    }
}
```

`MarkerSpec` 은 `data class` 로 정의 — equality 가 비주얼 변화 감지의 핵심.

```kotlin
sealed interface MarkerSpec {
    val id: String; val lat: Double; val lng: Double
    val visualKey: Any  // equals 비교 시 사용 — 비주얼 식별자
    val onTap: (() -> Unit)?

    data class Single(
        override val id: String, override val lat: Double, override val lng: Double,
        val category: String, val name: String, val color: Int, val isSelected: Boolean,
        override val onTap: (() -> Unit)?,
    ) : MarkerSpec {
        override val visualKey = Visual(id, lat, lng, category, name, color, isSelected)
        data class Visual(val id: String, val lat: Double, val lng: Double,
                          val category: String, val name: String, val color: Int, val isSelected: Boolean)
    }

    data class Cluster(
        override val id: String, override val lat: Double, override val lng: Double,
        val topCategories: List<String>, val count: Int,
        val memberIds: List<Int>,           // 탭 시 bbox 계산용
        val memberCoords: List<Pair<Double, Double>>,  // (lat, lng) — bbox 즉시 계산 가능
        override val onTap: (() -> Unit)?,
    ) : MarkerSpec {
        override val visualKey = Visual(id, lat, lng, topCategories, count)
        data class Visual(val id: String, val lat: Double, val lng: Double,
                          val topCategories: List<String>, val count: Int)
    }
}
```

**diff 동등성**: `syncMarkers` 의 변화 감지는 `spec.visualKey` 끼리 비교 — `onTap` 람다·`memberIds`·`memberCoords` 같은 비-비주얼 필드는 비교 대상 외. 람다 identity 가 매 컴포지션마다 바뀌어도 마커 재생성 트리거하지 않음. 단 `onTap` 자체는 항상 최신 값으로 교체 (`markerCallbacks[id] = spec.onTap`).

**Layer 4 · Bitmap LRU 캐시**:

```kotlin
private val bitmapCache = LruCache<BitmapKey, Bitmap>(200)

sealed interface BitmapKey {
    data class Single(val category: String, val name: String, val color: Int, val selected: Boolean) : BitmapKey
    data class Cluster(val topCategories: List<String>, val countBucket: Int) : BitmapKey
    object MyLocation : BitmapKey
}

fun countBucket(n: Int): Int = when {
    n < 10 -> n
    n < 50 -> 10
    n < 100 -> 50
    else -> 100
}
```

같은 카테고리·이름·선택상태 조합은 한 번만 그리고 재사용. 클러스터도 동일 (top3 카테고리 + countBucket).

**Layer 5 · 내 위치는 moveTo**:

```kotlin
fun updateMyLocation(lat: Double, lng: Double) {
    val existing = markers[MY_LOCATION_ID]
    if (existing != null) {
        existing.moveTo(LatLng.from(lat, lng))  // Kakao Label API
    } else {
        // 최초 1회만 생성
        val style = LabelStyles.from(LabelStyle.from(getOrCreateBitmap(BitmapKey.MyLocation)))
        // ...
    }
}
```

### 2.5 봉사요청 마커도 동일 클러스터링

`VolunteerMarkerDto` → `MarkerSpec` 변환을 동일한 픽셀 거리 알고리즘에 통과. 클러스터 비트맵은 카테고리 대신 핸드셰이크 아이콘 + count.

### 2.6 카테고리 필터 변경 시 매끄러움

`visibleCategories` 가 바뀌면 `viewportStores` 는 그대로, 필터링 결과만 다르므로 `syncMarkers` diff 가 자연스럽게 사라진 매장만 제거 + 새로 보이는 매장만 추가.

## 3. 영향 파일

```
app/src/main/java/com/example/siheunggagae/
├── MapViewWrapper.kt              ← 거의 재작성
│     · MarkerSpec sealed interface
│     · syncMarkers(prefix, specs) diff API
│     · BitmapKey LruCache
│     · toScreenPoint() 노출
│     · fitMapPoints(latLngs, paddingPx) 추가
│     · updateMyLocation 을 moveTo 기반으로
│     · 클러스터 비트맵 = 아이콘 스택
│     · 개별 비트맵에 selected halo + name chip 분기
│
├── ui/screen/MapScreen.kt
│     · LaunchedEffect 통합: 마커 동기화 1 회 호출
│     · computeViewportMarkers 시그니처: (stores, projector, zoom) → List<MarkerSpec>
│     · 클러스터 onTap = bbox fit
│     · 봉사 마커도 syncMarkers 경로로
│
└── ui/viewmodel/MapViewModel.kt
      · applyViewportResponse() — 동일 응답 short-circuit
```

테스트 파일: `app/src/test/java/com/example/siheunggagae/MarkerClusteringTest.kt` (신규) — `computeViewportMarkers` 알고리즘 단위 테스트 (격자 경계 매장 묶임, 픽셀 80px 임계, zoom < 11 빈 결과, zoom ≥ 16 전부 개별).

## 4. 비기능 요구

- 100 매장 + 클러스터 혼합 viewport 에서 줌 한 단계 변경 시 비트맵 신규 alloc ≤ 5 회 (캐시 hit).
- 마커가 동일 상태로 유지되는 zoom 변경에서 깜빡임 없음 (UX 검증).
- 클러스터 탭 → 카메라 애니메이션 400ms.

## 5. 스코프 제외

- 바텀시트 "주변 매장" 카운트 ↔ viewport 매장 수 불일치 (의미 다름, 별도 결정 필요).
- 마커 비트맵의 DPI 자동 스케일링 (별도 디자인 이슈).
- 클러스터 마커에 selection state (탭 시 사라지므로 불필요).
- 검색 결과·매장 상세 화면의 마커 (이번 스코프는 지도 탭).

## 6. 마이그레이션·롤아웃

- 단일 PR. feature flag 없음 (큰 행동 변화지만 회귀가 즉시 시각으로 검증 가능).
- **API 호환성 (중요)**: `MapViewWrapper` 는 4개 화면이 더 사용 중:
  - `HomeScreen` — `clearMarkersWithPrefix("mini_")`, `addMarker("mini_…", lat, lng, color)`
  - `PlaceDetailScreen` — `addMarker(...)`, `moveCamera`
  - `MatchingPublicDetailScreen` — `clearMarkers()`, `addMarker(...)`, `moveCamera`
  - `MapPinPickerScreen` — `moveCamera`만 사용
  → 기존 시그니처 (`addMarker`, `addClusterMarker`, `clearMarkers`, `clearMarkersWithPrefix`, `updateMyLocation`, `removeMyLocation`) 전부 **유지**. 내부적으로 `MarkerSpec` 기반 구현으로 위임하되 외부 API 는 안 깬다.
  → BitmapCache 와 `MyLocation moveTo` 최적화는 기존 API 호출 처에도 자동 적용 (추가 부수 효과).
  → 신규 `syncMarkers(prefix, specs)` 는 `MapScreen` 에서만 사용. 다른 화면은 단순 use case 라 호환 API 그대로 사용.
- 시각 변경 (개별 핀에 selection halo, 클러스터 아이콘 스택) 은 `MapScreen` 에서만 발화. `HomeScreen` 의 mini map 은 selected 상태 없음·클러스터 안 함 → 시각 변화 없음.
