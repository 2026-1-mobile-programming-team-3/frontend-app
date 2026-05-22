# 매장 추가/수정 요청 — 구현 플랜

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 사용자가 자기 매장을 지도에 추가하거나 정보를 수정 요청할 수 있는 흐름을 안드로이드 클라이언트에 구현. 신규 화면 4개 + 마이페이지·PlaceDetail 진입점 + 알림 deeplink 라우팅.

**Architecture:** 기존 시흥가개 Compose + Retrofit 단일 `AuthApiService` 패턴을 그대로 따라간다. 데이터 모델 → API/Repository → 아이콘 → NavGraph 스켈레톤 → 각 화면(목록 → 픽커 → 폼 → 상세)을 차례로 도입한다. 파일 업로드는 placeholder URL stub.

**Tech Stack:** Kotlin, Jetpack Compose, Retrofit2, Coil 3, Kakao Map SDK 2.12.8, Pretendard. 테스트는 JUnit (검증 로직 한정).

**Spec:** `docs/superpowers/specs/2026-05-22-store-request-design.md`

**필드 명명 규약 (모든 task 공통)**: 모든 DTO와 필드 접근은 **camelCase Kotlin + `@SerializedName(value="snake_case", alternate=["camelCase"])`** 패턴. 기존 `MapModels.kt` 일관성. 예: `item.requestId`, `payload.isPetAllowed`, `submitReq.targetStoreId`. 후속 task의 ViewModel/Screen 코드에서 plan 본문에 `snake_case`로 적힌 필드 참조가 있다면 camelCase로 바꿔 읽을 것 (예: `p.is_pet_allowed` → `p.isPetAllowed`).

---

## File Structure

### 신규 파일

```
app/src/main/java/com/example/siheunggagae/
├── data/
│   ├── model/
│   │   └── StoreRequestModels.kt          # DTO 일체
│   └── repository/
│       └── StoreRequestRepository.kt       # api 래퍼 (Response<T> 반환)
└── ui/
    ├── screen/
    │   ├── MyStoreRequestsScreen.kt        # 목록
    │   ├── StoreRequestFormScreen.kt       # ADD/UPDATE 통합 폼
    │   ├── StoreRequestDetailScreen.kt     # 상세 (PENDING/APPROVED/REJECTED)
    │   └── MapPinPickerScreen.kt           # 위치 핀 선택
    └── viewmodel/
        ├── MyStoreRequestsViewModel.kt
        ├── StoreRequestFormViewModel.kt
        ├── StoreRequestDetailViewModel.kt
        └── MapPinPickerViewModel.kt

app/src/main/res/drawable/
├── ic_coffee.xml
├── ic_utensils.xml
├── ic_trees.xml
├── ic_hotel.xml
├── ic_clock.xml
├── ic_check_circle.xml
├── ic_alert_circle.xml
├── ic_alert_triangle.xml
├── ic_plus.xml
├── ic_x.xml
├── ic_chevron_right.xml
├── ic_chevron_left.xml
├── ic_search_outline.xml
├── ic_map_pin.xml
├── ic_locate.xml
├── ic_paperclip.xml
├── ic_image.xml
├── ic_file_text.xml
├── ic_store.xml
├── ic_info_outline.xml
├── ic_pencil.xml
└── ic_lightbulb.xml

app/src/test/java/com/example/siheunggagae/
└── StoreRequestFormValidationTest.kt       # 검증 로직 단위 테스트
```

### 수정 파일

| 파일 | 변경 |
|---|---|
| `data/model/MapModels.kt` | `StoreDetailResponse`에 `is_owner: Boolean = false`, `owner_user_id: Int? = null` 추가 |
| `data/network/api/AuthApiService.kt` | 매장 요청 4개 메서드 + (필요 시) reverse-geocode 메서드 |
| `NavGraph.kt` | Screen 객체 4개 + composable 블록 4개 |
| `ui/screen/MyScreen.kt` | "내 기록" 섹션에 "내 매장 요청" 항목 추가 |
| `ui/screen/PlaceDetailScreen.kt` | "정보 수정 요청"/"이 매장 클레임하기" 버튼 추가 |
| `ui/screen/NotificationScreen.kt` | `link` deeplink 파서 분기 |
| `docs/gotcha.md` | placeholder URL stub 경고 한 줄 |

---

## Task 1 — Data Model

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/data/model/StoreRequestModels.kt`
- Modify: `app/src/main/java/com/example/siheunggagae/data/model/MapModels.kt` (StoreDetailResponse)
- Test: `app/src/test/java/com/example/siheunggagae/StoreRequestModelsTest.kt`

- [ ] **Step 1.1: 새 모델 파일 생성**

Create `app/src/main/java/com/example/siheunggagae/data/model/StoreRequestModels.kt`:

```kotlin
package com.example.siheunggagae.data.model

enum class StoreRequestType { ADD, UPDATE }
enum class StoreRequestStatus { PENDING, APPROVED, REJECTED }

data class StorePricingPlanInput(
    val plan_name: String,
    val price_krw: Int,
    val display_order: Int? = null,
)

data class StoreRequestPayload(
    val name: String? = null,
    val address: String? = null,
    val category: String? = null,
    val is_pet_allowed: Boolean? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val operating_hours: String? = null,
    val photo_urls: List<String>? = null,
    val plans: List<StorePricingPlanInput>? = null,
)

data class StoreRequestSubmitRequest(
    val type: StoreRequestType,
    val target_store_id: Int? = null,
    val payload: StoreRequestPayload,
    val proof_urls: List<String>? = null,
    val message: String? = null,
)

data class StoreRequestSubmitResponse(
    val request_id: Int,
    val type: StoreRequestType,
    val target_store_id: Int?,
    val status: StoreRequestStatus,
    val created_at: String,
)

data class StoreRequestItem(
    val request_id: Int,
    val type: StoreRequestType,
    val target_store_id: Int?,
    val payload: StoreRequestPayload,
    val proof_urls: List<String>,
    val message: String?,
    val status: StoreRequestStatus,
    val review_note: String?,
    val processed_at: String?,
    val created_at: String,
)

data class StoreRequestListResponse(
    val items: List<StoreRequestItem>,
    val total: Int,
    val page: Int,
    val size: Int,
)
```

- [ ] **Step 1.2: StoreDetailResponse 필드 추가**

Edit `MapModels.kt` — `StoreDetailResponse` data class에 두 필드 추가:

```kotlin
data class StoreDetailResponse(
    val store_id: Int,
    val name: String,
    val address: String,
    val phone: String?,
    val operating_hours: String?,
    val photo_urls: List<String>,
    val is_pet_allowed: Boolean,
    val rating_avg: Double,
    val review_pet_allowed_rate: Double,
    val is_favorited: Boolean,
    val is_owner: Boolean = false,
    val owner_user_id: Int? = null,
)
```

기본값으로 둬서 백엔드가 아직 응답에 안 박아도 빌드/파싱 깨지지 않음.

- [ ] **Step 1.3: 단위 테스트 — enum 직렬화 확인**

Create `app/src/test/java/com/example/siheunggagae/StoreRequestModelsTest.kt`:

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.model.StoreRequestType
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class StoreRequestModelsTest {
    private val gson = Gson()

    @Test
    fun storeRequestType_serializes_to_uppercase() {
        assertEquals("\"ADD\"", gson.toJson(StoreRequestType.ADD))
        assertEquals("\"UPDATE\"", gson.toJson(StoreRequestType.UPDATE))
    }

    @Test
    fun storeRequestStatus_serializes_to_uppercase() {
        assertEquals("\"PENDING\"", gson.toJson(StoreRequestStatus.PENDING))
        assertEquals("\"APPROVED\"", gson.toJson(StoreRequestStatus.APPROVED))
        assertEquals("\"REJECTED\"", gson.toJson(StoreRequestStatus.REJECTED))
    }
}
```

- [ ] **Step 1.4: 테스트 실행해서 통과 확인**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.StoreRequestModelsTest"
```

Expected: BUILD SUCCESSFUL, 2 tests passed.

- [ ] **Step 1.5: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/data/model/StoreRequestModels.kt \
        app/src/main/java/com/example/siheunggagae/data/model/MapModels.kt \
        app/src/test/java/com/example/siheunggagae/StoreRequestModelsTest.kt
git commit -m "feat: 매장 요청 DTO 및 StoreDetail owner 필드 추가"
```

---

## Task 2 — API Service + Repository

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt`
- Create: `app/src/main/java/com/example/siheunggagae/data/repository/StoreRequestRepository.kt`

- [ ] **Step 2.1: AuthApiService 메서드 추가**

`AuthApiService.kt`의 적절한 위치(다른 `maps/` 메서드 근처)에 추가:

```kotlin
@POST("maps/store-requests")
suspend fun submitStoreRequest(
    @Body body: StoreRequestSubmitRequest,
): Response<StoreRequestSubmitResponse>

@GET("maps/store-requests")
suspend fun getMyStoreRequests(
    @Query("status") status: String? = null,
    @Query("page") page: Int = 1,
    @Query("size") size: Int = 20,
): Response<StoreRequestListResponse>

@GET("maps/store-requests/{request_id}")
suspend fun getStoreRequest(
    @Path("request_id") requestId: Int,
): Response<StoreRequestItem>

@DELETE("maps/store-requests/{request_id}")
suspend fun cancelStoreRequest(
    @Path("request_id") requestId: Int,
): Response<Unit>
```

상단 import에 신규 모델들 추가.

- [ ] **Step 2.2: Repository 생성**

Create `app/src/main/java/com/example/siheunggagae/data/repository/StoreRequestRepository.kt`:

```kotlin
package com.example.siheunggagae.data.repository

