# 펫호텔 매트릭스 비교 — 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development (recommended) or superpowers:executing-plans.

**Goal:** 펫호텔 비교 리스트의 후속으로 1:1 비교 / 여러 곳 비교 토글이 있는 매트릭스 화면을 도입.

**Architecture:** 기존 `feature/pet-hotel-compare` 브랜치 안에 신규 화면 1개 + ViewModel 1개 + 신규 아이콘 3개 + 리스트 화면 TopBar 진입 버튼.

**Tech Stack:** Kotlin · Compose · Coil 3 · JUnit. 외부 차트 라이브러리 없음.

**Spec:** `docs/superpowers/specs/2026-05-23-pethotel-matrix-compare-design.md`

**Branch:** `feature/pet-hotel-compare`

---

## File Structure

### 신규
```
app/src/main/java/com/example/siheunggagae/
├── ui/
│   ├── screen/
│   │   └── PetHotelMatrixCompareScreen.kt
│   └── viewmodel/
│       └── PetHotelMatrixCompareViewModel.kt

app/src/main/res/drawable/
├── ic_chevron_down.xml
├── ic_sliders.xml
└── ic_award.xml

app/src/test/java/com/example/siheunggagae/
└── PetHotelInsightTest.kt
```

### 수정
- `NavGraph.kt` — Screen.PetHotelMatrixCompare + composable
- `ui/screen/PetHotelCompareScreen.kt` — TopBar 우측 "비교" 진입 버튼

---

## Task M1 — Lucide 아이콘 3개 추가

**Files:** `app/src/main/res/drawable/ic_chevron_down.xml`, `ic_sliders.xml`, `ic_award.xml`

- [ ] **Step M1.1** — `ic_chevron_down.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path
      android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="m6 9 6 6 6-6"/>
</vector>
```

- [ ] **Step M1.2** — `ic_sliders.xml` (Lucide `sliders-horizontal`):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M21 4H14"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M10 4H3"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M21 12H12"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M8 12H3"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M21 20H16"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M12 20H3"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M14 2v4"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M8 10v4"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000" android:pathData="M16 18v4"/>
</vector>
```

- [ ] **Step M1.3** — `ic_award.xml` (Lucide):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000"
        android:pathData="M15.477 12.89 17 22l-5-3-5 3 1.523-9.11"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
        android:strokeLineCap="round" android:strokeLineJoin="round"
        android:fillColor="#00000000"
        android:pathData="M12 2a6 6 0 1 0 0 12 6 6 0 0 0 0-12Z"/>
</vector>
```

- [ ] **Step M1.4** — Commit:

```bash
git add app/src/main/res/drawable/ic_chevron_down.xml \
        app/src/main/res/drawable/ic_sliders.xml \
        app/src/main/res/drawable/ic_award.xml
git commit -m "feat: 매트릭스 비교용 Lucide 아이콘 3개 추가"
```

---

