# 매장 추가/수정 요청 — 설계 스펙

작성일: 2026-05-22
작업 범위: 시흥가개 안드로이드 클라이언트 + 백엔드 변경 요청 정리

---

## 1. 목표

사용자가 자신이 운영하는 매장을 지도에 **추가** 또는 기존 매장 정보를 **수정**할 수 있도록 한다. 직접 반영 대신 **관리자 검수 워크플로우**를 거친다.

- 사용자: 폼 작성 + 증빙 자료 첨부 → 요청 제출 → 결과 확인(승인/반려).
- 관리자: 별도 UI 없이 백엔드 PATCH 엔드포인트(`/admin/store-requests/{id}`)를 직접 호출(Postman/curl/노션 시각화 + 외부 도구)하는 방식으로 검수. 이번 스코프에서 **클라이언트에 관리자 UI는 만들지 않는다.**

## 2. 백엔드 의존성

### 2.1 이미 정의·완료된 엔드포인트 (사용)

| 메서드 | 경로 | 용도 |
|---|---|---|
| POST | `/api/v1/maps/store-requests` | 요청 제출 (ADD/UPDATE 통합, 5/h rate limit) |
| GET | `/api/v1/maps/store-requests` | 내 요청 목록 (status 필터, 페이지네이션) |
| GET | `/api/v1/maps/store-requests/{request_id}` | 내 요청 단건 |
| DELETE | `/api/v1/maps/store-requests/{request_id}` | PENDING 요청 취소 |
| PATCH | `/api/v1/admin/store-requests/{request_id}` | (관리자만, 클라이언트는 호출하지 않음) |
| GET | `/api/v1/maps/geocode/reverse` | 좌표 → 행정구역 (핀 픽커에서 사용) |

응답·필드 정의는 노션 백엔드 명세 참조.

### 2.2 백엔드 변경 사항 (요청 → 처리 완료)

| # | 변경 | 상세 |
|---|---|---|
| 1 | DB 컬럼 추가 | `stores.owner_user_id` (FK to `users.id`, **NULLABLE**) — 외부 import POI는 NULL로 존재 가능 |
| 2 | API 응답 필드 추가 | `GET /maps/stores/{store_id}` 응답에 `is_owner: boolean` — 인증된 호출에서 `owner_user_id == 현재 user_id` 일 때만 `true`, 그 외 `false` |
| 3 | 승인 로직 분기 | `UPDATE` 승인 시 `target.owner_user_id == NULL` 이면 신청자 `user_id`로 채움 (소유권 클레임) |
| 4 | 알림 deeplink | SYSTEM 카테고리 매장 요청 알림 발송 시 `link` 필드에 `siheunggagae://store-request/{request_id}` 박기 |

### 2.3 향후 스코프 (이번 작업 외)

- 파일 업로드 multipart 엔드포인트 (사진/증빙 공통). 이번에는 **placeholder URL stub**으로 대체.
- 매장 요청 검수용 관리자 UI/백오피스.

## 3. 화면 구성

신규 화면 **4개** + 진입점 **2개** 보강.

### 3.1 진입점

#### ① 마이페이지 — "내 기록" 섹션
기존 "봉사 활동 이력 / 즐겨찾기 매장" 옆에 **"내 매장 요청"** 항목 추가. 아이콘 + 라벨 + 화살표 + PENDING 카운트(있으면) 미니 배지.

#### ② PlaceDetailScreen — "정보 수정 요청" 버튼
TopBar 영역 또는 상단 카드에 Outline 버튼. 표시 규칙:
- 비로그인 → 버튼 숨김
- 로그인 + `is_owner == true` → 활성 ("내 매장 정보 수정")
- 로그인 + `owner_user_id == null` → 활성 ("이 매장 클레임하기")
- 로그인 + 다른 owner_user_id → 회색 비활성 + "본인 명의 매장만 수정 요청 가능" 툴팁

