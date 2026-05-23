# 펫호텔 가격 비교 — 설계 스펙

작성일: 2026-05-22
작업 범위: 시흥가개 안드로이드 클라이언트 신규 화면 1개 + Map 진입 배너 + Canvas 가격 막대 시각화. 백엔드 변경 없음.

---

## 1. 목표

사용자가 주변 펫호텔(`category = PET_HOTEL`)을 **가격순으로 한눈에 비교**할 수 있게 한다. plan_name이 자유 문자열이라 직접 비교가 어려운 점은 집약 지표(최저가 / 가격 범위 / 별점 / 거리)로 대체하고, **Canvas로 그린 가격 범위 막대**로 전체 가격대 위에서 매장별 위치를 시각화한다. 야놀자·여기어때 류 숙박 비교 앱의 친숙한 카드 패턴 + 시흥가개 디자인 토큰을 결합.

리스트·정렬·필터를 1차로 깔고, 1:1 매장 비교는 후속 스코프.

## 2. 백엔드 의존성

### 2.1 기존 엔드포인트 (완료, 그대로 사용)

| 메서드 | 경로 | 용도 |
|---|---|---|
| GET | `/api/v1/maps/pet-hotels?lat&lng&radius` | 주변 PET_HOTEL 매장 + plans + 가격 요약 한 번에 반환. radius 기본 5km, 50건 cap, 거리 오름차순. 인증 불필요. |

응답 스키마 (관련 필드만):
```json
{
  "pet_hotels": [{
    "store_id": 207,
    "name": "배곧 펫호텔",
    "address": "경기도 시흥시 배곧동 ...",
    "latitude": 37.3752, "longitude": 126.7281,
    "distance_m": 320.5,
    "thumbnail_url": "https://...",
    "rating_avg": 4.6, "rating_count": 12,
    "plan_count": 3,
    "min_price_krw": 40000, "max_price_krw": 75000,
    "plans": [
      {"plan_name": "1박 소형견", "price_krw": 40000, "display_order": 0},
      ...
    ]
  }]
}
```

### 2.2 백엔드 변경 요청

**없음.** 모든 집약 지표(min/max, plan_count, rating, distance)가 이미 제공됨.

### 2.3 향후 스코프 외

- 1:1 비교 화면 — 본 스펙에서는 다루지 않음. 비교 후보 매장은 PetHotelCompare 카드에서 선택 모드로 골라 별도 화면으로 띄우는 형태로 후속 설계.
- 펫호텔 외 카테고리 비교 — 카페·식당·공원은 가격 비교 의미가 약해 스코프 외.

## 3. 화면 구성

신규 화면 **1개** + Map 화면 **진입 배너** 1개.

### 3.1 진입점 — MapScreen 비교 배너

`MapScreen.kt`의 카테고리 필터 칩에서 **"펫호텔"이 선택된 상태**일 때만, 지도 영역 상단에 슬라이드인 배너 노출.

```
┌─────────────────────────────────────┐
│  주변 펫호텔 6곳                       │
│  4~9만원 · 가격순 보기    [가격 비교 →]│
└─────────────────────────────────────┘
```

- bg: White, radius 12dp, shadow 4dp 12dp
- 좌상단: 매장명 굵게 + 카운트, 보조 라인 "최저가범위 · 가격순 보기" 10sp Brown700
- 우측 CTA: Brown900 둥근 pill "가격 비교 →" 11sp 700 White
- 표시 조건: 카테고리 필터에서 PET_HOTEL 단독 선택일 때만. 다른 카테고리에서는 숨김.
- 클릭 시 `Screen.PetHotelCompare.createRoute(lat, lng)` 로 navigate (Map의 카메라 중심 좌표 전달).

### 3.2 신규 화면 — PetHotelCompareScreen

#### TopBar
- 뒤로가기 카드(40×40dp shadow=2dp) + "주변 펫호텔 N곳" 18sp SemiBold (N은 동적)
- 우측: 빈 자리 (1:1 비교 진입은 후속)

