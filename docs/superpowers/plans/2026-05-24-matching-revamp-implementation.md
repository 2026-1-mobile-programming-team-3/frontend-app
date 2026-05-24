# 매칭 화면 개선 — 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax.

**Goal:** MatchingScreen 을 당근/번개장터 수준의 정보 밀도 + 임박순·거리·카테고리 필터 + 본인 글 자연스러운 표시로 재설계. P0+P1 14항목.

**Architecture:** 기존 `MatchingScreen.kt` + `MatchingViewModel.kt` 확장. 새 헬퍼(computeImminence/walkingMinutes/requiresVolunteerRole)와 enum(MatchCategory/MatchSort/DistanceFilter) 추가. 신규 아이콘 5개. 백엔드 변경은 점진 적용 가드로 흡수.

**Tech Stack:** Kotlin · Jetpack Compose · Retrofit/Gson · Coil 3 · JUnit. 외부 라이브러리 추가 없음.

**Spec:** `docs/superpowers/specs/2026-05-24-matching-revamp-design.md`
**Backend requests:** `docs/backend-requests/2026-05-24-matching-revamp.md`
**Branch:** `feature/matching-revamp` (main 기반)

---

## File Structure

### 신규 파일
```
app/src/main/res/drawable/
├── ic_paw.xml
├── ic_stethoscope.xml
├── ic_shopping_cart.xml
├── ic_car.xml
└── ic_users.xml

app/src/test/java/com/example/siheunggagae/
├── MatchImminenceTest.kt
├── MatchHelpersTest.kt        # walkingMinutes / requiresVolunteerRole / newDiff
└── MatchModelsTest.kt          # 신규 필드 직렬화
```

### 수정 파일
| 파일 | 변경 |
|---|---|
| `data/model/MatchModels.kt` | `MatchCategory` enum, `MatchListItem` 에 category/authorUserId/distanceM/latitude/longitude 추가 |
| `data/network/api/AuthApiService.kt` | `getMatches(...)` 에 sort/category/maxDistance/lat/lng 쿼리 |
| `data/repository/MatchRepository.kt` | `getMatchList(...)` 시그니처 확장 |
| `ui/viewmodel/MatchingViewModel.kt` | `MatchSort`/`DistanceFilter` enum, 상태 확장(필터·정렬·페이지·refresh·newCount), 헬퍼들 |
| `ui/screen/MatchingScreen.kt` | TopBar/탭/시트/카드/빈상태/스켈레톤/안내배너 — 전반 재작성 |

---

## Task R1 — Lucide 아이콘 5개

**Files:** `app/src/main/res/drawable/ic_paw.xml`, `ic_stethoscope.xml`, `ic_shopping_cart.xml`, `ic_car.xml`, `ic_users.xml`

모든 파일 viewport 24×24, stroke `#1E120A` width 2, round cap/join, fillColor transparent, tint `?attr/colorControlNormal`.

- [ ] **Step R1.1 — `ic_paw.xml`** (Lucide `paw-print` 변형):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M11 4a2 2 0 1 1 0 .01"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M18 8a2 2 0 1 1 0 .01"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M20 16a2 2 0 1 1 0 .01"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M4 8a2 2 0 1 1 0 .01"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M9 13a4 4 0 0 0-4 4c0 2 1 4 3 4s3-2 5-2 3 2 5 2 3-2 3-4-2-4-4-4Z"/>
</vector>
```

- [ ] **Step R1.2 — `ic_stethoscope.xml`** (Lucide `stethoscope`):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M4.8 2.3A.3.3 0 0 0 5 2H4a2 2 0 0 0-2 2v5a6 6 0 0 0 6 6 6 6 0 0 0 6-6V4a2 2 0 0 0-2-2h-1a.2.2 0 0 0-.2.3"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M8 15v1a6 6 0 0 0 6 6 6 6 0 0 0 6-6v-4"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M20 12a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z"/>
</vector>
```

- [ ] **Step R1.3 — `ic_shopping_cart.xml`** (Lucide `shopping-cart`):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M9 22a1 1 0 1 0 0-2 1 1 0 0 0 0 2Z"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M20 22a1 1 0 1 0 0-2 1 1 0 0 0 0 2Z"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
</vector>
```

- [ ] **Step R1.4 — `ic_car.xml`** (Lucide `car`):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9L18 8.5l-2.7-3.6a1 1 0 0 0-.8-.4H5.5a2 2 0 0 0-1.8 1.1l-.8 1.6A6 6 0 0 0 2 9.5V16c0 .6.4 1 1 1h2"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M14 17H9"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M6.5 19.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M16.5 19.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z"/>
</vector>
```

- [ ] **Step R1.5 — `ic_users.xml`** (Lucide `users`):

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M23 21v-2a4 4 0 0 0-3-3.87"/>
  <path android:strokeColor="#1E120A" android:strokeWidth="2"
      android:strokeLineCap="round" android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="M16 3.13a4 4 0 0 1 0 7.75"/>
</vector>
```

- [ ] **Step R1.6 — Compile + Commit**

```bash
./gradlew :app:assembleDebug 2>&1 | tail -5
git add app/src/main/res/drawable/ic_paw.xml \
        app/src/main/res/drawable/ic_stethoscope.xml \
        app/src/main/res/drawable/ic_shopping_cart.xml \
        app/src/main/res/drawable/ic_car.xml \
        app/src/main/res/drawable/ic_users.xml
git commit -m "feat: 매칭 화면용 Lucide 아이콘 5개 추가"
```

---

## Task R2 — MatchModels 확장 + API 시그니처 + 직렬화 테스트

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/data/model/MatchModels.kt`
- Modify: `app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt`
- Modify: `app/src/main/java/com/example/siheunggagae/data/repository/MatchRepository.kt`
- Create: `app/src/test/java/com/example/siheunggagae/MatchModelsTest.kt`

- [ ] **Step R2.1 — `MatchCategory` enum + `MatchListItem` 확장**

`MatchModels.kt` 의 `MatchListItem` 위쪽에 추가:

```kotlin
enum class MatchCategory { WALK, VET, SHOPPING, MOVE, VOLUNTEER }

/** 봉사자 자격이 있어야 작성·신청 가능한 카테고리. 백엔드와 협의 후 확정. */
fun MatchCategory.requiresVolunteerRole(): Boolean = when (this) {
    MatchCategory.VET, MatchCategory.VOLUNTEER -> true
    else -> false
}
```

`MatchListItem` 에 새 필드 추가 (기존 필드 모두 유지):

```kotlin
data class MatchListItem(
    @SerializedName("match_id") val matchId: Int? = null,
    val title: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("desired_date") val desiredDate: String? = null,
    @SerializedName("desired_time") val desiredTime: String? = null,
    val status: String? = null,
    @SerializedName("author_nickname") val authorNickname: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("applications_count") val applicationsCount: Int? = null,
    @SerializedName("matched_applicant_nickname") val matchedApplicantNickname: String? = null,
    @SerializedName("unread_message_count") val unreadMessageCount: Int = 0,
    @SerializedName("my_application_status") val myApplicationStatus: String? = null,
    @SerializedName("received_rating") val receivedRating: Int? = null,
    // ── 신규 (매칭 화면 개선) ──
    val category: MatchCategory? = null,
    @SerializedName(value = "author_user_id", alternate = ["authorUserId"])
    val authorUserId: Int? = null,
    @SerializedName(value = "distance_m", alternate = ["distanceM"])
    val distanceM: Double? = null,
)
```