#### ③ NotificationScreen — link deeplink
`NotificationItem.link`가 `siheunggagae://store-request/{id}` 패턴이면 클릭 시 `Screen.StoreRequestDetail` 로 navigate.

### 3.2 신규 화면 4개

#### MyStoreRequestsScreen (목록)
- TopBar: 뒤로가기 카드(40×40dp shadow=2dp) + "내 매장 요청" 18sp SemiBold
- 서브헤더: "전체 N건" 14sp SemiBold Brown700
- 상태 필터 칩 (가로 스크롤): 전체 / 대기(PENDING) / 승인(APPROVED) / 반려(REJECTED) — 카운트 동반
  - 선택: bg=`#1A1A1A` text=White
  - 미선택: bg=White border=`BorderBeige` text=`Brown700`
- 리스트 카드 (radius=16dp, elev=1dp):
  - Row: 좌측 카테고리 아이콘 + 매장명(15sp Bold) + 상태 태그(12sp) + 우측 `ic_chevron_right`
  - 보조 라인: "ADD/UPDATE · YYYY-MM-DD 신청/처리" 12sp `Brown700`
  - 상태 태그 색: PENDING(`#FEF3C7`/`#CA8A04` "검토중"), APPROVED(`#F0FDF4`/`#16A34A` "승인"), REJECTED(`#FEE7EC`/`#E84B6A` "반려")
- FAB: 56dp `FABBrown(#9A7B5E)` 원형, `ic_plus` 아이콘 White, 우하단 16dp
- 빈 상태: 80dp 원형 OrangeSand bg + `ic_store` 40dp + "아직 요청이 없어요" 18sp Bold + "+ 매장 추가 요청" CTA
- 무한 스크롤: page 1부터, size=20, 마지막 페이지에서 멈춤
- onResume마다 `refresh()` 호출

#### StoreRequestFormScreen (신규/수정 통합)
- TopBar: 뒤로가기 카드 + 제목 (ADD="매장 추가 요청" / UPDATE="매장 정보 수정")
- 본문 (단일 스크롤, 카드 없는 라벨형):
  1. **헤더**: 큰 질문문 22sp ExtraBold + 안내 13sp Brown700
  2. **매장명**: 라벨 + TextField (border 1dp BorderBeige, radius 12dp)
  3. **카테고리**: 2×2 그리드 — 각 셀 radius 14dp + 카테고리 아이콘(`ic_coffee` / `ic_utensils` / `ic_trees` / `ic_hotel`) + 한글 라벨
     - 카페(coffee) / 식당(utensils) / 공원(trees) / 펫호텔(hotel)
     - 선택: bg=`OrangeSand` border=2dp `Orange500` text Bold `Brown900`
     - 미선택: bg=White border=1dp `BorderBeige` text `Brown700`
  4. **위치**: 큰 미리보기 박스 (140dp 높이, 카카오맵 정적 미리보기 or MintLight placeholder + `ic_map_pin`)
     - 하단 오버레이 카드: 주소 + 좌표 12sp + "탭하여 핀 조정" 안내
     - 탭 시 `Screen.MapPinPicker` 진입, 결과 `lat/lng/address`를 SavedStateHandle로 회수
  5. **반려동물 동반 가능 토글**: Switch (켜짐=Brown900 / 꺼짐=Gray300)
  6. **가격 플랜** (카테고리=PET_HOTEL 일 때만 노출):
     - 라벨 + "호텔 전용" 핑크 핀
     - 카드(흰배경 + BorderBeige border): 각 행 `[plan_name TextField | price_krw TextField | × 삭제]`
     - 추가 행: `ic_plus` + "플랜 추가" — 클릭 시 빈 행 추가
     - 검증: 각 행 모두 채워야 제출 가능, plan_name 중복 금지(클라이언트 검증)
  7. **선택 정보**: 전화번호 / 영업시간 — 라벨 + TextField (placeholder color `#C1AEA0`)
  8. **매장 사진** (0/5): 가로 스크롤, 첫 칸은 `+` 박스(OrangeSand, `ic_image` + `ic_plus`). 각 썸네일에 우상단 `ic_x` 삭제
  9. **증빙 자료** (0/10) — **필수 빨간 태그** + "사업자등록증 등 본인 운영 증명":
     - 첨부된 항목: 카드 Row (40dp 타입 박지 + 파일명 + 용량 + `ic_x` 삭제)
       - PDF: `#FEE7EC` bg + "PDF" 텍스트 박지
       - 이미지: 그라디언트 썸네일 + `ic_image`
     - 추가 박스: OrangeSand bg + 점선 border + `ic_paperclip` + "파일 추가" + 안내 "사진·PDF·DOC 최대 10MB"
     - 안내 박스(`#FEF3C7` bg): "증빙 자료가 명확할수록 승인 가능성이 높아져요"
  10. **관리자에게 한마디** (선택, 0/1000): multiline TextField minHeight=80dp + 글자수 카운터