import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.model.StoreRequestListResponse
import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.model.StoreRequestSubmitRequest
import com.example.siheunggagae.data.model.StoreRequestSubmitResponse
import com.example.siheunggagae.data.network.RetrofitClient
import retrofit2.Response

class StoreRequestRepository {
    private val api = RetrofitClient.api

    suspend fun submit(body: StoreRequestSubmitRequest): Response<StoreRequestSubmitResponse> =
        api.submitStoreRequest(body)

    suspend fun list(
        status: StoreRequestStatus? = null,
        page: Int = 1,
        size: Int = 20,
    ): Response<StoreRequestListResponse> =
        api.getMyStoreRequests(status = status?.name, page = page, size = size)

    suspend fun detail(requestId: Int): Response<StoreRequestItem> =
        api.getStoreRequest(requestId)

    suspend fun cancel(requestId: Int): Response<Unit> =
        api.cancelStoreRequest(requestId)
}
```

- [ ] **Step 2.3: 컴파일 확인**

Run:
```bash
./gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2.4: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt \
        app/src/main/java/com/example/siheunggagae/data/repository/StoreRequestRepository.kt
git commit -m "feat: StoreRequest API 엔드포인트 + Repository 추가"
```

---

## Task 3 — Lucide 스타일 아이콘 일괄 추가

**Files:** 22개 vector drawable 신규 생성. 모든 파일 공통:
- viewport 24x24
- stroke 2dp, round cap/join
- fillColor transparent, strokeColor `#1E120A`
- tint `?attr/colorControlNormal`