- [ ] **Step R2.2 — API 쿼리 파라미터 추가**

`AuthApiService.kt:189-197` 의 `getMatches` 시그니처를 다음으로 교체:

```kotlin
@GET("api/v1/matches")
suspend fun getMatches(
    @Query("status") status: String? = null,
    @Query("region") region: String? = null,
    @Query("from_date") fromDate: String? = null,
    @Query("to_date") toDate: String? = null,
    @Query("page") page: Int? = null,
    @Query("size") size: Int? = null,
    @Query("sort") sort: String? = null,
    @Query("category") category: String? = null,
    @Query("max_distance") maxDistance: Int? = null,
    @Query("lat") lat: Double? = null,
    @Query("lng") lng: Double? = null,
): Response<MatchListResponse>
```

- [ ] **Step R2.3 — Repository 시그니처 확장**

`MatchRepository.kt` 의 `getMatchList` 를 다음으로 교체:

```kotlin
class MatchRepository {
    private val api = RetrofitClient.api

    suspend fun getMatchList(
        status: String? = null,
        page: Int? = 1,
        size: Int? = 20,
        sort: String? = null,
        category: String? = null,
        maxDistance: Int? = null,
        lat: Double? = null,
        lng: Double? = null,
    ): Result<MatchListResponse> {
        return try {
            val response = api.getMatches(
                status = status,
                page = page,
                size = size,
                sort = sort,
                category = category,
                maxDistance = maxDistance,
                lat = lat,
                lng = lng,
            )
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(IllegalStateException("Empty body"))
            } else {
                Result.failure(IllegalStateException("HTTP ${response.code()}"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
```

(기존 다른 메서드는 그대로 둠)

- [ ] **Step R2.4 — 직렬화 테스트**

Create `app/src/test/java/com/example/siheunggagae/MatchModelsTest.kt`:

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.model.MatchListResponse
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MatchModelsTest {
    private val gson = Gson()

    @Test
    fun deserializes_new_fields() {
        val json = """
            {"items":[{
              "match_id": 7, "title": "산책", "address": "정왕동",
              "latitude": 37.3752, "longitude": 126.7281,
              "desired_date": "2026-05-25", "desired_time": "14:00",
              "status": "RECRUITING",
              "category": "WALK",
              "author_user_id": 14,
              "distance_m": 350.0
            }]}
        """.trimIndent()
        val parsed = gson.fromJson(json, MatchListResponse::class.java)
        val item = parsed.items!![0]
        assertEquals(MatchCategory.WALK, item.category)
        assertEquals(14, item.authorUserId)
        assertEquals(350.0, item.distanceM!!, 0.01)
    }

    @Test
    fun missing_new_fields_default_null() {
        val json = """
            {"items":[{"match_id": 1, "title": "X", "status": "RECRUITING"}]}
        """.trimIndent()
        val item = gson.fromJson(json, MatchListResponse::class.java).items!![0]
        assertNull(item.category)
        assertNull(item.authorUserId)
        assertNull(item.distanceM)
    }

    @Test
    fun camelCase_alternate_keys_work() {
        val json = """
            {"items":[{"match_id":1,"authorUserId":42,"distanceM":120.5}]}
        """.trimIndent()
        val item = gson.fromJson(json, MatchListResponse::class.java).items!![0]
        assertEquals(42, item.authorUserId)
        assertEquals(120.5, item.distanceM!!, 0.01)
    }
}
```

- [ ] **Step R2.5 — Run tests**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.MatchModelsTest"
```

Expected: 3 tests passed.

- [ ] **Step R2.6 — Commit**

```bash
git add app/src/main/java/com/example/siheunggagae/data/model/MatchModels.kt \
        app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt \
        app/src/main/java/com/example/siheunggagae/data/repository/MatchRepository.kt \
        app/src/test/java/com/example/siheunggagae/MatchModelsTest.kt
git commit -m "feat: 매칭 모델 확장(category/authorUserId/distanceM) + API 쿼리 + 직렬화 테스트"
```

---

## Task R3 — ViewModel + 헬퍼 + 단위 테스트 (TDD)

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/MatchingViewModel.kt`
- Create: `app/src/test/java/com/example/siheunggagae/MatchImminenceTest.kt`
- Create: `app/src/test/java/com/example/siheunggagae/MatchHelpersTest.kt`

- [ ] **Step R3.1 — `MatchImminenceTest.kt` 먼저 작성**

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.ui.viewmodel.Imminence
import com.example.siheunggagae.ui.viewmodel.computeImminence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class MatchImminenceTest {
    // KST 고정 now: 2026-05-24 10:00
    private val now = LocalDateTime.of(2026, 5, 24, 10, 0)
        .atZone(ZoneId.of("Asia/Seoul")).toInstant()

    @Test fun returns_null_for_past() {
        // 1시간 전
        val result = computeImminence("2026-05-24", "09:00", now)
        assertNull(result)
    }

    @Test fun critical_6h_when_within_6h() {
        // 4시간 후
        val result = computeImminence("2026-05-24", "14:00", now)
        assertEquals(Imminence.CRITICAL_6H, result)
    }

    @Test fun today_24h_when_within_24h() {
        // 14시간 후
        val result = computeImminence("2026-05-25", "00:00", now)
        assertEquals(Imminence.TODAY_24H, result)
    }

    @Test fun tomorrow_d1_when_within_48h() {
        // 30시간 후
        val result = computeImminence("2026-05-25", "16:00", now)
        assertEquals(Imminence.TOMORROW_D1, result)
    }

    @Test fun null_when_beyond_48h() {
        // 72시간 후
        val result = computeImminence("2026-05-27", "10:00", now)
        assertNull(result)
    }

    @Test fun null_when_invalid_format() {
        assertNull(computeImminence("invalid", "14:00", now))
        assertNull(computeImminence("2026-05-24", "bad", now))
    }
}
```

