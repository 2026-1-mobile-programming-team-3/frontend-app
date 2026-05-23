# 펫호텔 매트릭스 비교 — 설계 스펙

작성일: 2026-05-23
작업 범위: 시흥가개 안드로이드 클라이언트 신규 비교 화면 1개. 백엔드 변경 없음. 기존 `feature/pet-hotel-compare` 브랜치 안에 추가.

---

## 1. 목표

기존 `PetHotelCompareScreen`(리스트)에서 한 단계 더 들어가, 펫호텔들을 **매트릭스 형태로 한눈에 비교**하는 화면을 도입한다. 두 모드를 지원:

- **1:1 비교** — 두 드롭다운으로 매장 A·B 자유 선택, 2-컬럼 매트릭스. 결정을 직전에 좁힌 사용자용.
- **여러 곳 비교** — 주변 매장(최대 N개)을 한 매트릭스에 가로 스크롤. 탐색·전반 비교용.

매장 수에 따라 진입 시 자동으로 적절한 모드가 켜진다.

## 2. 백엔드 의존성

신규 호출 없음. 기존 `GET /api/v1/maps/pet-hotels?lat&lng&radius` 응답 그대로 사용. 진입 시 호출.

## 3. 진입점

기존 `PetHotelCompareScreen`(리스트) 의 **TopBar 우측**에 작은 "비교" 아이콘 버튼(40×40dp, 흰 카드 + `ic_sliders` 또는 신규 `ic_compare` 아이콘 + shadow 2dp). 탭 시 `PetHotelMatrixCompare` 라우트로 navigate. 현재 리스트의 lat/lng/radius 그대로 전달.

> **대안**: 리스트 하단 고정 CTA "여러 곳 한눈에 비교 →" 도 가능하지만 스크롤 점유로 inferior. TopBar 아이콘 채택.

매장 수 ≤ 1 이면 버튼 비활성(회색).

## 4. 화면 구성 — PetHotelMatrixCompareScreen

### 4.1 TopBar
- 뒤로가기 카드(40×40dp shadow=2dp) + "펫호텔 비교" 18sp SemiBold
- 우측 빈 영역(나중에 즐겨찾기·공유 같은 추가 액션 자리)

### 4.2 모드 토글 (세그먼티드)
- 컨테이너: `TagGray(#F2F2F2)` bg, radius 50dp, padding 4dp, margin h=18dp v=14dp(하단)
- 두 옵션 라벨: **"1:1 비교" / "여러 곳 비교"**
- 미선택: 13sp SemiBold `Brown700` 배경 투명
- 선택: 13sp ExtraBold `Brown900` bg=White radius=50 shadow 1dp
- fade 120ms 전환

### 4.3 1:1 모드

#### 매장 선택 드롭다운 (2개)
- A 드롭다운 (border 1.5dp `Orange500`) + "VS" 12sp 800 `Placeholder` + B 드롭다운 (border 1.5dp `Pink500`)
- 각 드롭다운 내부: 썸네일 36×36dp radius 10 + 매장명(14sp 800) + 보조라인(11sp `Brown700`): ★별점 + `ic_map_pin` 거리 + "N만원~" + 우측 `ic_chevron_down`
- **썸네일 처리** — 매장 `thumbnailUrl` 우선:
  - `thumbnailUrl` 이 비어있지 않으면 Coil `AsyncImage(contentScale = Crop)` 로 매장 이미지 표시 (radius 10dp clip 적용)
  - `thumbnailUrl` 이 null/blank 또는 로드 실패 시 폴백: A 는 `OrangeSand→Orange500` 그라디언트 + `ic_hotel` 흰색, B 는 `PinkSurface→Pink500` 그라디언트 + `ic_hotel` 흰색
  - 즉 thumbnail이 있으면 항상 매장 이미지가 우선. 폴백은 항상 두 가지 컬러 인덱스(A=주황, B=분홍)로 매장 구분 유지.

#### 드롭다운 동작
- 탭 시 `ModalBottomSheet` (또는 DropdownMenu) 열림 — 현재 매장 목록을 거리순으로 리스트. 각 아이템은 드롭다운 row 와 동일 형태(썸네일 + 매장명 + 보조)
- 선택 시 시트 닫고 해당 매장으로 갱신
- B 드롭다운의 옵션 목록에서는 A 에서 선택한 매장 제외(중복 선택 방지). A 도 동일하게 B 매장 제외.

#### 매트릭스
- 컨테이너: 흰 카드 radius=16dp shadow 2dp margin h=18dp
- 헤더: `OrangeSand` bg, 3컬럼 `(74dp, 1fr, 1fr)`
  - 좌측 "항목" (텍스트 left, `Brown700` 11sp 700)
  - A 컬럼명 (가운데, 색 `Orange500` 11sp 800)
  - B 컬럼명 (가운데, 색 `Pink500` 11sp 800)