## Task M2 — ViewModel + Insight 단위 테스트 (TDD)

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/PetHotelMatrixCompareViewModel.kt`
- Test: `app/src/test/java/com/example/siheunggagae/PetHotelInsightTest.kt`

- [ ] **Step M2.1** — 테스트 작성

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.data.model.PetHotelPlan
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.ui.viewmodel.computeInsight
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PetHotelInsightTest {

    private val gson = Gson()

    private fun hotel(
        id: Int,
        min: Int? = null,
        max: Int? = null,
        rating: Double? = null,
        dist: Double? = null,
        plans: Int = 0,
    ): PetHotelResponse {
        val plansJson = (1..plans).joinToString(",") {
            "{\"plan_name\":\"p$it\",\"price_krw\":${min ?: 0}}"
        }
        val json = """
            {"store_id":$id,"name":"h$id","address":"","latitude":0.0,"longitude":0.0,
             ${min?.let { "\"min_price_krw\":$it," } ?: ""}
             ${max?.let { "\"max_price_krw\":$it," } ?: ""}
             ${rating?.let { "\"rating_avg\":$it," } ?: ""}
             ${dist?.let { "\"distance_m\":$it," } ?: ""}
             "plan_count":$plans,
             "plans":[$plansJson]}
        """.trimIndent()
        return gson.fromJson(json, PetHotelResponse::class.java)
    }

    @Test fun cheapest_picks_min_minPrice() {
        val items = listOf(hotel(1, min = 50000), hotel(2, min = 40000), hotel(3, min = 60000))
        val i = computeInsight(items)
        assertEquals(2, i.cheapestId)
    }

    @Test fun highest_rated_excludes_null_ratings() {
        val items = listOf(hotel(1, rating = null), hotel(2, rating = 4.5), hotel(3, rating = 4.8))
        val i = computeInsight(items)
        assertEquals(3, i.highestRatedId)
    }

    @Test fun nearest_excludes_null_distances() {
        val items = listOf(hotel(1, dist = 2000.0), hotel(2, dist = null), hotel(3, dist = 500.0))
        val i = computeInsight(items)
        assertEquals(3, i.nearestId)
    }

    @Test fun most_options_picks_max_plan_count() {
        val items = listOf(hotel(1, plans = 3), hotel(2, plans = 5), hotel(3, plans = 2))
        val i = computeInsight(items)
        assertEquals(2, i.mostOptionsId)
    }

    @Test fun all_null_categories_return_null() {
        val items = listOf(hotel(1), hotel(2))
        val i = computeInsight(items)
        assertNull(i.cheapestId)
        assertNull(i.highestRatedId)
        assertNull(i.nearestId)
        // plan_count 는 default 0 이라 0 끼리 비교, maxBy 가 첫 항목을 반환
        assertEquals(1, i.mostOptionsId)
    }
}
```

- [ ] **Step M2.2** — Test 실행 → 컴파일 실패 확인:
```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.PetHotelInsightTest"
```
Expected: BUILD FAILED (`computeInsight` 미정의)

- [ ] **Step M2.3** — ViewModel 작성

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

enum class CompareMode { ONE_ON_ONE, MULTI }

data class CompareInsight(
    val cheapestId: Int?,
    val highestRatedId: Int?,
    val mostOptionsId: Int?,
    val nearestId: Int?,
)

internal fun computeInsight(items: List<PetHotelResponse>): CompareInsight {
    if (items.isEmpty()) return CompareInsight(null, null, null, null)
    val cheapest = items.filter { it.minPriceKrw != null }
        .minByOrNull { it.minPriceKrw!! }?.storeId
    val highest = items.filter { it.ratingAvg != null }
        .maxByOrNull { it.ratingAvg!! }?.storeId
    val nearest = items.filter { it.distanceM != null }
        .minByOrNull { it.distanceM!! }?.storeId
    val mostOpts = items.maxByOrNull { it.planCount }?.storeId
    return CompareInsight(cheapest, highest, mostOpts, nearest)
}