- [ ] **Step R3.2 — `MatchHelpersTest.kt`**

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.requiresVolunteerRole
import com.example.siheunggagae.ui.viewmodel.diffNewMatchIds
import com.example.siheunggagae.ui.viewmodel.walkingMinutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchHelpersTest {

    @Test fun walkingMinutes_floor_to_minimum_1() {
        assertEquals(1, walkingMinutes(50.0))   // 50m / 67 ~ 0.75 → 1 (min clamp)
        assertEquals(1, walkingMinutes(67.0))   // 1
        assertEquals(7, walkingMinutes(500.0))  // 500 / 67 ~ 7.46 → 7
        assertEquals(29, walkingMinutes(2000.0))// 2000 / 67 ~ 29.85 → 29
    }

    @Test fun requiresVolunteerRole_VET_and_VOLUNTEER_true() {
        assertTrue(MatchCategory.VET.requiresVolunteerRole())
        assertTrue(MatchCategory.VOLUNTEER.requiresVolunteerRole())
    }

    @Test fun requiresVolunteerRole_others_false() {
        assertFalse(MatchCategory.WALK.requiresVolunteerRole())
        assertFalse(MatchCategory.SHOPPING.requiresVolunteerRole())
        assertFalse(MatchCategory.MOVE.requiresVolunteerRole())
    }

    @Test fun diffNewMatchIds_returns_only_new() {
        val previous = setOf(1, 2, 3)
        val current  = listOf(2, 3, 4, 5)
        assertEquals(2, diffNewMatchIds(previous, current))  // 4, 5
    }

    @Test fun diffNewMatchIds_zero_when_no_new() {
        assertEquals(0, diffNewMatchIds(setOf(1, 2, 3), listOf(1, 2, 3)))
        assertEquals(0, diffNewMatchIds(setOf(1, 2, 3), listOf(2)))  // 줄어든 건 새 아님
    }
}
```

- [ ] **Step R3.3 — Run tests, expect FAIL**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.MatchImminenceTest" --tests "com.example.siheunggagae.MatchHelpersTest" 2>&1 | tail -10
```

Expected: BUILD FAILED — `Imminence`, `computeImminence`, `walkingMinutes`, `diffNewMatchIds` 미정의.

- [ ] **Step R3.4 — `MatchingViewModel.kt` 본체 재작성**

기존 `MatchingViewModel.kt` 를 다음 내용으로 전체 교체 (기존은 단순 status 필터 + getMatchList 만 호출):

```kotlin
package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class MatchSort { IMMINENT, RECENT, NEAREST }
enum class DistanceFilter(val meters: Int?) {
    KM1(1000), KM3(3000), KM5(5000), ALL(null)
}
enum class Imminence { CRITICAL_6H, TODAY_24H, TOMORROW_D1 }

private val KST = ZoneId.of("Asia/Seoul")
private val DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")

internal fun computeImminence(
    desiredDate: String?,
    desiredTime: String?,
    now: Instant = Instant.now(),
): Imminence? {
    if (desiredDate.isNullOrBlank() || desiredTime.isNullOrBlank()) return null
    val date = runCatching { java.time.LocalDate.parse(desiredDate, DATE_FMT) }.getOrNull() ?: return null
    val time = runCatching { java.time.LocalTime.parse(desiredTime, TIME_FMT) }.getOrNull() ?: return null
    val target = LocalDateTime.of(date, time).atZone(KST).toInstant()
    val deltaSec = target.epochSecond - now.epochSecond
    return when {
        deltaSec < 0 -> null
        deltaSec < 6 * 3600 -> Imminence.CRITICAL_6H
        deltaSec < 24 * 3600 -> Imminence.TODAY_24H
        deltaSec < 48 * 3600 -> Imminence.TOMORROW_D1
        else -> null
    }
}

internal fun walkingMinutes(distanceM: Double): Int =
    (distanceM / 67.0).toInt().coerceAtLeast(1)

internal fun diffNewMatchIds(previous: Set<Int>, current: List<Int>): Int =
    current.count { it !in previous }

sealed class MatchingUi {
    object Loading : MatchingUi()
    data class Success(
        val items: List<MatchListItem>,
        val statusTabCounts: Map<String?, Int>,    // null=전체
        val selectedStatus: String?,                 // null=전체 / "RECRUITING" / "REVIEWING" / "IN_PROGRESS" / "DONE"
        val selectedCategory: MatchCategory?,
        val sort: MatchSort,
        val distance: DistanceFilter,
        val hasMore: Boolean,
        val isRefreshing: Boolean,
        val newCount: Int,
        val showVolunteerWarning: Boolean,           // 비봉사자가 봉사자 전용 카테고리 선택 시
    ) : MatchingUi()
    data class Error(val message: String) : MatchingUi()
}

class MatchingViewModel(
    private val repository: MatchRepository,
    private val isCurrentUserVolunteer: () -> Boolean,
    private val getCurrentLocation: () -> Pair<Double, Double>?,
) : ViewModel() {

    private val _state = MutableStateFlow<MatchingUi>(MatchingUi.Loading)
    val state: StateFlow<MatchingUi> = _state

    private var raw: List<MatchListItem> = emptyList()
    private var page: Int = 1
    private var lastIdSet: Set<Int> = emptySet()
    private var newCount: Int = 0

    private var selectedStatus: String? = null
    private var selectedCategory: MatchCategory? = null
    private var sort: MatchSort = MatchSort.IMMINENT
    private var distance: DistanceFilter = DistanceFilter.KM5

    init { fetch(reset = true) }

    fun setStatus(status: String?) { if (selectedStatus == status) return; selectedStatus = status; fetch(reset = true) }
    fun setCategory(category: MatchCategory?) { if (selectedCategory == category) return; selectedCategory = category; fetch(reset = true) }
    fun setSort(s: MatchSort) {
        if (sort == s) return
        // NEAREST 는 위치 권한 + lat/lng 필수
        if (s == MatchSort.NEAREST && getCurrentLocation() == null) return
        sort = s
        fetch(reset = true)
    }
    fun setDistance(d: DistanceFilter) { if (distance == d) return; distance = d; fetch(reset = true) }
    fun refresh() = fetch(reset = true, isRefresh = true)
    fun loadMore() {
        val s = _state.value as? MatchingUi.Success ?: return
        if (!s.hasMore || s.isRefreshing) return
        fetch(reset = false)
    }
    fun dismissNewCount() {
        newCount = 0
        recompute()
    }

    private fun fetch(reset: Boolean, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (reset && !isRefresh) _state.value = MatchingUi.Loading
            if (isRefresh) recompute(refreshing = true)
            val (lat, lng) = getCurrentLocation() ?: (null to null)
            val targetPage = if (reset) 1 else (page + 1)
            val result = repository.getMatchList(
                status = selectedStatus,
                page = targetPage,
                size = 20,
                sort = sort.name.lowercase(),
                category = selectedCategory?.name,
                maxDistance = distance.meters,
                lat = lat,
                lng = lng,
            )
            result.onSuccess { resp ->
                val newItems = resp.items.orEmpty()
                val incomingIds = newItems.mapNotNull { it.matchId }
                if (reset) {
                    // 새 N건 감지 (이전 lastIdSet 과 비교)
                    newCount = if (lastIdSet.isNotEmpty()) diffNewMatchIds(lastIdSet, incomingIds) else 0
                    raw = newItems
                    page = 1
                    lastIdSet = incomingIds.toSet()
                } else {
                    raw = raw + newItems
                    page = targetPage
                    lastIdSet = (lastIdSet + incomingIds).toSet()
                }
                recompute(hasMore = newItems.size >= 20)
            }.onFailure { err ->
                _state.value = MatchingUi.Error(err.message ?: "매칭을 불러오지 못했어요")
            }
        }
    }

    private fun recompute(
        hasMore: Boolean? = null,
        refreshing: Boolean = false,
    ) {
        val current = _state.value as? MatchingUi.Success
        val counts = computeStatusTabCounts(raw)
        val showWarn = selectedCategory?.let {
            it.requiresVolunteerRole() && !isCurrentUserVolunteer()
        } ?: false
        _state.value = MatchingUi.Success(
            items = raw,
            statusTabCounts = counts,
            selectedStatus = selectedStatus,
            selectedCategory = selectedCategory,
            sort = sort,
            distance = distance,
            hasMore = hasMore ?: current?.hasMore ?: true,
            isRefreshing = refreshing,
            newCount = newCount,
            showVolunteerWarning = showWarn,
        )
    }

    private fun computeStatusTabCounts(items: List<MatchListItem>): Map<String?, Int> {
        val byStatus = items.groupBy { it.status }
        return mapOf(
            null to items.size,
            "RECRUITING" to (byStatus["RECRUITING"]?.size ?: 0),
            "REVIEWING" to (byStatus["REVIEWING"]?.size ?: 0),
            "IN_PROGRESS" to (byStatus["IN_PROGRESS"]?.size ?: 0),
            "DONE" to (byStatus["DONE"]?.size ?: 0),
        )
    }

    class Factory(
        private val repository: MatchRepository,
        private val isCurrentUserVolunteer: () -> Boolean,
        private val getCurrentLocation: () -> Pair<Double, Double>?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MatchingViewModel(repository, isCurrentUserVolunteer, getCurrentLocation) as T
    }
}

// requiresVolunteerRole 는 MatchModels.kt 에 정의됨 (R2)
private fun MatchCategory.requiresVolunteerRole(): Boolean =
    com.example.siheunggagae.data.model.requiresVolunteerRole(this)
```

