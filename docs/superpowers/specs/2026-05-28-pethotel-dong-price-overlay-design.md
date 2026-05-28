# 펫호텔 동(洞) 단위 가격 오버레이 — 디자인 스펙

**Date:** 2026-05-28
**Status:** Draft, awaiting implementation plan
**Scope:** `MapScreen` (PET_HOTEL 카테고리 한정)

## 1. 배경 & 목표

부동산 앱(직방/네이버 부동산)처럼, 지도를 줌아웃한 상태에서 시흥시 행정동 단위로 펫호텔 가격대를 한눈에 비교할 수 있게 한다.

**목표:**
- 사용자가 펫호텔 카테고리에서 줌아웃했을 때, 개별 마커 대신 동별 가격 버블을 본다
- 가격 비교가 "지도를 보면서" 가능 — `PetHotelCompareScreen`으로 이동하지 않아도 가격대를 파악
- 버블을 탭하면 해당 동으로 줌인하면서 개별 호텔 마커로 자연스럽게 드릴다운

**비목표:**
- 새 화면 추가 없음 (기존 MapScreen에 오버레이 레이어 1개 추가)
- 백엔드 수정 없음
- 다른 카테고리(카페/병원 등)로 일반화하지 않음 (가격은 펫호텔에만 있는 개념)

## 2. 트리거 & 줌 전환

**활성 조건:** `selectedCategory == PET_HOTEL`일 때만.

**줌 임계값:**
- `currentZoom ≤ 13` → 동 버블만 표시, 개별 펫호텔 마커 숨김
- `currentZoom ≥ 14` → 개별 펫호텔 마커만, 동 버블 숨김
- 히스테리시스 없음 — Kakao 줌 레벨이 정수라 사용자가 한 칸씩 의도적으로 움직여 깜빡임 위험 낮음

**칩 전환 시:** PET_HOTEL → 다른 카테고리로 바뀌면 `syncMarkers("dong", emptyList())`로 즉시 제거.

**기존 "주변 펫호텔 N곳" 배너:** `currentZoom >= 14`일 때만 노출 (줌아웃 상태에서는 동 버블이 동일 정보를 제공하므로 중복 제거).

## 3. 데이터 흐름

```
사용자가 PET_HOTEL 칩 선택
        ↓
MapViewModel.loadPetHotels()
   ↓ GET /api/v1/maps/pet-hotels?lat=37.3799&lng=126.8030&radius=15000
List<PetHotelResponse> → uiState.petHotels (캐시)
        ↓
DongAggregator.aggregate(petHotels, SiheungRegions.dongCoordinates)
        ↓
List<DongPriceBucket> → uiState.dongBuckets
        ↓
MapScreen LaunchedEffect (currentZoom, selectedCategory, dongBuckets)
        ↓
zoom ≤ 13 && PET_HOTEL → MarkerSpec.DongBubble 리스트로 변환 → syncMarkers("dong", specs)
zoom ≥ 14 || !PET_HOTEL → syncMarkers("dong", emptyList())
```

**Fetch 시점:** PET_HOTEL 칩이 *처음* 선택될 때 1회. 카테고리가 다른 곳으로 바뀌었다가 다시 PET_HOTEL로 돌아오면 캐시 재사용 (재호출 안 함).

**재호출 트리거:** "새로고침" FAB만. 기존 `MapViewModel.refresh()`가 현재 카테고리에 따라 분기하도록 수정.

**중심점/반경 선택:** 시흥시청(37.3799, 126.8030) 기준 15km. 시흥시 전 영역(대략 12km 직경) 포함하고도 여유. 시흥 외 호텔이 일부 섞일 수 있으나 동 매핑 단계에서 최근접 시흥 동에 할당되므로 노이즈는 동 버킷 가격에 살짝 섞이는 정도 — v1 수용 가능.

**캐시 TTL:** 없음. 화면 재진입 시 새 ViewModel이 생성되므로 자동 갱신.

## 4. 동 매핑

**알고리즘:** 각 펫호텔 좌표 → `SiheungRegions.dongCoordinates`(18개 동 중심점) 중 가장 가까운 동에 할당. Haversine 거리로 충분 (시흥은 위경도 1도 차이가 안 나는 좁은 영역이라 평면 근사도 가능하지만 명확성을 위해 Haversine).

**경계 정확도:** 폴리곤이 아니므로 동 경계 근처 호텔은 행정구역과 다르게 분류될 수 있음. v1에선 수용. 추후 개선 옵션:
1. `address` 필드에서 "○○동" 정규식 추출 (도로명주소 케이스로 누락 위험)
2. 백엔드에 dong 필드 추가
3. 시흥시 행정동 폴리곤 GeoJSON 번들링

**Null 가격 처리:** `min_price_krw == null` (플랜 0개) 호텔은 aggregation에서 제외. 동의 모든 호텔이 null이면 그 동의 버킷 자체가 생성되지 않음 → 버블 미노출.