각 아이콘은 Lucide 공식 SVG(https://lucide.dev/icons/) 의 path를 Android vector drawable 문법으로 변환.

> **참고**: SVG path의 `M`/`L`/`H`/`V`/`C`/`Q`/`A`/`Z` 명령은 그대로 사용 가능. SVG에서 `stroke-linecap`/`stroke-linejoin`은 vector drawable의 `android:strokeLineCap`/`android:strokeLineJoin` 으로 옮긴다.

- [ ] **Step 3.1: 공통 템플릿**

각 아이콘은 다음 구조를 따른다:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
  <path
      android:strokeColor="#1E120A"
      android:strokeWidth="2"
      android:strokeLineCap="round"
      android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="…lucide path…"/>
</vector>
```

- [ ] **Step 3.2: 22개 파일 작성**

각 파일을 `app/src/main/res/drawable/<name>.xml` 로 생성. pathData는 lucide.dev에서 24x24 viewport SVG의 `d` 속성값을 복사. 아래 목록의 각 아이콘에 대응되는 Lucide 이름이 SVG 소스 키:

| 파일 | Lucide 키 | pathData (모두 합쳐서 안 됨, path 여러 개면 각각 별도 `<path>` 태그) |
|---|---|---|
| `ic_coffee.xml` | `coffee` | M17 8h1a4 4 0 1 1 0 8h-1; M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4Z; M6 2v2; M10 2v2; M14 2v2 |
| `ic_utensils.xml` | `utensils` | M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2; M7 2v20; M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7 |
| `ic_trees.xml` | `trees` | M10 10v.2A3 3 0 0 1 8.9 16v0H5v0h0a3 3 0 0 1-1-5.8V10a3 3 0 0 1 6 0Z; M7 16v6; M13 19h8.5a1 1 0 0 0 .8-1.6L17.4 11A4 4 0 0 1 17 7L17 7a3 3 0 0 0-3.4-3l-.6.1; M14 22v-3 |
| `ic_hotel.xml` | `hotel` | M10 22v-6.57; M12 11h.01; M12 7h.01; M14 15.43V22; M15 16a5 5 0 0 0-6 0; M16 11h.01; M16 7h.01; M8 11h.01; M8 7h.01; M2 22V4a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v18 |
| `ic_clock.xml` | `clock` | (circle, polyline) — Lucide circle은 `<path>`로 표현: M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z; M12 6v6l4 2 |
| `ic_check_circle.xml` | `check-circle-2` | M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10Z; m9 12 2 2 4-4 |
| `ic_alert_circle.xml` | `alert-circle` | M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10Z; M12 8v4; M12 16h.01 |
| `ic_alert_triangle.xml` | `alert-triangle` | m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z; M12 9v4; M12 17h.01 |
| `ic_plus.xml` | `plus` | M5 12h14; M12 5v14 |
| `ic_x.xml` | `x` | M18 6 6 18; m6 6 12 12 |
| `ic_chevron_right.xml` | `chevron-right` | m9 18 6-6-6-6 |
| `ic_chevron_left.xml` | `chevron-left` | m15 18-6-6 6-6 |
| `ic_search_outline.xml` | `search` | m21 21-4.3-4.3; M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z |
| `ic_map_pin.xml` | `map-pin` | M20 10c0 7-8 13-8 13s-8-6-8-13a8 8 0 0 1 16 0Z; M12 13a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z |
| `ic_locate.xml` | `locate-fixed` | M2 12h3; M19 12h3; M12 2v3; M12 19v3; M12 16a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z |
| `ic_paperclip.xml` | `paperclip` | m21.44 11.05-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48 |
| `ic_image.xml` | `image` | M19 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2Z; M8.5 10a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3Z; m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21 |
| `ic_file_text.xml` | `file-text` | M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z; M14 2v4a2 2 0 0 0 2 2h4; M10 9H8; M16 13H8; M16 17H8 |
| `ic_store.xml` | `store` | m2 7 4.41-4.41A2 2 0 0 1 7.83 2h8.34a2 2 0 0 1 1.42.59L22 7; M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8; M15 22v-4a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v4; M2 7h20; M22 7v3a2 2 0 0 1-2 2v0a2.7 2.7 0 0 1-1.59-.63.7.7 0 0 0-.82 0A2.7 2.7 0 0 1 16 12a2.7 2.7 0 0 1-1.59-.63.7.7 0 0 0-.82 0A2.7 2.7 0 0 1 12 12a2.7 2.7 0 0 1-1.59-.63.7.7 0 0 0-.82 0A2.7 2.7 0 0 1 8 12a2.7 2.7 0 0 1-1.59-.63.7.7 0 0 0-.82 0A2.7 2.7 0 0 1 4 12v0a2 2 0 0 1-2-2V7 |
| `ic_info_outline.xml` | `info` | M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10Z; M12 16v-4; M12 8h.01 |
| `ic_pencil.xml` | `pencil` | M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z; m15 5 4 4 |
| `ic_lightbulb.xml` | `lightbulb` | M15 14c.2-1 .7-1.7 1.5-2.5 1-.9 1.5-2.2 1.5-3.5A6 6 0 0 0 6 8c0 1 .2 2.2 1.5 3.5.7.7 1.3 1.5 1.5 2.5; M9 18h6; M10 22h4 |

> **참고**: pathData에 세미콜론(`;`)으로 묶은 path들은 각각 별도 `<path>` 태그로 쪼개야 함. lucide.dev에서 SVG 다운로드한 후 그대로 옮기는 게 가장 안전. 작성 후 Android Studio Vector Asset 미리보기로 모양 검증.

- [ ] **Step 3.3: 컴파일 + 빠른 시각 확인**

Run:
```bash
./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL.

Android Studio Project pane에서 22개 파일 우클릭 → "Show in Resource Manager"로 한 번에 미리보기 확인. 깨진 path 있는 경우 lucide.dev 원본으로 다시 받기.

- [ ] **Step 3.4: 커밋**

```bash
git add app/src/main/res/drawable/ic_coffee.xml \
        app/src/main/res/drawable/ic_utensils.xml \
        app/src/main/res/drawable/ic_trees.xml \
        app/src/main/res/drawable/ic_hotel.xml \
        app/src/main/res/drawable/ic_clock.xml \
        app/src/main/res/drawable/ic_check_circle.xml \
        app/src/main/res/drawable/ic_alert_circle.xml \
        app/src/main/res/drawable/ic_alert_triangle.xml \
        app/src/main/res/drawable/ic_plus.xml \
        app/src/main/res/drawable/ic_x.xml \
        app/src/main/res/drawable/ic_chevron_right.xml \
        app/src/main/res/drawable/ic_chevron_left.xml \
        app/src/main/res/drawable/ic_search_outline.xml \
        app/src/main/res/drawable/ic_map_pin.xml \
        app/src/main/res/drawable/ic_locate.xml \
        app/src/main/res/drawable/ic_paperclip.xml \
        app/src/main/res/drawable/ic_image.xml \
        app/src/main/res/drawable/ic_file_text.xml \
        app/src/main/res/drawable/ic_store.xml \
        app/src/main/res/drawable/ic_info_outline.xml \
        app/src/main/res/drawable/ic_pencil.xml \
        app/src/main/res/drawable/ic_lightbulb.xml
git commit -m "feat: Lucide stroke 스타일 아이콘 22개 추가"
```

---

## Task 4 — NavGraph 라우트 스켈레톤

화면을 본격 구현하기 전에 라우트만 먼저 박아두고 placeholder Composable로 연결. 이후 task에서 placeholder를 실제 화면으로 교체.

**Files:**
- Modify: `app/src/main/java/com/example/siheunggagae/NavGraph.kt`

- [ ] **Step 4.1: Screen 객체 4개 추가**

`Screen` sealed class 안(다른 object들 근처)에 다음 추가:

```kotlin
object MyStoreRequests : Screen("my_store_requests")

object StoreRequestForm : Screen(
    "store_request_form?type={type}&storeId={storeId}&requestId={requestId}",
) {
    const val ARG_TYPE = "type"
    const val ARG_STORE_ID = "storeId"
    const val ARG_REQUEST_ID = "requestId"

    fun createRoute(
        type: com.example.siheunggagae.data.model.StoreRequestType,
        storeId: Int? = null,
        requestId: Int? = null,
    ): String {
        val sb = StringBuilder("store_request_form?type=$type")
        storeId?.let { sb.append("&storeId=$it") }
        requestId?.let { sb.append("&requestId=$it") }
        return sb.toString()
    }
}

object StoreRequestDetail : Screen("store_request_detail/{requestId}") {
    const val ARG_REQUEST_ID = "requestId"
    fun createRoute(requestId: Int): String = "store_request_detail/$requestId"
}

object MapPinPicker : Screen("map_pin_picker?lat={lat}&lng={lng}") {
    const val ARG_LAT = "lat"
    const val ARG_LNG = "lng"
    const val RESULT_LAT = "picked_lat"
    const val RESULT_LNG = "picked_lng"
    const val RESULT_ADDRESS = "picked_address"

    fun createRoute(lat: Double? = null, lng: Double? = null): String {
        if (lat == null && lng == null) return "map_pin_picker"
        val parts = buildList {
            lat?.let { add("lat=$it") }
            lng?.let { add("lng=$it") }
        }
        return "map_pin_picker?" + parts.joinToString("&")
    }
}
```

- [ ] **Step 4.2: composable 4개 블록 추가 (placeholder)**

NavHost { ... } 내부에 4개 블록 추가. 각 블록은 임시로 단순 Text Composable로:

```kotlin
composable(Screen.MyStoreRequests.route) {
    androidx.compose.material3.Text("MyStoreRequests TODO")
}

composable(
    route = Screen.StoreRequestForm.route,
    arguments = listOf(
        androidx.navigation.navArgument(Screen.StoreRequestForm.ARG_TYPE) {
            type = androidx.navigation.NavType.StringType
            defaultValue = "ADD"
        },
        androidx.navigation.navArgument(Screen.StoreRequestForm.ARG_STORE_ID) {
            type = androidx.navigation.NavType.IntType
            defaultValue = -1
        },
        androidx.navigation.navArgument(Screen.StoreRequestForm.ARG_REQUEST_ID) {
            type = androidx.navigation.NavType.IntType
            defaultValue = -1
        },
    ),
) {
    androidx.compose.material3.Text("StoreRequestForm TODO")
}

composable(
    route = Screen.StoreRequestDetail.route,
    arguments = listOf(
        androidx.navigation.navArgument(Screen.StoreRequestDetail.ARG_REQUEST_ID) {
            type = androidx.navigation.NavType.IntType
        },
    ),
) {
    androidx.compose.material3.Text("StoreRequestDetail TODO")
}

composable(
    route = Screen.MapPinPicker.route,
    arguments = listOf(
        androidx.navigation.navArgument(Screen.MapPinPicker.ARG_LAT) {
            type = androidx.navigation.NavType.StringType
            nullable = true
            defaultValue = null
        },
        androidx.navigation.navArgument(Screen.MapPinPicker.ARG_LNG) {
            type = androidx.navigation.NavType.StringType
            nullable = true
            defaultValue = null
        },
    ),
) {
    androidx.compose.material3.Text("MapPinPicker TODO")
}
```

> 참고: lat/lng 은 Float/Double 타입으로 navArgument 직접 지원 안 됨 — String으로 받고 ViewModel에서 `toDoubleOrNull()` 변환.

- [ ] **Step 4.3: 컴파일 확인**

Run:
```bash
./gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4.4: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/NavGraph.kt
git commit -m "feat: 매장 요청 4개 라우트 스켈레톤 추가"
```

---

## Task 5 — MyStoreRequestsScreen + 마이페이지 진입점

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/MyStoreRequestsViewModel.kt`
- Create: `app/src/main/java/com/example/siheunggagae/ui/screen/MyStoreRequestsScreen.kt`
- Modify: `NavGraph.kt` (placeholder → 실제 호출)
- Modify: `app/src/main/java/com/example/siheunggagae/ui/screen/MyScreen.kt` (내 기록 섹션에 항목 추가 + 콜백)

- [ ] **Step 5.1: ViewModel 작성**

Create `ui/viewmodel/MyStoreRequestsViewModel.kt`:

```kotlin
package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.repository.StoreRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MyStoreRequestsUiState {
    object Loading : MyStoreRequestsUiState()
    data class Success(
        val items: List<StoreRequestItem>,
        val total: Int,
        val page: Int,
        val hasMore: Boolean,
        val filter: StoreRequestStatus?,
    ) : MyStoreRequestsUiState()
    data class Error(val message: String) : MyStoreRequestsUiState()
}

class MyStoreRequestsViewModel(
    private val repository: StoreRequestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<MyStoreRequestsUiState>(MyStoreRequestsUiState.Loading)
    val state: StateFlow<MyStoreRequestsUiState> = _state

    private var currentFilter: StoreRequestStatus? = null

    init { refresh() }

    fun setFilter(status: StoreRequestStatus?) {
        if (currentFilter == status) return
        currentFilter = status
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = MyStoreRequestsUiState.Loading
            val resp = repository.list(status = currentFilter, page = 1, size = 20)
            if (resp.isSuccessful) {
                val body = resp.body()!!
                _state.value = MyStoreRequestsUiState.Success(
                    items = body.items,
                    total = body.total,
                    page = 1,
                    hasMore = body.items.size >= 20,
                    filter = currentFilter,
                )
            } else {
                _state.value = MyStoreRequestsUiState.Error("목록을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    fun loadMore() {
        val s = _state.value as? MyStoreRequestsUiState.Success ?: return
        if (!s.hasMore) return
        viewModelScope.launch {
            val next = s.page + 1
            val resp = repository.list(status = currentFilter, page = next, size = 20)
            if (resp.isSuccessful) {
                val body = resp.body()!!
                _state.value = s.copy(
                    items = s.items + body.items,
                    page = next,
                    hasMore = body.items.size >= 20,
                )
            }
        }
    }

    class Factory(private val repository: StoreRequestRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MyStoreRequestsViewModel(repository) as T
    }
}
```

- [ ] **Step 5.2: Screen 작성**

Create `ui/screen/MyStoreRequestsScreen.kt` 시흥가개 패턴 따라:

```kotlin
package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.model.StoreRequestType
import com.example.siheunggagae.ui.viewmodel.MyStoreRequestsUiState
import com.example.siheunggagae.ui.viewmodel.MyStoreRequestsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

private val BackgroundQ = Color(0xFFFEFEFE)
private val TextBlackQ = Color(0xFF1E120A)
private val Brown700Q = Color(0xFF8A6E58)
private val Brown900Q = Color(0xFF614B3A)
private val BorderBeigeQ = Color(0xFFE8D3C2)
private val OrangeSandQ = Color(0xFFFFEDD4)
private val Orange500Q = Color(0xFFF7A35B)
private val FABBrownQ = Color(0xFF9A7B5E)
private val Gray300Q = Color(0xFFE8E8E8)
private val TagPendingBgQ = Color(0xFFFEF3C7)
private val TagPendingTextQ = Color(0xFFCA8A04)
private val TagApprovedBgQ = Color(0xFFF0FDF4)
private val TagApprovedTextQ = Color(0xFF16A34A)
private val TagRejectedBgQ = Color(0xFFFEE7EC)
private val TagRejectedTextQ = Color(0xFFE84B6A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoreRequestsScreen(
    viewModel: MyStoreRequestsViewModel? = null,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    val state by (viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(MyStoreRequestsUiState.Loading) })
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { last ->
                val s = state as? MyStoreRequestsUiState.Success ?: return@collect
                if (last != null && last >= s.items.size - 3 && s.hasMore) {
                    viewModel?.loadMore()
                }
            }
    }

    Scaffold(
        topBar = { TopBarBackTitle(title = "내 매장 요청", onBack = onBack) },
        containerColor = BackgroundQ,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNavigate(
                        com.example.siheunggagae.Screen.StoreRequestForm.createRoute(
                            type = StoreRequestType.ADD,
                        ),
                    )
                },
                containerColor = FABBrownQ,
                contentColor = Color.White,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = "매장 추가 요청",
                )
            }
        },
    ) { padding ->
        when (val s = state) {
            MyStoreRequestsUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Orange500Q) }

            is MyStoreRequestsUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = Brown700Q, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel?.refresh() }) { Text("다시 시도") }
                }
            }

            is MyStoreRequestsUiState.Success -> Column(
                Modifier.fillMaxSize().padding(padding),
            ) {
                Text(
                    "전체 ${s.total}건",
                    color = Brown700Q,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 18.dp, top = 4.dp, bottom = 12.dp),
                )
                FilterChipsRow(
                    selected = s.filter,
                    onSelect = { viewModel?.setFilter(it) },
                )
                if (s.items.isEmpty()) {
                    EmptyState(onAddClick = {
                        onNavigate(
                            com.example.siheunggagae.Screen.StoreRequestForm.createRoute(
                                type = StoreRequestType.ADD,
                            ),
                        )
                    })
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                    ) {
                        items(s.items, key = { it.request_id }) { item ->
                            StoreRequestCard(item) {
                                onNavigate(
                                    com.example.siheunggagae.Screen.StoreRequestDetail.createRoute(
                                        item.request_id,
                                    ),
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
private fun FilterChipsRow(
    selected: StoreRequestStatus?,
    onSelect: (StoreRequestStatus?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp),
    ) {
        item { FilterChip(label = "전체", isSelected = selected == null) { onSelect(null) } }
        item { FilterChip(label = "대기", isSelected = selected == StoreRequestStatus.PENDING) { onSelect(StoreRequestStatus.PENDING) } }
        item { FilterChip(label = "승인", isSelected = selected == StoreRequestStatus.APPROVED) { onSelect(StoreRequestStatus.APPROVED) } }
        item { FilterChip(label = "반려", isSelected = selected == StoreRequestStatus.REJECTED) { onSelect(StoreRequestStatus.REJECTED) } }
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) Color(0xFF1A1A1A) else Color.White
    val fg = if (isSelected) Color.White else Brown700Q
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .then(if (!isSelected) Modifier.border(1.dp, BorderBeigeQ, RoundedCornerShape(50.dp)) else Modifier)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StoreRequestCard(item: StoreRequestItem, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(categoryIcon(item.payload.category)),
                contentDescription = null,
                tint = Brown900Q,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypePill(item.type)
                    Spacer(Modifier.width(6.dp))
                    StatusTag(item.status)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    item.payload.name ?: "매장명 미입력",
                    color = TextBlackQ,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    formatSubLine(item),
                    color = Brown700Q,
                    fontSize = 12.sp,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Color(0xFFC1AEA0),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun TypePill(type: StoreRequestType) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(OrangeSandQ)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) { Text(type.name, color = Brown900Q, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun StatusTag(status: StoreRequestStatus) {
    val (bg, fg, label) = when (status) {
        StoreRequestStatus.PENDING -> Triple(TagPendingBgQ, TagPendingTextQ, "검토중")
        StoreRequestStatus.APPROVED -> Triple(TagApprovedBgQ, TagApprovedTextQ, "승인")
        StoreRequestStatus.REJECTED -> Triple(TagRejectedBgQ, TagRejectedTextQ, "반려")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) { Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
}

private fun formatSubLine(item: StoreRequestItem): String {
    val date = (item.processed_at ?: item.created_at).take(10)
    val cat = item.payload.category ?: "-"
    val word = if (item.status == StoreRequestStatus.PENDING) "신청" else "처리"
    return "$cat · $date $word"
}

private fun categoryIcon(category: String?): Int = when (category) {
    "CAFE" -> R.drawable.ic_coffee
    "RESTAURANT" -> R.drawable.ic_utensils
    "PARK" -> R.drawable.ic_trees
    "PET_HOTEL" -> R.drawable.ic_hotel
    else -> R.drawable.ic_store
}

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(OrangeSandQ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_store),
                    contentDescription = null,
                    tint = Orange500Q,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("아직 요청이 없어요", color = TextBlackQ, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("매장을 지도에 추가해보세요", color = Brown700Q, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Brown900Q),
                shape = RoundedCornerShape(12.dp),
            ) { Text("+ 매장 추가 요청", color = Color.White) }
        }
    }
}

@Composable
private fun TopBarBackTitle(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
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
                    tint = TextBlackQ,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            title,
            color = TextBlackQ,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun MyStoreRequestsScreenPreview() {
    MyStoreRequestsScreen()
}
```

- [ ] **Step 5.3: NavGraph placeholder 교체**

`NavGraph.kt`의 `Screen.MyStoreRequests.route` composable 블록을 다음으로 교체:

```kotlin
composable(Screen.MyStoreRequests.route) {
    val vm: MyStoreRequestsViewModel = viewModel(
        factory = MyStoreRequestsViewModel.Factory(StoreRequestRepository()),
    )
    MyStoreRequestsScreen(
        viewModel = vm,
        onBack = { navController.popBackStack() },
        onNavigate = { route -> navController.navigate(route) },
    )
}
```

상단 import 추가:
```kotlin
import com.example.siheunggagae.data.repository.StoreRequestRepository
import com.example.siheunggagae.ui.screen.MyStoreRequestsScreen
import com.example.siheunggagae.ui.viewmodel.MyStoreRequestsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
```

- [ ] **Step 5.4: 마이페이지 진입점 추가**

`MyScreen.kt`에서 "내 기록" 섹션 (보통 `"봉사 활동 이력"`, `"즐겨찾기 매장"` 항목이 모인 곳) 위쪽에 다음 항목을 끼워 넣는다. 기존 함수 시그니처에 콜백 추가:

```kotlin
@Composable
fun MyScreen(
    // 기존 파라미터 유지
    onMyStoreRequestsClick: () -> Unit = {},
)
```

기록 섹션 안 항목 추가 (다른 항목과 동일한 RecordRow 패턴):
```kotlin
RecordRow(
    iconRes = R.drawable.ic_store,
    label = "내 매장 요청",
    onClick = onMyStoreRequestsClick,
)
```

> RecordRow는 기존 MyScreen에 정의된 헬퍼. 다른 항목과 같은 시그니처로 호출. RecordRow가 없다면 기존 `"봉사 활동 이력"` 항목 코드를 그대로 복사해서 텍스트·아이콘만 교체.

- [ ] **Step 5.5: NavGraph MyScreen 호출에 콜백 연결**

NavGraph 안 `MyScreen(...)` 호출 두 곳(My 탭 / 그 외) 모두에 다음 추가:

```kotlin
onMyStoreRequestsClick = { navController.navigate(Screen.MyStoreRequests.route) },
```

- [ ] **Step 5.6: 앱 실행 + 수동 확인**

```bash
./gradlew :app:installDebug
```

기기/에뮬레이터에서:
- 마이 탭 진입 → "내 기록" 섹션에 "내 매장 요청" 항목 보임
- 항목 탭 → MyStoreRequestsScreen 진입 → 비어 있으면 EmptyState 정상 표시
- 백 → 마이로 복귀

- [ ] **Step 5.7: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/MyStoreRequestsViewModel.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/MyStoreRequestsScreen.kt \
        app/src/main/java/com/example/siheunggagae/NavGraph.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/MyScreen.kt
git commit -m "feat: MyStoreRequestsScreen + 마이페이지 진입점"
```

---

## Task 6 — MapPinPickerScreen

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/MapPinPickerViewModel.kt`
- Create: `app/src/main/java/com/example/siheunggagae/ui/screen/MapPinPickerScreen.kt`
- Modify: `AuthApiService.kt` (reverse geocode 메서드 — 없는 경우)
- Modify: `NavGraph.kt` (placeholder → 실제 호출)

- [ ] **Step 6.1: Reverse Geocode 메서드 확인 및 추가**

먼저 `AuthApiService.kt` 에 `/maps/geocode/reverse` 호출이 이미 정의돼 있는지 확인:
```bash
grep -n "geocode/reverse" app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt
```

없으면 추가:
```kotlin
@GET("maps/geocode/reverse")
suspend fun reverseGeocode(
    @Query("lat") lat: Double,
    @Query("lng") lng: Double,
): Response<ReverseGeocodeResponse>
```

응답 모델(없다면 `MapModels.kt`에 추가):
```kotlin
data class ReverseGeocodeResponse(
    val address: String,
    val region_dong: String? = null,
)
```

- [ ] **Step 6.2: ViewModel 작성**

Create `ui/viewmodel/MapPinPickerViewModel.kt`:

```kotlin
package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PinPickerUi(
    val lat: Double,
    val lng: Double,
    val address: String? = null,
    val resolving: Boolean = false,
)

class MapPinPickerViewModel(
    initialLat: Double,
    initialLng: Double,
) : ViewModel() {

    private val api = RetrofitClient.api
    private var resolveJob: Job? = null

    private val _state = MutableStateFlow(PinPickerUi(lat = initialLat, lng = initialLng))
    val state: StateFlow<PinPickerUi> = _state

    init { scheduleResolve(initialLat, initialLng) }

    fun onCameraIdle(lat: Double, lng: Double) {
        if (_state.value.lat == lat && _state.value.lng == lng) return
        _state.value = _state.value.copy(lat = lat, lng = lng, address = null)
        scheduleResolve(lat, lng)
    }

    private fun scheduleResolve(lat: Double, lng: Double) {
        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            delay(200)
            _state.value = _state.value.copy(resolving = true)
            try {
                val resp = api.reverseGeocode(lat, lng)
                if (resp.isSuccessful) {
                    _state.value = _state.value.copy(
                        address = resp.body()?.address,
                        resolving = false,
                    )
                } else {
                    _state.value = _state.value.copy(address = null, resolving = false)
                }
            } catch (_: Throwable) {
                _state.value = _state.value.copy(address = null, resolving = false)
            }
        }
    }

    class Factory(private val lat: Double, private val lng: Double) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MapPinPickerViewModel(lat, lng) as T
    }
}
```

- [ ] **Step 6.3: Screen 작성**

Create `ui/screen/MapPinPickerScreen.kt`:

```kotlin
package com.example.siheunggagae.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.siheunggagae.R
import com.example.siheunggagae.ui.viewmodel.MapPinPickerViewModel

private val Brown900M = Color(0xFF614B3A)
private val Brown700M = Color(0xFF8A6E58)
private val TextBlackM = Color(0xFF1E120A)
private val OrangeSandM = Color(0xFFFFEDD4)
private val Orange500M = Color(0xFFF7A35B)
private val MintLightM = Color(0xFFD0FEE1)

@Composable
fun MapPinPickerScreen(
    viewModel: MapPinPickerViewModel,
    onBack: () -> Unit,
    onConfirm: (lat: Double, lng: Double, address: String?) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(MintLightM)) {
        // TODO: 카카오맵 SDK MapViewWrapper 사용 — onCameraIdle 콜백에서 viewModel.onCameraIdle 호출.
        // 카카오 SDK 통합 패턴은 기존 MapScreen.kt 참고. 이 placeholder는 추후 MapViewWrapper 로 교체.

        // 그라디언트 placeholder
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(MintLightM, Color(0xFFF0FDF4), Color.White),
                    ),
                ),
        )

        // 중앙 핀
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_map_pin),
                contentDescription = null,
                tint = Orange500M,
                modifier = Modifier.size(40.dp),
            )
        }

        // TopBar (투명 그라디언트)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(36.dp).clickable { onBack() },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_left),
                        contentDescription = "뒤로가기",
                        tint = TextBlackM,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text("위치 선택", color = TextBlackM, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(36.dp))
        }

        // 하단 시트
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    "선택된 위치",
                    color = Brown700M,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OrangeSandM),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_map_pin),
                            contentDescription = null,
                            tint = Orange500M,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            state.address ?: if (state.resolving) "주소 확인 중..." else "주소를 가져올 수 없어요",
                            color = TextBlackM,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${"%.5f".format(state.lat)}, ${"%.5f".format(state.lng)}",
                            color = Brown700M,
                            fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onConfirm(state.lat, state.lng, state.address) },
                    colors = ButtonDefaults.buttonColors(containerColor = Brown900M),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text(
                        "이 위치로 설정",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MapPinPickerScreenPreview() {
    // Preview placeholder
}
```

> **카카오맵 통합 TODO**: 위 코드는 placeholder 그라디언트와 중앙 핀만 그린다. 실제로는 `MapViewWrapper`(`com.example.siheunggagae.MapViewWrapper`)를 사용해 KakaoMap을 띄우고 `setOnCameraMoveEndListener` 콜백에서 카메라 중심의 좌표를 `viewModel.onCameraIdle(lat, lng)`에 전달해야 한다. 통합 패턴은 `ui/screen/MapScreen.kt` 참고. 이번 task는 placeholder로 빌드 통과 + 좌표/주소 동기 흐름 검증까지만; 카카오맵 실제 임베드는 Task 6 마지막 단계에서 진행.

- [ ] **Step 6.4: 카카오맵 임베드**

`MapScreen.kt` 참고하여 placeholder Box 자리에 `MapViewWrapper(...)` 호출 + `onCameraMoveEnd { lat, lng -> viewModel.onCameraIdle(lat, lng) }` 람다 연결. 초기 카메라 위치는 `state.lat`/`state.lng`. 패턴은 MapScreen.kt 의 `MapViewWrapper` 사용 부분을 그대로 적용.

- [ ] **Step 6.5: NavGraph placeholder 교체**

`NavGraph.kt`의 `Screen.MapPinPicker.route` composable 블록 교체:

```kotlin
composable(
    route = Screen.MapPinPicker.route,
    arguments = listOf(
        navArgument(Screen.MapPinPicker.ARG_LAT) {
            type = NavType.StringType; nullable = true; defaultValue = null
        },
        navArgument(Screen.MapPinPicker.ARG_LNG) {
            type = NavType.StringType; nullable = true; defaultValue = null
        },
    ),
) { backStackEntry ->
    val argLat = backStackEntry.arguments?.getString(Screen.MapPinPicker.ARG_LAT)?.toDoubleOrNull()
    val argLng = backStackEntry.arguments?.getString(Screen.MapPinPicker.ARG_LNG)?.toDoubleOrNull()
    val initLat = argLat ?: 37.3799   // 시흥시청
    val initLng = argLng ?: 126.8030
    val vm: MapPinPickerViewModel = viewModel(
        factory = MapPinPickerViewModel.Factory(initLat, initLng),
    )
    MapPinPickerScreen(
        viewModel = vm,
        onBack = { navController.popBackStack() },
        onConfirm = { lat, lng, addr ->
            val handle = navController.previousBackStackEntry?.savedStateHandle
            handle?.set(Screen.MapPinPicker.RESULT_LAT, lat)
            handle?.set(Screen.MapPinPicker.RESULT_LNG, lng)
            handle?.set(Screen.MapPinPicker.RESULT_ADDRESS, addr)
            navController.popBackStack()
        },
    )
}
```

- [ ] **Step 6.6: 컴파일 + 실행 확인**

```bash
./gradlew :app:installDebug
```

- 화면 띄우기 (임시 진입은 Task 7 폼에서 진행 — 지금은 컴파일만 확인)

- [ ] **Step 6.7: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/MapPinPickerViewModel.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/MapPinPickerScreen.kt \
        app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt \
        app/src/main/java/com/example/siheunggagae/data/model/MapModels.kt \
        app/src/main/java/com/example/siheunggagae/NavGraph.kt
git commit -m "feat: MapPinPickerScreen + Reverse Geocode + 결과 회수"
```

---

## Task 7 — StoreRequestFormScreen (ADD 흐름)

이 task는 단일 PR로 가장 큼. ADD 모드만 우선. UPDATE/재제출 prefill은 Task 8.

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/StoreRequestFormViewModel.kt`
- Create: `app/src/main/java/com/example/siheunggagae/ui/screen/StoreRequestFormScreen.kt`
- Create: `app/src/test/java/com/example/siheunggagae/StoreRequestFormValidationTest.kt`
- Modify: `NavGraph.kt` (placeholder → 실제 호출 + 결과 회수)

- [ ] **Step 7.1: 검증 로직 + 테스트 먼저**

Create `app/src/test/java/com/example/siheunggagae/StoreRequestFormValidationTest.kt`:

```kotlin
package com.example.siheunggagae

import com.example.siheunggagae.ui.viewmodel.PlanInput
import com.example.siheunggagae.ui.viewmodel.StoreRequestFormState
import com.example.siheunggagae.ui.viewmodel.validateForSubmit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class StoreRequestFormValidationTest {

    private val baseValid = StoreRequestFormState(
        name = "배곧 펫호텔",
        category = "PET_HOTEL",
        isPetAllowed = true,
        latitude = 37.3752,
        longitude = 126.7281,
        address = "경기 시흥시 배곧동",
        plans = listOf(PlanInput("1박 소형견", 40000)),
        proofUrls = listOf("https://placeholder.local/x"),
    )

    @Test fun valid_form_passes() {
        val (ok, _) = baseValid.validateForSubmit()
        assertTrue(ok)
    }

    @Test fun blank_name_fails() {
        val (ok, errors) = baseValid.copy(name = "").validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("name"))
    }

    @Test fun missing_category_fails() {
        val (ok, errors) = baseValid.copy(category = null).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("category"))
    }

    @Test fun missing_location_fails() {
        val (ok, errors) = baseValid.copy(latitude = null, longitude = null).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("location"))
    }

    @Test fun empty_proof_fails_strict() {
        val (ok, errors) = baseValid.copy(proofUrls = emptyList()).validateForSubmit()
        assertFalse(ok)
        assertEquals("증빙 자료를 1개 이상 첨부해주세요", errors["proof"])
    }

    @Test fun pet_hotel_without_plans_fails() {
        val (ok, errors) = baseValid.copy(plans = emptyList()).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("plans"))
    }

    @Test fun non_pet_hotel_with_plans_strips_plans() {
        val (ok, _) = baseValid.copy(category = "CAFE", plans = emptyList()).validateForSubmit()
        assertTrue(ok)
    }

    @Test fun duplicate_plan_name_fails() {
        val (ok, errors) = baseValid.copy(
            plans = listOf(
                PlanInput("1박", 40000),
                PlanInput("1박", 50000),
            ),
        ).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("plans"))
    }

    @Test fun zero_price_plan_fails() {
        val (ok, errors) = baseValid.copy(
            plans = listOf(PlanInput("1박", 0)),
        ).validateForSubmit()
        assertFalse(ok)
        assertTrue(errors.containsKey("plans"))
    }
}
```

- [ ] **Step 7.2: ViewModel 작성**

Create `ui/viewmodel/StoreRequestFormViewModel.kt`:

```kotlin
package com.example.siheunggagae.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.StorePricingPlanInput
import com.example.siheunggagae.data.model.StoreRequestPayload
import com.example.siheunggagae.data.model.StoreRequestSubmitRequest
import com.example.siheunggagae.data.model.StoreRequestType
import com.example.siheunggagae.data.repository.StoreRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class PlanInput(val plan_name: String, val price_krw: Int)