- 행 (각 항목):
  - 셀 padding 11dp 4dp, border-bottom `TagGray`, 12sp 600 `TextBlack`
  - 최우값 셀은 `OrangeSand` bg + 좌상단 5×5dp `Orange500` dot + 12sp 800 `Brown900`
- 표시 항목 순서: 별점(+리뷰수 부텍스트) / 거리 / 최저가 / 최고가 / 옵션 수
- 별점 셀은 inline `ic_star`(StarYellow) + 숫자 + 그 아래 작은 회색 "N개" 리뷰

#### 인사이트 카드
- 매트릭스 아래 margin h=18dp
- `OrangeSand` bg radius 12dp padding 12dp 14dp
- 좌측 `ic_lightbulb` 20dp Orange500 + 우측 본문 12sp 600 `Brown900`
- 본문은 두 매장의 우위 영역을 한 줄로 요약 — 예: "**배곧 펫호텔**이 거리·가격에서 유리. **댕댕하우스**는 별점·옵션 수가 더 좋음." (계산 로직은 §6 참조)

#### 상세 진입 버튼
- 매트릭스 카드 아래 두 가로 버튼:
  - A: bg=`OrangeSand` border=1dp `Orange500` text=`Brown900`
  - B: bg=`PinkSurface` border=1dp `Pink500` text=`#9F1239`
- 각 버튼 라벨: "[매장명] 상세 보기"
- 탭 시 `Screen.PlaceDetail.createRoute(storeId)` navigate

### 4.4 여러 곳 모드

#### 서브헤더
- "주변 펫호텔 **N**곳 비교" 12sp `Brown700` (N 800 `TextBlack`) + 우측 `필터` 칩 (`ic_sliders` + 라벨, bg=White border=1dp `BorderBeige` 11sp 700)
- 필터 칩 탭은 현재 스코프 외(후속). 1차에서는 시각 placeholder만.

#### 매트릭스 (가로 스크롤)
- 컨테이너 동일 (radius 16 shadow 2dp)
- 그리드: 좌측 라벨 72dp + 우측 각 매장 컬럼 60dp 씩
- 한 화면 폭(약 320dp 내부)에서 4컬럼 노출, 5번째부터 가로 스크롤 (`HorizontalScroll` 또는 `LazyRow`)
- 5번째 컬럼은 살짝(약 40dp 폭) 보이게 해서 스크롤 힌트
- 헤더 컬럼은 매장명 2줄(`name.take(3) + 줄바꿈 + name.drop(3).take(3)` 류 보정 또는 ellipsize). 4글자 넘으면 자르고 …
- 표시 항목 동일. 최우값 셀 강조 동일 (`OrangeSand` + dot)
- 우하단에 "← 가로로 스와이프 →" 10sp `Placeholder` 600 힌트 (한 번 본 후 SharedPreferences로 숨기는 등 후속 가능)

#### 인사이트
- `OrangeSand` 카드 동일
- 본문: `ic_award`(Orange500) + "최저가 **배곧** · 별점 **댕댕** · 옵션 **댕댕** · 가까움 **배곧**" 형태. 4가지 카테고리(최저가/별점/옵션/거리) 1위 매장명을 굵게.

#### 매장별 상세 진입 (가로 스크롤)
- 매트릭스 아래 매장당 80dp 폭 작은 버튼: bg=White border=1dp `BorderBeige` 11sp 700 `Brown900` "[매장명]"
- 탭 시 PlaceDetail navigate
- 가로 스크롤로 N개 모두 노출

### 4.5 엣지 케이스 / 기본 모드 선택
| 매장 수 | 기본 모드 | 토글 |
|---|---|---|
| 1 이하 | (진입 비활성) | — |
| 2 | 1:1 모드 강제 | 토글 비활성 (회색, 클릭 무반응) |
| 3 이상 | 여러 곳 비교 모드 | 활성 |

#### 모드 전환 시 prefill
- 여러 곳 → 1:1: 매트릭스 1·2번째 매장(현재 정렬 기준 거리순 1·2위)을 1:1 모드의 A·B 로 prefill
- 1:1 → 여러 곳: 별도 처리 없음(매트릭스가 매장 목록 전체를 보여줌)

### 4.6 로딩/에러/빈 상태
- 로딩: 화면 중앙 `CircularProgressIndicator(Orange500)`
- 에러: 중앙 메시지 + 다시 시도 (기존 PetHotelCompareScreen 의 에러 톤 동일)
- 빈 상태: 진입 자체가 매장 1개 이하 시 막혀있으므로 별도 처리 불필요. 만약 도착 후 0개로 응답되면 "주변에 펫호텔이 없어요" + 뒤로가기.

## 5. 데이터 모델

기존 `PetHotelResponse` 그대로 사용. 신규 모델 없음.

## 6. Insight 계산 로직

`ViewModel` 내부 pure function 으로:

```kotlin
data class CompareInsight(
    val cheapestId: Int?,
    val highestRatedId: Int?,
    val mostOptionsId: Int?,
    val nearestId: Int?,
)

internal fun computeInsight(items: List<PetHotelResponse>): CompareInsight = CompareInsight(
    cheapestId    = items.minByOrNull { it.minPriceKrw ?: Int.MAX_VALUE }?.storeId,
    highestRatedId = items.filter { it.ratingAvg != null }.maxByOrNull { it.ratingAvg!! }?.storeId,
    mostOptionsId  = items.maxByOrNull { it.planCount }?.storeId,
    nearestId      = items.filter { it.distanceM != null }.minByOrNull { it.distanceM!! }?.storeId,
)
```

1:1 모드 에서는 두 매장에서 각 우위 부문을 텍스트 한 줄로 변환(빈 카테고리는 생략):
- "[A]는 [거리·가격]에서 유리, [B]는 [별점·옵션]이 더 좋음" 패턴

## 7. ViewModel — PetHotelMatrixCompareViewModel

```kotlin
enum class CompareMode { ONE_ON_ONE, MULTI }

sealed class MatrixCompareUi {
    object Loading : MatrixCompareUi()
    data class Success(
        val items: List<PetHotelResponse>,   // 거리순 정렬
        val mode: CompareMode,
        val selectedA: Int,                  // storeId
        val selectedB: Int,                  // storeId
        val insight: CompareInsight,
    ) : MatrixCompareUi()
    data class Error(val message: String) : MatrixCompareUi()
}

class PetHotelMatrixCompareViewModel(
    private val repository: PetHotelRepository,
    private val initialLat: Double,
    private val initialLng: Double,
    initialRadius: Int = PetHotelCompareViewModel.RADIUS_DEFAULT_M,
) : ViewModel() {
    private val _state = MutableStateFlow<MatrixCompareUi>(MatrixCompareUi.Loading)
    val state: StateFlow<MatrixCompareUi> = _state

    private var raw: List<PetHotelResponse> = emptyList()
    private var radius = initialRadius

    init { fetch() }

    fun setMode(mode: CompareMode) { /* ... */ }
    fun selectA(storeId: Int)       { /* B 와 중복이면 자동 swap */ }
    fun selectB(storeId: Int)       { /* A 와 중복이면 자동 swap */ }
    fun retry()                     = fetch()

    private fun fetch() { /* repository.getNearby → 거리순 정렬 → 모드 자동 선택 → recompute */ }
    private fun recompute() { /* state.value 갱신 */ }

    class Factory(...) : ViewModelProvider.Factory { ... }
}
```

## 8. Navigation

`NavGraph.kt` 의 `Screen` sealed class 에 추가:

```kotlin
object PetHotelMatrixCompare : Screen("pet_hotel_matrix_compare?lat={lat}&lng={lng}&radius={radius}") {
    const val ARG_LAT = "lat"
    const val ARG_LNG = "lng"
    const val ARG_RADIUS = "radius"

    fun createRoute(lat: Double, lng: Double, radius: Int? = null): String {
        val parts = mutableListOf("lat=$lat", "lng=$lng")
        radius?.let { parts += "radius=$it" }
        return "pet_hotel_matrix_compare?" + parts.joinToString("&")
    }
}
```

NavHost composable 블록 + ViewModel factory(`PetHotelMatrixCompareViewModel.Factory`).

## 9. 아이콘

- `ic_chevron_left` (있음, P3 cherry-pick)
- `ic_chevron_down` — **신규 추가 필요** (Lucide `chevron-down`). vector drawable 같은 stroke 2dp 패턴.
- `ic_star` (Material symbols, 있음)
- `ic_map_pin` (있음)
- `ic_hotel` (있음)
- `ic_lightbulb` (있음)
- `ic_sliders` — **신규 추가 필요** (Lucide `sliders-horizontal`)
- `ic_award` — **신규 추가 필요** (Lucide `award`)

총 3개 vector drawable 신규.

## 10. 테스트 전략

- 단위 테스트:
  1. `computeInsight` — 모든 매장에 가격/별점/거리/옵션 있을 때 각 카테고리 1위 매장 ID 정확
  2. `computeInsight` 일부 null — null 값은 비교에서 제외, 모든 매장이 null 이면 해당 카테고리 ID = null
  3. `selectA` 가 현재 B 와 같으면 자동 swap
  4. 진입 시 매장 수 == 2 이면 mode == ONE_ON_ONE 강제

## 11. Gotchas
- Coil 3 AsyncImage 는 placeholder 가 빈 문자열 ("") 일 때 로드 시도해서 에러 흔적 남길 수 있음. `model = url.takeIf { !it.isNullOrBlank() }` 로 가드.
- 5번째 매장이 살짝 보이는 가로 스크롤 힌트는 LazyRow 만으로는 어렵고 컨테이너 우측에 padding-end 또는 첫 매장 padding-start 트릭 필요.
- 1:1 모드 드롭다운에서 매장이 2개뿐일 때 B 후보 매장이 1개라 swap 시 항상 같은 매장으로 돌아옴 — 이 케이스에서는 토글 자체가 1:1 자동(§4.5)이라 사용자가 변경하지 않으므로 문제 없음.