- 하단 고정 CTA: "요청 제출하기" 56dp Brown900 fullWidth radius=12dp 16sp SemiBold White
  - 비활성 조건: 필수값 미충족 또는 `proof_urls.size < 1` (엄격 정책) → alpha=40%
  - 제출 중: CircularProgressIndicator (White) 16dp
- 모드 전환:
  - 신규(ADD): 빈 폼
  - 수정(UPDATE): `target_store_id` 인자 받아서 `GET /maps/stores/{id}`로 prefill
  - 재제출(반려 후): `requestId` 인자 받아서 해당 요청의 `payload`로 prefill

#### StoreRequestDetailScreen
- TopBar: 뒤로가기 카드 + "요청 상세" 18sp SemiBold
- 상태별 헤더 카드(그라디언트, border 1dp):
  - PENDING: `#FEF3C7→#FFEDD4` border `Orange500` — `ic_clock` 흰 원형 + "검토 중" Bold `#CA8A04` + 안내 "평일 기준 1~2일 소요됩니다." + "신청 [date]"
  - APPROVED: `#F0FDF4→#D0FEE1` border `Green600` — `ic_check_circle` + "승인되었어요" Bold `#16A34A` + "매장이 지도에 추가되었습니다" + "처리 [date] · 관리자 메모: [note]"
  - REJECTED: `#FEE7EC→#FEFEFE` border `Pink500` — `ic_alert_circle` + "반려되었어요" Bold `#E84B6A` + 사유 White 카드 안에 `review_note` + "처리 [date]"
- (APPROVED) "등록된 매장" 미니 카드: 카테고리 그라디언트 박지 + 매장명 + ID + chevron → `Screen.PlaceDetail`
- "제출 내용" 섹션: White 카드 + 항목 Rows (유형, 매장명, 카테고리, 주소, 사진 수, 증빙 수, 가격 플랜 수)
- (REJECTED) 안내 박스: "💡 다시 작성하면 기존 내용이 채워진 채로 폼이 열려요."
- 하단 고정 CTA:
  - PENDING: "요청 취소" Outline `Pink500` 56dp — 확인 다이얼로그 후 DELETE
  - APPROVED: "매장 상세 보기" Brown900 56dp → PlaceDetail
  - REJECTED: "다시 작성하기" Brown900 56dp → StoreRequestForm(requestId=현재id, type=원본type)

#### MapPinPickerScreen
- 풀스크린 카카오맵 (`MapViewWrapper` 재사용)
- 상단: 백그라운드 그라디언트(white→투명) TopBar — 뒤로가기 카드 + "위치 선택" 18sp Bold (취소 시 결과 없이 닫음)
- 상단 검색바 (TopBar 아래 14dp): white radius=14dp + `ic_search_outline` + placeholder "주소 또는 장소명 검색"
  - 입력 시 카카오 Local API 검색(별도 호출, 디바운스 300ms) → 결과 드롭다운 → 선택 시 카메라 이동