data class StoreRequestFormState(
    val name: String = "",
    val category: String? = null,                // CAFE/RESTAURANT/PARK/PET_HOTEL
    val isPetAllowed: Boolean = true,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",
    val phone: String = "",
    val operatingHours: String = "",
    val plans: List<PlanInput> = emptyList(),
    val photoUris: List<Uri> = emptyList(),
    val photoStubUrls: List<String> = emptyList(),
    val proofUris: List<Uri> = emptyList(),
    val proofUrls: List<String> = emptyList(),
    val message: String = "",
)

fun StoreRequestFormState.validateForSubmit(): Pair<Boolean, Map<String, String>> {
    val errors = mutableMapOf<String, String>()
    if (name.isBlank()) errors["name"] = "매장명을 입력해주세요"
    if (category.isNullOrBlank()) errors["category"] = "카테고리를 선택해주세요"
    if (latitude == null || longitude == null) errors["location"] = "지도에서 위치를 선택해주세요"
    if (proofUrls.isEmpty()) errors["proof"] = "증빙 자료를 1개 이상 첨부해주세요"
    if (category == "PET_HOTEL") {
        if (plans.isEmpty()) {
            errors["plans"] = "가격 플랜을 1개 이상 추가해주세요"
        } else {
            if (plans.any { it.plan_name.isBlank() || it.price_krw <= 0 }) {
                errors["plans"] = "플랜명과 가격(0보다 큰 값)을 입력해주세요"
            }
            if (plans.map { it.plan_name }.toSet().size != plans.size) {
                errors["plans"] = "플랜명이 중복되었어요"
            }
        }
    }
    return errors.isEmpty() to errors
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Submitting : SubmitState()
    data class Submitted(val requestId: Int) : SubmitState()
    data class Failed(val message: String) : SubmitState()
}

