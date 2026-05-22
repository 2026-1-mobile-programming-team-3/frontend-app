# 시흥가개 — API 엔드포인트 카탈로그

전체 엔드포인트는 단일 인터페이스 `app/src/main/java/com/example/siheunggagae/data/network/api/AuthApiService.kt` 에 모여 있다. 새 엔드포인트는 이 파일에 추가한다 (도메인별 파일 분리하지 않음). DTO는 `data/model/*Models.kt`.

**Base URL**: `https://backend-production-f6c0.up.railway.app/`

외부 참고: [Swagger](https://backend-production-f6c0.up.railway.app/docs), [Notion 명세서](https://www.notion.so/69e13e2ffd0e416285eb9358480c3673)

---

## 도메인별 엔드포인트

### Auth

| 메서드 | 경로 | 함수 | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/auth/signup` | `signup` | `SignupRequest` | `UserResponse` |
| POST | `/api/v1/auth/login` | `login` | `LoginRequest` | `LoginResponse` (access·refresh·expiresIn) |
| POST | `/api/v1/auth/refresh` | `refresh` | `TokenRefreshRequest` | `TokenRefreshResponse` |
| POST | `/api/v1/auth/logout` | `logout` | `LogoutRequest` (refresh_token) | `MessageResponse` |

`/auth/*` 경로는 `TokenAuthenticator`가 401 재시도 대상에서 제외함 (무한 루프 방지).

### Users (`/api/v1/users/me`)

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/users/me` | `getMe` | `UserMeResponse` |
| PATCH | `/users/me` | `updateMe(UserUpdateRequest)` | `UserMeResponse` |
| DELETE | `/users/me` | `deleteMe(AccountDeleteRequest)` | `MessageResponse` (탈퇴) |
| PUT | `/users/me/password` | `changePassword(PasswordChangeRequest)` | `MessageResponse` |
| GET | `/users/me/activity-stats` | `getActivityStats` | `ActivityStatsResponse` (내요청·봉사·즐겨찾기 수) |
| GET | `/users/me/volunteer-stats` | `getVolunteerStats` | `VolunteerStatsResponse` (누적 봉사·시간·평점) |
| GET | `/users/me/matches` | `getMyMatches(role, status, page, size)` | `MatchListResponse` (role 기본 `applicant`) |
| POST | `/users/me/volunteer-request` | `submitVolunteerRequest(VolunteerRequestCreate)` | `VolunteerRequestResponse` |

### Pets (`/api/v1/users/me/pets`)

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| POST | `/users/me/pets` | `addPet(PetCreate)` | `PetResponse` |
| PATCH | `/users/me/pets/{petId}` | `updatePet(petId, PetUpdate)` | `PetResponse` |
| DELETE | `/users/me/pets/{petId}` | `deletePet(petId)` | `Unit` (204 No Content) |

**반려동물 목록 조회 엔드포인트는 따로 없음** — `getMe()` 응답의 `pets` 필드에 포함됨.

### Blocks (`/api/v1/users/me/blocks`)

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/users/me/blocks?page&size` | `getBlocks` | `BlockListResponse` (기본 page=1, size=50) |
| POST | `/users/me/blocks` | `createBlock(BlockCreateRequest)` | `BlockCreatedResponse` |
| DELETE | `/users/me/blocks/{blockId}` | `deleteBlock(blockId)` | `Unit` |

### Favorites (`/api/v1/users/me/favorites/stores`)

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/users/me/favorites/stores?page&size` | `getFavoriteStores` | `FavoriteStoreListResponse` |
| POST | `/users/me/favorites/stores` | `addFavoriteStore(FavoriteStoreCreateRequest)` | `FavoriteStoreCreateResponse` |
| DELETE | `/users/me/favorites/stores/{storeId}` | `deleteFavoriteStore(storeId)` | `Unit` |

즐겨찾기 ID 집합은 클라이언트에서 `FavoritesCache`(in-memory `object`)에 동기화 유지.

### Matches (`/api/v1/matches`)

매칭(matching) = 이동 지원 요청 1건. 요청자 시점 vs 봉사자 시점이 동일 리소스를 다른 화면에서 표시함.

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/matches` | `getMatches(status, region, fromDate, toDate, page, size)` | `MatchListResponse` |
| POST | `/matches` | `createMatch(MatchCreateRequest)` | `MatchCreateResponse` |
| GET | `/matches/{matchId}` | `getMatchDetail(matchId)` | `MatchDetailResponse` |
| PATCH | `/matches/{matchId}` | `updateMatch(matchId, MatchUpdateRequest)` | `MessageResponse` |
| DELETE | `/matches/{matchId}` | `deleteMatch(matchId)` | `MessageResponse` |
| PATCH | `/matches/{matchId}/status` | `updateMatchStatus(matchId, MatchStatusUpdateRequest)` | `MatchStatusUpdateResponse` |

`status` 쿼리 값: `RECRUITING` / `REVIEWING` / `IN_PROGRESS` / `DONE` (대문자).

### Applications (`/api/v1/matches/{matchId}/applications`)

application = 한 봉사자가 특정 매칭에 보낸 신청 1건.

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/matches/{matchId}/applications` | `getApplications(matchId)` | `ApplicationListResponse` |
| POST | `/matches/{matchId}/applications` | `applyToMatch(matchId, ApplicationCreateRequest)` | `ApplicationCreateResponse` |
| PATCH | `/matches/{matchId}/applications/{applicationId}` | `respondToApplication(matchId, applicationId, ApplicationActionRequest)` | `ApplicationActionResponse` |
| POST | `/matches/{matchId}/review` | `submitMatchReview(matchId, MatchReviewRequest)` | `MatchReviewResponse` |

### Chat (`/api/v1/matches/.../messages`)

채팅방 = matchId + applicationId 쌍.

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/matches/{matchId}/chats` | `getChatThreads(matchId)` | `ChatThreadListResponse` (요청자가 받은 신청별 채팅 목록) |
| GET | `/matches/{matchId}/applications/{applicationId}/messages?before_id&size` | `getChatMessages` | `ChatMessageListResponse` (size 기본 50, 페이지네이션 `before_id`) |
| POST | `/matches/{matchId}/applications/{applicationId}/messages` | `sendChatMessage(ChatMessageCreateRequest)` | `ChatMessageCreatedResponse` |

**메시지 전송은 항상 REST POST.** WebSocket으로 보내면 서버가 무시함. WebSocket은 수신 전용(`wss://.../ws/applications/{applicationId}?token=...`).

### Home

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/api/v1/home/dashboard` | `getDashboard` | `DashboardResponse` (산책지수·날씨·주변 매장·뉴스 묶음) |

### News

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/api/v1/news` | `getNews` | `NewsListResponse` |
| GET | `/api/v1/news/{newsId}` | `getNewsDetail(newsId: String)` | `NewsDetailResponse` |
| GET | `/api/v1/news/calendar?year&month` | `getCalendar` | `CalendarResponse` |
| GET | `/api/v1/news/calendar/daily?date` | `getDailyCalendar` | `DailyCalendarResponse` |

`newsId`만 String, 나머지 ID는 모두 Int.

### Maps (`/api/v1/maps/stores`, `/maps/volunteers`)

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/maps/stores?lat&lng&radius` | `getNearbyStores` | `StoreListResponse` (radius 기본 2000m) |
| GET | `/maps/stores/search?keyword` | `searchStores` | `StoreSearchResponse` |
| GET | `/maps/stores/filter?category&is_pet_allowed` | `getFilteredStores` | `StoreListResponse` |
| GET | `/maps/stores/viewport?sw_lat&sw_lng&ne_lat&ne_lng&category` | `getStoresByViewport` | `StoreViewportResponse` |
| GET | `/maps/stores/{storeId}` | `getStoreDetail` | `StoreDetailResponse` |
| GET | `/maps/stores/{storeId}/reviews` | `getStoreReviews` | `StoreReviewListResponse` |
| POST | `/maps/stores/{storeId}/reviews` | `createStoreReview(StoreReviewCreateRequest)` | `StoreReviewCreateResponse` |
| DELETE | `/maps/stores/{storeId}/reviews/{reviewId}` | `deleteStoreReview` | `MessageResponse` |
| GET | `/maps/volunteers` | `getVolunteerMarkers` | `List<VolunteerMarkerDto>` (지도 봉사 요청 핀) |
| POST | `/maps/store-requests` | `submitStoreRequest` | `StoreRequestSubmitResponse` |
| GET | `/maps/store-requests` | `getMyStoreRequests` | `StoreRequestListResponse` |
| GET | `/maps/store-requests/{requestId}` | `getStoreRequest` | `StoreRequestItem` |
| DELETE | `/maps/store-requests/{requestId}` | `cancelStoreRequest` | `Unit` |

`category`(쿼리) 값: `CAFE` / `RESTAURANT` / `PARK` / `HOSPITAL` / `GROOMING` (대문자, `MapFilterStore.DEFAULT_CATEGORIES`).

**store-requests 엔드포인트 추가 안내:**
- 인증: 모든 store-requests 엔드포인트 인증 필요 (`Authorization: Bearer`).
- 요청 본문 카테고리: `CAFE` / `RESTAURANT` / `PARK` / `PET_HOTEL`.
- PET_HOTEL 전용 `plans` 배열 (각 항목 `planName`, `priceKrw`, `displayOrder?`).
- `proof_urls` / `proofUrls` 최대 10개. `photoUrls` 최대 5개.
- 사용자 측 알림 발송: 승인/거부 처리 시 백엔드가 SYSTEM 카테고리 알림 발송. `link` 필드에 `siheunggagae://store-request/{id}` deeplink가 박힘.

### Geo

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| GET | `/api/v1/geo/reverse?lat&lng` | `reverseGeocode` | `ReverseGeocodeResponse` (역지오코딩 → 동 라벨) |

### Notifications

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| POST | `/api/v1/notifications/devices` | `registerDevice(DeviceRegisterRequest)` | `DeviceRegisteredResponse` (FCM 토큰 + 디바이스명 등록) |
| GET | `/api/v1/notifications?category&is_read&page&size` | `getNotifications` | `NotificationListResponse` |
| GET | `/api/v1/notifications/unread-count` | `getUnreadCount` | `UnreadCountResponse` |
| PATCH | `/api/v1/notifications/{notificationId}/read` | `markNotificationRead` | `NotificationReadResponse` |
| PATCH | `/api/v1/notifications/read-all` | `markAllNotificationsRead` | `MarkAllReadResponse` |
| GET | `/api/v1/users/me/notification-settings` | `getNotificationSettings` | `NotificationSettingsResponse` |
| PATCH | `/api/v1/users/me/notification-settings` | `updateNotificationSettings(NotificationSettingsUpdateRequest)` | `NotificationSettingsResponse` |

`NotificationCategory` enum: 코드의 `data/model/NotificationModels.kt` 참고 (MATCH/VOLUNTEER/REVIEW/NEWS/POLICY/SYSTEM 등).

### Reports

| 메서드 | 경로 | 함수 | Response |
|---|---|---|---|
| POST | `/api/v1/reports` | `createReport(ReportCreateRequest)` | `ReportCreatedResponse` (사용자 신고) |
| POST | `/api/v1/reports/chat` | `reportChatUser(ChatReportCreateRequest)` | `ChatReportCreateResponse` (채팅 신고, 메시지 ID 포함) |

---

## 에러 분류 (`data/network/ApiResult.kt`)

| HTTP | `ApiErrorType` | 한국어 메시지 (기본) | 비고 |
|---|---|---|---|
| 400 | `BadRequest` | "잘못된 요청입니다" | |
| 401 | `Unauthorized` | "로그인이 필요합니다" | `TokenAuthenticator`가 refresh 시도. `/auth/*` 제외 |
| 403 | `Forbidden` | "접근 권한이 없습니다" | |
| 404 | `NotFound` | "찾을 수 없습니다" | |
| 409 | `Conflict` | "이미 존재하는 정보입니다" | 회원가입 이메일 중복 등 |
| 422 | `Validation` | (백엔드 detail 그대로) | `detail` 필드를 파싱해 원문 사용 |
| 429 | `RateLimited` | "요청이 너무 많습니다. 잠시 후 다시 시도해주세요" | Snackbar 대신 `RateLimitBanner` 노출 |
| 5xx | `ServerError` | "잠시 후 다시 시도해주세요" | |
| IOException | `Network` | "네트워크 연결을 확인해주세요" | |

422 응답 형식 (백엔드 표준):
```json
{ "detail": "이메일 형식이 올바르지 않습니다" }
{ "detail": [{ "msg": "비밀번호는 8자 이상", "loc": ["password"] }, ...] }
```
`parseErrorDetail()` 이 둘 다 처리해서 콤마로 합침.

---

## 표준 호출 패턴

### Repository (간단)

```kotlin
class UserRepository {
    private val api = RetrofitClient.api
    suspend fun getMe(): Response<UserMeResponse> = api.getMe()
}
```

### Repository (ApiResult 래핑)

`runApiCall` 헬퍼로 try/catch 보일러플레이트 제거:

```kotlin
suspend fun login(req: LoginRequest): ApiResult<LoginResponse> =
    runApiCall { api.login(req) }
```

### ViewModel에서 ApiResult 처리

```kotlin
fun load() = viewModelScope.launch {
    when (val result = repository.fetch()) {
        is ApiResult.Success -> _uiState.update { it.copy(data = result.data) }
        is ApiResult.Error -> {
            _lastError.value = result
            if (result.type == ApiErrorType.RateLimited) _rateLimited.value = true
        }
    }
}
```

### Screen에서 에러 표시

```kotlin
ApiErrorEffect(
    result = lastError,
    snackbarHostState = snackbarHostState,
    onRateLimited = { isRateLimited = true }
)
RateLimitBanner(visible = isRateLimited)
```

상세는 [architecture.md §3·§6](architecture.md#3-network-레이어).

---

## 새 엔드포인트 추가 체크리스트

1. **DTO 추가** — `data/model/XxxModels.kt`
   - 요청: `data class XxxRequest(val foo: String)`
   - 응답: `data class XxxResponse(...)`
   - Gson 정책이 `LOWER_CASE_WITH_UNDERSCORES` 이므로 Kotlin 필드는 camelCase 그대로 (예: `userId` ↔ `user_id` 자동 매핑).
2. **API 메서드 추가** — `AuthApiService.kt` 의 해당 도메인 섹션에 `suspend fun` 추가
   - 모든 메서드는 `suspend fun` + `Response<T>` 반환 (`Response<Unit>` 은 204 No Content)
   - 인자: `@Body`, `@Path`, `@Query` 사용. `@Path` 는 Int/String, `@Query` 는 기본값 가능
3. **Repository 메서드 추가** — `data/repository/XxxRepository.kt`
   - 단순 위임이면 `suspend fun foo() = api.foo()`
   - 에러 분류 필요하면 `runApiCall { api.foo() }` 로 `ApiResult` 반환
4. **ViewModel에서 호출** — `viewModelScope.launch` + `_uiState.update {}` 패턴
5. **사이드이펙트** (선택)
   - 로그인/회원가입 직후라면 `tokenManager.saveTokens()` + `fcmTokenManager.registerCurrentDevice()`
   - 즐겨찾기 변경이면 `FavoritesCache.add()/remove()` 동기화

---

## 자주 헷갈리는 포인트

- **`{newsId}` 만 String**, 나머지 ID(`matchId`/`applicationId`/`storeId`/`petId`/`blockId`/`reviewId`) 는 모두 Int.
- **`/users/me/matches` vs `/matches`**: 전자는 "내가 신청·요청한 매칭", 후자는 "전체 매칭 목록"(공개 게시판).
- **`getMyMatches(role)`**: `applicant` = 봉사자로 신청한 매칭, `requester` = 내가 작성한 요청.
- **`updateMatch` 응답이 `MessageResponse`** (단순 메시지). 상세 갱신이 필요하면 별도로 `getMatchDetail` 재호출.
- **`updateMatchStatus`** 는 별도 엔드포인트. body는 `MatchStatusUpdateRequest`.
- **반려동물 목록 단독 GET 없음**. `getMe()` 응답에 포함.
- **`getNotifications(page=1)`** 호출 시 클라이언트 `LocalNotificationStore` 에 쌓인 로컬 알림이 앞에 머지됨 (page 2+는 머지 안 함).
- **WebSocket 메시지 송신 금지**. 항상 `sendChatMessage` REST POST.
- **`deletePet` / `deleteBlock` / `deleteFavoriteStore` / `respondToApplication`** 등은 `Response<Unit>` 또는 `Response<MessageResponse>` 로 응답이 거의 비어있음 — `body()` 의존하지 말고 `isSuccessful` 만 확인.