## 5. 컴포넌트 설계

### 5.1 신규 파일

**`app/src/main/java/com/example/siheunggagae/data/model/DongPriceBucket.kt`**
```kotlin
data class DongPriceBucket(
    val dong: String,          // "신천동"
    val lat: Double,           // 동 중심 좌표
    val lng: Double,
    val count: Int,            // 동 내 호텔 수 (가격 정보 있는 것만)
    val minKrw: Int,           // 동 내 최저 가격 (전 호텔 min_price_krw의 min)
    val maxKrw: Int,           // 동 내 최고 가격
)
```

**`app/src/main/java/com/example/siheunggagae/map/DongAggregator.kt`**
```kotlin
object DongAggregator {
    fun aggregate(
        hotels: List<PetHotelResponse>,
        dongCenters: Map<String, Pair<Double, Double>>,
    ): List<DongPriceBucket>

    fun Int.toManwon(): String  // 53000 → "5.3", 50000 → "5"
}
```

순수 함수, 의존성 없음. 단위 테스트 대상.

### 5.2 기존 파일 수정

**`map/MarkerClustering.kt` — `MarkerSpec` sealed interface에 variant 추가**
```kotlin
sealed interface MarkerSpec {
    // 기존 Single, Cluster 유지
    data class DongBubble(
        val id: String,            // "dong_신천동"
        val lat: Double,
        val lng: Double,
        val dongName: String,
        val count: Int,
        val minKrw: Int,
        val maxKrw: Int,
        val onTap: (() -> Unit)? = null,
    ) : MarkerSpec {
        override val visualKey = Triple(dongName, count, minKrw to maxKrw)
    }
}
```

`computeMarkerSpecs()`는 건드리지 않음 — 동 버블은 별도 변환 함수에서 처리.

**`MapViewWrapper.kt`**

- `BitmapKey`에 `DongBubble(dongName: String, count: Int, minKrw: Int, maxKrw: Int)` variant 추가
- `addSpecInternal()`에 `is MarkerSpec.DongBubble` 분기 추가
- `createDongBubbleBitmap(dongName, count, minKrw, maxKrw)` 신설:
  - 흰 배경, RoundedCornerShape(14dp), 1.5dp 베이지(`#E8D3C2`) 보더
  - 그림자 (alpha 0.20 brown, blur ~14)
  - 좌상단: 동명 12sp Bold `#1E120A`
  - 우상단: "${count}곳" 10sp SemiBold `#8A6E58`
  - 아래 라인: "${min}~${max}만" 또는 "${val}만" (min==max) 14sp ExtraBold `#614B3A`
  - 하단 중앙 7px 삼각형 tail (앵커 표시)
  - min-width 96px

**`ui/viewmodel/MapViewModel.kt`**

- `MapUiState`에 추가:
  ```kotlin
  val petHotels: List<PetHotelResponse> = emptyList(),
  val dongBuckets: List<DongPriceBucket> = emptyList(),
  ```
- `selectCategory(PET_HOTEL)` 호출 시 `petHotels.isEmpty()`면 `loadPetHotels()` → `DongAggregator.aggregate()` → uiState 업데이트
- `refresh()` 분기: PET_HOTEL 카테고리면 `loadPetHotels()`도 함께 재호출
- private `suspend fun loadPetHotels()` 신설 — 시흥시청 기준 15km, 실패 시 emptyList 유지

**`ui/screen/MapScreen.kt`**

- 새 `LaunchedEffect(mapReady, uiState.selectedCategory, uiState.currentZoom, uiState.dongBuckets)`:
  ```kotlin
  if (!mapReady) return@LaunchedEffect
  val showBubbles = uiState.selectedCategory == StoreCategory.PET_HOTEL && uiState.currentZoom <= 13
  if (!showBubbles) {
      mapWrapper.syncMarkers("dong", emptyList())
      return@LaunchedEffect
  }
  val specs = uiState.dongBuckets.map { bucket ->
      MarkerSpec.DongBubble(
          id = "dong_${bucket.dong}",
          lat = bucket.lat, lng = bucket.lng,
          dongName = bucket.dong, count = bucket.count,
          minKrw = bucket.minKrw, maxKrw = bucket.maxKrw,
          onTap = { mapWrapper.animateCamera(bucket.lat, bucket.lng, zoomLevel = 16) },
      )
  }
  mapWrapper.syncMarkers("dong", specs)
  ```
- 기존 viewport store 마커 sync (`LaunchedEffect(mapReady, uiState.viewportStores, ...)`): PET_HOTEL 모드 + 줌 ≤ 13 조건일 때 펫호텔 카테고리만 필터에서 추가로 제외 (다른 카테고리 마커는 영향 없음)
- 기존 PetHotelCompareBanner의 `AnimatedVisibility` 조건에 `&& uiState.currentZoom >= 14` 추가

