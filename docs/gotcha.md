# 시흥가개 — 함정 & 주의사항

코드만 봐서는 알기 어려운 비직관적인 동작, 한 번 데인 적 있는 함정, 변경하기 전에 확인해야 할 항목 모음. 새 기능 추가 전·디버깅 중 한 번씩 훑어볼 것.

분류 순서: **인증 → 네트워크 → 채팅·FCM → 지도·위치 → 네비게이션·화면 → 데이터·캐시 → 빌드·환경**.

---

## 인증 / 토큰

### 401 응답을 직접 처리하지 말 것

`TokenAuthenticator`(OkHttp `Authenticator`)가 모든 401을 가로채서 refresh 시도 후 원 요청을 재시도한다. ViewModel/Repository 에서 `response.code() == 401` 분기를 추가로 두면 중복 처리되어 사용자가 의도치 않은 경로로 흐를 수 있음. 401은 **`TokenAuthenticator` 의 콜백(`onSessionExpired`)에 맡기고** 화면 코드는 그냥 `Unauthorized` `ApiResult.Error` 로 받기.

### `/auth/*` 경로는 401 재시도에서 제외됨

`TokenAuthenticator.authenticate()` 에서 `if ("/auth/" in response.request.url.encodedPath) return null` 처리. 로그인 자체가 401 받으면 refresh 시도하지 않고 그대로 실패. 비밀번호 틀렸을 때 "잠시만요" 같은 로딩이 안 보이는 이유.

### 토큰은 EncryptedSharedPreferences. 평문 prefs 와 섞지 말 것

`TokenManager` 의 `secure_auth_tokens` prefs 파일은 AES256_GCM 으로 암호화. 다른 SharedPreferences (예: `MapFilterStore`, `LocalNotificationStore`) 와 같은 이름 쓰면 안 됨. 토큰 디버깅하려고 `prefs.all` 출력해도 복호화 안 된 바이트만 보임 — 디바이스 셸로 못 봄.

### 세션 만료는 채널로 신호

`TokenAuthenticator` 가 refresh 실패 시 `SiheungGagaeApp.sessionExpiredChannel.trySend(Unit)`. `NavGraph` 가 이 채널을 구독하고 `navigate(Login) { popUpTo(0) { inclusive=true } }` 호출. 새 진입점(다른 Activity)을 추가하면 이 채널 구독을 같이 옮겨야 함.

### `expiresIn` 단위는 초

`TokenManager.saveTokens(access, refresh, expiresIn = 3600)` — 두 번째 인자는 **초**. 백엔드도 초 단위로 줌. 잘못 ms 로 다루면 한참 후에야 만료된다고 판단됨.

---

## 네트워크 / Retrofit

### Base URL 이 3곳에 분산