> **참고**: `MatchModels.kt` 의 `requiresVolunteerRole` 가 `MatchCategory` 의 확장 함수로 정의돼 있다면 이 import 만으로 충분. 위 마지막 `private fun` 은 불필요 — 작성 시점에 import 가능하면 그대로 사용하고, 이 helper 라인은 제거.

- [ ] **Step R3.5 — Run tests, expect PASS**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.MatchImminenceTest" --tests "com.example.siheunggagae.MatchHelpersTest" 2>&1 | tail -10
```

Expected: 10 tests passed (6 + 4).

- [ ] **Step R3.6 — Commit**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/MatchingViewModel.kt \
        app/src/test/java/com/example/siheunggagae/MatchImminenceTest.kt \
        app/src/test/java/com/example/siheunggagae/MatchHelpersTest.kt
git commit -m "feat: MatchingViewModel 재작성 + computeImminence/walkingMinutes/diffNewMatchIds + 10 tests"
```

---

## Task R4 — 카드 디자인 본체

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt`

기존 `MatchingScreen.kt` (~416줄) 의 카드 부분(`MatchingRequestCard` 또는 동등 composable)을 spec §4.5 기준으로 교체. 다른 부분(TopBar/탭/FAB/BottomNav/Scaffold)은 R5 에서 처리.

- [ ] **Step R4.1 — 카드 composable 본체**

`MatchingScreen.kt` 안에 다음 private composable 추가 (또는 기존 `MatchingRequestCard` 교체):

```kotlin
@Composable
private fun MatchCardR(
    item: MatchListItem,
    isMine: Boolean,
    imminence: Imminence?,
    distanceLabel: String,                  // "정왕동 · 도보 22분" or "정왕동" 폴백
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val status = item.status ?: "RECRUITING"
    val cardAlpha = if (status == "DONE") 0.55f else 1f
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .alpha(cardAlpha)
            .clickable { onClick() },
    ) {
        Box {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                CardThumbnail(category = item.category)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Row1 — 칩들
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        StatusChip(status)
                        item.category?.let { cat ->
                            if (cat.requiresVolunteerRole()) VolunteerCatChip()
                        }
                        imminence?.let { ImminenceChip(it, item.desiredDate, item.desiredTime) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.title.orEmpty(),
                        color = TextBlackM,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_location_on),
                            contentDescription = null,
                            tint = Brown700M,
                            modifier = Modifier.size(11.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(distanceLabel, color = Brown700M, fontSize = 11.sp, maxLines = 1)
                        if (isMine) {
                            Spacer(Modifier.width(6.dp))
                            Text("·", color = PlaceholderM, fontSize = 11.sp)
                            Spacer(Modifier.width(6.dp))
                            Text("내가 작성", color = Brown700M, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    (item.applicationsCount ?: 0).takeIf { it > 0 }?.let { n ->
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_users),
                                contentDescription = null,
                                tint = if (isMine) Pink500M else Brown700M,
                                modifier = Modifier.size(11.dp),
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                if (isMine) "신청 ${n}명" else "신청 $n",
                                color = if (isMine) Pink500M else Brown700M,
                                fontSize = 11.sp,
                                fontWeight = if (isMine) FontWeight.ExtraBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
            // 우상단 ⋮
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.align(Alignment.TopEnd).size(28.dp).padding(end = 6.dp, top = 6.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = "더보기",
                    tint = Brown700M,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CardThumbnail(category: MatchCategory?) {
    val brush = when (category) {
        MatchCategory.WALK -> Brush.linearGradient(listOf(Color(0xFFFFEDD4), Color(0xFFF7A35B)))
        MatchCategory.VET -> Brush.linearGradient(listOf(Color(0xFFDCFCE7), Color(0xFF16A34A)))
        MatchCategory.SHOPPING -> Brush.linearGradient(listOf(Color(0xFFFEE7EC), Color(0xFFF04268)))
        MatchCategory.MOVE -> Brush.linearGradient(listOf(Color(0xFFDBEAFE), Color(0xFF388AF5)))
        MatchCategory.VOLUNTEER -> Brush.linearGradient(listOf(Color(0xFFD0FEE1), Color(0xFF00A63E)))
        null -> Brush.linearGradient(listOf(Color(0xFFF4F4F4), Color(0xFFE0E0E0)))
    }
    val iconRes = when (category) {
        MatchCategory.WALK -> R.drawable.ic_paw
        MatchCategory.VET -> R.drawable.ic_stethoscope
        MatchCategory.SHOPPING -> R.drawable.ic_shopping_cart
        MatchCategory.MOVE -> R.drawable.ic_car
        MatchCategory.VOLUNTEER -> R.drawable.ic_award
        null -> R.drawable.ic_handshake  // 일반 매칭 폴백
    }
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bg, fg, label) = when (status) {
        "RECRUITING" -> Triple(Color(0xFFFEE7EC), Color(0xFFE84B6A), "모집중")
        "REVIEWING" -> Triple(Color(0xFFFEF3C7), Color(0xFFCA8A04), "검토중")
        "IN_PROGRESS" -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "진행중")
        "DONE" -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), "완료")
        else -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), status)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) { Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun VolunteerCatChip() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF00A63E))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_award),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(10.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text("봉사자", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ImminenceChip(imm: Imminence, date: String?, time: String?) {
    val (bg, fg, label) = when (imm) {
        Imminence.CRITICAL_6H -> Triple(
            Color(0xFFFEE7EC), Color(0xFFF04268),
            "마감 임박" + (computeRemainingShort(date, time)?.let { " · $it" } ?: "")
        )
        Imminence.TODAY_24H -> Triple(
            Color(0xFFFFEDD4), Color(0xFFF7A35B),
            "오늘 " + (time?.take(5) ?: "")
        )
        Imminence.TOMORROW_D1 -> Triple(
            Color(0xFFF2F2F2), Color(0xFF8A6E58),
            "내일 · D-1"
        )
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_clock),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(10.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun computeRemainingShort(date: String?, time: String?): String? {
    if (date.isNullOrBlank() || time.isNullOrBlank()) return null
    val d = runCatching { java.time.LocalDate.parse(date) }.getOrNull() ?: return null
    val t = runCatching { java.time.LocalTime.parse(time) }.getOrNull() ?: return null
    val target = java.time.LocalDateTime.of(d, t).atZone(java.time.ZoneId.of("Asia/Seoul")).toInstant()
    val now = java.time.Instant.now()
    val sec = target.epochSecond - now.epochSecond
    return when {
        sec <= 0 -> null
        sec < 3600 -> "${sec / 60}m"
        sec < 6 * 3600 -> "${sec / 3600}h"
        else -> null
    }
}
```

상단 import (필요한 것 추가):

```kotlin
import androidx.compose.foundation.layout.FlowRow  // experimentalApi 필요 시 @OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.MatchListItem
import com.example.siheunggagae.data.model.requiresVolunteerRole
import com.example.siheunggagae.ui.viewmodel.Imminence
```

private val 색 토큰은 파일 상단(다른 화면 패턴 따라):

```kotlin
private val TextBlackM   = Color(0xFF1E120A)
private val Brown700M    = Color(0xFF8A6E58)
private val Brown900M    = Color(0xFF614B3A)
private val Pink500M     = Color(0xFFF04268)
private val PlaceholderM = Color(0xFFC1AEA0)
```

- [ ] **Step R4.2 — Compile**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
```

`FlowRow` 가 `@OptIn(ExperimentalLayoutApi::class)` 필요할 수 있음 — `MatchCardR` 위에 `@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)` 어노테이션 추가.

- [ ] **Step R4.3 — Commit**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt
git commit -m "feat: 매칭 카드 재설계 (썸네일/칩/⋮/임박 배지/본인 식별/완료 dim)"
```

---

## Task R5 — Pill 탭 + 정렬·거리 시트 + 카테고리 칩 + 봉사자 안내

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt`

R4 가 카드 본체 작성, 이 단계는 그 외 모든 상단 영역 (TopBar/탭/시트/카테고리/안내배너) 작성.

- [ ] **Step R5.1 — TopBar 재작성**

기존 TopBar 코드 교체:

```kotlin
@Composable
private fun MatchTopBar(onSearchClick: () -> Unit, onMyRequestsClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding()
            .padding(horizontal = 18.dp).padding(top = 4.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("매칭", color = TextBlackM, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.weight(1f))
        TopBarIconCard(R.drawable.ic_search) { onSearchClick() }
        Spacer(Modifier.width(8.dp))
        TopBarIconCard(R.drawable.ic_assignment) { onMyRequestsClick() }
    }
}

@Composable
private fun TopBarIconCard(iconRes: Int, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.size(40.dp).clickable { onClick() },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Brown700M,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
```

`ic_search` 가 없으면 R1 의 일부로 추가했어야 — `app/src/main/res/drawable/ic_search.xml` 이미 있음(검색 패턴). 없는 경우만 `ic_search_outline.xml` 사용으로 변경.

- [ ] **Step R5.2 — Pill 탭 row**

```kotlin
@Composable
private fun StatusTabRow(
    selected: String?,                       // null = 전체
    counts: Map<String?, Int>,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp),
    ) {
        item { PillTab("전체", selected == null, count = null) { onSelect(null) } }
        item { PillTab("모집중", selected == "RECRUITING", count = counts["RECRUITING"]) { onSelect("RECRUITING") } }
        item { PillTab("검토중", selected == "REVIEWING", count = counts["REVIEWING"]) { onSelect("REVIEWING") } }
        item { PillTab("진행중", selected == "IN_PROGRESS", count = counts["IN_PROGRESS"]) { onSelect("IN_PROGRESS") } }
        item { PillTab("완료", selected == "DONE", count = null) { onSelect("DONE") } }
    }
}

@Composable
private fun PillTab(label: String, on: Boolean, count: Int?, onClick: () -> Unit) {
    val bg = if (on) Color(0xFF1A1A1A) else Color.White
    val fg = if (on) Color.White else Brown700M
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(if (!on) Modifier.border(1.dp, Color(0xFFE8D3C2), RoundedCornerShape(50.dp)) else Modifier)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        val text = if (count != null && count > 0) "$label $count" else label
        Text(text, color = fg, fontSize = 13.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium)
    }
}
```

- [ ] **Step R5.3 — 정렬·거리 행 + 시트**

```kotlin
@Composable
private fun SortDistanceRow(
    sort: MatchSort,
    distance: DistanceFilter,
    locationAvailable: Boolean,
    onSortClick: () -> Unit,
    onDistanceClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 18.dp).padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SortDistancePill(label = sort.label(), onClick = onSortClick, active = true)
        SortDistancePill(label = distance.label(), onClick = onDistanceClick, active = distance != DistanceFilter.ALL)
    }
}

private fun MatchSort.label(): String = when (this) {
    MatchSort.IMMINENT -> "임박순"
    MatchSort.RECENT -> "최신순"
    MatchSort.NEAREST -> "가까운순"
}

private fun DistanceFilter.label(): String = when (this) {
    DistanceFilter.KM1 -> "1km 이내"
    DistanceFilter.KM3 -> "3km 이내"
    DistanceFilter.KM5 -> "5km 이내"
    DistanceFilter.ALL -> "전체 거리"
}

@Composable
private fun SortDistancePill(label: String, onClick: () -> Unit, active: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .border(
                1.dp,
                if (active) Brown900M else Color(0xFFE8D3C2),
                RoundedCornerShape(50.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = if (active) Brown900M else Brown700M,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            painter = painterResource(R.drawable.ic_chevron_down),
            contentDescription = null,
            tint = Brown700M,
            modifier = Modifier.size(12.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    current: MatchSort,
    locationAvailable: Boolean,
    onPick: (MatchSort) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            Text("정렬", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextBlackM)
            Spacer(Modifier.height(14.dp))
            SortOptionRow("임박순", "출발 시각이 가까운 순", current == MatchSort.IMMINENT) { onPick(MatchSort.IMMINENT); onDismiss() }
            SortOptionRow("최신순", "최근 작성된 순", current == MatchSort.RECENT) { onPick(MatchSort.RECENT); onDismiss() }
            SortOptionRow(
                "가까운순",
                if (locationAvailable) "내 위치에서 가까운 순" else "위치 권한이 필요해요 (비활성)",
                current == MatchSort.NEAREST,
                enabled = locationAvailable,
            ) { if (locationAvailable) { onPick(MatchSort.NEAREST); onDismiss() } }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    desc: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 10.dp)
            .alpha(if (enabled) 1f else 0.4f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = TextBlackM, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = Brown700M, fontSize = 12.sp)
        }
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Brown900M,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistanceSheet(
    current: DistanceFilter,
    onPick: (DistanceFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            Text("거리", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextBlackM)
            Spacer(Modifier.height(14.dp))
            SortOptionRow("1km 이내", "도보 15분 정도", current == DistanceFilter.KM1) { onPick(DistanceFilter.KM1); onDismiss() }
            SortOptionRow("3km 이내", "동네 한 바퀴", current == DistanceFilter.KM3) { onPick(DistanceFilter.KM3); onDismiss() }
            SortOptionRow("5km 이내", "근처 지역까지", current == DistanceFilter.KM5) { onPick(DistanceFilter.KM5); onDismiss() }
            SortOptionRow("전체 거리", "거리 제한 없이", current == DistanceFilter.ALL) { onPick(DistanceFilter.ALL); onDismiss() }
            Spacer(Modifier.height(12.dp))
        }
    }
}
```

- [ ] **Step R5.4 — 카테고리 칩 row + 봉사자 안내 배너**

```kotlin
@Composable
private fun CategoryChipsRow(
    selected: MatchCategory?,
    onSelect: (MatchCategory?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 14.dp),
    ) {
        item { CategoryChip(null, "전체", null, selected == null) { onSelect(null) } }
        item { CategoryChip(MatchCategory.WALK, "산책동행", R.drawable.ic_paw, selected == MatchCategory.WALK) { onSelect(MatchCategory.WALK) } }
        item { CategoryChip(MatchCategory.VET, "병원동행", R.drawable.ic_stethoscope, selected == MatchCategory.VET) { onSelect(MatchCategory.VET) } }
        item { CategoryChip(MatchCategory.SHOPPING, "장보기", R.drawable.ic_shopping_cart, selected == MatchCategory.SHOPPING) { onSelect(MatchCategory.SHOPPING) } }
        item { CategoryChip(MatchCategory.MOVE, "이동", R.drawable.ic_car, selected == MatchCategory.MOVE) { onSelect(MatchCategory.MOVE) } }
        item { CategoryChip(MatchCategory.VOLUNTEER, "봉사", R.drawable.ic_award, selected == MatchCategory.VOLUNTEER) { onSelect(MatchCategory.VOLUNTEER) } }
    }
}

@Composable
private fun CategoryChip(
    cat: MatchCategory?,
    label: String,
    iconRes: Int?,
    on: Boolean,
    onClick: () -> Unit,
) {
    val isVolunteer = cat?.requiresVolunteerRole() == true
    val bg = when {
        on -> Color(0xFF1A1A1A)
        isVolunteer -> Color(0xFFDCFCE7)
        else -> Color.White
    }
    val fg = when {
        on -> Color.White
        isVolunteer -> Color(0xFF16A34A)
        else -> Brown700M
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(
                if (!on) Modifier.border(
                    1.dp,
                    if (isVolunteer) Color(0xFF16A34A) else Color(0xFFE8D3C2),
                    RoundedCornerShape(50.dp),
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(label, color = fg, fontSize = 12.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun VolunteerWarningBanner(onApplyClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 18.dp).padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0FDF4))
            .border(1.dp, Color(0xFF00A63E), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_award),
            contentDescription = null,
            tint = Color(0xFF00A63E),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("봉사자 전용 카테고리", color = Color(0xFF00A63E), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(2.dp))
            Text(
                "이 카테고리는 봉사자 자격 보유자만 작성·신청할 수 있어요. 자격 신청을 먼저 해 주세요.",
                color = Brown700M,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF00A63E))
                .clickable { onApplyClick() }
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text("자격 신청", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
```

- [ ] **Step R5.5 — Compile + Commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
git add app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt
git commit -m "feat: 매칭 화면 TopBar/Pill 탭/정렬·거리 시트/카테고리 칩/봉사자 안내"
```

---

## Task R6 — pull-to-refresh + 새 N건 pill + 무한 스크롤 + 스켈레톤

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt`

- [ ] **Step R6.1 — `MatchingScreen` 본체 결합**

지금까지 만든 모든 composable 을 `MatchingScreen` 안에서 결합:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingScreen(
    viewModel: MatchingViewModel,
    onBack: () -> Unit = {},
    onMyRequestsClick: () -> Unit = {},
    onRequestFlowClick: () -> Unit = {},
    onCardClick: (matchId: Int, isMine: Boolean) -> Unit = { _, _ -> },
    onVolunteerApplyClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = (state as? MatchingUi.Success)?.isRefreshing == true,
        onRefresh = { viewModel.refresh() },
    )

    var showSortSheet by remember { mutableStateOf(false) }
    var showDistanceSheet by remember { mutableStateOf(false) }

    // 무한 스크롤 감지
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastIndex ->
                val success = state as? MatchingUi.Success ?: return@collect
                if (lastIndex != null && lastIndex >= success.items.size - 3 && success.hasMore) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(containerColor = Color(0xFFFEFEFE)) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).pullRefresh(pullRefreshState),
        ) {
            Column(Modifier.fillMaxSize()) {
                MatchTopBar(
                    onSearchClick = { /* this scope 외 */ },
                    onMyRequestsClick = onMyRequestsClick,
                )
                StatusTabRow(
                    selected = (state as? MatchingUi.Success)?.selectedStatus,
                    counts = (state as? MatchingUi.Success)?.statusTabCounts ?: emptyMap(),
                    onSelect = { viewModel.setStatus(it) },
                )
                SortDistanceRow(
                    sort = (state as? MatchingUi.Success)?.sort ?: MatchSort.IMMINENT,
                    distance = (state as? MatchingUi.Success)?.distance ?: DistanceFilter.KM5,
                    locationAvailable = true,  // R7에서 권한 처리 추가
                    onSortClick = { showSortSheet = true },
                    onDistanceClick = { showDistanceSheet = true },
                )
                CategoryChipsRow(
                    selected = (state as? MatchingUi.Success)?.selectedCategory,
                    onSelect = { viewModel.setCategory(it) },
                )
                if ((state as? MatchingUi.Success)?.showVolunteerWarning == true) {
                    VolunteerWarningBanner(onApplyClick = onVolunteerApplyClick)
                }

                when (val s = state) {
                    MatchingUi.Loading -> SkeletonCards()
                    is MatchingUi.Error -> ErrorBlock(s.message) { viewModel.refresh() }
                    is MatchingUi.Success -> {
                        if (s.items.isEmpty()) {
                            EmptyState(
                                onExpandDistance = { viewModel.setDistance(DistanceFilter.ALL) },
                                onCreate = onRequestFlowClick,
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 96.dp),
                            ) {
                                items(s.items, key = { it.matchId ?: 0 }) { item ->
                                    val isMine = item.authorUserId != null && /* TODO me */ false
                                        || (item.authorNickname == /* myNickname */ "")
                                    val imm = computeImminence(item.desiredDate, item.desiredTime)
                                    val distanceLabel = buildDistanceLabel(item)
                                    MatchCardR(
                                        item = item,
                                        isMine = isMine,
                                        imminence = imm,
                                        distanceLabel = distanceLabel,
                                        onClick = { onCardClick(item.matchId ?: return@MatchCardR, isMine) },
                                        onMoreClick = { /* R7에서 ⋮ 시트 */ },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // pull-to-refresh indicator
            PullRefreshIndicator(
                refreshing = (state as? MatchingUi.Success)?.isRefreshing == true,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = Color(0xFFF7A35B),
            )

            // 새 N건 floating pill
            val newCount = (state as? MatchingUi.Success)?.newCount ?: 0
            if (newCount > 0) {
                NewCountPill(
                    count = newCount,
                    onClick = {
                        viewModel.dismissNewCount()
                        // scroll top
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 200.dp),
                )
            }

            // FAB
            FloatingActionButton(
                onClick = onRequestFlowClick,
                containerColor = Color(0xFF9A7B5E),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 100.dp),
            ) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = "요청 작성")
            }
        }
    }

    if (showSortSheet) {
        SortSheet(
            current = (state as? MatchingUi.Success)?.sort ?: MatchSort.IMMINENT,
            locationAvailable = true,  // R7에서 권한
            onPick = { viewModel.setSort(it) },
            onDismiss = { showSortSheet = false },
        )
    }
    if (showDistanceSheet) {
        DistanceSheet(
            current = (state as? MatchingUi.Success)?.distance ?: DistanceFilter.KM5,
            onPick = { viewModel.setDistance(it) },
            onDismiss = { showDistanceSheet = false },
        )
    }
}

private fun buildDistanceLabel(item: MatchListItem): String {
    val addressShort = shortAddressM(item.address.orEmpty())
    val dist = item.distanceM
    return if (dist != null) {
        val unit = if (dist < 1000) "도보 ${walkingMinutes(dist)}분" else "%.1fkm".format(dist / 1000)
        "$addressShort · $unit"
    } else addressShort.ifBlank { "거리 미상" }
}

private fun shortAddressM(address: String): String {
    val parts = address.split(" ").filter { it.isNotBlank() }
    if (parts.size <= 1) return address
    return parts.drop(1).take(2).joinToString(" ").ifBlank { address }
}
```

- [ ] **Step R6.2 — 스켈레톤·새 N건 pill·에러 블록**

```kotlin
@Composable
private fun SkeletonCards() {
    Column {
        repeat(3) { i ->
            val alpha = 1f - (i * 0.2f)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .alpha(alpha),
            ) {
                Row(Modifier.padding(14.dp)) {
                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF4F4F4)))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Box(Modifier.height(14.dp).fillMaxWidth(0.5f).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF4F4F4)))
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.height(14.dp).fillMaxWidth(0.8f).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF4F4F4)))
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.height(10.dp).fillMaxWidth(0.6f).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF4F4F4)))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewCountPill(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF614B3A))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_refresh),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text("새 요청 ${count}건 · 탭하여 보기", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Brown700M, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRetry) { Text("다시 시도") }
        }
    }
}
```

- [ ] **Step R6.3 — pull-to-refresh import**

상단에 추가:

```kotlin
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
```

`pullRefresh` 는 Material 2 API. M3 에서는 `androidx.compose.material3.pulltorefresh.PullToRefreshBox` 사용 가능. compileSdk 36 이라 M3 API 사용 권장:

```kotlin
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