## 6. 비주얼 디테일 (Brown 카드)

| 속성 | 값 |
|---|---|
| Shape | RoundedCornerShape 14dp |
| Background | `Color.White` (#FEFEFE 무관, 명시적 White) |
| Border | 1.5dp `#E8D3C2` (BorderBeige) |
| Shadow | `Paint.setShadowLayer(radius=14f, dx=0f, dy=4f, color=0x33614B3A)` — bitmap에 베이크 (Kakao Label은 elevation 미지원) |
| Padding | h=12dp, v=7dp |
| Min width | 96px (canvas pixel — 기존 cluster bitmap 사이즈 컨벤션 따름) |
| 동명 | 12sp Bold `#1E120A` (TextBlack) |
| 개수 | 10sp SemiBold `#8A6E58` (Brown700) |
| 가격 | 14sp ExtraBold `#614B3A` (Brown900), letter-spacing -0.4sp |
| Tail | 7px 삼각형 아래 방향, white, drop-shadow 0/2/1 brown alpha 0.15 |
| 가격 포맷 | `min~max만` (만원 단위 반올림, 천 단위까지). min==max면 단일 표시 |

만원 변환 규칙 (`Int.toManwon()`):
- 53000 → "5.3"
- 50000 → "5" (trailing .0 제거)
- 120000 → "12"
- 125000 → "12.5"
- 1000 → "0.1"
- 0 → "0"

## 7. 엣지 케이스

| 케이스 | 처리 |
|---|---|
| 동에 펫호텔 0곳 | 버킷 미생성 → 버블 없음 |
| 동에 펫호텔 1곳 + 가격 1개 | 버블 표시, "5.3만" 단일 가격 |
| 호텔의 min_price_krw가 null | aggregation에서 스킵 |
| 동의 모든 호텔이 null 가격 | 버킷 미생성 |
| /maps/pet-hotels 실패 | silent fail, dongBuckets = emptyList, 버블 없음 |
| 줌 빠르게 변경 | 기존 syncMarkers의 visualKey 기반 diff로 no-op 흡수 |
| GPS가 시흥 밖 | 호출 좌표는 시흥시청 고정이라 무관 |
| 동 좌표가 화면 밖 | Kakao Label이 자동으로 안 그림 |

## 8. 테스트

**신규 단위 테스트:** `app/src/test/java/com/example/siheunggagae/DongAggregatorTest.kt`

- `aggregate_emptyInput_returnsEmpty`
- `aggregate_singleHotel_assignedToNearestDong_countOne_minEqualsMax`
- `aggregate_twoHotelsInDifferentDongs_returnsTwoBuckets`
- `aggregate_threeHotelsInSameDong_aggregatesMinMax`
- `aggregate_hotelWithNullMinPrice_isSkipped`
- `aggregate_allHotelsNullPrice_bucketNotCreated`
- `toManwon_53000_returns5dot3`
- `toManwon_50000_returns5_noTrailingDecimal`
- `toManwon_120000_returns12`
- `toManwon_125000_returns12dot5`
- `toManwon_1000_returns0dot1`
- `toManwon_0_returns0`

**UI/시각 테스트:** 없음. Preview composable로 디자인 확인 (`createDongBubbleBitmap` 결과를 Image로 감싼 `@Preview` 1개).

## 9. 백엔드 변경

**없음.**

## 10. 향후 개선 옵션 (out of scope)

1. 동 경계 폴리곤 GeoJSON 번들링 → 정확한 행정구역 매핑
2. 백엔드 `/maps/pet-hotels/by-dong` 엔드포인트 (페이지뷰 늘면 가성비 ↑)
3. 다른 카테고리 일반화 (예: 그루밍 가격대) — 현재 ratingAvg/price 데이터가 카테고리별로 다른 구조라 별도 설계 필요
4. 동 버블 탭 시 줌인 + 바텀시트가 해당 동 호텔 리스트로 자동 필터 (현재 spec은 줌인만)

## 11. 영향 받는 파일 요약

| 파일 | 변경 유형 |
|---|---|
| `data/model/DongPriceBucket.kt` | 신규 |
| `map/DongAggregator.kt` | 신규 |
| `test/.../DongAggregatorTest.kt` | 신규 |
| `map/MarkerClustering.kt` | `MarkerSpec.DongBubble` variant 추가 |
| `MapViewWrapper.kt` | `BitmapKey.DongBubble`, `createDongBubbleBitmap()`, `addSpecInternal()` 분기 |
| `ui/viewmodel/MapViewModel.kt` | `petHotels`, `dongBuckets` 상태 + `loadPetHotels()` + `selectCategory`/`refresh` 분기 |
| `ui/screen/MapScreen.kt` | 새 LaunchedEffect (dong sync), 기존 store sync에 PET_HOTEL 필터링, 기존 배너 조건에 zoom ≥ 14 추가 |