#### 정렬 칩 row
가로 스크롤, padding h=18 v=8. 칩 14sp Medium, h=16 v=8, radius 50.
- 선택: bg=`#1A1A1A` text=White
- 미선택: bg=White border=1dp `BorderBeige` text=`Brown700`

칩 3개:
- **최저가** (디폴트, 선택)
- **거리**
- **별점**

#### 펫 크기 필터 chip row
가로 스크롤, padding h=18 v=14.
- **전체** (디폴트, 선택)
- **소형** / **중형** / **대형**

펫 크기 필터는 **클라이언트 휴리스틱**으로 분류 — 펫호텔의 `plans[].plan_name` 안에 다음 키워드가 있는지 검사:
- 소형: `소형` | `small` | `S(단독)` | `소`
- 중형: `중형` | `medium` | `M(단독)`
- 대형: `대형` | `large` | `L(단독)` | `대`

키워드 매칭은 case-insensitive. 한 plan이 여러 크기에 동시 매치되면 매치된 모든 크기에 포함. 어느 크기에도 매치되지 않는 plan을 가진 매장은 "전체" 필터에서만 노출.

필터 적용 시 매장 자체는 그 크기에 해당하는 plan을 가진 매장만 보이게 하고, **카드의 최저가 표시는 그 크기 plan들의 최저가**로 동적 변환.

#### 본문 — LazyColumn 카드 리스트

카드 디자인 (시안 v2 B안 — 여기어때형):

```
┌─────────────────────────────────┐
│                                  │
│        [상단 가로 썸네일 140dp]    │
│        (좌상단 "최저가" 뱃지        │
│         조건부, 우상단 ♥)        │
│                                  │
├─────────────────────────────────┤
│  배곧 펫호텔            ★ 4.6 (12) │
│  시흥시 배곧동 · 320m              │
│                                  │
│  [▓▓▓▓▓▓░░░░░░░░░░░░]  ← Canvas │
│   가격 범위 막대 + dot (최저가)     │
│                                  │
│  [3옵션]      최저가 40,000원~     │
└─────────────────────────────────┘
```

상세:
- 카드 White radius 16dp shadow 2dp, margin h=18 v=6.
- **상단 썸네일** (140dp 높이, 풀폭): `Coil AsyncImage(thumbnail_url)` content scale Crop. URL이 null이거나 로드 실패 시 카테고리 그라디언트(`#FFEDD4 → #FEE7EC`) + 중앙 `ic_hotel` 아이콘 폴백.
- **뱃지** (좌상단 8dp inset): 최저가 카드일 때만 `bg=#614B3A text=White "최저가"` 10sp 700 padding h=8 v=3 radius 6.
- **♥ 하트** (우상단 8dp inset): 28×28 White 반투명 원형. `is_favorited`가 true면 `Pink500`, 아니면 `BorderBeige` 색. 탭 시 즐겨찾기 토글 (기존 `/users/me/favorites/stores` API 재사용).
- **정보 영역** (padding 12dp 14dp 14dp):
  - Row1: 매장명 16sp 800 + 우측 별점 ★ 4.6 (12) — 별점 색 StarYellow, "(12)" Brown700
  - Row2: 주소 시 동 + 거리(m/km) 11sp Brown700, 거리는 `formatDistance(meters)` 헬퍼 — 1000m 미만 "320m", 이상 "1.2km" (소수점 1자리)
  - Row3 — **가격 범위 막대** (Canvas):
    - 트랙: 높이 8dp, bg `#F4F4F4`, radius 50
    - 막대: 매장 min~max를 전체 범위(주변 모든 펫호텔의 절대 min ~ 절대 max) 비율로 매핑. bg `linear-gradient(90deg, #FFEDD4, #F7A35B)`, radius 50
    - dot: 최저가 위치에 12×12 `Brown900` 원형 + 2dp White stroke + shadow
    - 막대 길이 0인 경우(min==max, plans 1개) dot만 표시
  - Row4: 좌측 `[3옵션]` OrangeSand pill 10sp 600 + 우측 "최저가 **40,000**원~" — 최저가 숫자 20sp 900 Brown900