- 중앙 고정 핀: `ic_map_pin` 40sp `Orange500` + 그림자 + 살짝 떠 있는 애니메이션(bob)
- 핀 아래 토스트형 안내: "지도를 움직여 핀을 조정하세요" — 처음 4초만 표시
- 우측 하단 FAB(검색바 아래 200dp): `ic_locate` (현재 위치) 44dp white radius=50% shadow
- 하단 시트(고정): radius 20dp top, padding 18dp shadow up
  - 라벨 "선택된 위치" 11sp Brown700 SemiBold
  - 주소 + 좌표 (Reverse Geocoding `/maps/geocode/reverse` 호출, 카메라 멈춤 후 200ms 디바운스)
  - CTA "이 위치로 설정" 56dp Brown900 → previousBackStackEntry.savedStateHandle에 `{ lat, lng, address }` 넣고 `popBackStack()`
- 진입 시 prefill: 인자 `lat`/`lng` 있으면 해당 좌표로 카메라 이동, 없으면 현재 위치(권한 있으면) 또는 시흥시청 기본 좌표.

## 4. 데이터 모델

`app/src/main/java/com/example/siheunggagae/data/model/StoreRequestModels.kt` 신규 생성.

```kotlin
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
    val category: String? = null,           // CAFE / RESTAURANT / PARK / PET_HOTEL
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
    val target_store_id: Int? = null,       // UPDATE 시 필수
    val payload: StoreRequestPayload,
    val proof_urls: List<String>? = null,
    val message: String? = null,
)

data class StoreRequestSubmitResponse(
    val request_id: Int,
    val type: StoreRequestType,
    val target_store_id: Int?,
    val status: StoreRequestStatus,         // 항상 PENDING
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

`MapModels.kt`의 `StoreDetailResponse`에 필드 추가:

```kotlin
data class StoreDetailResponse(
    // ... 기존 필드 ...
    val is_owner: Boolean = false,          // 백엔드 변경 ② — 비인증 시 기본 false
    val owner_user_id: Int? = null,         // 백엔드 응답 동봉 가정 — NULL이면 "클레임 가능 매장"
)
```

**진입점 ②(PlaceDetail "정보 수정 요청" 버튼) 표시 로직** — 위 두 필드 조합으로 판단:

| 토큰 | `is_owner` | `owner_user_id` | 버튼 상태 | 라벨 |
|---|---|---|---|---|
| 없음 | — | — | 숨김 | — |
| 있음 | `true` | (내 id) | 활성 | "내 매장 정보 수정" |
| 있음 | `false` | `null` | 활성 | "이 매장 클레임하기" |
| 있음 | `false` | (다른 id) | 비활성(회색) | "본인 명의 매장만 수정 요청 가능" (툴팁) |

## 5. Navigation Routes

`NavGraph.kt`의 `Screen` sealed class에 추가:

```kotlin
object MyStoreRequests    : Screen("my_store_requests")
object StoreRequestForm   : Screen("store_request_form?type={type}&storeId={storeId}&requestId={requestId}") {
    fun createRoute(type: StoreRequestType, storeId: Int? = null, requestId: Int? = null): String =
        "store_request_form?type=$type" +
            (storeId?.let { "&storeId=$it" } ?: "") +
            (requestId?.let { "&requestId=$it" } ?: "")
}
object StoreRequestDetail : Screen("store_request_detail/{requestId}") {
    fun createRoute(requestId: Int) = "store_request_detail/$requestId"
}
object MapPinPicker       : Screen("map_pin_picker?lat={lat}&lng={lng}") {
    fun createRoute(lat: Double? = null, lng: Double? = null): String =
        "map_pin_picker" +
            (lat?.let { "?lat=$it" } ?: "") +
            (if (lat != null && lng != null) "&lng=$lng" else lng?.let { "?lng=$it" } ?: "")
}
```

`NavHost` 컴포저블 블록 추가. ViewModel은 각 화면에서 `viewModel(factory = ...)` 패턴. MapPinPicker 결과 회수는:

```kotlin
val savedHandle = navController.currentBackStackEntry?.savedStateHandle
// 폼에서 picker 진입 후
val lat = savedHandle?.get<Double>("picked_lat")
```

## 6. Repository · API

`AuthApiService.kt`에 메서드 4개 추가:

```kotlin
@POST("maps/store-requests")
suspend fun submitStoreRequest(@Body body: StoreRequestSubmitRequest): StoreRequestSubmitResponse