class StoreRequestFormViewModel(
    private val repository: StoreRequestRepository,
    val mode: StoreRequestType,
    val targetStoreId: Int?,
    val sourceRequestId: Int?,
) : ViewModel() {

    private val _form = MutableStateFlow(StoreRequestFormState())
    val form: StateFlow<StoreRequestFormState> = _form

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors: StateFlow<Map<String, String>> = _errors

    private val _submit = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submit: StateFlow<SubmitState> = _submit

    // === field setters ===
    fun setName(v: String) { _form.value = _form.value.copy(name = v) }
    fun setCategory(v: String) { _form.value = _form.value.copy(category = v) }
    fun setIsPetAllowed(v: Boolean) { _form.value = _form.value.copy(isPetAllowed = v) }
    fun setPhone(v: String) { _form.value = _form.value.copy(phone = v) }
    fun setHours(v: String) { _form.value = _form.value.copy(operatingHours = v) }
    fun setMessage(v: String) { _form.value = _form.value.copy(message = v.take(1000)) }

    fun applyPickedLocation(lat: Double, lng: Double, address: String?) {
        _form.value = _form.value.copy(
            latitude = lat,
            longitude = lng,
            address = address ?: _form.value.address,
        )
    }

    // === plans ===
    fun addPlan() {
        _form.value = _form.value.copy(plans = _form.value.plans + PlanInput("", 0))
    }
    fun updatePlanName(index: Int, name: String) {
        val list = _form.value.plans.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(plan_name = name)
            _form.value = _form.value.copy(plans = list)
        }
    }
    fun updatePlanPrice(index: Int, price: Int) {
        val list = _form.value.plans.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(price_krw = price)
            _form.value = _form.value.copy(plans = list)
        }
    }
    fun removePlan(index: Int) {
        val list = _form.value.plans.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _form.value = _form.value.copy(plans = list)
        }
    }

    // === photos / proofs (Stub) ===
    private fun makeStubUrl(): String = "https://placeholder.local/uploads/" + UUID.randomUUID()

    fun addPhotos(uris: List<Uri>) {
        val newUris = _form.value.photoUris + uris
        val newStubs = _form.value.photoStubUrls + uris.map { makeStubUrl() }
        _form.value = _form.value.copy(photoUris = newUris.take(5), photoStubUrls = newStubs.take(5))
    }
    fun removePhoto(index: Int) {
        val u = _form.value.photoUris.toMutableList()
        val s = _form.value.photoStubUrls.toMutableList()
        if (index in u.indices) { u.removeAt(index); s.removeAt(index) }
        _form.value = _form.value.copy(photoUris = u, photoStubUrls = s)
    }

    fun addProofs(uris: List<Uri>) {
        val newUris = _form.value.proofUris + uris
        val newUrls = _form.value.proofUrls + uris.map { makeStubUrl() }
        _form.value = _form.value.copy(proofUris = newUris.take(10), proofUrls = newUrls.take(10))
    }
    fun removeProof(index: Int) {
        val u = _form.value.proofUris.toMutableList()
        val s = _form.value.proofUrls.toMutableList()
        if (index in u.indices) { u.removeAt(index); s.removeAt(index) }
        _form.value = _form.value.copy(proofUris = u, proofUrls = s)
    }

    // === submit ===
    fun submit() {
        val (ok, errs) = _form.value.validateForSubmit()
        _errors.value = errs
        if (!ok) return
        val f = _form.value
        val body = StoreRequestSubmitRequest(
            type = mode,
            target_store_id = if (mode == StoreRequestType.UPDATE) targetStoreId else null,
            payload = StoreRequestPayload(
                name = f.name,
                address = f.address,
                category = f.category,
                is_pet_allowed = f.isPetAllowed,
                latitude = f.latitude,
                longitude = f.longitude,
                phone = f.phone.ifBlank { null },
                operating_hours = f.operatingHours.ifBlank { null },
                photo_urls = f.photoStubUrls.ifEmpty { null },
                plans = if (f.category == "PET_HOTEL")
                    f.plans.mapIndexed { i, p ->
                        StorePricingPlanInput(p.plan_name, p.price_krw, i)
                    } else null,
            ),
            proof_urls = f.proofUrls.ifEmpty { null },
            message = f.message.ifBlank { null },
        )
        viewModelScope.launch {
            _submit.value = SubmitState.Submitting
            val resp = repository.submit(body)
            _submit.value = if (resp.isSuccessful) {
                SubmitState.Submitted(resp.body()!!.request_id)
            } else when (resp.code()) {
                409 -> SubmitState.Failed("이미 검토 중인 요청이 있어요")
                429 -> SubmitState.Failed("잠시 후 다시 시도해주세요")
                422 -> SubmitState.Failed("입력 정보를 다시 확인해주세요")
                else -> SubmitState.Failed("제출에 실패했어요 (${resp.code()})")
            }
        }
    }

    class Factory(
        private val repository: StoreRequestRepository,
        private val mode: StoreRequestType,
        private val targetStoreId: Int?,
        private val sourceRequestId: Int?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoreRequestFormViewModel(repository, mode, targetStoreId, sourceRequestId) as T
    }
}
```

- [ ] **Step 7.3: 단위 테스트 실행**

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.siheunggagae.StoreRequestFormValidationTest"
```