- 카드 탭 → `Screen.PlaceDetail.createRoute(storeId)` (기존 매장 상세).

#### 빈 상태
- 80dp OrangeSand 원형 + `ic_hotel` 40dp Orange500
- "주변에 펫호텔이 없어요" 18sp Bold
- "반경을 넓혀 검색해보세요" 13sp Brown700
- "반경 늘리기" Brown900 CTA → radius +5km 증가 후 재요청. 최대 50km(`pet-hotels` API 상한).

#### 로딩
- 화면 중앙 CircularProgressIndicator Orange500
- 또는 첫 진입 시 빈 카드 3개 skeleton (이번 스코프에서는 단순 indicator로)

#### 에러
- 중앙 메시지 14sp Brown700 + "다시 시도" TextButton
- 메시지 패턴: "주변 펫호텔을 불러오지 못했어요"

## 4. 데이터 모델

`app/src/main/java/com/example/siheunggagae/data/model/PetHotelModels.kt` 신규.

```kotlin
import com.google.gson.annotations.SerializedName

data class PetHotelPlan(
    @SerializedName(value = "plan_name", alternate = ["planName"])
    val planName: String,
    @SerializedName(value = "price_krw", alternate = ["priceKrw"])
    val priceKrw: Int,
    @SerializedName(value = "display_order", alternate = ["displayOrder"])
    val displayOrder: Int? = null,
)

data class PetHotelResponse(
    @SerializedName(value = "store_id", alternate = ["storeId", "id"])
    val storeId: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName(value = "distance_m", alternate = ["distanceM"])
    val distanceM: Double?,
    @SerializedName(value = "thumbnail_url", alternate = ["thumbnailUrl"])
    val thumbnailUrl: String? = null,
    @SerializedName(value = "rating_avg", alternate = ["ratingAvg"])
    val ratingAvg: Double? = null,
    @SerializedName(value = "rating_count", alternate = ["ratingCount"])
    val ratingCount: Int = 0,
    @SerializedName(value = "is_favorited", alternate = ["isFavorited"])
    val isFavorited: Boolean = false,
    @SerializedName(value = "plan_count", alternate = ["planCount"])
    val planCount: Int = 0,
    @SerializedName(value = "min_price_krw", alternate = ["minPriceKrw"])
    val minPriceKrw: Int? = null,
    @SerializedName(value = "max_price_krw", alternate = ["maxPriceKrw"])
    val maxPriceKrw: Int? = null,
    val plans: List<PetHotelPlan> = emptyList(),
)

data class PetHotelListResponse(
    @SerializedName(value = "pet_hotels", alternate = ["petHotels"])
    val petHotels: List<PetHotelResponse> = emptyList(),
)
```

## 5. Navigation Routes

`NavGraph.kt`의 `Screen` sealed class에 추가:

```kotlin
object PetHotelCompare : Screen("pet_hotel_compare?lat={lat}&lng={lng}&radius={radius}") {
    const val ARG_LAT = "lat"
    const val ARG_LNG = "lng"
    const val ARG_RADIUS = "radius"

    fun createRoute(lat: Double? = null, lng: Double? = null, radius: Int? = null): String {
        val parts = buildList {
            lat?.let { add("lat=$it") }
            lng?.let { add("lng=$it") }
            radius?.let { add("radius=$it") }
        }
        return if (parts.isEmpty()) "pet_hotel_compare" else "pet_hotel_compare?" + parts.joinToString("&")
    }
}
```

NavHost에 composable 블록 추가. 인자는 lat/lng를 `StringType` nullable (Picker 패턴과 동일), radius는 `IntType` defaultValue=5000.