@GET("maps/store-requests")
suspend fun getMyStoreRequests(
    @Query("status") status: String? = null,
    @Query("page") page: Int = 1,
    @Query("size") size: Int = 20,
): StoreRequestListResponse

@GET("maps/store-requests/{request_id}")
suspend fun getStoreRequest(@Path("request_id") requestId: Int): StoreRequestItem

@DELETE("maps/store-requests/{request_id}")
suspend fun cancelStoreRequest(@Path("request_id") requestId: Int)
```

`data/repository/StoreRequestRepository.kt` 신규 생성 — 각 메서드를 `ApiResult<T>`로 래핑(기존 패턴 따라).

## 7. ViewModel

### MyStoreRequestsViewModel
- UiState: `Loading` / `Success(items, total, page, hasMore, filter)` / `Error(message)`
- 액션: `refresh()`, `loadMore()`, `setFilter(status: StoreRequestStatus?)`
- 진입/onResume에 `refresh()`

### StoreRequestFormViewModel
- 인자: `type`, `storeId?`, `requestId?` (Factory에서 SavedStateHandle 경유)
- 상태:
  - `formState`: name/category/isPetAllowed/lat/lng/address/phone/hours/plans/photos/proofs/message
  - `validation`: 필드별 에러 맵
  - `submitState`: Idle / Submitting / Submitted(requestId) / Failed(message)
- 액션: `setField`, `addPlan`, `removePlan`, `addPhoto(Uri)`, `removePhoto`, `addProof(Uri)`, `removeProof`, `applyPickedLocation(lat, lng, address)`, `submit()`
- 제출 검증: 엄격 — `proof_urls` 비어 있으면 버튼 비활성
- Stub URL 생성: 선택한 Uri마다 `"https://placeholder.local/uploads/" + UUID.randomUUID()` 매핑 보관 후 제출 시 URL 리스트로 변환
- prefill:
  - UPDATE 모드: `storeId`로 `GET /maps/stores/{id}` → StoreDetailResponse → formState 채움
  - 재제출 모드: `requestId`로 `GET /maps/store-requests/{id}` → payload → formState 채움

### StoreRequestDetailViewModel
- UiState: `Loading` / `Success(item)` / `Error(message)`
- 액션: `cancel()` (PENDING만), `refresh()`

## 8. 아이콘 시스템 — Lucide 스타일

**컨벤션**: `app/src/main/res/drawable/ic_<영문이름>.xml`. 접두사 없음. 기존 `ic_*.xml`(Material Symbols filled 스타일)과 같은 폴더에 공존하되, 이번 스코프의 신규 4개 화면 + 진입점 2개에서 사용하는 아이콘은 **모두 Lucide stroke 스타일**로 통일. 기존 화면은 손대지 않음.

### 필요 아이콘 (vector drawable로 추가)

기존 `ic_*.xml`과 이름이 겹치지 않도록 정리. 같은 역할이라도 모양이 다르면 새 영문 이름으로 추가 (예: 기존 `ic_close.xml`은 유지, 신규는 `ic_x.xml`).

| 파일명 | 사용처 | 기존 충돌 |
|---|---|---|
| `ic_coffee.xml` | 카테고리 — 카페 | — |
| `ic_utensils.xml` | 카테고리 — 식당 | — |
| `ic_trees.xml` | 카테고리 — 공원 | — |
| `ic_hotel.xml` | 카테고리 — 펫호텔 | — |
| `ic_clock.xml` | 상태 — PENDING 헤더 | — (기존 `ic_schedule.xml`은 다른 화면 그대로 사용) |
| `ic_check_circle.xml` | 상태 — APPROVED 헤더 | — |
| `ic_alert_circle.xml` | 상태 — REJECTED 헤더 | — |
| `ic_alert_triangle.xml` | 안내 박스 | — (기존 `ic_priority_high.xml` 미사용) |
| `ic_plus.xml` | FAB · 플랜 추가 · 사진 추가 | 기존 `ic_add.xml` 별개 |
| `ic_x.xml` | 삭제 | 기존 `ic_close.xml` 별개 |
| `ic_chevron_right.xml` | 리스트 row 화살표 | 기존 `ic_keyboard_arrow_right.xml` 별개 |
| `ic_chevron_left.xml` | 뒤로가기 카드 내부 | 기존 `ic_keyboard_arrow_left.xml` 별개 |
| `ic_search_lucide.xml` 대신 → 기존 `ic_search.xml`은 Material 스타일이므로 **신규 파일명은 `ic_search_outline.xml`** | 검색바 | 기존 `ic_search.xml` 별개 |
| `ic_map_pin.xml` | 위치 마커 · 핀 픽커 | 기존 `ic_location_on.xml` 별개 |
| `ic_locate.xml` | 현재 위치 FAB | 기존 `ic_my_location.xml` 별개 |
| `ic_paperclip.xml` | 증빙 첨부 | — |
| `ic_image.xml` | 사진 첨부 / 이미지 파일 | — |
| `ic_file_text.xml` | PDF/DOC 파일 | — |
| `ic_store.xml` | 매장 일반 · 빈 상태 | — |
| `ic_info_outline.xml` | 정보 안내 | 기존 `ic_info.xml`(filled) 별개 |
| `ic_pencil.xml` | 편집 | — |
| `ic_lightbulb.xml` | 팁 안내 박스 | — |

### Lucide → Android Vector Drawable 변환 규칙

- viewport: 24×24
- 모든 path는 `android:fillColor="@android:color/transparent"` + `android:strokeColor="#1E120A"` + `android:strokeWidth="2"` + `android:strokeLineCap="round"` + `android:strokeLineJoin="round"`
- tint 적용용으로 `android:tint="?attr/colorControlNormal"` 추가 → Compose `Icon(painter = painterResource(...), tint = ...)` 에서 색 오버라이드
- 라이센스: Lucide ISC License — 별도 `NOTICE` 또는 앱 내 OSS 라이센스 화면에 출처 명시

## 9. 사진 / 증빙 Stub 전략

- 파일 선택: `rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents())`
  - 사진 섹션: mime `image/*`
  - 증빙 섹션: mime `*/*` (이미지·PDF·DOC 혼합)
- 선택된 `Uri`는 ViewModel `formState.photoUris` / `formState.proofUris`에 보관
- 미리보기:
  - 이미지: Coil `AsyncImage` 40dp radius=10dp
  - 파일: `ic_file_text` + 파일명 + 확장자별 색 박지(40dp radius=10dp)
    - PDF: bg `#FEE7EC` / text `#E84B6A` / 라벨 "PDF"
    - DOC·DOCX: bg `#DBEAFE` / text `#2563EB` / 라벨 "DOC"
    - 기타: bg `#F2F2F2` / text `#6B7280` / 라벨 확장자 대문자