- `RetrofitClient.kt` — REST
- `TokenAuthenticator.kt` (`REFRESH_URL` 상수)
- `ChatWebSocketManager.kt` (`WS_BASE`, wss://)

환경(스테이징/프로덕션) 변경 시 셋 다 수정. `BuildConfig` 로 빼는 게 정석인데 아직 안 빠져있음.

### Gson 네이밍 정책: `LOWER_CASE_WITH_UNDERSCORES`

Kotlin 필드 `userId` ↔ JSON `user_id` 자동 매핑. **수동으로 `@SerializedName` 붙이면 정책과 충돌**해서 디버깅 어려움. DTO 작성할 때 camelCase 그대로 두기.

### `Response<Unit>` 은 204 No Content 의미

`deletePet`, `deleteBlock`, `deleteFavoriteStore` 등. `response.body()` 가 `Unit` 또는 null 일 수 있어서 `.isSuccessful` 만 확인. body 추출하려 들면 NPE.

### 422 Validation 만 백엔드 메시지 그대로 사용

다른 상태코드(400/401/...)는 한국어 고정 메시지(`koreanMessage()`). 422만 `detail` 필드(string 또는 array)를 파싱해서 사용자에게 보여줌. 백엔드가 메시지 바꾸면 즉시 노출되니 검수 필요.

### 로깅 인터셉터가 BODY 레벨

`HttpLoggingInterceptor.Level.BODY` — 비밀번호·토큰까지 전부 logcat에 찍힘. **릴리스 빌드에서 무조건 끌 것** (현재 `BuildConfig.DEBUG` 분기 없음).

### `runApiCall` 의 catch 가 광범위

`IOException` 만 Network로 분류하고, 그 외 `Exception` 은 모두 `ServerError` 로 묶임. `JSONException`(파싱 실패) 같은 게 ServerError 로 둔갑하니 5xx 인지 파싱 문제인지 구분하려면 BODY 로그를 봐야 함.

---

## 채팅 / WebSocket / FCM

### WebSocket 으로 메시지 전송 금지

`ChatWebSocketManager` 는 **수신 전용**. `send()` 메서드가 있어도 서버가 무시함. 메시지 전송은 **항상 REST POST** (`api.sendChatMessage`). 보낸 후 자기 메시지는 WebSocket이 echo로 받아서 화면에 추가됨.

### WS 토큰은 쿼리스트링

URL: `wss://.../ws/applications/{applicationId}?token=<accessToken>`. HTTP 헤더 아님. **토큰이 만료되면 close code 4401** 로 끊김 → `WsEvent.Unauthorized` 발행 → `refreshToken()` 콜백 호출 → 재연결.

### 채팅방 단위는 `applicationId`

`matchId` 가 같아도 신청별로 별도 채팅방. URL/WebSocket 모두 `{matchId}/{applicationId}` 쌍. ChatScreen에 들어갈 때 둘 다 필요.

### FCM 토큰은 3곳에서 등록

1. 앱 부팅 — `SiheungGagaeApp.onCreate()` → `fcmTokenManager.registerCurrentDevice()`
2. 로그인 직후 — `AuthRepository.login()`
3. Firebase 토큰 갱신 — `FcmService.onNewToken()`

**로그아웃 시점에는 토큰 해제 안 함** (서버에 디바이스 삭제 호출 없음). 같은 디바이스로 다른 계정 로그인하면 이전 계정 알림이 잠시 더 옴.

### 알림 권한이 Android 13+ 에서 필수

`POST_NOTIFICATIONS` 권한은 `AndroidManifest.xml` 에 선언되어 있지만 **런타임 요청 코드는 현재 없음**. 사용자가 시스템 설정에서 직접 허용해야 푸시가 보임. `NotificationManagerCompat.from(this).areNotificationsEnabled()` 체크는 `FcmService` 안에만 있음.

### FCM 알림 ID 충돌 주의

`FcmService.showNotification()` 이 `System.currentTimeMillis().toInt()` 를 알림 ID로 씀. 같은 ms 에 두 개 도착하면 한 개가 다른 걸 덮어쓸 수 있음. `LocalNotificationStore` 도 별도로 음수 ID 를 쓰는데 — 서버 ID(양수) 와 충돌 안 나도록 분리되어 있으니 ID 새로 만들 때 부호 규칙 지킬 것.

### `LocalNotificationStore` 는 page 1 만 머지

`NotificationRepository.getNotifications(page=1)` 일 때만 로컬 알림을 서버 응답 앞에 붙임. page 2+ 호출에서는 머지 안 함. 페이지네이션 무한 스크롤이 어느 페이지부터 로컬 알림을 빠뜨리는지 확인할 때 이거 기억할 것.

---

## 지도 / 위치 / Kakao

### `MapViewWrapper.reinit()` 호출 후 마커 사라짐

내부적으로 KakaoMap 세션을 다시 만드므로 추가했던 마커들 다 날아감. `reinit()` 후엔 **반드시 마커를 다시 추가**.

### 줌 레벨에 따라 마커 렌더가 다름

- 줌 14↑: 개별 마커(카테고리 이모지 + 매장명 비트맵)
- 줌 11–13: 그리드 기반 클러스터 (수가 많을 때만)
- 줌 10↓: 사실상 안 보이게 처리

매장이 너무 안 보이면 우선 줌 레벨부터 확인.

### Kakao SDK 키 누락 시 무성격 크래시

`local.properties:kakao.app.key` 가 비어있으면 `BuildConfig.KAKAO_APP_KEY` 가 빈 문자열 → `manifestPlaceholders["kakaoAppKey"]` 가 빈 값 → `AndroidManifest.xml` 의 `meta-data` 가 비어서 SDK 초기화 시 의미 없는 NPE. 신규 개발자 셋업에서 자주 막힘.

### 위치 권한이 없으면 placeholder

`LocationProvider.hasPermission()` 이 false 면 `getLocationOrNull()` 이 즉시 null 반환. 지도 화면에서 위치 권한 거부 시 KakaoMap 위에 민트-White 그라디언트 placeholder + 더미 핀 표시. 사용자에게 "GPS 없음" 안내 메시지는 현재 없음.

### 시흥시 18개 동(洞) 좌표 하드코딩

`HomeViewModel.kt` 에 시흥시 행정동 GPS 가 직접 박혀있음. 동 추가/변경되면 여기 갱신. 다른 도시로 확장하면 이 맵을 동적으로 받아야 함.

### `getStoresByViewport` vs `getNearbyStores`

- `getNearbyStores(lat, lng, radius)`: 한 점 기준 반경 검색 (홈 미니맵)
- `getStoresByViewport(sw_lat, sw_lng, ne_lat, ne_lng)`: 화면에 보이는 사각형 영역 (지도 화면)

지도 패닝 중에는 viewport API 를 써야 함. nearby 로 계속 호출하면 가장자리 매장이 누락됨.

---

## Navigation / Compose

### `MyRequestsScreen.kt` 파일은 사용되지 않음

`ui/screen/MyRequestsScreen.kt` 가 존재하지만 NavGraph 는 **같은 NavGraph.kt 파일 안의 placeholder("준비 중입니다", `NavGraph.kt:1011-1059`)** 를 사용 중. 실제 화면 연결하려면 NavGraph 의 `composable(Screen.MyRequests.route)` 블록을 수정해서 `ui.screen.MyRequestsScreen` 으로 교체해야 함.

### `Screen.Privacy` 라우트가 두 경로로 도달

- `Screen.Privacy` (`privacy`) → `PrivacyPolicyScreen` (WebView, 직접)
- `Screen.Settings` (`settings?section=privacy`) → SettingsScreen 내에서 자동 모달 오픈

MyScreen 에서 "개인정보 및 보안" 누르면 후자(설정 화면 + 시트). 별도 진입점이 있다는 걸 잊으면 동작 다른 두 경로를 만들기 쉬움.

### 메인탭 5개 라우트는 `navigateTab` 필요

```kotlin
private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Screen.Home.route) { saveState = true }
    }
}
```

일반 `navigate()` 호출하면 백스택에 탭들이 쌓이고 BottomBar 가 사라졌다 나타났다 함. 메인탭에서 다른 메인탭으로 갈 땐 반드시 `navigateTab`.

### `isTopLevelTabRoute` 가 Map 라우트는 prefix 매칭

`Screen.Map.route` 는 `"map"` 이지만 실제 라우트는 `"map?volunteerMode=..."` 처럼 쿼리가 붙음. `isTopLevelTabRoute()` 는 `route.startsWith(Screen.Map.route)` 로 체크. 다른 탭에 옵션 쿼리 추가하면 같은 패턴 따라야 BottomBar 가 정상 표시됨.

### `collectAsState()` 가 아닌 `collectAsStateWithLifecycle()`

기본 `collectAsState()` 는 화면이 백그라운드여도 계속 collect. 배터리 낭비·메모리 누수 가능. 모든 ViewModel UiState 는 `collectAsStateWithLifecycle()` 로 받기.

### `viewModel()` 호출 위치에 따라 인스턴스 공유 여부 달라짐

`composable {}` 안에서 그냥 `viewModel(factory = ...)` → 해당 NavBackStackEntry 기준. 다른 화면으로 가면 onCleared 됨.

`viewModel(viewModelStoreOwner = backStackEntry, ...)` 명시 → 명시적 owner. `RequestFlowScreen` 이 이 패턴 사용 (3단계 폼 상태를 한 ViewModel 인스턴스로 유지하기 위해).

`Activity` ViewModelStoreOwner 로 잡으면 앱 전역 공유 (현재 사용처 없음).

### `@Preview` 에서 ViewModel = null 처리

화면 시그니처는 `viewModel: XxxViewModel? = null` 패턴. Preview 에서는 null 넘기고 ViewModel 사용처는 `viewModel?.let { ... } ?: dummyState` 같은 방어 코드 추가. 새 화면 만들 때 잊지 말 것.

---

## 데이터 / 캐시

### `FavoritesCache` 는 메모리에만 산다

`object FavoritesCache` 는 프로세스 메모리. 앱이 죽으면 사라지고 다시 받아야 함. 앱 부팅 시점에 한 번 `init()` 호출되도록 보장(현재는 화면 진입 시 lazy init). 즐겨찾기 토글 후 백엔드 동기화 실패하면 캐시랑 서버가 어긋날 수 있음.

### 같은 API 가 다른 응답 모양

- 매칭 목록: `MatchListResponse`
- 내 매칭: `MyMatchListResponse` (응답 모양이 다름! `MatchListItem` vs `MyMatchResponse`)

`VolunteerHistoryScreen` 에서 한 번 잘못 import 해서 컴파일 안 됐던 적 있음 (코드 주석에 흔적 있음: `// MyMatchResponse 대신 MatchListItem 사용`).

### `getMyMatches(role)` 의 status null 이슈

기본 `status = null` 이면 모든 상태 반환. 빈 문자열 `""` 보내면 백엔드가 빈 결과 반환할 수 있음. **상태 미필터링은 반드시 null**.

### `LocalNotificationStore` 의 음수 ID

서버 알림 ID는 양수, 로컬 알림 ID는 음수로 충돌 회피. ID로 정렬·검색하는 코드 추가할 때 부호 가정 깨지 말 것.

### 이미지 캐시는 Coil 기본 정책

Coil 3 의 기본 메모리·디스크 캐시 사용. 프로필 이미지 같이 자주 바뀌는 것은 캐시 무효화 필요할 수 있음. 현재 강제 새로고침 로직 없음.

---

## 빌드 / 환경

### 최소 SDK 35

Android 14 (API 34) 디바이스에서 설치 안 됨. 에뮬레이터 만들 때 **API 35+** 로 만들어야 함. 사내 보유 단말 호환성 확인 필요.

### `google-services.json` 위치

프로젝트 루트가 아닌 `app/` 폴더에 있어야 Firebase Gradle 플러그인이 인식. 옮기지 말 것.

### `local.properties` 는 깃 제외

`kakao.app.key` 가 들어가는 파일. `.gitignore` 에 포함되어 있으니 새 클론한 동료는 직접 채워야 함. 누락 시 빌드는 되지만 지도 SDK 초기화 시 NPE.

### `colors.xml` 은 비어있다시피 함

`res/values/colors.xml` 은 거의 빈 파일이고, **실제 색상은 Compose `Color.kt` + 각 화면 파일의 `private val ColorX = Color(0xFF...)`** 에 정의됨. XML 색상 참조 코드(예전 View 기반) 추가하면 색이 안 맞음.

### `MapViewWrapper.kt` 는 루트 패키지

`com.example.siheunggagae` 바로 아래(`MapViewWrapper.kt`, `MainActivity.kt`, `SiheungGagaeApp.kt`, `NavGraph.kt` 와 같은 레벨). `ui/` 나 `data/` 안에 두지 않은 이유는 — 명확치 않음, 그냥 그렇게 됐음. 옮기면 import 경로 줄줄이 바뀜.

---

## 변경 전 확인할 것

| 작업 | 주의 |
|---|---|
| Base URL 바꾸기 | `RetrofitClient`, `TokenAuthenticator`, `ChatWebSocketManager` 3곳 |
| 새 영역(시) 추가 | `HomeViewModel` 의 동 좌표 맵 + 백엔드 매핑 확인 |
| 새 매칭 상태 enum 추가 | UI 색상 매핑(`tag` 색), 필터 탭, 상태 텍스트 매핑 |
| 새 알림 카테고리 | `NotificationCategory` enum, 아이콘 색 매핑, `LocalNotificationStore` |
| Compose Material BOM 업그레이드 | 폰트 패밀리 변경 시 `Pretendard` 로딩 깨질 수 있음 |
| Kakao Map SDK 업그레이드 | `MapViewWrapper` 의 콜백 시그니처 변경 가능 |
| 빌드 도구 (AGP/Kotlin) 업그레이드 | Compose Compiler 호환성 표 확인 |