## 6. Repository · API

`AuthApiService.kt`에 추가:

```kotlin
@GET("api/v1/maps/pet-hotels")
suspend fun getPetHotels(
    @Query("lat") lat: Double,
    @Query("lng") lng: Double,
    @Query("radius") radius: Int = 5000,
): Response<PetHotelListResponse>
```

`data/repository/PetHotelRepository.kt` 신규:

```kotlin
class PetHotelRepository {
    private val api = RetrofitClient.api

    suspend fun getNearby(
        lat: Double,
        lng: Double,
        radius: Int = 5000,
    ): Response<PetHotelListResponse> = api.getPetHotels(lat, lng, radius)
}
```

## 7. ViewModel

`PetHotelCompareViewModel.kt`:

```kotlin
enum class CompareSortAxis { PRICE, DISTANCE, RATING }
enum class PetSize { ALL, SMALL, MEDIUM, LARGE }

sealed class PetHotelCompareUi {
    object Loading : PetHotelCompareUi()
    data class Success(
        val items: List<PetHotelResponse>,
        val absMinPrice: Int,        // 전체 매장 절대 min (Canvas 막대 스케일링용)
        val absMaxPrice: Int,        // 전체 매장 절대 max
        val sort: CompareSortAxis,
        val size: PetSize,
        val radius: Int,
    ) : PetHotelCompareUi()
    data class Error(val message: String) : PetHotelCompareUi()
}

class PetHotelCompareViewModel(
    private val repository: PetHotelRepository,
    private val initialLat: Double,
    private val initialLng: Double,
) : ViewModel() {
    private val _state = MutableStateFlow<PetHotelCompareUi>(PetHotelCompareUi.Loading)
    val state: StateFlow<PetHotelCompareUi> = _state

    private var sort: CompareSortAxis = CompareSortAxis.PRICE
    private var size: PetSize = PetSize.ALL
    private var radius: Int = 5000
    private var raw: List<PetHotelResponse> = emptyList()

    init { fetch() }

    fun setSort(axis: CompareSortAxis) { sort = axis; recompute() }
    fun setSize(s: PetSize) { size = s; recompute() }
    fun expandRadius() {
        radius = (radius + 5000).coerceAtMost(50000)
        fetch()
    }
    fun retry() = fetch()

    private fun fetch() {
        viewModelScope.launch {
            _state.value = PetHotelCompareUi.Loading
            val resp = repository.getNearby(initialLat, initialLng, radius)
            if (resp.isSuccessful) {
                raw = resp.body()?.petHotels.orEmpty()
                recompute()
            } else {
                _state.value = PetHotelCompareUi.Error("주변 펫호텔을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    private fun recompute() {
        val filtered = applySizeFilter(raw, size)
        val sorted = applySort(filtered, sort)
        val absMin = filtered.mapNotNull { it.minPriceKrw }.minOrNull() ?: 0
        val absMax = filtered.mapNotNull { it.maxPriceKrw }.maxOrNull() ?: absMin
        _state.value = PetHotelCompareUi.Success(
            items = sorted,
            absMinPrice = absMin,
            absMaxPrice = absMax,
            sort = sort,
            size = size,
            radius = radius,
        )
    }

    private fun applySort(items: List<PetHotelResponse>, axis: CompareSortAxis) = when (axis) {
        CompareSortAxis.PRICE    -> items.sortedBy { it.minPriceKrw ?: Int.MAX_VALUE }
        CompareSortAxis.DISTANCE -> items.sortedBy { it.distanceM ?: Double.MAX_VALUE }
        CompareSortAxis.RATING   -> items.sortedByDescending { it.ratingAvg ?: 0.0 }
    }

    private fun applySizeFilter(items: List<PetHotelResponse>, s: PetSize): List<PetHotelResponse> {
        if (s == PetSize.ALL) return items
        return items.filter { hotel -> hotel.plans.any { matchesSize(it.planName, s) } }
    }
}

internal fun matchesSize(planName: String, size: PetSize): Boolean {
    val lower = planName.lowercase()
    return when (size) {
        PetSize.ALL    -> true
        PetSize.SMALL  -> "소형" in planName || "small" in lower || Regex("\\b[sS]\\b").containsMatchIn(planName)
        PetSize.MEDIUM -> "중형" in planName || "medium" in lower || Regex("\\b[mM]\\b").containsMatchIn(planName)
        PetSize.LARGE  -> "대형" in planName || "large" in lower || Regex("\\b[lL]\\b").containsMatchIn(planName)
    }
}
```