- 제출 시 각 Uri를 `https://placeholder.local/uploads/<uuid>` 형태 URL로 변환 (실제 업로드 없음). `docs/gotcha.md`에 stub 경고 한 줄 추가.

## 10. 알림 라우팅

기존 `NotificationScreen` 아이템 클릭 핸들러에 deeplink 파싱 분기 추가:

```kotlin
fun handleNotificationClick(item: NotificationItem) {
    val link = item.link ?: return
    when {
        link.startsWith("siheunggagae://store-request/") -> {
            val id = link.substringAfterLast("/").toIntOrNull() ?: return
            onNavigate(Screen.StoreRequestDetail.createRoute(id))
        }
        // 다른 deeplink 패턴이 생기면 여기 추가
        else -> {}
    }
}
```

## 11. 에러 처리

- 공통: `ApiErrorEffect` + `SiheungSnackbarHost` 기존 패턴 재사용.
- 폼 제출 시 응답 상태별 처리:
  - **401**: 토큰 만료 흐름(기존 `TokenAuthenticator`) 위임
  - **403**: 폼 상단 배너 "본인 명의 매장만 수정 요청 가능" (UPDATE에서만 발생 가능)
  - **404** (UPDATE): "대상 매장이 더 이상 존재하지 않습니다. 목록으로 돌아갑니다." + 자동 뒤로가기
  - **409**: "이미 검토 중인 요청이 있습니다. 기존 요청을 먼저 처리해주세요." + "내 매장 요청" 이동 버튼
  - **422**: 필드별 에러 매핑 (서버 응답의 `errors[]` 사용)
  - **429**: RateLimit 배너 "잠시 후 다시 시도해주세요" (기존 `RateLimitBanner`)