Expected: 9 tests passed.

- [ ] **Step 7.4: Screen 작성**

Create `ui/screen/StoreRequestFormScreen.kt`. 스펙 §3.2 StoreRequestFormScreen "B안 시원시원 라벨형" 그대로 따라간다. 단일 스크롤, 카테고리 2x2 그리드, 위치 큰 미리보기(탭→Picker), PET_HOTEL plans, 사진/증빙, CTA 고정 하단.

(파일이 매우 김 — 시안 HTML(`04-form-proof-detail.html`, `03-form-screen.html`) 의 B안을 그대로 코드로 옮기고, 컬러는 위 ViewModel과 동일한 hex 사용. 사진/증빙 첨부는 `rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents())` 사용. 사진은 `image/*`, 증빙은 `*/*`.)

핵심 시그니처:
```kotlin
@Composable
fun StoreRequestFormScreen(
    viewModel: StoreRequestFormViewModel,
    onBack: () -> Unit,
    onPickLocation: (lat: Double?, lng: Double?) -> Unit,
    onSubmitted: (requestId: Int) -> Unit,
)
```

- 사진 picker 결과 시 `viewModel.addPhotos(uris)`
- 증빙 picker 결과 시 `viewModel.addProofs(uris)`
- 위치 영역 탭 → `onPickLocation(state.latitude, state.longitude)`
- 제출 결과 `SubmitState.Submitted(id)`이면 `LaunchedEffect`로 `onSubmitted(id)` 호출

PET_HOTEL 카테고리 선택일 때만 plans 섹션 노출. 카테고리 2x2 그리드 각 셀에 카테고리 아이콘 적용:
- CAFE → `ic_coffee`
- RESTAURANT → `ic_utensils`
- PARK → `ic_trees`
- PET_HOTEL → `ic_hotel`

선택 셀: bg `#FFEDD4`, border 2dp `#F7A35B`, text Bold `#614B3A`
미선택 셀: bg White, border 1dp `#E8D3C2`, text `#8A6E58`

증빙 첨부 항목의 파일 타입 박지 색:
- PDF: bg `#FEE7EC` / text `#E84B6A`
- DOC, DOCX: bg `#DBEAFE` / text `#2563EB`
- 기타: bg `#F2F2F2` / text `#6B7280`

확장자 추출: `Uri.lastPathSegment?.substringAfterLast('.', "")?.uppercase()` 사용. 이미지 mime은 Coil AsyncImage.

CTA 비활성 조건: `viewModel.errors.value.isNotEmpty()` OR `state.proofUrls.isEmpty()` OR `submit is Submitting`. 비활성 시 alpha 0.4.

`@Preview` 포함.

- [ ] **Step 7.5: NavGraph placeholder 교체**

`NavGraph.kt`의 `Screen.StoreRequestForm.route` composable 블록 교체. SavedStateHandle로 picker 결과 회수 + ViewModel에 주입:

```kotlin
composable(
    route = Screen.StoreRequestForm.route,
    arguments = listOf(
        navArgument(Screen.StoreRequestForm.ARG_TYPE) {
            type = NavType.StringType; defaultValue = "ADD"
        },
        navArgument(Screen.StoreRequestForm.ARG_STORE_ID) {
            type = NavType.IntType; defaultValue = -1
        },
        navArgument(Screen.StoreRequestForm.ARG_REQUEST_ID) {
            type = NavType.IntType; defaultValue = -1
        },
    ),
) { backStackEntry ->
    val typeStr = backStackEntry.arguments?.getString(Screen.StoreRequestForm.ARG_TYPE) ?: "ADD"
    val storeId = backStackEntry.arguments?.getInt(Screen.StoreRequestForm.ARG_STORE_ID, -1)
        ?.takeIf { it >= 0 }
    val reqId = backStackEntry.arguments?.getInt(Screen.StoreRequestForm.ARG_REQUEST_ID, -1)
        ?.takeIf { it >= 0 }
    val mode = StoreRequestType.valueOf(typeStr)
    val vm: StoreRequestFormViewModel = viewModel(
        factory = StoreRequestFormViewModel.Factory(
            StoreRequestRepository(), mode, storeId, reqId,
        ),
    )

    // picker 결과 회수
    val handle = backStackEntry.savedStateHandle
    LaunchedEffect(handle) {
        handle.getLiveData<Double>(Screen.MapPinPicker.RESULT_LAT).observeForever { lat ->
            val lng = handle.get<Double>(Screen.MapPinPicker.RESULT_LNG)
            val addr = handle.get<String>(Screen.MapPinPicker.RESULT_ADDRESS)
            if (lat != null && lng != null) {
                vm.applyPickedLocation(lat, lng, addr)
                handle.remove<Double>(Screen.MapPinPicker.RESULT_LAT)
                handle.remove<Double>(Screen.MapPinPicker.RESULT_LNG)
                handle.remove<String>(Screen.MapPinPicker.RESULT_ADDRESS)
            }
        }
    }

    StoreRequestFormScreen(
        viewModel = vm,
        onBack = { navController.popBackStack() },
        onPickLocation = { lat, lng ->
            navController.navigate(Screen.MapPinPicker.createRoute(lat, lng))
        },
        onSubmitted = { id ->
            navController.popBackStack()
            navController.navigate(Screen.StoreRequestDetail.createRoute(id))
        },
    )
}
```