Factory 내부 클래스로 lat/lng/radius 주입.

## 8. Canvas 가격 막대 — 시각화 구현

`PriceRangeBar` Composable (Screen 파일 안에 private):

```kotlin
@Composable
private fun PriceRangeBar(
    storeMin: Int,
    storeMax: Int,
    absMin: Int,
    absMax: Int,
    modifier: Modifier = Modifier,
) {
    val track = Color(0xFFF4F4F4)
    val gradient = Brush.horizontalGradient(listOf(Color(0xFFFFEDD4), Color(0xFFF7A35B)))
    val dotColor = Color(0xFF614B3A)
    val range = (absMax - absMin).coerceAtLeast(1)
    val startFrac = ((storeMin - absMin).toFloat() / range).coerceIn(0f, 1f)
    val endFrac = ((storeMax - absMin).toFloat() / range).coerceIn(0f, 1f)

    Canvas(modifier = modifier.height(8.dp).fillMaxWidth()) {
        val h = size.height
        val w = size.width
        val cornerRadius = h / 2f
        // 트랙
        drawRoundRect(
            color = track,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            size = Size(w, h),
        )
        // 가격 막대
        val barLeft = startFrac * w
        val barWidth = (endFrac - startFrac) * w
        if (barWidth > 0f) {
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(barLeft, 0f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            )
        }
        // 최저가 dot
        val dotRadius = h * 0.85f
        drawCircle(
            color = Color.White,
            radius = dotRadius + 1.5.dp.toPx(),
            center = Offset(barLeft, h / 2f),
        )
        drawCircle(
            color = dotColor,
            radius = dotRadius,
            center = Offset(barLeft, h / 2f),
        )
    }
}
```

전체 가격대(absMin~absMax)는 ViewModel의 `Success` 상태에서 가져옴. 매장이 1곳뿐이거나 모든 매장이 같은 가격이면 막대 길이 0 — dot만 표시.

> **외부 라이브러리 사용 안 함** — Compose Canvas로 충분. minSdk 35 / Compose 1.x 호환, 패키지 사이즈 영향 0.

## 9. Map 진입 배너 통합

`MapScreen.kt` 카테고리 필터 칩 영역 아래(또는 지도 상단 오버레이로) 조건부 배너 추가.

- MapScreen ViewModel(`MapViewModel`)의 카테고리 필터 상태 관찰:
  - 선택된 카테고리가 **PET_HOTEL 한 개만** 일 때 배너 표시 (다른 카테고리 같이 켜져 있으면 숨김).
  - 별도 API 호출 없이 이미 가져온 viewport 결과의 카테고리 = PET_HOTEL 매장 카운트와 minPrice/maxPrice를 보여줘도 되지만, 배너 자체에 그 숫자 부정확해도 OK — "주변 펫호텔 N곳"만으로 충분.
- 배너 클릭 → `onNavigate(Screen.PetHotelCompare.createRoute(lat = currentCameraLat, lng = currentCameraLng))`

배너 컴포저블은 `MapScreen.kt` 내부 private function — 외부 화면이 아니라 같은 파일 안에서 응집.

## 10. 즐겨찾기 토글

