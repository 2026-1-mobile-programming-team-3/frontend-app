# 펫호텔 가격 비교 — 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 사용자가 주변 펫호텔을 가격순으로 한눈에 비교할 수 있는 신규 화면을 도입한다 — 여기어때형 카드(상단 가로 썸네일) + Canvas 가격 범위 막대 + 정렬·필터 + Map의 펫호텔 필터 시 진입 배너.

**Architecture:** 백엔드는 이미 `GET /api/v1/maps/pet-hotels`로 모든 집약 지표(min/max 가격, 별점, 거리, plans[])를 한 번에 제공. 클라이언트는 단일 API 호출 후 정렬·필터·시각화만 담당. 외부 차트 라이브러리 없음 — Compose Canvas로 가격 범위 막대 직접 그림.

**Tech Stack:** Kotlin, Jetpack Compose, Retrofit2 + Gson, Coil 3, JUnit. 모든 DTO는 기존 코드베이스 패턴인 camelCase Kotlin + `@SerializedName(value="snake_case", alternate=["camelCase"])`.

**Spec:** `docs/superpowers/specs/2026-05-22-pethotel-compare-design.md`

**Branch:** `feature/pet-hotel-compare` (main에서 분기, spec commit `1b72fa4` 포함)

---

## File Structure

### 신규 파일

```
app/src/main/java/com/example/siheunggagae/
├── data/
│   ├── model/
│   │   └── PetHotelModels.kt              # PetHotelPlan, PetHotelResponse, PetHotelListResponse
│   └── repository/
│       └── PetHotelRepository.kt           # API 메서드 thin wrapper
└── ui/
    ├── screen/
    │   └── PetHotelCompareScreen.kt        # 화면 + PriceRangeBar + 카드 + 정렬·필터 칩
    └── viewmodel/
        └── PetHotelCompareViewModel.kt     # 상태 + sort/filter 로직 + matchesSize 함수

app/src/test/java/com/example/siheunggagae/
└── PetHotelCompareLogicTest.kt             # matchesSize, applySort, applySizeFilter 단위 테스트
```

### 수정 파일

| 파일 | 변경 |
|---|---|
| `app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt` | `getPetHotels(lat, lng, radius): Response<PetHotelListResponse>` 메서드 1개 |
| `app/src/main/java/com/example/siheunggagae/NavGraph.kt` | `Screen.PetHotelCompare` 객체 + composable 블록 |
| `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt` | 펫호텔 카테고리 단독 선택 시 비교 배너 + 콜백 |

---

## Task 1 — Data Model

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/data/model/PetHotelModels.kt`
- Test: `app/src/test/java/com/example/siheunggagae/PetHotelModelsTest.kt`

- [ ] **Step 1.1: 모델 파일 생성**

Create `app/src/main/java/com/example/siheunggagae/data/model/PetHotelModels.kt`:

```kotlin
package com.example.siheunggagae.data.model

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
    val distanceM: Double? = null,
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

- [ ] **Step 1.2: 직렬화 단위 테스트**