> 위 `observeForever` 사용 시 메모리 누수 우려가 있다면 `DisposableEffect`로 cleanup, 또는 `currentBackStackEntry?.savedStateHandle?.getStateFlow(...)` 패턴으로 교체. 가장 단순한 형태로 일단 두고, QA 단계에서 누수 검사.

- [ ] **Step 7.6: 빌드 + 실행 + 수동 흐름 확인**

```bash
./gradlew :app:installDebug
```

- 마이페이지 → 내 매장 요청 → FAB(+) 탭 → Form 진입 (ADD 모드)
- 카테고리 PET_HOTEL 탭 → plans 섹션 노출 확인
- 위치 영역 탭 → MapPinPicker 진입 → "이 위치로 설정" → 폼 복귀, 주소 prefill 확인
- 증빙 자료 첨부 (사진 파일 1개) → 제출 버튼 활성화
- 제출 → 응답 확인 → StoreRequestDetail 이동 (없는 화면이라 placeholder 보임 — 정상)

- [ ] **Step 7.7: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/StoreRequestFormViewModel.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/StoreRequestFormScreen.kt \
        app/src/test/java/com/example/siheunggagae/StoreRequestFormValidationTest.kt \
        app/src/main/java/com/example/siheunggagae/NavGraph.kt
git commit -m "feat: StoreRequestFormScreen (ADD) + 검증 단위 테스트"
```

---

## Task 8 — Form UPDATE/재제출 prefill + PlaceDetail 진입점

**Files:**
- Modify: `ui/viewmodel/StoreRequestFormViewModel.kt` (init에서 prefill 로드)
- Modify: `ui/screen/StoreRequestFormScreen.kt` (제목/모드 분기)
- Modify: `ui/screen/PlaceDetailScreen.kt` (수정 요청 버튼 추가)
- Modify: `NavGraph.kt` (PlaceDetail 호출에 onEditRequestClick 콜백 연결)

- [ ] **Step 8.1: ViewModel init prefill**

`StoreRequestFormViewModel`에 init 블록 추가:

```kotlin
init {
    if (mode == StoreRequestType.UPDATE && targetStoreId != null && sourceRequestId == null) {
        viewModelScope.launch {
            val resp = RetrofitClient.api.getStoreDetail(targetStoreId)
            if (resp.isSuccessful) {
                val d = resp.body()!!
                _form.value = _form.value.copy(
                    name = d.name,
                    category = inferCategory(d),
                    address = d.address,
                    phone = d.phone.orEmpty(),
                    operatingHours = d.operating_hours.orEmpty(),
                    isPetAllowed = d.is_pet_allowed,
                    photoStubUrls = d.photo_urls,
                    // lat/lng 은 StoreDetail에 없으므로 그대로 두고 사용자가 위치 다시 선택
                )
            }
        }
    } else if (sourceRequestId != null) {
        viewModelScope.launch {
            val resp = repository.detail(sourceRequestId)
            if (resp.isSuccessful) {
                val item = resp.body()!!
                val p = item.payload
                _form.value = _form.value.copy(
                    name = p.name.orEmpty(),
                    category = p.category,
                    address = p.address.orEmpty(),
                    isPetAllowed = p.is_pet_allowed ?: true,
                    latitude = p.latitude,
                    longitude = p.longitude,
                    phone = p.phone.orEmpty(),
                    operatingHours = p.operating_hours.orEmpty(),
                    plans = p.plans.orEmpty().map { PlanInput(it.plan_name, it.price_krw) },
                    photoStubUrls = p.photo_urls.orEmpty(),
                    proofUrls = item.proof_urls,
                    message = item.message.orEmpty(),
                )
            }
        }
    }
}

private fun inferCategory(d: com.example.siheunggagae.data.model.StoreDetailResponse): String? {
    // StoreDetailResponse에 category가 있으면 그대로, 없으면 null
    return null // TODO: StoreDetailResponse에 category 필드가 추가되면 사용
}
```

> **참고**: 현재 `StoreDetailResponse`에 `category` 필드가 없는 경우 NavGraph에서 추가 인자로 받거나 `GET /maps/stores/{id}` 응답에 category 필드 노출 요청. 일단 null로 두고 사용자가 다시 선택하게 함.

- [ ] **Step 8.2: 폼 제목 분기**

`StoreRequestFormScreen` TopBar 타이틀:
```kotlin
val title = when {
    viewModel.sourceRequestId != null -> "다시 작성"
    viewModel.mode == StoreRequestType.UPDATE -> "매장 정보 수정"
    else -> "매장 추가 요청"
}
```

- [ ] **Step 8.3: PlaceDetailScreen 버튼 추가**

`PlaceDetailScreen.kt` 시그니처에 추가:
```kotlin
onEditRequestClick: (storeId: Int) -> Unit = {},
```

기존 매장 정보 카드 상단 또는 사이드 영역에 OutlinedButton 또는 TextButton 추가:
```kotlin
val isOwner = detail.is_owner
val isClaimable = !isOwner && detail.owner_user_id == null
val canRequest = isLoggedIn && (isOwner || isClaimable)
val label = when {
    isOwner -> "내 매장 정보 수정"
    isClaimable -> "이 매장 클레임하기"
    else -> "본인 명의 매장만 수정 요청 가능"
}
OutlinedButton(
    enabled = canRequest,
    onClick = { onEditRequestClick(detail.store_id) },
    border = BorderStroke(1.dp, Brown900Q),
    shape = RoundedCornerShape(12.dp),
) {
    Icon(painter = painterResource(R.drawable.ic_pencil), contentDescription = null, modifier = Modifier.size(16.dp))
    Spacer(Modifier.width(6.dp))
    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}
```

`isLoggedIn` 은 `TokenManager`(기존 패턴) 또는 `LocalContext.current` 통한 prefs 조회. PetAddScreen 등 기존 인증 분기 패턴 참고.

- [ ] **Step 8.4: NavGraph PlaceDetail 호출 보강**

NavGraph의 PlaceDetail composable 블록 안 `PlaceDetailScreen(...)` 호출에 추가:
```kotlin
onEditRequestClick = { sid ->
    navController.navigate(
        Screen.StoreRequestForm.createRoute(
            type = StoreRequestType.UPDATE,
            storeId = sid,
        ),
    )
},
```

- [ ] **Step 8.5: 빌드 + 수동 확인**

```bash
./gradlew :app:installDebug
```

- 매장 상세 → 버튼 표시 조건 분기 확인 (3개 케이스)
- 버튼 탭 → Form (UPDATE 모드) 진입 → 기존 매장 정보 prefill 확인
- 카테고리 미설정인 경우 사용자가 다시 선택 (TODO 항목)

- [ ] **Step 8.6: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/StoreRequestFormViewModel.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/StoreRequestFormScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt \
        app/src/main/java/com/example/siheunggagae/NavGraph.kt
git commit -m "feat: Form UPDATE/재제출 prefill + PlaceDetail 진입점"
```

---

## Task 9 — StoreRequestDetailScreen + 알림 deeplink

**Files:**
- Create: `app/src/main/java/com/example/siheunggagae/ui/viewmodel/StoreRequestDetailViewModel.kt`
- Create: `app/src/main/java/com/example/siheunggagae/ui/screen/StoreRequestDetailScreen.kt`
- Modify: `ui/screen/NotificationScreen.kt` (deeplink 파서)
- Modify: `NavGraph.kt` (placeholder → 실제 호출)
- Modify: `docs/gotcha.md` (stub 경고 한 줄)

- [ ] **Step 9.1: ViewModel 작성**

Create `ui/viewmodel/StoreRequestDetailViewModel.kt`:

```kotlin
package com.example.siheunggagae.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.repository.StoreRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class StoreRequestDetailUi {
    object Loading : StoreRequestDetailUi()
    data class Success(val item: StoreRequestItem) : StoreRequestDetailUi()
    data class Error(val message: String) : StoreRequestDetailUi()
}

sealed class CancelState {
    object Idle : CancelState()
    object Cancelling : CancelState()
    object Cancelled : CancelState()
    data class Failed(val message: String) : CancelState()
}

class StoreRequestDetailViewModel(
    private val repository: StoreRequestRepository,
    private val requestId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow<StoreRequestDetailUi>(StoreRequestDetailUi.Loading)
    val state: StateFlow<StoreRequestDetailUi> = _state

    private val _cancel = MutableStateFlow<CancelState>(CancelState.Idle)
    val cancel: StateFlow<CancelState> = _cancel

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = StoreRequestDetailUi.Loading
            val resp = repository.detail(requestId)
            _state.value = if (resp.isSuccessful) {
                StoreRequestDetailUi.Success(resp.body()!!)
            } else {
                StoreRequestDetailUi.Error("요청을 불러오지 못했어요 (${resp.code()})")
            }
        }
    }

    fun cancelRequest() {
        viewModelScope.launch {
            _cancel.value = CancelState.Cancelling
            val resp = repository.cancel(requestId)
            _cancel.value = when {
                resp.isSuccessful -> CancelState.Cancelled
                resp.code() == 409 -> CancelState.Failed("이미 처리된 요청은 취소할 수 없어요")
                else -> CancelState.Failed("취소에 실패했어요 (${resp.code()})")
            }
        }
    }

    class Factory(private val repository: StoreRequestRepository, private val requestId: Int) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoreRequestDetailViewModel(repository, requestId) as T
    }
}
```