카드 우상단 ♥ 탭 시:
- 기존 `POST /users/me/favorites/stores` (등록) / `DELETE /users/me/favorites/stores/{storeId}` (해제) 호출
- ViewModel에 `toggleFavorite(storeId, currentlyFavorited)` 추가 — 호출 결과로 raw 리스트의 해당 매장 `isFavorited` 갱신 후 `recompute()`
- 비로그인 시 ♥ 보이지 않음 (또는 회색·비활성). 비로그인 판단은 기존 `TokenManager` 패턴 따라 `myUserId` 같은 변수로.

## 11. 에러 처리

| 상황 | 처리 |
|---|---|
| 401 (토큰 만료) | 기존 `TokenAuthenticator` 위임 |
| 5xx / Network | "주변 펫호텔을 불러오지 못했어요" + "다시 시도" |
| 빈 응답 | 빈 상태 UI ("주변에 펫호텔이 없어요" + 반경 늘리기) |
| 위치 권한 없음 | 시흥시청 좌표(37.3799, 126.8030) 폴백 + 안내 토스트 "위치 권한이 없어 기본 위치로 검색합니다" |

## 12. 빈/로딩/에러 상태

`PetHotelCompareUi`가 3-state sealed로 그대로 매핑. 별도 추가 상태 없음.

## 13. 테스트 전략

- 단위 테스트:
  1. `matchesSize(planName, size)` — 소형/중형/대형/대소문자/약자(S/M/L)·한글·영어 케이스 (이미 spec에 케이스 다양함)
  2. `applySort` 3가지 축 — null 가격/거리/별점은 끝으로 가는지
  3. `applySizeFilter` — ALL은 그대로, 소형 필터는 소형 plan 1개라도 있는 매장만
- 통합/UI: manual smoke test
  - Map → 펫호텔 필터 → 배너 클릭 → CompareScreen 진입
  - 정렬 칩 토글 시 즉시 재정렬 (API 재호출 없음 — 클라이언트 sort)
  - 크기 칩 토글 시 매장 수 + 최저가 동적 변환
  - 빈 상태에서 "반경 늘리기" → 새 API 호출

## 14. 라이센스·외부 의존

신규 외부 라이브러리 **0**. 기존 Compose / Retrofit / Coil / Kakao SDK 의존만 사용.

## 15. 작업 단위 (구현 시 분리할 PR 후보)

상세는 writing-plans 스킬에서 도출. 대략:
1. Data model + API + Repository + matchesSize 단위 테스트
2. PetHotelCompareViewModel + 단위 테스트(sort/filter)
3. PetHotelCompareScreen + Canvas PriceRangeBar
4. NavGraph 라우트 + MapScreen 배너 통합
5. 즐겨찾기 토글 + 빈 상태 / 에러 처리 / QA

## 16. Gotchas

- 펫 크기 매칭은 휴리스틱이라 plan_name 패턴이 매우 자유로우면 누락 가능. UX 결정: "소형/중형/대형 필터에 안 잡히면 그 매장은 그 필터에서 숨김" (보수적). 사용자가 plan_name을 "Pet 소형 1박" 같이 띄어쓰기·접두 변형으로 적으면 매칭 OK. 차후 필요 시 매장 등록 폼에 "이 plan의 펫 크기" 메타 필드 추가하면 휴리스틱 제거 가능.
- Canvas 가격 막대의 absMin/absMax는 **현재 필터 적용 후** 매장들 기준. 크기 필터 토글 시 막대 스케일도 같이 변함 — 이건 의도 (필터 컨텍스트 안에서 비교).
- thumbnail_url이 placeholder URL(stub)이면 Coil은 로드 실패 → 폴백 그라디언트. 정상 동작.
- 배너 노출 조건은 "PET_HOTEL 단독 선택" — 사용자가 펫호텔 + 다른 카테고리 동시 선택 시 숨김. MapScreen의 카테고리 multi-select 패턴 그대로 따름.