Create `app/src/test/java/com/example/siheunggagae/PetHotelModelsTest.kt`:

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.data.model.PetHotelListResponse
import com.example.siheunggagae.data.model.PetHotelResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PetHotelModelsTest {
    private val gson = Gson()

    @Test
    fun deserializes_snake_case_payload() {
        val json = """
            {
              "pet_hotels": [{
                "store_id": 207,
                "name": "배곧 펫호텔",
                "address": "경기도 시흥시 배곧동",
                "latitude": 37.3752, "longitude": 126.7281,
                "distance_m": 320.5,
                "thumbnail_url": "https://example.com/x.jpg",
                "rating_avg": 4.6, "rating_count": 12,
                "is_favorited": false,
                "plan_count": 3,
                "min_price_krw": 40000, "max_price_krw": 75000,
                "plans": [
                  {"plan_name":"1박 소형견","price_krw":40000,"display_order":0},
                  {"plan_name":"1박 중형견","price_krw":55000,"display_order":1},
                  {"plan_name":"1박 대형견","price_krw":75000,"display_order":2}
                ]
              }]
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, PetHotelListResponse::class.java)
        assertEquals(1, parsed.petHotels.size)
        val h = parsed.petHotels[0]
        assertEquals(207, h.storeId)
        assertEquals(320.5, h.distanceM!!, 0.01)
        assertEquals("https://example.com/x.jpg", h.thumbnailUrl)
        assertEquals(40000, h.minPriceKrw)
        assertEquals(75000, h.maxPriceKrw)
        assertEquals(3, h.plans.size)
        assertEquals("1박 소형견", h.plans[0].planName)
        assertEquals(40000, h.plans[0].priceKrw)
    }

    @Test
    fun missing_optional_fields_default() {
        val json = """
            {
              "pet_hotels": [{
                "store_id": 1, "name": "X", "address": "Y",
                "latitude": 0.0, "longitude": 0.0
              }]
            }
        """.trimIndent()
        val h = gson.fromJson(json, PetHotelListResponse::class.java).petHotels[0]
        assertNull(h.distanceM)
        assertNull(h.thumbnailUrl)
        assertEquals(0, h.ratingCount)
        assertEquals(false, h.isFavorited)
        assertEquals(emptyList<Any>(), h.plans)
    }
}
```

- [ ] **Step 1.3: 테스트 실행**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.PetHotelModelsTest"
```

Expected: 2 tests passed.

- [ ] **Step 1.4: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/data/model/PetHotelModels.kt \
        app/src/test/java/com/example/siheunggagae/PetHotelModelsTest.kt
git commit -m "feat: PetHotel DTO 모델 추가"
```

---

## Task 2 — API Service + Repository

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt`
- Create: `app/src/main/java/com/example/siheunggagae/data/repository/PetHotelRepository.kt`

- [ ] **Step 2.1: AuthApiService 메서드 추가**

Open `AuthApiService.kt`. 다른 `maps/` 엔드포인트들 근처(예: `getPetHotels` 자리 — `submitStoreRequest`나 `geocode/reverse` 인근)에 다음 추가:

```kotlin
@GET("api/v1/maps/pet-hotels")
suspend fun getPetHotels(
    @Query("lat") lat: Double,
    @Query("lng") lng: Double,
    @Query("radius") radius: Int = 5000,
): Response<PetHotelListResponse>
```

상단 import에 신규 DTO 추가:

```kotlin
import com.example.siheunggagae.data.model.PetHotelListResponse
```

- [ ] **Step 2.2: Repository 생성**

Create `app/src/main/java/com/example/siheunggagae/data/repository/PetHotelRepository.kt`:

```kotlin
package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.model.PetHotelListResponse
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class PetHotelRepository {
    private val api = RetrofitClient.api

    suspend fun getNearby(
        lat: Double,
        lng: Double,
        radius: Int = 5000,
    ): Response<PetHotelListResponse> = api.getPetHotels(lat, lng, radius)
}
```

- [ ] **Step 2.3: 컴파일 확인**

```bash
./gradlew :app:compileDebugKotlin
```

SDK 환경 문제로 실패하면 보고하고 진행. 코드 정합성은 자체 점검.

- [ ] **Step 2.4: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt \
        app/src/main/java/com/example/siheunggagae/data/repository/PetHotelRepository.kt
git commit -m "feat: 펫호텔 비교 API + Repository 추가"
```

---

## Task 3 — ViewModel + 로직 단위 테스트 (TDD)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/PetHotelCompareViewModel.kt`
- Test: `app/src/test/java/com/example/siheunggagae/PetHotelCompareLogicTest.kt`

테스트 우선 작성 → 함수 구현 → 테스트 통과 확인.

- [ ] **Step 3.1: 단위 테스트 먼저 작성**

Create `app/src/test/java/com/example/siheunggagae/PetHotelCompareLogicTest.kt`:

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.data.model.PetHotelPlan
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.ui.viewmodel.CompareSortAxis
import com.example.siheunggagae.ui.viewmodel.PetSize
import com.example.siheunggagae.ui.viewmodel.applySizeFilter
import com.example.siheunggagae.ui.viewmodel.applySort
import com.example.siheunggagae.ui.viewmodel.matchesSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetHotelCompareLogicTest {

    // ─── matchesSize ──────────────────────────────────────────────

    @Test fun matchesSize_korean_explicit() {
        assertTrue(matchesSize("1박 소형견", PetSize.SMALL))
        assertTrue(matchesSize("1박 중형견", PetSize.MEDIUM))
        assertTrue(matchesSize("1박 대형견", PetSize.LARGE))
    }

    @Test fun matchesSize_english_explicit() {
        assertTrue(matchesSize("Small overnight", PetSize.SMALL))
        assertTrue(matchesSize("medium care", PetSize.MEDIUM))
        assertTrue(matchesSize("LARGE 1 day", PetSize.LARGE))
    }

    @Test fun matchesSize_letter_abbreviation() {
        assertTrue(matchesSize("1박 S", PetSize.SMALL))
        assertTrue(matchesSize("1박 M", PetSize.MEDIUM))
        assertTrue(matchesSize("1박 L", PetSize.LARGE))
    }

    @Test fun matchesSize_no_match_returns_false() {
        assertFalse(matchesSize("프리미엄 1박", PetSize.SMALL))
        assertFalse(matchesSize("프리미엄 1박", PetSize.MEDIUM))
        assertFalse(matchesSize("프리미엄 1박", PetSize.LARGE))
    }

    @Test fun matchesSize_all_always_true() {
        assertTrue(matchesSize("any", PetSize.ALL))
        assertTrue(matchesSize("", PetSize.ALL))
    }

    @Test fun matchesSize_letter_must_be_word_boundary() {
        // "Small" 안의 'S' 가 SMALL 매칭에 우연히 잡히지 않아야 함 (단독 S 만)
        // 다만 "Small"은 "small" 키워드로 SMALL 매칭. 별개.
        // 'Snack' 같은 경우 단독 S 가 아니므로 false.
        assertFalse(matchesSize("Snack", PetSize.SMALL))
        assertFalse(matchesSize("Lunch", PetSize.LARGE))
    }

    // ─── applySort ────────────────────────────────────────────────

    private fun hotel(
        id: Int,
        min: Int? = null,
        max: Int? = null,
        dist: Double? = null,
        rating: Double? = null,
        plans: List<PetHotelPlan> = emptyList(),
    ) = PetHotelResponse(
        storeId = id, name = "h$id", address = "", latitude = 0.0, longitude = 0.0,
        distanceM = dist, ratingAvg = rating, minPriceKrw = min, maxPriceKrw = max,
        plans = plans,
    )

    @Test fun applySort_price_ascending_nulls_last() {
        val items = listOf(
            hotel(1, min = 60000),
            hotel(2, min = null),       // null 가격은 끝
            hotel(3, min = 40000),
        )
        val sorted = applySort(items, CompareSortAxis.PRICE)
        assertEquals(listOf(3, 1, 2), sorted.map { it.storeId })
    }

    @Test fun applySort_distance_ascending_nulls_last() {
        val items = listOf(
            hotel(1, dist = 2000.0),
            hotel(2, dist = null),
            hotel(3, dist = 500.0),
        )
        val sorted = applySort(items, CompareSortAxis.DISTANCE)
        assertEquals(listOf(3, 1, 2), sorted.map { it.storeId })
    }

    @Test fun applySort_rating_descending_nulls_last() {
        val items = listOf(
            hotel(1, rating = 4.2),
            hotel(2, rating = null),
            hotel(3, rating = 4.8),
        )
        val sorted = applySort(items, CompareSortAxis.RATING)
        assertEquals(listOf(3, 1, 2), sorted.map { it.storeId })
    }

    // ─── applySizeFilter ──────────────────────────────────────────

    @Test fun applySizeFilter_all_returns_all() {
        val items = listOf(hotel(1), hotel(2))
        val filtered = applySizeFilter(items, PetSize.ALL)
        assertEquals(2, filtered.size)
    }

    @Test fun applySizeFilter_small_keeps_only_hotels_with_small_plan() {
        val items = listOf(
            hotel(1, plans = listOf(PetHotelPlan("1박 소형견", 40000))),
            hotel(2, plans = listOf(PetHotelPlan("1박 대형견", 70000))),
            hotel(3, plans = listOf(
                PetHotelPlan("1박 소형견", 40000),
                PetHotelPlan("1박 중형견", 55000),
            )),
        )
        val filtered = applySizeFilter(items, PetSize.SMALL)
        assertEquals(listOf(1, 3), filtered.map { it.storeId })
    }
}
```

- [ ] **Step 3.2: 테스트 실행 → 컴파일 실패 확인**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.PetHotelCompareLogicTest"
```

Expected: BUILD FAILED — `matchesSize`, `applySort`, `applySizeFilter`, `CompareSortAxis`, `PetSize` 미존재.

- [ ] **Step 3.3: ViewModel 본체 작성**

Create `app/src/main/java/com/example/siheunggagae/ui/viewmodel/PetHotelCompareViewModel.kt`:

```kotlin
package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.data.repository.PetHotelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class CompareSortAxis { PRICE, DISTANCE, RATING }
enum class PetSize { ALL, SMALL, MEDIUM, LARGE }

sealed class PetHotelCompareUi {
    object Loading : PetHotelCompareUi()
    data class Success(
        val items: List<PetHotelResponse>,
        val absMinPrice: Int,
        val absMaxPrice: Int,
        val sort: CompareSortAxis,
        val size: PetSize,
        val radius: Int,
    ) : PetHotelCompareUi()
    data class Error(val message: String) : PetHotelCompareUi()
}

// Top-level pure functions — testable in isolation.

internal fun matchesSize(planName: String, size: PetSize): Boolean {
    val lower = planName.lowercase()
    return when (size) {
        PetSize.ALL    -> true
        PetSize.SMALL  -> "소형" in planName ||
            "small" in lower ||
            Regex("(?<![A-Za-z])[sS](?![A-Za-z])").containsMatchIn(planName)
        PetSize.MEDIUM -> "중형" in planName ||
            "medium" in lower ||
            Regex("(?<![A-Za-z])[mM](?![A-Za-z])").containsMatchIn(planName)
        PetSize.LARGE  -> "대형" in planName ||
            "large" in lower ||
            Regex("(?<![A-Za-z])[lL](?![A-Za-z])").containsMatchIn(planName)
    }
}

internal fun applySort(
    items: List<PetHotelResponse>,
    axis: CompareSortAxis,
): List<PetHotelResponse> = when (axis) {
    CompareSortAxis.PRICE    -> items.sortedBy { it.minPriceKrw ?: Int.MAX_VALUE }
    CompareSortAxis.DISTANCE -> items.sortedBy { it.distanceM ?: Double.MAX_VALUE }
    CompareSortAxis.RATING   -> items.sortedByDescending { it.ratingAvg ?: -1.0 }
}

internal fun applySizeFilter(
    items: List<PetHotelResponse>,
    size: PetSize,
): List<PetHotelResponse> {
    if (size == PetSize.ALL) return items
    return items.filter { hotel -> hotel.plans.any { matchesSize(it.planName, size) } }
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

    fun setSort(axis: CompareSortAxis) {
        if (sort == axis) return
        sort = axis
        recompute()
    }

    fun setSize(s: PetSize) {
        if (size == s) return
        size = s
        recompute()
    }

    fun expandRadius() {
        val next = (radius + 5000).coerceAtMost(50000)
        if (next == radius) return
        radius = next
        fetch()
    }

    fun retry() = fetch()

    fun toggleFavorite(storeId: Int) {
        // Optimistic local update only. 실제 favorite API 호출은 Task 7 에서 통합.
        raw = raw.map { if (it.storeId == storeId) it.copy(isFavorited = !it.isFavorited) else it }
        recompute()
    }

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

    class Factory(
        private val repository: PetHotelRepository,
        private val initialLat: Double,
        private val initialLng: Double,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PetHotelCompareViewModel(repository, initialLat, initialLng) as T
    }
}
```

- [ ] **Step 3.4: 테스트 통과 확인**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.PetHotelCompareLogicTest"
```

Expected: 10+ tests passed.

- [ ] **Step 3.5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/PetHotelCompareViewModel.kt \
        app/src/test/java/com/example/siheunggagae/PetHotelCompareLogicTest.kt
git commit -m "feat: PetHotelCompareViewModel + sort/filter/size 매칭 단위 테스트"
```

---

## Task 4 — Canvas PriceRangeBar + Screen 스켈레톤

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelCompareScreen.kt`

이 task는 Screen 파일을 처음 생성하면서 Canvas 컴포넌트와 TopBar/정렬·필터 칩까지 한 번에 만든다. 카드 row와 빈 상태는 다음 task.

- [ ] **Step 4.1: Screen 파일 헤더 + 색상 토큰 + TopBar + 정렬칩 + 사이즈칩**

Create `app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelCompareScreen.kt`:

```kotlin
package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.ui.viewmodel.CompareSortAxis
import com.example.siheunggagae.ui.viewmodel.PetHotelCompareUi
import com.example.siheunggagae.ui.viewmodel.PetHotelCompareViewModel
import com.example.siheunggagae.ui.viewmodel.PetSize

private val BackgroundP    = Color(0xFFFEFEFE)
private val TextBlackP     = Color(0xFF1E120A)
private val Brown700P      = Color(0xFF8A6E58)
private val Brown900P      = Color(0xFF614B3A)
private val BorderBeigeP   = Color(0xFFE8D3C2)
private val OrangeSandP    = Color(0xFFFFEDD4)
private val Orange500P     = Color(0xFFF7A35B)
private val PinkSurfaceP   = Color(0xFFFEE7EC)
private val Pink500P       = Color(0xFFF04268)
private val Gray300P       = Color(0xFFE8E8E8)
private val GrayTrackP     = Color(0xFFF4F4F4)
private val StarYellowP    = Color(0xFFFDC700)
private val PlaceholderP   = Color(0xFFC1AEA0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetHotelCompareScreen(
    viewModel: PetHotelCompareViewModel? = null,
    onBack: () -> Unit = {},
    onStoreClick: (storeId: Int) -> Unit = {},
) {
    val state by (viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(PetHotelCompareUi.Loading) })

    Scaffold(
        containerColor = BackgroundP,
        topBar = { CompareTopBar(state = state, onBack = onBack) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SortChipsRow(
                selected = (state as? PetHotelCompareUi.Success)?.sort ?: CompareSortAxis.PRICE,
                onSelect = { viewModel?.setSort(it) },
            )
            SizeChipsRow(
                selected = (state as? PetHotelCompareUi.Success)?.size ?: PetSize.ALL,
                onSelect = { viewModel?.setSize(it) },
            )
            when (val s = state) {
                PetHotelCompareUi.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = Orange500P) }

                is PetHotelCompareUi.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.message, color = Brown700P, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { viewModel?.retry() }) { Text("다시 시도") }
                    }
                }

                is PetHotelCompareUi.Success -> {
                    if (s.items.isEmpty()) {
                        EmptyState(onExpand = { viewModel?.expandRadius() })
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                        ) {
                            items(s.items, key = { it.storeId }) { hotel ->
                                PetHotelCard(
                                    hotel = hotel,
                                    absMin = s.absMinPrice,
                                    absMax = s.absMaxPrice,
                                    isLowest = s.items.firstOrNull()?.storeId == hotel.storeId &&
                                        s.sort == CompareSortAxis.PRICE,
                                    onClick = { onStoreClick(hotel.storeId) },
                                    onFavoriteClick = { viewModel?.toggleFavorite(hotel.storeId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareTopBar(state: PetHotelCompareUi, onBack: () -> Unit) {
    val count = (state as? PetHotelCompareUi.Success)?.items?.size ?: 0
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(40.dp).clickable { onBack() },
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    tint = TextBlackP,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            if (count > 0) "주변 펫호텔 ${count}곳" else "주변 펫호텔",
            color = TextBlackP, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun SortChipsRow(
    selected: CompareSortAxis,
    onSelect: (CompareSortAxis) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        item { ChipP("최저가", selected == CompareSortAxis.PRICE) { onSelect(CompareSortAxis.PRICE) } }
        item { ChipP("거리", selected == CompareSortAxis.DISTANCE) { onSelect(CompareSortAxis.DISTANCE) } }
        item { ChipP("별점", selected == CompareSortAxis.RATING) { onSelect(CompareSortAxis.RATING) } }
    }
}

@Composable
private fun SizeChipsRow(
    selected: PetSize,
    onSelect: (PetSize) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 14.dp),
    ) {
        item { ChipP("전체", selected == PetSize.ALL, small = true) { onSelect(PetSize.ALL) } }
        item { ChipP("소형", selected == PetSize.SMALL, small = true) { onSelect(PetSize.SMALL) } }
        item { ChipP("중형", selected == PetSize.MEDIUM, small = true) { onSelect(PetSize.MEDIUM) } }
        item { ChipP("대형", selected == PetSize.LARGE, small = true) { onSelect(PetSize.LARGE) } }
    }
}

@Composable
private fun ChipP(
    label: String,
    isSelected: Boolean,
    small: Boolean = false,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) Color(0xFF1A1A1A) else Color.White
    val fg = if (isSelected) Color.White else Brown700P
    val ph = if (small) 10.dp else 16.dp
    val pv = if (small) 4.dp else 8.dp
    val font = if (small) 11.sp else 14.sp
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(if (!isSelected) Modifier.border(1.dp, BorderBeigeP, RoundedCornerShape(50.dp)) else Modifier)
            .clickable { onClick() }
            .padding(horizontal = ph, vertical = pv),
    ) { Text(label, color = fg, fontSize = font, fontWeight = FontWeight.Medium) }
}

@Composable
private fun PetHotelCard(
    hotel: PetHotelResponse,
    absMin: Int,
    absMax: Int,
    isLowest: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    // 다음 task 에서 구현.
}

@Composable
private fun EmptyState(onExpand: () -> Unit) {
    // 다음 task 에서 구현.
}

@Composable
internal fun PriceRangeBar(
    storeMin: Int,
    storeMax: Int,
    absMin: Int,
    absMax: Int,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.horizontalGradient(listOf(OrangeSandP, Orange500P))
    val dotColor = Brown900P
    val track = GrayTrackP
    val range = (absMax - absMin).coerceAtLeast(1)
    val startFrac = ((storeMin - absMin).toFloat() / range).coerceIn(0f, 1f)
    val endFrac = ((storeMax - absMin).toFloat() / range).coerceIn(0f, 1f)

    Canvas(modifier = modifier.height(8.dp).fillMaxWidth()) {
        val h = size.height
        val w = size.width
        val cr = h / 2f
        drawRoundRect(
            color = track,
            cornerRadius = CornerRadius(cr, cr),
            size = Size(w, h),
        )
        val barLeft = startFrac * w
        val barWidth = (endFrac - startFrac) * w
        if (barWidth > 0f) {
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(barLeft, 0f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(cr, cr),
            )
        }
        val dotR = h * 0.85f
        drawCircle(
            color = Color.White,
            radius = dotR + 1.5.dp.toPx(),
            center = Offset(barLeft, h / 2f),
        )
        drawCircle(
            color = dotColor,
            radius = dotR,
            center = Offset(barLeft, h / 2f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PetHotelCompareScreenPreview() {
    PetHotelCompareScreen()
}
```

> **참고**: `PetHotelCard`, `EmptyState`는 placeholder로 두고 Task 5 에서 본체 작성.

- [ ] **Step 4.2: 컴파일 확인**

```bash
./gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL (SDK 환경이면 환경 사유로 fail 가능, 그래도 코드 정합 자체 검증).

- [ ] **Step 4.3: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelCompareScreen.kt
git commit -m "feat: PetHotelCompareScreen 스켈레톤 + Canvas PriceRangeBar"
```

---

## Task 5 — PetHotelCard 본체 + EmptyState

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelCompareScreen.kt`

- [ ] **Step 5.1: PetHotelCard 본체 작성**

Task 4의 placeholder `PetHotelCard` 를 다음으로 교체:

```kotlin
@Composable
private fun PetHotelCard(
    hotel: PetHotelResponse,
    absMin: Int,
    absMax: Int,
    isLowest: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .clickable { onClick() },
    ) {
        Column {
            // 상단 가로 썸네일 140dp
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                if (!hotel.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = hotel.thumbnailUrl,
                        contentDescription = hotel.name,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(OrangeSandP, PinkSurfaceP),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_hotel),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
                // 좌상단 "최저가" 뱃지 (최저가 카드만)
                if (isLowest) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brown900P)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text("최저가", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // 우상단 ♥
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_favorite),
                        contentDescription = "즐겨찾기",
                        tint = if (hotel.isFavorited) Pink500P else BorderBeigeP,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // 정보 영역
            Column(modifier = Modifier.padding(12.dp, 12.dp, 14.dp, 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        hotel.name,
                        color = TextBlackP,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f),
                    )
                    if (hotel.ratingAvg != null) {
                        Text(
                            "★ ${"%.1f".format(hotel.ratingAvg)}",
                            color = StarYellowP,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "(${hotel.ratingCount})",
                            color = Brown700P,
                            fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${shortAddress(hotel.address)} · ${formatDistance(hotel.distanceM)}",
                    color = Brown700P,
                    fontSize = 11.sp,
                )

                // PriceRangeBar
                if (hotel.minPriceKrw != null && hotel.maxPriceKrw != null && absMax > absMin) {
                    Spacer(Modifier.height(10.dp))
                    PriceRangeBar(
                        storeMin = hotel.minPriceKrw,
                        storeMax = hotel.maxPriceKrw,
                        absMin = absMin,
                        absMax = absMax,
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(OrangeSandP)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            "${hotel.planCount}옵션",
                            color = Brown900P,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (hotel.minPriceKrw != null) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("최저가 ", color = Brown700P, fontSize = 11.sp)
                            Text(
                                "%,d".format(hotel.minPriceKrw),
                                color = Brown900P,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                "원~",
                                color = Brown700P,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun shortAddress(address: String): String {
    // "경기도 시흥시 배곧동 ..." → "시흥시 배곧동" 로 축약 (도/특별시 제거).
    val parts = address.split(" ").filter { it.isNotBlank() }
    if (parts.size <= 2) return address
    return parts.drop(1).take(2).joinToString(" ")
}

private fun formatDistance(meters: Double?): String {
    if (meters == null) return "거리 -"
    return if (meters < 1000) "${meters.toInt()}m" else "${"%.1f".format(meters / 1000.0)}km"
}
```

- [ ] **Step 5.2: EmptyState 본체 작성**

placeholder `EmptyState` 를 다음으로 교체:

```kotlin
@Composable
private fun EmptyState(onExpand: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(OrangeSandP),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_hotel),
                    contentDescription = null,
                    tint = Orange500P,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "주변에 펫호텔이 없어요",
                color = TextBlackP,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "반경을 넓혀 검색해보세요",
                color = Brown700P,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onExpand,
                colors = ButtonDefaults.buttonColors(containerColor = Brown900P),
                shape = RoundedCornerShape(12.dp),
            ) { Text("반경 늘리기", color = Color.White) }
        }
    }
}
```

- [ ] **Step 5.3: 컴파일 확인**

```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step 5.4: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelCompareScreen.kt
git commit -m "feat: PetHotelCard 본체 + EmptyState"
```

---

## Task 6 — NavGraph 통합

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/NavGraph.kt`

- [ ] **Step 6.1: Screen 객체 추가**

`Screen` sealed class 안 적절한 위치에 추가:

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
        return if (parts.isEmpty()) "pet_hotel_compare"
        else "pet_hotel_compare?" + parts.joinToString("&")
    }
}
```

- [ ] **Step 6.2: composable 블록 추가**

NavHost 내부 적절한 위치(다른 maps/place 관련 composable 근처)에 추가:

```kotlin
composable(
    route = Screen.PetHotelCompare.route,
    arguments = listOf(
        navArgument(Screen.PetHotelCompare.ARG_LAT) {
            type = NavType.StringType; nullable = true; defaultValue = null
        },
        navArgument(Screen.PetHotelCompare.ARG_LNG) {
            type = NavType.StringType; nullable = true; defaultValue = null
        },
        navArgument(Screen.PetHotelCompare.ARG_RADIUS) {
            type = NavType.IntType; defaultValue = 5000
        },
    ),
) { backStackEntry ->
    val lat = backStackEntry.arguments?.getString(Screen.PetHotelCompare.ARG_LAT)?.toDoubleOrNull()
        ?: 37.3799
    val lng = backStackEntry.arguments?.getString(Screen.PetHotelCompare.ARG_LNG)?.toDoubleOrNull()
        ?: 126.8030
    val vm: PetHotelCompareViewModel = viewModel(
        factory = PetHotelCompareViewModel.Factory(PetHotelRepository(), lat, lng),
    )
    PetHotelCompareScreen(
        viewModel = vm,
        onBack = { navController.popBackStack() },
        onStoreClick = { storeId ->
            navController.navigate(Screen.PlaceDetail.createRoute(storeId))
        },
    )
}
```

상단에 import 추가:
```kotlin
import com.example.siheunggagae.data.repository.PetHotelRepository
import com.example.siheunggagae.ui.screen.PetHotelCompareScreen
import com.example.siheunggagae.ui.viewmodel.PetHotelCompareViewModel
```

- [ ] **Step 6.3: 컴파일 확인**

```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step 6.4: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/NavGraph.kt
git commit -m "feat: PetHotelCompare 라우트 + composable 통합"
```

---

## Task 7 — MapScreen 비교 배너 통합

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt`

MapScreen은 1,164줄로 큰 파일. 카테고리 필터 상태 변수를 찾아 PET_HOTEL 단독 선택 시 배너 표시 로직 추가.

- [ ] **Step 7.1: MapScreen 안 카테고리 상태 식별**

`MapScreen.kt`를 열고 `categories` 또는 `visibleCategories`, `selectedCategories` 같은 변수를 검색. ViewModel의 `uiState.visibleCategories` 또는 화면 내부 state에서 카테고리 multi-select 상태 추출.

확인 후 이 task의 다음 step에서 그 변수명에 맞춰 조건 작성.

```bash
grep -n "categor\|Category" /home/rivermoon/Documents/Github/frontend-app/app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt | head -20
```

발견된 변수명을 다음 step의 코드 자리에 적용한다.

- [ ] **Step 7.2: 배너 컴포저블 추가**

`MapScreen.kt` 파일 하단(다른 private composable 들 근처)에 새 private composable 추가:

```kotlin
@Composable
private fun PetHotelCompareBanner(
    count: Int,
    minPriceKrw: Int?,
    maxPriceKrw: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sub = buildString {
        if (minPriceKrw != null && maxPriceKrw != null) {
            val minK = minPriceKrw / 10000
            val maxK = maxPriceKrw / 10000
            append("${minK}~${maxK}만원 · ")
        }
        append("가격순 보기")
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "주변 펫호텔 ${count}곳",
                    color = Color(0xFF1E120A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(sub, color = Color(0xFF8A6E58), fontSize = 10.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF614B3A))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) { Text("가격 비교 →", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
```

import 추가가 필요하면 (이미 대부분 있을 가능성): `androidx.compose.foundation.background`, `Color`, `RoundedCornerShape`, `Surface`, `Row`, `Column`, `Text`, `FontWeight`, `Modifier.padding/clip/clickable` 등.

- [ ] **Step 7.3: 배너 노출 분기 + 클릭 콜백 추가**

MapScreen 상단(지도 영역 위, 카테고리 칩 row 바로 아래 또는 지도 위 오버레이 어느 쪽이든 디자인상 자연스러운 곳)에 다음 분기 추가:

```kotlin
val cats = uiState.visibleCategories  // Step 7.1 에서 확인한 실제 변수명 사용
val isPetHotelOnly = cats.size == 1 && cats.contains("PET_HOTEL")
val petHotelStoresCount = if (isPetHotelOnly) {
    uiState.viewportStores.count { it.category == "PET_HOTEL" }
} else 0

AnimatedVisibility(visible = isPetHotelOnly && petHotelStoresCount > 0) {
    PetHotelCompareBanner(
        count = petHotelStoresCount,
        minPriceKrw = null,  // 지도 응답에서 가격 정보 없음. count 만 표시.
        maxPriceKrw = null,
        onClick = {
            val center = uiState.cameraTarget ?: (37.3799 to 126.8030)
            onNavigate(
                com.example.siheunggagae.Screen.PetHotelCompare.createRoute(
                    lat = center.first,
                    lng = center.second,
                ),
            )
        },
        modifier = Modifier.padding(top = 8.dp),
    )
}
```

> **주의 — 실제 코드와 정합 맞춤**: 위 코드는 spec 가정. 실제 `uiState.viewportStores`/`StoreViewportItem`/`cameraTarget` 의 필드명·타입을 Step 7.1 에서 `grep` 으로 확인하고 정확히 매칭. `category` 필드 이름이 다르거나(예: 백엔드가 안 내려주는 경우) `petHotelStoresCount = 0` 으로 두고 count 만 "주변 펫호텔" 로 표시해도 됨.

`onNavigate` 콜백이 MapScreen에 없으면 함수 시그니처에 추가 (다른 화면 패턴 따라).

- [ ] **Step 7.4: 컴파일 확인 + 수동 sanity**

```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step 7.5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/MapScreen.kt
git commit -m "feat: Map 펫호텔 단독 필터 시 비교 배너"
```

---

## Task 8 — 즐겨찾기 토글 실제 API 연결

Task 3의 ViewModel `toggleFavorite` 는 로컬 상태만 변경. 실제 백엔드 호출(`POST/DELETE /users/me/favorites/stores`)을 연결.

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/PetHotelCompareViewModel.kt`

- [ ] **Step 8.1: 기존 즐겨찾기 API 메서드 위치 확인**

```bash
grep -n "favorit\|addFavorite\|removeFavorite" /home/rivermoon/Documents/Github/frontend-app/app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt | head -10
```

기존 메서드명·시그니처 확인. 보통:
- `POST /api/v1/users/me/favorites/stores` → `addFavoriteStore(@Body CreateFavoriteRequest)` 형태
- `DELETE /api/v1/users/me/favorites/stores/{storeId}` → `removeFavoriteStore(@Path Int)` 형태

- [ ] **Step 8.2: ViewModel `toggleFavorite` 본체 교체**

기존 단순 로컬 토글:
```kotlin
fun toggleFavorite(storeId: Int) {
    raw = raw.map { if (it.storeId == storeId) it.copy(isFavorited = !it.isFavorited) else it }
    recompute()
}
```

을 다음으로 교체 (실제 API 호출 + optimistic update + 실패 시 롤백):

```kotlin
fun toggleFavorite(storeId: Int) {
    val current = raw.firstOrNull { it.storeId == storeId }?.isFavorited ?: return
    val nextValue = !current

    // optimistic
    raw = raw.map { if (it.storeId == storeId) it.copy(isFavorited = nextValue) else it }
    recompute()

    viewModelScope.launch {
        val ok = runCatching {
            if (nextValue) {
                userRepository.addFavoriteStore(storeId).isSuccessful
            } else {
                userRepository.removeFavoriteStore(storeId).isSuccessful
            }
        }.getOrDefault(false)

        if (!ok) {
            // 롤백
            raw = raw.map { if (it.storeId == storeId) it.copy(isFavorited = current) else it }
            recompute()
        }
    }
}
```

생성자에 `userRepository: UserRepository` 추가, Factory도 같이 갱신:

```kotlin
class PetHotelCompareViewModel(
    private val repository: PetHotelRepository,
    private val userRepository: UserRepository,
    private val initialLat: Double,
    private val initialLng: Double,
) : ViewModel() {
    // ...
    class Factory(
        private val repository: PetHotelRepository,
        private val userRepository: UserRepository,
        private val initialLat: Double,
        private val initialLng: Double,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PetHotelCompareViewModel(repository, userRepository, initialLat, initialLng) as T
    }
}
```

상단 import:
```kotlin
import com.example.siheunggagae.data.repository.UserRepository
```

- [ ] **Step 8.3: NavGraph factory 호출 수정**

Task 6에서 만든 composable의 ViewModel factory 부분을:

```kotlin
factory = PetHotelCompareViewModel.Factory(
    PetHotelRepository(),
    UserRepository(),    // 추가
    lat,
    lng,
),
```

`UserRepository` import 추가 (이미 다른 곳에서 사용 중이므로 import 있을 가능성 높음).

> **참고**: 만약 `UserRepository` 의 `addFavoriteStore/removeFavoriteStore` 메서드 시그니처가 plan의 가정과 다르면 (예: 다른 메서드명, suspend 아님 등), Step 8.2의 호출부를 실제 시그니처에 맞춰 수정. `UserRepository.kt` 를 직접 읽어 메서드명을 확인하는 게 안전.

- [ ] **Step 8.4: 비로그인 시 ♥ 숨김 처리**

비로그인 사용자에게 ♥ 토글이 무의미함. PetHotelCompareScreen은 단순화 위해 ♥를 항상 표시하되 비로그인 시 토글이 401로 실패하면 롤백되어 변화 없음(자연 폴백). 명시적 숨김은 후속.

- [ ] **Step 8.5: 컴파일 + 커밋**

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/PetHotelCompareViewModel.kt \
        app/src/main/java/com/example/siheunggagae/NavGraph.kt
git commit -m "feat: 즐겨찾기 토글 실제 API 연결 (optimistic update + 롤백)"
```

---

## Task 9 — 수동 smoke test + 최종 QA

이 task는 코드를 작성하지 않는다. 실 디바이스/에뮬레이터에서 흐름 확인.

- [ ] **Step 9.1: 빌드 + 설치**

```bash
./gradlew :app:installDebug
```

- [ ] **Step 9.2: 흐름 확인**

1. Map 화면 진입 → 카테고리 칩에서 "펫호텔"만 선택 → 지도 상단에 비교 배너 노출 확인.
2. 다른 카테고리(예: 카페)도 같이 켰을 때 배너 사라지는지 확인.
3. 배너 탭 → PetHotelCompareScreen 진입, 매장 목록 로드 확인.
4. 정렬 칩 3개 토글 — 즉시 재정렬되는지 (API 재호출 X).
5. 크기 칩 토글 — 매장 수 변경 + Canvas 막대 스케일 갱신.
6. 카드 ♥ 탭 — Pink500 ↔ Beige 토글, 화면 회전 후 유지(즐겨찾기 API 정상 호출).
7. 카드 본체 탭 → PlaceDetail 진입.
8. 빈 상태 시뮬 (예: radius 검색 결과 0인 위치) → "반경 늘리기" 클릭 후 결과 갱신.
9. 에러 시뮬 (네트워크 OFF 진입) → 에러 메시지 + "다시 시도".

- [ ] **Step 9.3: lint + assemble (가능 시)**

```bash
./gradlew :app:lintDebug :app:assembleDebug
```

빌드 통과 확인. SDK 환경에서 막히면 사용자 PC에서 검증.

- [ ] **Step 9.4: 문서 최신화**

기존 문서들에 펫호텔 비교 항목 추가:

`docs/screens.md` — "서브" 섹션에 항목 추가:
```markdown
### [PetHotelCompareScreen] 주변 펫호텔 가격 비교

- 라우트: `pet_hotel_compare?lat={lat}&lng={lng}&radius={radius}` — Map 화면의 펫호텔 단독 필터 배너에서 진입.
- TopBar: 뒤로가기 카드 + "주변 펫호텔 N곳" 18sp SemiBold
- 정렬 칩: 최저가(기본) / 거리 / 별점 — bg=#1A1A1A / White border BorderBeige
- 크기 필터: 전체(기본) / 소형 / 중형 / 대형 — plan_name 휴리스틱 매칭
- 카드 (radius 16dp shadow 2dp): 상단 140dp 썸네일(Coil AsyncImage + 폴백 그라디언트) + 좌상단 "최저가" 뱃지(최저가 1위만) + 우상단 ♥ 토글 + 정보 영역(매장명/별점/주소·거리/Canvas 가격범위 막대/옵션수+최저가)
- Canvas PriceRangeBar: 전체 가격대(absMin~absMax) 위 매장별 범위 막대 + 최저가 dot
- 빈 상태: 80dp OrangeSand 원형 + ic_hotel + "주변에 펫호텔이 없어요" + "반경 늘리기" Brown900 CTA (5km씩 증가, 최대 50km)
- ViewModel: PetHotelCompareViewModel — setSort, setSize, expandRadius, toggleFavorite, retry
```

`docs/api.md` — Map 도메인 표에 행 추가:
```markdown
| GET | /maps/pet-hotels | getPetHotels | PetHotelListResponse |
```

`CLAUDE.md` Navigation Routes 표:
```kotlin
object PetHotelCompare : Screen("pet_hotel_compare?lat&lng&radius")
```

- [ ] **Step 9.5: docs 커밋**

```bash
git add docs/screens.md docs/api.md CLAUDE.md
git commit -m "docs: 펫호텔 비교 화면·API·라우트 문서 반영"
```

---

## Self-Review (작성자가 확인)

- **Spec 커버리지**: §1 목표·§2 백엔드 의존성·§3 진입점+화면·§4 모델·§5 라우트·§6 API·§7 ViewModel·§8 Canvas 시각화·§9 Map 진입·§10 즐겨찾기·§11 에러·§12 빈/로딩 모두 Task 1~9 어딘가에서 처리. §13 테스트는 Task 1·3에서 단위 테스트, 나머지는 manual smoke (Task 9).
- **Placeholder 없음**: 모든 step에 실제 코드/명령. Task 7 Step 7.1 만 grep 의존 (실제 변수명 확인 후 진행) — 의도된 read-then-adapt.
- **타입 일관성**: `PetHotelPlan`, `PetHotelResponse`, `PetHotelListResponse`, `CompareSortAxis`, `PetSize`, `PetHotelCompareUi`, `PetHotelCompareViewModel` 모두 task 1~3 사이 일관. `matchesSize`/`applySort`/`applySizeFilter` — Task 3 정의, Task 3 테스트 사용.
- **스코프**: 단일 화면 + 진입 배너 + 단위 테스트. 적정 규모.
- **의존 관계**: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9. 각 task는 그 전 task 산출물을 사용.