- [ ] **Step 9.2: Screen 작성**

Create `ui/screen/StoreRequestDetailScreen.kt`. 스펙 §3.2 StoreRequestDetailScreen 그대로:

```kotlin
@Composable
fun StoreRequestDetailScreen(
    viewModel: StoreRequestDetailViewModel,
    onBack: () -> Unit,
    onSeeStore: (storeId: Int) -> Unit,
    onRetryRequest: (sourceRequestId: Int, type: StoreRequestType) -> Unit,
)
```

상태별 헤더 카드 (그라디언트 + border) — 시안 `05-detail-screen.html` 따라.
- PENDING: `ic_clock`, `#FEF3C7→#FFEDD4` 그라디언트, "검토 중" `#CA8A04`, 하단 CTA "요청 취소" (Outline `#E84B6A`) — 클릭 시 AlertDialog ("정말 취소할까요?") → `viewModel.cancelRequest()` → 성공 시 `onBack()`
- APPROVED: `ic_check_circle`, `#F0FDF4→#D0FEE1`, "승인되었어요" `#16A34A`. "등록된 매장" 미니 카드 → `onSeeStore(item.target_store_id ?: 0)`. 하단 CTA "매장 상세 보기"
- REJECTED: `ic_alert_circle`, `#FEE7EC→#FEFEFE`, "반려되었어요" `#E84B6A`. 사유는 White 카드 안에 `item.review_note`. 안내 박스 `#FEF3C7` "💡 다시 작성하면 기존 내용이 채워진 채로 폼이 열려요." (`ic_lightbulb`). 하단 CTA "다시 작성하기" → `onRetryRequest(item.request_id, item.type)`

"제출 내용" 섹션은 모든 상태 공통. White 카드 + Row 형태로 항목:
- 유형: `item.type.name`
- 매장명: `item.payload.name`
- 카테고리: `item.payload.category`
- 주소: `item.payload.address`
- 사진: `${item.payload.photo_urls.orEmpty().size}장`
- 증빙: `${item.proof_urls.size}개`
- 가격 플랜: `${item.payload.plans.orEmpty().size}개` (PET_HOTEL일 때만)

- [ ] **Step 9.3: NavGraph placeholder 교체**

`NavGraph.kt`의 `Screen.StoreRequestDetail.route` composable 블록 교체:

```kotlin
composable(
    route = Screen.StoreRequestDetail.route,
    arguments = listOf(
        navArgument(Screen.StoreRequestDetail.ARG_REQUEST_ID) { type = NavType.IntType },
    ),
) { backStackEntry ->
    val reqId = backStackEntry.arguments?.getInt(Screen.StoreRequestDetail.ARG_REQUEST_ID) ?: return@composable
    val vm: StoreRequestDetailViewModel = viewModel(
        factory = StoreRequestDetailViewModel.Factory(StoreRequestRepository(), reqId),
    )
    StoreRequestDetailScreen(
        viewModel = vm,
        onBack = { navController.popBackStack() },
        onSeeStore = { storeId ->
            navController.navigate(Screen.PlaceDetail.createRoute(storeId))
        },
        onRetryRequest = { sourceId, type ->
            navController.navigate(
                Screen.StoreRequestForm.createRoute(
                    type = type,
                    requestId = sourceId,
                ),
            )
        },
    )
}
```

- [ ] **Step 9.4: NotificationScreen deeplink 파서**

`NotificationScreen.kt` 의 onItemClick 또는 onNotify 콜백 흐름에서 `item.link` 가 있으면 파싱. 기존 onClick 핸들러 위치는 `NotificationViewModel`이 markRead 호출 + 콜백을 외부로 위임하는 패턴일 가능성. 단순화:

NavGraph 안 `NotificationScreen(...)` 호출 부분에서 `onItemClick = { item -> handleNotificationLink(item, navController) }` 형태로 외부에서 라우팅:

```kotlin
private fun handleNotificationLink(
    item: com.example.siheunggagae.data.model.NotificationItem,
    nav: NavController,
) {
    val link = item.link ?: return
    when {
        link.startsWith("siheunggagae://store-request/") -> {
            val id = link.substringAfterLast("/").toIntOrNull() ?: return
            nav.navigate(Screen.StoreRequestDetail.createRoute(id))
        }
        // 추후 다른 deeplink 패턴 여기에
        else -> {}
    }
}
```

- [ ] **Step 9.5: docs/gotcha.md 보강**

`docs/gotcha.md` 적절한 위치에 한 줄 추가:

```markdown
- **매장 요청 사진/증빙 업로드는 stub**: `StoreRequestFormViewModel.makeStubUrl()`이 `https://placeholder.local/uploads/<uuid>` 형태 URL을 생성한다. 백엔드는 URL 형식만 통과시키므로 실제 파일이 전달되지 않음. 진짜 업로드 엔드포인트(예: `POST /maps/files`) 도입 시 교체 필요.
```

- [ ] **Step 9.6: 빌드 + 전체 흐름 수동 확인**

```bash
./gradlew :app:installDebug
```

전체 시나리오:
1. 마이페이지 → 내 매장 요청 → FAB → Form (ADD) → 카테고리 PET_HOTEL → 위치 핀 → 사진 1장 + 증빙 1개 → 제출 → Detail (PENDING) → 백 → 목록에 새 PENDING 보임
2. (백엔드에서 PATCH로 APPROVE 시켜놓고) 알림 받아서 클릭 → Detail (APPROVED) → "매장 상세 보기" → PlaceDetail
3. (백엔드에서 REJECT 후) 알림 → Detail (REJECTED) → "다시 작성하기" → Form prefill → 수정 후 재제출 → 새 PENDING
4. PENDING 상태에서 "요청 취소" → 확인 다이얼로그 → 취소 성공 → 백 + 목록 새로고침
5. PlaceDetail 진입 → owner=NULL 매장에서 "이 매장 클레임하기" 버튼 → Form (UPDATE, prefill) → 제출

- [ ] **Step 9.7: 커밋**

```bash
git add app/src/main/java/com/example/siheunggagae/ui/viewmodel/StoreRequestDetailViewModel.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/StoreRequestDetailScreen.kt \
        app/src/main/java/com/example/siheunggagae/NavGraph.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/NotificationScreen.kt \
        docs/gotcha.md
git commit -m "feat: StoreRequestDetailScreen + 알림 deeplink + stub 경고"
```

---

## Task 10 — docs 갱신 + 최종 QA

**Files:**
- Modify: `docs/screens.md` (신규 4개 화면 항목 추가)
- Modify: `docs/api.md` (Map 도메인에 store-requests 4개 + reverse-geocode 추가)
- Modify: `CLAUDE.md` (Navigation Routes 표 갱신, 컴포넌트 섹션은 그대로)

- [ ] **Step 10.1: docs/screens.md 갱신**

기존 마이/설정 섹션 근처에 4개 화면 항목 추가. 각 화면별 TopBar/본문/ViewModel/에러 분기 표기. 형식은 기존 PetListScreen 항목 그대로 따라.

- [ ] **Step 10.2: docs/api.md 갱신**

Map 도메인 표에 다음 행 추가:

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| POST | /maps/store-requests | submitStoreRequest | StoreRequestSubmitResponse |
| GET | /maps/store-requests | getMyStoreRequests | StoreRequestListResponse |
| GET | /maps/store-requests/{id} | getStoreRequest | StoreRequestItem |
| DELETE | /maps/store-requests/{id} | cancelStoreRequest | Unit |
| GET | /maps/geocode/reverse | reverseGeocode | ReverseGeocodeResponse |

(reverseGeocode가 이미 있었으면 추가하지 않음)

- [ ] **Step 10.3: CLAUDE.md Navigation Routes 표 갱신**

기존 라우트 표에 4개 행 추가:
```kotlin
object MyStoreRequests    : Screen("my_store_requests")
object StoreRequestForm   : Screen("store_request_form?type&storeId&requestId")
object StoreRequestDetail : Screen("store_request_detail/{requestId}")
object MapPinPicker       : Screen("map_pin_picker?lat&lng")
```

- [ ] **Step 10.4: 전체 빌드 + 단위 테스트 재실행**

```bash
./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

Expected: 모두 SUCCESSFUL.

- [ ] **Step 10.5: 커밋**

```bash
git add docs/screens.md docs/api.md CLAUDE.md
git commit -m "docs: 매장 요청 화면·API·라우트 문서 반영"
```

---

## Self-Review (작성자가 직접 확인)

- **Spec 커버리지**: §1 목표·§2 백엔드 의존성·§3 화면·§4 모델·§5 라우트·§6 API·§7 ViewModel·§8 아이콘·§9 stub·§10 알림·§11 에러·§13 gotcha 모두 task 1~10 어딘가에서 처리됨. §12 테스트는 Task 1·7에서 단위 테스트로 일부 커버, 나머지는 manual smoke.
- **Placeholder 없음**: 모든 step에 실제 코드/명령. 단, Task 6.3은 카카오맵 임베드 부분이 "패턴 참고" 가이드라인 — 의도적 (코드는 기존 MapScreen.kt에 실제 예시 있음).
- **타입 일관성**: `StoreRequestType`, `StoreRequestStatus`, `StoreRequestPayload`, `StoreRequestItem`, `PlanInput`, `StoreRequestFormState`, `SubmitState`, `CancelState` 등 모든 타입이 Task 1·2·7·9 사이에서 일관.
- **스코프**: 단일 PR로 묶기엔 크지만 task 단위로 충분히 잘게 쪼개짐. 의존 관계: 1→2→3→4→5→6→7→8→9→10.