PullToRefreshBox(
    isRefreshing = (state as? MatchingUi.Success)?.isRefreshing == true,
    onRefresh = { viewModel.refresh() },
    modifier = Modifier.fillMaxSize().padding(padding),
) { /* content */ }
```

`PullToRefreshBox` 가 Box 자식 컨테이너 역할. 위 R6.1 의 outer Box 를 PullToRefreshBox 로 교체.

- [ ] **Step R6.4 — Compile + Commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
git add app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt
git commit -m "feat: 매칭 화면 pull-to-refresh/새 N건 pill/무한 스크롤/스켈레톤 결합"
```

---

## Task R7 — 빈 상태 + 위치 권한 + ⋮ 시트 + 점진 적용

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt`
- Modify: `app/src/main/java/com/example/siheunggagae/NavGraph.kt`

- [ ] **Step R7.1 — 빈 상태**

`MatchingScreen.kt` 에 추가:

```kotlin
@Composable
private fun EmptyState(onExpandDistance: () -> Unit, onCreate: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
            Box(
                modifier = Modifier
                    .size(96.dp).clip(CircleShape).background(Color(0xFFFFEDD4)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_paw),
                    contentDescription = null,
                    tint = Color(0xFFF7A35B),
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text("근처에 요청이 없어요", color = TextBlackM, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "거리를 더 넓게 보거나\n직접 요청을 작성해 보세요",
                color = Brown700M,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onExpandDistance,
                    border = BorderStroke(1.dp, Brown900M),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("거리 넓히기", color = Brown900M, fontWeight = FontWeight.Bold) }
                Button(
                    onClick = onCreate,
                    colors = ButtonDefaults.buttonColors(containerColor = Brown900M),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("요청 작성", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
```

- [ ] **Step R7.2 — ⋮ 메뉴 ModalBottomSheet**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardMoreSheet(
    isMine: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
            if (isMine) {
                MoreRow("수정") { onEdit(); onDismiss() }
                MoreRow("끌어올림 (다시 알리기)", enabled = false) {
                    // 토스트
                }
                MoreRow("삭제", danger = true) { onDelete(); onDismiss() }
            } else {
                MoreRow("공유하기") { onShare(); onDismiss() }
                MoreRow("신고하기", enabled = false) { /* TBD */ }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MoreRow(label: String, danger: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp)
            .alpha(if (enabled) 1f else 0.4f),
    ) {
        Text(
            label,
            color = if (danger) Color(0xFFF04268) else TextBlackM,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
```

`MatchingScreen` 안에서 `cardMoreState` state hoisting 으로 어떤 카드의 ⋮ 가 열렸는지 추적 + onMoreClick 콜백.

- [ ] **Step R7.3 — 위치 권한 처리**

`MatchingScreen` 안에 추가:

```kotlin
val context = LocalContext.current
val locationPermission = rememberLocationPermissionState { granted ->
    if (granted) { /* viewModel 에 즉시 알릴 필요 없음 - Factory 에 클로저 전달 */ }
}
val locationAvailable = locationPermission.hasPermission
```

ViewModel Factory 호출 시 `getCurrentLocation = { ... }` 람다 안에서 `LocationProvider.getLocationOrNull()?.let { it.latitude to it.longitude }` 로 lat/lng 반환.

`SortDistanceRow`, `SortSheet` 호출에 `locationAvailable = locationAvailable` 전달.

- [ ] **Step R7.4 — NavGraph wiring 업데이트**

`NavGraph.kt` 의 `Screen.Matching.route` composable 블록에서 ViewModel Factory + 새 콜백 wiring:

```kotlin
composable(Screen.Matching.route) {
    val app = LocalContext.current.applicationContext as SiheungGagaeApp
    val locationProvider = remember { LocationProvider(app) }
    val tokenManager = app.tokenManager
    val vm: MatchingViewModel = viewModel(
        factory = MatchingViewModel.Factory(
            repository = MatchRepository(),
            isCurrentUserVolunteer = { tokenManager.isVolunteer() }, // boolean accessor — 기존 토큰 매니저에서
            getCurrentLocation = {
                val loc = runBlocking { locationProvider.getLocationOrNull() }
                loc?.let { it.latitude to it.longitude }
            },
        ),
    )
    MatchingScreen(
        viewModel = vm,
        onMyRequestsClick = { navController.navigate(Screen.MyRequests.route) },
        onRequestFlowClick = { navController.navigate(Screen.RequestFlow.route) },
        onCardClick = { matchId, isMine ->
            val route = if (isMine) Screen.MatchingDetail.createRoute(matchId)
                       else Screen.MatchingPublicDetail.createRoute(matchId)
            navController.navigate(route)
        },
        onVolunteerApplyClick = { navController.navigate(Screen.VolunteerApply.route) },
    )
}
```

`tokenManager.isVolunteer()` 가 없으면 임시로 `{ false }` 람다 사용. 백엔드 응답에 user role 추가되면 진짜 boolean 으로.

- [ ] **Step R7.5 — Compile + Commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
git add app/src/main/java/com/example/siheunggagae/ui/screen/MatchingScreen.kt \
        app/src/main/java/com/example/siheunggagae/NavGraph.kt
git commit -m "feat: 매칭 화면 빈상태/⋮ 메뉴/위치 권한/NavGraph wiring"
```

---

## Task R8 — 수동 QA + docs

- [ ] **Step R8.1 — Build + install**

```bash
./gradlew :app:installDebug
```

- [ ] **Step R8.2 — 흐름 확인**

1. 매칭 탭 진입 → pill 탭 (전체/모집중/검토중/진행중/완료) 표시
2. 정렬 pill 탭 → 시트 → "임박순" 선택 / "가까운순" 비활성 (권한 거부 시) 확인
3. 거리 pill → 시트 → 5km/3km/1km/전체 선택
4. 카테고리 칩에서 "병원동행" 선택 (봉사자 자격 없는 사용자) → 안내 배너 + "자격 신청" 진입
5. pull-to-refresh → 새 N건 있으면 floating pill → 탭 → scroll top
6. 카드 ⋮ → 본인 글: 수정/끌어올림(disabled)/삭제 / 다른 사람: 공유/신고(disabled)
7. 카드 본체 탭 → 본인이면 MatchingDetail, 아니면 MatchingPublicDetail
8. 무한 스크롤 (3+ 페이지 fetch)
9. 빈 상태 (필터 조건 강하게 적용 후) → "거리 넓히기"/"요청 작성" CTA
10. 임박 배지 (6h 이내 카드, 24h 카드, D-1 카드) 색 분기 확인

- [ ] **Step R8.3 — lint + assemble**

```bash
./gradlew :app:lintDebug :app:assembleDebug 2>&1 | tail -10
```

- [ ] **Step R8.4 — docs/screens.md 갱신**

[MatchingScreen] 섹션 전체 교체. 새 항목들 반영 (P0+P1 14 항목).

```markdown
### [MatchingScreen] 매칭 리스트

- TopBar: "매칭" 26sp ExtraBold + 우측 ic_search(검색 진입, this scope 비활성)·ic_assignment(내 요청, MyRequests)
- Pill 상태 탭 (가로 스크롤): 전체 / 모집중 N / 검토중 N / 진행중 N / 완료. 선택 #1A1A1A bg White text 700, 미선택 White bg BorderBeige border Brown700 500
- 정렬·거리 pill (Brown900 border on active): "임박순 ▾" "5km 이내 ▾". 탭 시 ModalBottomSheet → 라디오 선택
- 카테고리 칩 (가로 스크롤): 전체 / 산책동행 (ic_paw) / 병원동행 (ic_stethoscope, 봉사자 전용 Green) / 장보기 (ic_shopping_cart) / 이동 (ic_car) / 봉사 (ic_award, 봉사자 전용 Green)
- 봉사자 안내 배너: 비봉사자 사용자가 봉사자 전용 카테고리 선택 시 #F0FDF4 bg Green600 border 노출 + "자격 신청" CTA → VolunteerApply
- 카드 (radius 16 shadow 1 padding 14): 60×60 카테고리 그라디언트 썸네일 + 칩 행(상태/봉사자/임박 배지) + 제목 15sp 800 + meta(주소·도보분·"· 내가 작성") + 신청자 수 + 우상단 ⋮
- 임박 배지: 6h 내 PinkSurface/Pink500 "마감 임박 · Nh" / 24h 내 OrangeSand/Orange500 "오늘 HH:MM" / D-1 TagGray/Brown700 "내일 · D-1"
- 완료(DONE) 카드: 전체 alpha 0.55
- 본인 글 식별: authorUserId 우선, fallback authorNickname. 우상단 ⋮ 메뉴(수정/끌어올림 disabled/삭제) + meta 라인 "· 내가 작성" + 신청자 수 Pink500 Bold
- pull-to-refresh + 새 N건 floating pill (Brown900) — refresh 후 신규 매칭 발견 시 상단 노출
- 무한 스크롤: 마지막 항목 -3 도달 시 다음 페이지 fetch (size 20)
- 스켈레톤: 로딩 시 카드 3장 placeholder (alpha 1.0/0.8/0.6)
- 빈 상태: 96dp OrangeSand 원형 + ic_paw 40dp Orange500 + "근처에 요청이 없어요" + "거리 넓히기"/"요청 작성" CTA 2개
- FAB: 56dp FABBrown ic_plus → RequestFlow
- ViewModel: MatchingViewModel(MatchRepository, isCurrentUserVolunteer, getCurrentLocation) — setStatus/setCategory/setSort/setDistance/refresh/loadMore/dismissNewCount
```

- [ ] **Step R8.5 — Commit**

```bash
git add docs/screens.md
git commit -m "docs: MatchingScreen 섹션 P0+P1 개선 반영"
```

---

## Self-Review

- **Spec 커버리지**: §4.1 TopBar(R5), §4.2 Pill 탭(R5), §4.3 정렬·거리(R5), §4.4 카테고리(R5), §4.5 카드(R4), §4.6 임박 배지(R4), §4.7 FAB(R6), §4.8 새 N건(R6), §4.9 빈 상태(R7), §4.10 스켈레톤(R6), §4.11 봉사자 안내(R5). §6 ViewModel(R3). §10 아이콘(R1). 모두 커버.
- **Placeholder 없음**: 모든 step 에 실제 코드. R7.3 의 `tokenManager.isVolunteer()` 가 미존재 시 `{ false }` 람다 사용 — 명시.
- **타입 일관성**: `MatchCategory` / `MatchSort` / `DistanceFilter` / `Imminence` / `MatchingUi` 일관. `computeImminence`/`walkingMinutes`/`diffNewMatchIds` R3에서 정의, R4·R6에서 호출.
- **의존 순서**: R1 → R2 → R3 → R4 → R5 → R6 → R7 → R8.
- **점진 적용**: spec §13 — 백엔드 미반영 시 `category null` → 칩 row 숨김(R5 의 조건 추가 — `MatchingScreen` 본체에서 `categoryAvailable` flag), `distanceM null` → 도보분 생략(R6.1 의 `buildDistanceLabel`), `authorUserId null` → nickname fallback(R6.1).