- 취소(DELETE) 시 **409**: "이미 처리된 요청은 취소할 수 없어요" Snackbar + 자동 refresh
- 픽커 Reverse Geocoding 실패: 좌표만 표시, 주소는 "주소를 가져올 수 없어요" 회색 텍스트

## 12. 테스트 전략

- 단위 테스트 우선순위:
  1. `StoreRequestFormViewModel.validate()` — proof_urls 최소 1개, plans 중복 plan_name, lat/lng 페어, 필수 필드
  2. Deeplink 파서 (`handleNotificationClick`) — 잘못된 URL 무시 케이스
  3. Stub URL 생성기 — Uri 리스트 → URL 리스트 매핑 안정성
- UI 테스트(가능하면):
  - 상태별 DetailScreen 분기 렌더링
  - PET_HOTEL 카테고리 선택 시 plans 섹션 노출
  - 폼 prefill 검증 (UPDATE 모드, 재제출 모드)
- 통합: 실제 백엔드(스테이징)와 manual smoke test — ADD → PENDING 확인 → (백엔드 PATCH 수동 호출로) APPROVED → 알림 deeplink 클릭 → DetailScreen 진입 확인

## 13. Gotchas

- `MyRequestsScreen.kt`는 다른 기능(도움 요청 목록)을 위한 placeholder. 이 스펙의 `MyStoreRequestsScreen.kt`와 혼동 주의.
- `is_owner`는 인증된 호출일 때만 신뢰. 비인증 호출은 항상 `false`이므로 "정보 수정 요청" 버튼 표시 전에 토큰 존재 여부도 같이 확인.
- placeholder URL stub은 백엔드가 URL 형식 검증을 통과하는 한 그대로 들어감 (검수 단계에서 관리자가 알아채야 함). docs/gotcha.md에 명시.
- `MapPinPickerScreen`에서 뒤로가기로 닫는 경우 `savedStateHandle`에 값 안 넣음 → 폼은 기존 값 유지.

## 14. 작업 단위 (구현 시 분리할 PR 후보)

스펙 승인 후 writing-plans 스킬에서 세부 단계 도출. 대략적 묶음:

1. Data model + API service + repository (테스트 가능 단위)
2. Lucide 아이콘 vector drawable 일괄 추가
3. MyStoreRequestsScreen + ViewModel + NavGraph 연결 + 마이페이지 진입점
4. MapPinPickerScreen + ViewModel + 결과 회수 패턴 검증
5. StoreRequestFormScreen + ViewModel (ADD 흐름 우선)
6. StoreRequestFormScreen UPDATE/재제출 prefill 분기
7. StoreRequestDetailScreen + 알림 deeplink 파서 + PlaceDetail 진입점
8. 에러 처리 · 빈 상태 · 안내 박스 · QA