sealed class MatrixCompareUi {
    object Loading : MatrixCompareUi()
    data class Success(
        val items: List<PetHotelResponse>,
        val mode: CompareMode,
        val selectedAId: Int,
        val selectedBId: Int,
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
    private var radius: Int = initialRadius.coerceIn(1000, PetHotelCompareViewModel.RADIUS_MAX_M)
    private var mode: CompareMode = CompareMode.MULTI
    private var selectedA: Int = -1
    private var selectedB: Int = -1

    init { fetch() }

    fun setMode(m: CompareMode) {
        if (mode == m) return
        mode = m
        recompute()
    }

    fun selectA(storeId: Int) {
        if (storeId == selectedB) {
            // swap
            val tmp = selectedA
            selectedA = storeId
            selectedB = tmp
        } else {
            selectedA = storeId
        }
        recompute()
    }

    fun selectB(storeId: Int) {
        if (storeId == selectedA) {
            val tmp = selectedB
            selectedB = storeId
            selectedA = tmp
        } else {
            selectedB = storeId
        }
        recompute()
    }

    fun retry() = fetch()

    private fun fetch() {
        viewModelScope.launch {
            _state.value = MatrixCompareUi.Loading
            val resp = repository.getNearby(initialLat, initialLng, radius)
            if (resp.isSuccessful) {
                raw = resp.body()?.petHotels.orEmpty()
                    .sortedBy { it.distanceM ?: Double.MAX_VALUE }
                mode = if (raw.size <= 2) CompareMode.ONE_ON_ONE else CompareMode.MULTI
                selectedA = raw.getOrNull(0)?.storeId ?: -1
                selectedB = raw.getOrNull(1)?.storeId ?: -1
                recompute()
            } else {
                _state.value = MatrixCompareUi.Error("주변 펫호텔을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    private fun recompute() {
        _state.value = MatrixCompareUi.Success(
            items = raw,
            mode = mode,
            selectedAId = selectedA,
            selectedBId = selectedB,
            insight = computeInsight(raw),
        )
    }

    class Factory(
        private val repository: PetHotelRepository,
        private val initialLat: Double,
        private val initialLng: Double,
        private val initialRadius: Int = PetHotelCompareViewModel.RADIUS_DEFAULT_M,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PetHotelMatrixCompareViewModel(repository, initialLat, initialLng, initialRadius) as T
    }
}
```

- [ ] **Step M2.4** — Test 통과 확인:
```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.PetHotelInsightTest"
```
Expected: 5 tests passed.

- [ ] **Step M2.5** — Commit:
```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/PetHotelMatrixCompareViewModel.kt \
        app/src/test/java/com/example/siheunggagae/PetHotelInsightTest.kt
git commit -m "feat: PetHotelMatrixCompareViewModel + computeInsight 단위 테스트"
```

---

## Task M3 — Screen 본체 (큰 파일 한 번에)

**Files:** Create `app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelMatrixCompareScreen.kt`

기존 `PetHotelCompareScreen.kt` 의 톤·헬퍼 패턴(Color tokens P 접미사, TopBar 패턴) 그대로 따라가되 접미사는 `M` 사용해 충돌 방지.

코드 골격:

```kotlin
package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.PetHotelResponse
import com.example.siheunggagae.ui.viewmodel.CompareMode
import com.example.siheunggagae.ui.viewmodel.MatrixCompareUi
import com.example.siheunggagae.ui.viewmodel.PetHotelMatrixCompareViewModel

private val BgM           = Color(0xFFFEFEFE)
private val TextBlackM    = Color(0xFF1E120A)
private val Brown700M     = Color(0xFF8A6E58)
private val Brown900M     = Color(0xFF614B3A)
private val BorderBeigeM  = Color(0xFFE8D3C2)
private val OrangeSandM   = Color(0xFFFFEDD4)
private val Orange500M    = Color(0xFFF7A35B)
private val PinkSurfaceM  = Color(0xFFFEE7EC)
private val Pink500M      = Color(0xFFF04268)
private val PinkText      = Color(0xFF9F1239)
private val StarYellowM   = Color(0xFFFDC700)
private val TagGrayM      = Color(0xFFF2F2F2)
private val PlaceholderM  = Color(0xFFC1AEA0)

@Composable
fun PetHotelMatrixCompareScreen(
    viewModel: PetHotelMatrixCompareViewModel,
    onBack: () -> Unit,
    onStoreClick: (storeId: Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = BgM,
        topBar = { MatrixTopBar(onBack) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                MatrixCompareUi.Loading -> Loading()
                is MatrixCompareUi.Error -> ErrorState(s.message) { viewModel.retry() }
                is MatrixCompareUi.Success -> SuccessBody(s, viewModel, onStoreClick)
            }
        }
    }
}

@Composable
private fun MatrixTopBar(onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp,
            modifier = Modifier.size(40.dp).clickable { onBack() },
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기", tint = TextBlackM,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text("펫호텔 비교", color = TextBlackM, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Orange500M)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Brown700M, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRetry) { Text("다시 시도") }
        }
    }
}
```

이어서 `SuccessBody`(verticalScroll), `ModeToggle` (세그먼티드), `OneOnOneBlock` (드롭다운 2개 + Matrix2Col + Insight + 상세 진입 2개), `MultiBlock` (서브헤더 + Matrix N + 가로 스크롤 힌트 + Insight + 매장별 상세 진입 가로 스크롤), 매트릭스 셀 헬퍼들, 드롭다운 시트 등을 한 파일에 작성.

핵심 디테일:
- 드롭다운 thumbnail:
  ```kotlin
  Box(
      modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)),
      contentAlignment = Alignment.Center,
  ) {
      val url = hotel.thumbnailUrl?.takeIf { it.isNotBlank() }
      if (url != null) {
          AsyncImage(model = url, contentDescription = hotel.name,
              contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
      } else {
          val grad = if (isSlotA)
              Brush.linearGradient(listOf(OrangeSandM, Orange500M))
          else
              Brush.linearGradient(listOf(PinkSurfaceM, Pink500M))
          Box(Modifier.fillMaxSize().background(grad), contentAlignment = Alignment.Center) {
              Icon(painter = painterResource(R.drawable.ic_hotel), contentDescription = null,
                  tint = Color.White, modifier = Modifier.size(18.dp))
          }
      }
  }
  ```
- 모드 토글 (세그먼티드 in Box / Row):
  ```kotlin
  Row(
      modifier = Modifier
          .padding(horizontal = 18.dp).padding(bottom = 14.dp)
          .clip(RoundedCornerShape(50.dp))
          .background(TagGrayM)
          .padding(4.dp),
  ) {
      SegOpt("1:1 비교", mode == CompareMode.ONE_ON_ONE, Modifier.weight(1f)) {
          if (raw.size > 2) viewModel.setMode(CompareMode.ONE_ON_ONE)
      }
      SegOpt("여러 곳 비교", mode == CompareMode.MULTI, Modifier.weight(1f)) {
          if (raw.size > 2) viewModel.setMode(CompareMode.MULTI)
      }
  }
  ```
- 매트릭스 셀 best 강조:
  ```kotlin
  Box(modifier = Modifier
      .fillMaxWidth().background(if (isBest) OrangeSandM else Color.Transparent)
      .padding(11.dp, 4.dp)) {
      if (isBest) Box(modifier = Modifier
          .align(Alignment.TopStart).size(5.dp)
          .clip(CircleShape).background(Orange500M))
      Text(value, fontSize = 12.sp, color = if (isBest) Brown900M else TextBlackM,
          fontWeight = if (isBest) FontWeight.ExtraBold else FontWeight.SemiBold,
          modifier = Modifier.align(Alignment.Center))
  }
  ```
- 인사이트 카드(여러 곳):
  ```kotlin
  Row(
      modifier = Modifier.padding(horizontal = 18.dp).fillMaxWidth()
          .clip(RoundedCornerShape(12.dp)).background(OrangeSandM)
          .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.Top,
  ) {
      Icon(painter = painterResource(R.drawable.ic_lightbulb), contentDescription = null,
          tint = Orange500M, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(10.dp))
      Text(buildInsightText(insight, items),
          color = Brown900M, fontSize = 12.sp, lineHeight = 18.sp)
  }
  ```

- [ ] **Step M3.1** — Screen 파일 생성 (위 골격 + 본문 컴포저블 완성). 분량 약 400~600줄 예상.

- [ ] **Step M3.2** — Compile:
```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step M3.3** — Commit:
```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelMatrixCompareScreen.kt
git commit -m "feat: PetHotelMatrixCompareScreen 본체"
```

---

## Task M4 — NavGraph + 리스트 화면 진입 버튼

**Files:**
- Modify: `NavGraph.kt`
- Modify: `ui/screen/PetHotelCompareScreen.kt`

- [ ] **Step M4.1** — `Screen.PetHotelMatrixCompare` 추가 (NavGraph.kt):

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

- [ ] **Step M4.2** — composable 블록 (`PetHotelCompare` 블록 근처):

```kotlin
composable(
    route = Screen.PetHotelMatrixCompare.route,
    arguments = listOf(
        navArgument(Screen.PetHotelMatrixCompare.ARG_LAT) {
            type = NavType.StringType; nullable = true; defaultValue = null
        },
        navArgument(Screen.PetHotelMatrixCompare.ARG_LNG) {
            type = NavType.StringType; nullable = true; defaultValue = null
        },
        navArgument(Screen.PetHotelMatrixCompare.ARG_RADIUS) {
            type = NavType.IntType; defaultValue = 5000
        },
    ),
) { entry ->
    val lat = entry.arguments?.getString(Screen.PetHotelMatrixCompare.ARG_LAT)?.toDoubleOrNull() ?: 37.3799
    val lng = entry.arguments?.getString(Screen.PetHotelMatrixCompare.ARG_LNG)?.toDoubleOrNull() ?: 126.8030
    val radius = entry.arguments?.getInt(Screen.PetHotelMatrixCompare.ARG_RADIUS)?.takeIf { it > 0 }
        ?: PetHotelCompareViewModel.RADIUS_DEFAULT_M
    val vm: PetHotelMatrixCompareViewModel = viewModel(
        factory = PetHotelMatrixCompareViewModel.Factory(
            PetHotelRepository(), lat, lng, radius,
        ),
    )
    PetHotelMatrixCompareScreen(
        viewModel = vm,
        onBack = { navController.popBackStack() },
        onStoreClick = { storeId ->
            navController.navigate(Screen.PlaceDetail.createRoute(storeId))
        },
    )
}
```

imports 추가: `PetHotelMatrixCompareScreen`, `PetHotelMatrixCompareViewModel`.

- [ ] **Step M4.3** — `PetHotelCompareScreen.kt` 의 `CompareTopBar` 우측에 "비교" 아이콘 버튼 추가:

찾기:
```kotlin
@Composable
private fun CompareTopBar(state: PetHotelCompareUi, onBack: () -> Unit) { ... }
```

변경:
```kotlin
@Composable
private fun CompareTopBar(
    state: PetHotelCompareUi,
    onBack: () -> Unit,
    onCompareClick: () -> Unit,
    canCompare: Boolean,
) { ... }
```

내부에서 우측 `Spacer(Modifier.size(40.dp))` 자리에 다음 버튼 추가:

```kotlin
Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color.White,
    shadowElevation = 2.dp,
    modifier = Modifier.size(40.dp)
        .alpha(if (canCompare) 1f else 0.4f)
        .clickable(enabled = canCompare) { onCompareClick() },
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(R.drawable.ic_sliders),
            contentDescription = "비교",
            tint = TextBlackP,
            modifier = Modifier.size(22.dp),
        )
    }
}
```

- [ ] **Step M4.4** — Composable 호출에 추가 인자 wiring:

```kotlin
fun PetHotelCompareScreen(
    viewModel: PetHotelCompareViewModel? = null,
    onBack: () -> Unit = {},
    onStoreClick: (storeId: Int) -> Unit = {},
    onCompareClick: () -> Unit = {},        // NEW
) {
    ...
    topBar = {
        CompareTopBar(
            state = state,
            onBack = onBack,
            onCompareClick = onCompareClick,
            canCompare = (state as? PetHotelCompareUi.Success)?.items?.size ?: 0 >= 2,
        )
    },
    ...
}
```

- [ ] **Step M4.5** — NavGraph 의 `PetHotelCompare` composable 블록에서 onCompareClick wiring:

```kotlin
PetHotelCompareScreen(
    viewModel = vm,
    onBack = { navController.popBackStack() },
    onStoreClick = { storeId -> navController.navigate(Screen.PlaceDetail.createRoute(storeId)) },
    onCompareClick = {
        navController.navigate(
            Screen.PetHotelMatrixCompare.createRoute(lat = lat, lng = lng, radius = radius),
        )
    },
)
```

- [ ] **Step M4.6** — Compile:
```bash
./gradlew :app:compileDebugKotlin
```

- [ ] **Step M4.7** — Commit:
```bash
git add app/src/main/java/com/example/siheunggagae/NavGraph.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/PetHotelCompareScreen.kt
git commit -m "feat: 매트릭스 비교 라우트 + 리스트 화면 TopBar 진입 버튼"
```

---

## Task M5 — 수동 QA + docs 갱신

**Files:**
- Modify: `docs/screens.md` (PetHotelMatrixCompareScreen 항목 + PetHotelCompareScreen 의 "비교" 버튼 보강)
- Modify: `CLAUDE.md` (Navigation Routes 추가)

- [ ] **Step M5.1** — Build + install:
```bash
./gradlew :app:installDebug
```

- [ ] **Step M5.2** — 수동 흐름:
1. Map → 카테고리 펫호텔 → 배너 → 리스트
2. 리스트 TopBar 우측 비교 버튼 탭 → 매트릭스 진입
3. 매장 3개 이상이면 "여러 곳 비교" 디폴트, 가로 스크롤 확인
4. 토글 → "1:1 비교", 드롭다운으로 A·B 자유 선택. B 에서 A 매장 안 보이는지.
5. 인사이트 카드 텍스트 정합 (가장 싼 매장이 굵게 표시되는지)
6. 매장 상세 진입 버튼 → PlaceDetail
7. 매장 수 2개로 좁아진 경우 → 토글 비활성, 1:1 강제

- [ ] **Step M5.3** — docs/screens.md 보강. PetHotelMatrixCompareScreen 섹션 추가:

```markdown
### [PetHotelMatrixCompareScreen] 펫호텔 매트릭스 비교

- 라우트: `pet_hotel_matrix_compare?lat={lat}&lng={lng}&radius={radius}` — PetHotelCompareScreen TopBar 우측 `ic_sliders` 버튼에서 진입.
- TopBar: 뒤로가기 카드 + "펫호텔 비교" 18sp SemiBold
- 모드 토글 (세그먼티드 in TagGray pill): "1:1 비교" / "여러 곳 비교"
- 1:1 모드: 드롭다운 2개 (A=Orange500 border, B=Pink500 border). 썸네일 36dp radius 10 — thumbnailUrl 있으면 Coil AsyncImage(Crop), 없으면 카테고리 그라디언트(A=OrangeSand→Orange500, B=PinkSurface→Pink500) + ic_hotel.
- 매트릭스 (2 또는 N 컬럼): 헤더 OrangeSand bg. 각 셀 12sp. 최우값 OrangeSand bg + 5dp Orange500 dot (좌상단).
- 인사이트 카드: OrangeSand bg + ic_lightbulb. 1:1 모드는 두 매장 우위 영역 한 줄. 여러 곳 모드는 award 아이콘 + "최저가 X · 별점 Y · 옵션 Z · 가까움 W" 한 줄.
- 가로 스크롤 (여러 곳): 4컬럼 노출, 5번째부터 가로 스크롤. 우하단 "← 가로로 스와이프 →" 힌트.
- 매장 상세 진입: 1:1 은 두 큰 버튼, 여러 곳은 매장별 작은 버튼 가로 스크롤.
- ViewModel: PetHotelMatrixCompareViewModel — setMode / selectA / selectB / retry / computeInsight.
- 엣지: 매장 ≤ 1 진입 비활성. == 2 자동 1:1 + 토글 비활성. ≥ 3 디폴트 여러 곳.
```

- [ ] **Step M5.4** — CLAUDE.md Navigation Routes 추가:

```kotlin
object PetHotelMatrixCompare : Screen("pet_hotel_matrix_compare?lat&lng&radius")
```

- [ ] **Step M5.5** — Commit:
```bash
git add docs/screens.md CLAUDE.md
git commit -m "docs: 펫호텔 매트릭스 비교 화면 문서 반영"
```

---

## Self-Review

- **Spec 커버리지**: §3 진입점·§4 화면·§5 모델·§6 인사이트·§7 ViewModel·§8 라우트·§9 아이콘·§10 테스트 모두 Task M1~M5 어딘가에서 처리됨.
- **Placeholder 없음**: 모든 step 실제 코드 또는 명령. Screen 본체(M3)는 골격 + 핵심 디테일 가이드 (subagent 가 골격 따라 본문 작성).
- **타입 일관성**: `CompareMode`, `CompareInsight`, `MatrixCompareUi`, `PetHotelMatrixCompareViewModel`, `Screen.PetHotelMatrixCompare` 일관.
- **스코프**: 단일 신규 화면 + 단위 테스트. 적정 규모.
- **의존**: M1 → M2 → M3 → M4 → M5 순.
