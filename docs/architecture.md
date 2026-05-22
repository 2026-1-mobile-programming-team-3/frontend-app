# 시흥가개 — 아키텍처

화면별 디자인 스펙은 [`screens.md`](screens.md), 디자인 시스템(컬러·Typography·공통 컴포넌트)은 [`../CLAUDE.md`](../CLAUDE.md) 참고.

이 문서는 새 화면·API·기능을 만들 때 따라야 할 **코드 패턴**을 정의한다.

## 1. 레이어 구조

```
Screen (Compose)  ──collectAsStateWithLifecycle──>  UiState (StateFlow)
                                                         ▲
                                                         │ MutableStateFlow
ViewModel  ──repository.foo()──>  Repository  ──api.foo()──>  Retrofit
                                       │
                                       └──>  Local (TokenManager / DataStore / Cache)
```

| 레이어 | 책임 | 위치 |
|---|---|---|
| **Screen** | Compose UI, 이벤트 콜백, 상태 표시. 비즈니스 로직 없음 | `ui/screen/` |
| **ViewModel** | UiState 보관(`StateFlow`), Repository 호출, `viewModelScope` | `ui/viewmodel/` |
| **Repository** | API 호출 래핑, 로컬 저장소 통합, 데이터 변환 | `data/repository/` |
| **Network** | Retrofit, OkHttp 체인, WebSocket, FCM | `data/network/`, `data/network/api/` |
| **Local** | EncryptedSharedPreferences, DataStore, 인메모리 캐시 | `data/local/` |
| **Model (DTO)** | API 요청·응답 데이터 클래스 | `data/model/` |

### 호출 흐름 예시 (로그인)

```kotlin
// LoginScreen → AuthViewModel
viewModel.login(email, password)

// AuthViewModel
fun login(email: String, password: String) {
    viewModelScope.launch {
        _uiState.value = AuthUiState.Loading
        val response = repository.login(LoginRequest(email, password))
        _uiState.value = when {
            response.isSuccessful -> AuthUiState.Success
            response.code() == 429 -> AuthUiState.RateLimited
            else -> AuthUiState.Error(parseError(response))
        }
    }
}

// AuthRepository
suspend fun login(request: LoginRequest): Response<LoginResponse> {
    val response = api.login(request)
    if (response.isSuccessful) {
        response.body()?.let { tokenManager.saveTokens(it.accessToken, it.refreshToken, it.expiresIn) }
        fcmTokenManager?.registerCurrentDevice()
    }
    return response
}
```

---

## 2. 의존성 주입 (수동 주입, Hilt 미사용)

`SiheungGagaeApp`(`Application`)이 싱글톤을 보유하고, NavGraph 각 `composable {}` 블록이 Application에서 꺼내 ViewModel Factory에 넘긴다.

### Application 보유 인스턴스 — `SiheungGagaeApp.kt`

```kotlin
class SiheungGagaeApp : Application() {
    lateinit var tokenManager: TokenManager
    lateinit var fcmTokenManager: FcmTokenManager
    lateinit var localNotificationStore: LocalNotificationStore
    var geoRepository: GeoRepository? = null
    val sessionExpiredChannel = Channel<Unit>(Channel.CONFLATED)

    override fun onCreate() {
        super.onCreate()
        KakaoMapSdk.init(this, BuildConfig.KAKAO_APP_KEY)
        tokenManager = TokenManager(applicationContext)
        localNotificationStore = LocalNotificationStore(applicationContext)
        RetrofitClient.init(tokenManager) { sessionExpiredChannel.trySend(Unit) }
        fcmTokenManager = FcmTokenManager(RetrofitClient.api, tokenManager)
        geoRepository = GeoRepository(RetrofitClient.api, LocationProvider(applicationContext))
    }
}
```

`AndroidManifest.xml` 에 `android:name=".SiheungGagaeApp"` 로 등록되어 있다.

### ViewModel 인스턴스화 표준 패턴

#### ViewModel 정의 — `Factory`를 내부에 둔다

```kotlin
class HomeViewModel(
    private val api: AuthApiService,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    class Factory(
        private val api: AuthApiService,
        private val locationProvider: LocationProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(api, locationProvider) as T
    }
}
```

#### NavGraph 안에서 주입

```kotlin
composable(Screen.Home.route) {
    val app = LocalContext.current.applicationContext as SiheungGagaeApp
    val api = RetrofitClient.api
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(api, LocationProvider(LocalContext.current))
    )
    HomeScreen(viewModel = homeViewModel, onNavigate = { navController.navigateTab(it) })
}
```

#### 규칙

- `RetrofitClient.api`는 어디서나 호출 가능 (싱글톤, `Application.onCreate()`에서 `init()` 끝남).
- ViewModel은 **Repository를 인자로 받는 게 표준** (단, `HomeViewModel`처럼 `AuthApiService`를 직접 받는 케이스도 존재).
- ViewModel이 인자(예: `{requestId}`)에 의존하면 NavGraph에서 `backStackEntry.arguments`를 꺼내 Factory에 넘긴다.

### 라우트 인자 받기 — `SavedStateHandle` 대신 Factory 주입

```kotlin
composable(
    route = Screen.MatchingDetail.route,
    arguments = listOf(navArgument("requestId") { type = NavType.IntType }),
) { backStackEntry ->
    val requestId = backStackEntry.arguments?.getInt("requestId") ?: 0
    val detailViewModel: MatchDetailViewModel = viewModel(
        factory = MatchDetailViewModel.Factory(RetrofitClient.api)
    )
    MatchingDetailScreen(
        requestId = requestId,                 // ViewModel 내부에서 fetchDetail(requestId)
        viewModel = detailViewModel,
        onBack = { navController.popBackStack() },
    )
}
```

`SavedStateHandle`은 사용하지 않음. 화면 컴포저블에 인자(`requestId: Int`)를 그대로 넘긴다.

---

## 3. Network 레이어

### 3.1 RetrofitClient (`data/network/RetrofitClient.kt`)

- **Base URL**: `https://backend-production-f6c0.up.railway.app/`
- **타임아웃**: 30초 (connect / read / write)
- **JSON 직렬화**: Gson, `FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES`
- **싱글톤**: `object RetrofitClient`, `init(tokenManager, onSessionExpired)` 호출 후 `api` 사용 가능

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://backend-production-f6c0.up.railway.app/"
    fun init(tokenManager: TokenManager, onSessionExpired: () -> Unit) { ... }
    val api: AuthApiService by lazy { ... }
}
```

### 3.2 OkHttp 인터셉터 체인

순서: **AuthInterceptor → HttpLogging → Authenticator(401 후처리)**

| 컴포넌트 | 파일 | 역할 |
|---|---|---|
| `AuthInterceptor` | `AuthInterceptor.kt` | 모든 요청에 `Authorization: Bearer <accessToken>` 추가 |
| `HttpLoggingInterceptor` | OkHttp 내장 | BODY 레벨 전체 로깅 |
| `TokenAuthenticator` | `TokenAuthenticator.kt` | 401 응답 시 refresh 후 원 요청 재시도. 실패 시 `onSessionExpired()` 콜백 → 로그인 화면 강제 이동 |

### 3.3 ApiResult — 에러 분류 표준 (`data/network/ApiResult.kt`)

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val type: ApiErrorType, val message: String) : ApiResult<Nothing>()
}

enum class ApiErrorType {
    BadRequest, Unauthorized, Forbidden, NotFound,
    Conflict, Validation, RateLimited, ServerError, Network,
}

fun <T> Response<T>.toApiResult(): ApiResult<T>
suspend fun <T> runApiCall(block: suspend () -> Response<T>): ApiResult<T>
```

#### 사용 가이드

- `ApiResult.Error.message`는 한국어 사용자 표시용으로 매핑되어 있음. 화면에서 그대로 Snackbar에 노출.
- `RateLimited(429)`는 Snackbar가 아닌 `RateLimitBanner`로 표시 (4초 자동 사라짐).
- `Validation(422)`만 백엔드 원문 메시지를 그대로 사용.

### 3.4 API 인터페이스 — `data/network/api/AuthApiService.kt`

전체 엔드포인트가 단일 인터페이스(`AuthApiService`)에 모여 있다 (인증·매칭·알림·사용자·반려동물·즐겨찾기·차단·신고·결제 등 50+).
**규칙**: 새 엔드포인트는 이 파일에 추가. 도메인별로 파일 쪼개지 않음.

```kotlin
interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/v1/matches")
    suspend fun getMatches(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
    ): Response<MatchListResponse>

    // ... 50+ 더 많은 엔드포인트
}
```

요청·응답 DTO는 `data/model/*Models.kt`. 도메인별 파일 분리 (예: `MatchModels.kt`, `AuthModels.kt`).

---

## 4. 인증 토큰 흐름

### 4.1 저장 — `TokenManager` (`data/local/TokenManager.kt`)

- **저장소**: EncryptedSharedPreferences (`secure_auth_tokens`)
- **암호화**: 키 AES256_SIV, 값 AES256_GCM, MasterKey AES256_GCM
- **항목**: `access_token`, `refresh_token`, `expires_at`(만료 epoch ms)
- **추가 보유**: `localProfileImageUri: StateFlow<String?>` — 프로필 이미지 로컬 URI

```kotlin
val accessToken: String?
val refreshToken: String?
fun saveTokens(access: String, refresh: String, expiresIn: Int = 3600)
fun clearTokens()
fun isAccessTokenExpired(): Boolean   // 만료 30초 전부터 true
```

### 4.2 수명 주기

```
로그인 성공
  → AuthRepository.login() 안에서 tokenManager.saveTokens()
  → fcmTokenManager.registerCurrentDevice()  (FCM 등록)

모든 API 요청
  → AuthInterceptor: Authorization: Bearer <accessToken>

401 응답 수신
  → TokenAuthenticator.authenticate() (동기화 블록)
  → POST /api/v1/auth/refresh { refresh_token }
  → 새 토큰 saveTokens() → 원 요청 재시도

refresh 실패 (401, 네트워크 오류)
  → tokenManager.clearTokens()
  → sessionExpiredChannel.trySend(Unit)
  → NavGraph: navigate(Login) { popUpTo(0) { inclusive = true } }
```

`/auth/` 경로 요청은 401 받아도 재시도하지 않음 (무한 루프 방지).

---

## 5. UiState 패턴

### 5.1 두 가지 스타일

#### (A) Sealed class — 상태가 명확히 갈리는 경우 (Loading/Success/Error)

```kotlin
sealed class MatchingUiState {
    object Loading : MatchingUiState()
    data class Success(val matches: List<MatchListItem>) : MatchingUiState()
    data class Error(val message: String) : MatchingUiState()
}

private val _uiState = MutableStateFlow<MatchingUiState>(MatchingUiState.Loading)
val uiState: StateFlow<MatchingUiState> = _uiState
```

#### (B) data class — 복합 상태 (홈처럼 여러 필드가 동시에 변동)

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val regionDong: String = "정왕동",
    val walkScore: Int = 0,
    val news: List<NewsItem> = emptyList(),
    // ...
)

private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

fun loadDashboard() = viewModelScope.launch {
    _uiState.update { it.copy(isLoading = true) }
    // ...
    _uiState.update { it.copy(isLoading = false, walkScore = score, ...) }
}
```

#### 언제 어느 쪽?

- 단순 목록·상세 → sealed class
- 대시보드·복합 폼(여러 섹션이 독립적으로 갱신) → data class

### 5.2 Screen에서 수신

```kotlin
@Composable
fun MatchingScreen(viewModel: MatchingViewModel, onCardClick: (Int) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        MatchingUiState.Loading -> LoadingOverlay(isLoading = true)
        is MatchingUiState.Success -> LazyColumn { items(state.matches) { ... } }
        is MatchingUiState.Error -> ErrorMessage(state.message)
    }
}
```

#### 규칙

- 반드시 `collectAsStateWithLifecycle()` 사용 (기본 `collectAsState()` 금지).
- 화면 내부에서 `mutableStateOf`로 폼 입력 임시 상태는 OK, 서버 데이터·로딩·에러는 모두 ViewModel에.
- 화면 컴포저블은 ViewModel을 직접 만들지 않음 — NavGraph에서 주입받음. (단, `viewModel: XxxViewModel? = null` 형태 기본값을 두고 Preview에서 null로 호출).

---

## 6. 로딩 · 에러 · 스낵바

### 6.1 LoadingOverlay (`ui/component/LoadingOverlay.kt`)

전체 화면 위에 반투명 오버레이 + Orange 스피너. 입력 차단됨.

```kotlin
Box {
    Scaffold { ... }
    LoadingOverlay(isLoading = state.isLoading)
}
```

### 6.2 ApiErrorEffect + Snackbar (`ui/component/ErrorUi.kt`)

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
var isRateLimited by remember { mutableStateOf(false) }

ApiErrorEffect(
    result = lastApiError,
    snackbarHostState = snackbarHostState,
    onRateLimited = { isRateLimited = true }
)

Scaffold(snackbarHost = { SiheungSnackbarHost(snackbarHostState) }) { ... }

RateLimitBanner(visible = isRateLimited)
LaunchedEffect(isRateLimited) {
    if (isRateLimited) { delay(4000); isRateLimited = false }
}
```

### 6.3 SiheungSnackbarHost

- 컨테이너: `#1E120A`(진갈색), 텍스트 White 14sp, 액션 텍스트 Orange(`#F7A35B`).
- 위치: 하단 16dp 패딩, radius 12dp.

---

## 7. Navigation

### 7.1 라우트 정의 — `NavGraph.kt`

`sealed class Screen(val route: String)` 안에 모든 라우트를 정의. 인자가 있는 라우트는 `createRoute(...)` 정적 메서드를 함께 둔다.

```kotlin
sealed class Screen(val route: String) {
    object Home          : Screen("home")
    object MatchingDetail : Screen("matching_detail/{requestId}") {
        fun createRoute(requestId: Int) = "matching_detail/$requestId"
    }
    object Chat          : Screen("chat/{matchId}/{applicationId}") {
        fun createRoute(matchId: Int, applicationId: Int) = "chat/$matchId/$applicationId"
    }
    object PetAdd        : Screen("pet_add?petId={petId}") {
        fun editRoute(petId: Int) = "pet_add?petId=$petId"
    }
    object Settings      : Screen("settings") // ?section={section} 옵션
    // ...
}
```

**전체 라우트 목록** (정의 순서):

| 라우트 | 인자 | 화면 |
|---|---|---|
| `auto_splash` | — | AutoSplashScreen (토큰 확인 후 분기) |
| `splash` | — | StartScreen (로그인/회원가입 선택) |
| `login` | — | LoginScreen |
| `signup` | — | SignUpScreen |
| `home` | — | HomeScreen (탭) |
| `notification` | — | NotificationScreen |
| `matching` | — | MatchingScreen (탭) |
| `request_flow?requestId={requestId}` | requestId(Int, 기본 0=신규) | RequestFlowScreen |
| `my_requests` | — | (NavGraph 내부 placeholder "준비 중입니다") |
| `map?volunteerMode={..}&focusLat={..}&focusLng={..}&focusStoreId={..}` | 4개 옵션 | MapScreen (탭) |
| `news` | — | NewsScreen (탭) |
| `place_detail/{placeId}?lat={lat}&lng={lng}` | placeId(Int), lat/lng(String→Double) | PlaceDetailScreen |
| `my` | — | MyScreen (탭) |
| `settings?section={section}` | section: notifications / location / privacy / null | SettingsScreen |
| `pet_list` | — | PetListScreen |
| `pet_add?petId={petId}` | petId(Int, -1=신규) | PetAddScreen |
| `volunteer_apply` | — | VolunteerApplyScreen |
| `volunteer_badge_list` | — | VolunteerBadgeListScreen |
| `volunteer_history` | — | VolunteerHistoryScreen |
| `profile_edit` | — | ProfileEditScreen |
| `matching_detail/{requestId}` | requestId(Int) | MatchingDetailScreen (요청자 시점) |
| `matching_public_detail/{requestId}` | requestId(Int) | MatchingPublicDetailScreen (봉사자 시점) |
| `chat/{matchId}/{applicationId}` | 둘 다 Int | ChatScreen |
| `news_detail/{newsId}` | newsId(String) | NewsDetailScreen |
| `favorite_stores` | — | FavoriteStoresScreen |
| `block_manage` | — | BlockManageScreen |
| `help` | — | HelpScreen |
| `privacy` | — | PrivacyPolicyScreen (WebView) |

### 7.2 이동 헬퍼

```kotlin
// 탭 전환 (백스택에 1개만 유지, 상태 복원)
private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Screen.Home.route) { saveState = true }
    }
}

// 일반 푸시
navController.navigate(Screen.MatchingDetail.createRoute(requestId))

// 뒤로
navController.popBackStack()

// 전체 스택 초기화 (로그인 성공·로그아웃 후)
navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
```

### 7.3 화면 컴포저블 시그니처 규약

```kotlin
@Composable
fun XxxScreen(
    viewModel: XxxViewModel? = null,          // Preview용 nullable
    onBack: () -> Unit = {},                  // 뒤로가기 (서브 화면)
    onNavigate: (String) -> Unit = {},        // 라우트 문자열로 이동 (탭/공통)
    onSpecificClick: (Int) -> Unit = {},      // 도메인 특화 콜백은 별도 함수
)
```

- 메인탭(Home/Matching/Map/News/My)은 `onNavigate(route)`를 받아 NavGraph가 `navigateTab()`으로 해석.
- 서브 화면은 일반 `navigate()`로 해석.
- 라우트 인자(`requestId` 등)는 **컴포저블 인자로 직접 받음** — ViewModel 내부 `init`이나 Factory에서 처리.

### 7.4 BottomNavigationBar

`NavGraph.kt:AppBottomBar` 가 단일 인스턴스. `currentRoute`가 메인탭 5개 중 하나일 때만 `AnimatedVisibility`로 보임. 5개 탭: 홈·매칭·지도·소식·마이.

### 7.5 세션 만료 자동 처리

`AppNavGraph` 가 시작될 때 `app.sessionExpiredChannel` 을 구독. TokenAuthenticator 가 갱신 실패 시 채널에 신호를 보내고, NavGraph 는 즉시 Login 으로 강제 이동(전체 스택 비움).

---

## 8. 로컬 저장소

| 클래스 | 저장 방식 | 용도 |
|---|---|---|
| `TokenManager` | EncryptedSharedPreferences | access·refresh 토큰, 만료시각, 프로필 이미지 URI |
| `MapFilterStore` | DataStore Preferences | 지도 카테고리 필터(`Set<String>` of `CAFE/RESTAURANT/PARK/HOSPITAL/GROOMING`) |
| `LocalNotificationStore` | SharedPreferences + Gson | 포그라운드 FCM 알림 로컬 저장. 서버 ID는 양수, 로컬 ID는 음수로 충돌 방지 |
| `FavoritesCache` | In-Memory `object` (StateFlow) | 즐겨찾기 매장 ID 집합. 앱 시작 시 API에서 로드 후 `init()` |

`LocalNotificationStore`는 `NotificationRepository.getNotifications(page=1)` 호출 시 서버 응답에 머지되어 알림 화면에 함께 표시됨.

---

## 9. WebSocket (채팅) — `ChatWebSocketManager`

- **URL**: `wss://backend-production-f6c0.up.railway.app/api/v1/ws/applications/{applicationId}?token=<accessToken>`
- **토큰 전달**: 쿼리스트링 (`?token=...`). HTTP 헤더 아님.
- **Ping/Pong**: 30초
- **재연결**: 지수 백오프 (1s → 30s 까지)
- **메시지 전송 금지**: WebSocket으로 보내면 서버가 무시. REST POST로만 전송.
- **이벤트**:
  ```kotlin
  sealed class WsEvent {
      object Connected
      data class Message(val payload: JSONObject)
      object Disconnected
      object Unauthorized  // close code 4401 / 4403
  }
  ```

- **Lifecycle**: ChatScreen 진입 시 `connect(applicationId)`, 나갈 때 `disconnect()`.

---

## 10. FCM (푸시 알림)

| 컴포넌트 | 파일 | 역할 |
|---|---|---|
| `FcmTokenManager` | `data/network/FcmTokenManager.kt` | Firebase 토큰 → 백엔드 `/api/v1/devices` 등록 |
| `FcmService` | `data/service/FcmService.kt` | `FirebaseMessagingService` 상속. `onNewToken` / `onMessageReceived` 처리 |

**토큰 등록 시점** (셋 다 동일하게 `registerCurrentDevice()` 호출):

1. 앱 부팅 — `SiheungGagaeApp.onCreate()`
2. 로그인 성공 — `AuthRepository.login()`
3. Firebase 토큰 갱신 — `FcmService.onNewToken()`

**알림 채널**: `siheunggagae_push` ("시흥가개 알림"). 채널은 매번 `ensureNotificationChannel()`로 보장.

**탭 시 동작**: `MainActivity` 로 PendingIntent (`SINGLE_TOP | CLEAR_TOP`).

`AndroidManifest.xml`:
```xml
<service android:name=".data.service.FcmService" android:exported="false">
    <intent-filter><action android:name="com.google.firebase.MESSAGING_EVENT" /></intent-filter>
</service>
```

---

## 11. 위치 (`LocationProvider`)

`data/location/LocationProvider.kt`. Play Services Fused Location 사용.

```kotlin
fun hasPermission(): Boolean                 // FINE 또는 COARSE
suspend fun getLocationOrNull(): Location?   // 마지막 위치 시도 → 실패 시 fresh 좌표
```

**권한**: `AndroidManifest.xml`
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

권한 요청 UI는 `ui/util/LocationPermissionState.kt`. Compose에서 `rememberLocationPermissionState()`로 호출.

---

## 12. 지도 (Kakao Map)

- SDK: `com.kakao.maps.open:android:2.12.8`
- 키 주입: `local.properties:kakao.app.key` → `BuildConfig.KAKAO_APP_KEY` → `manifestPlaceholders["kakaoAppKey"]` → `AndroidManifest.xml`의 `com.kakao.sdk.AppKey` 메타데이터
- 초기화: `KakaoMapSdk.init(this, BuildConfig.KAKAO_APP_KEY)` in `SiheungGagaeApp.onCreate()`
- 래퍼: `MapViewWrapper.kt` (루트에 있는 특이 파일) — Kakao Map 콜백을 Compose-친화적 인터페이스로 노출. 마커 추가/제거·커스텀 비트맵 핀·`reinit()` 지원.
  - **주의**: `reinit()` 호출 후 마커가 사라지므로 다시 추가해야 함.

화면별 지도(홈·요청 상세·장소 상세 등) 미리보기는 민트 그라디언트 `Box` placeholder 사용 (CLAUDE.md 규칙).

---

## 13. 빌드 · 실행

### 13.1 환경

- **minSdk**: 35, **targetSdk/compileSdk**: 36, **Java**: 11
- **Compose**: BOM 기반
- **AGP**: 8.x, **Kotlin Compose Plugin** 적용
- **Application ID / Namespace**: `com.example.siheunggagae`

### 13.2 필수 설정 파일

| 파일 | 위치 | 내용 |
|---|---|---|
| `google-services.json` | 프로젝트 루트 또는 `app/` | Firebase 프로젝트 설정 (체크인됨) |
| `local.properties` | 프로젝트 루트 | `kakao.app.key=YOUR_KAKAO_APP_KEY` (체크인 금지) |

### 13.3 명령어

```bash
./gradlew assembleDebug          # 빌드
./gradlew installDebug           # 디바이스/에뮬레이터 설치
./gradlew connectedAndroidTest   # 계측 테스트
```

### 13.4 핵심 외부 의존성

| 카테고리 | 라이브러리 | 비고 |
|---|---|---|
| 네트워크 | Retrofit2, OkHttp3, Gson | 단일 `AuthApiService` 인터페이스 |
| 로컬 | DataStore Preferences, EncryptedSharedPreferences | |
| 이미지 | Coil 3 (3.1.0, compose+svg+okhttp) | `AsyncImage` 사용 |
| 지도 | Kakao Map SDK 2.12.8 | |
| 푸시 | Firebase Messaging (BOM) | |
| 위치 | Play Services Location | Fused Location |

---

## 14. 패키지 컨벤션

```
com/example/siheunggagae/
├── MainActivity.kt              # 단일 액티비티
├── SiheungGagaeApp.kt           # Application — 싱글톤 보유
├── NavGraph.kt                  # Screen sealed class + NavHost + BottomBar
├── MapViewWrapper.kt            # Kakao Map Compose 래퍼 (특이 파일)
│
├── data/
│   ├── model/        XxxModels.kt  — 도메인별 DTO 한 파일
│   ├── network/      RetrofitClient, AuthInterceptor, TokenAuthenticator,
│   │                 ApiResult, ChatWebSocketManager, FcmTokenManager
│   │   └── api/      AuthApiService.kt  — 모든 엔드포인트 단일 인터페이스
│   ├── repository/   XxxRepository.kt
│   ├── local/        TokenManager, MapFilterStore, LocalNotificationStore, FavoritesCache
│   ├── location/     LocationProvider
│   └── service/      FcmService
│
└── ui/
    ├── screen/       XxxScreen.kt        — 화면당 1파일, private val로 색 hex 정의
    ├── viewmodel/    XxxViewModel.kt     — Factory 내부 클래스 포함
    ├── component/    LoadingOverlay, ErrorUi (ApiErrorEffect + RateLimitBanner), SiheungSnackbarHost
    ├── theme/        Color.kt, Type.kt, Theme.kt
    └── util/         LocationPermissionState, EnumExtensions
```

### 파일 네이밍

- 화면: `XxxScreen.kt` (UpperCamel + `Screen` 접미사)
- 뷰모델: `XxxViewModel.kt` + 내부 `Factory` 클래스
- DTO: `XxxModels.kt` (도메인 단위, 단수형 모델이라도 파일은 `Models` 복수)
- 라우트 상수: `Screen.Xxx` 객체 (단수)

### 색상 사용 규칙

- `ui/theme/Color.kt` 에는 Material3 호환 토큰(`Brown40`, `Pink40` 등)만 정의되어 있지만, **각 화면 파일에서 실제로 쓰는 토큰은 화면 상단에 `private val Brown900X = Color(0xFF614B3A)` 식으로 직접 hex로 박는 게 표준** (X는 화면 약자: H=Home, C=Chat, D=Detail, B=Block/Badge, P=Pet/Place, F=Favorite/Flow 등).
- hex 값은 [CLAUDE.md 컬러 팔레트](../CLAUDE.md#컬러-팔레트)와 동일하게 맞춘다.
- 새 화면 만들 때도 같은 패턴 따를 것 — Theme의 ColorScheme 참조 대신 hex 직접 사용.

---

## 15. 새 화면 추가 체크리스트

1. **DTO**: `data/model/XxxModels.kt` 에 요청·응답 데이터 클래스 추가
2. **API 메서드**: `data/network/api/AuthApiService.kt` 에 `@GET/@POST suspend fun` 추가
3. **Repository**: `data/repository/XxxRepository.kt` — 이미 도메인 Repo가 있으면 메서드 추가, 없으면 신규
4. **ViewModel**: `ui/viewmodel/XxxViewModel.kt` — UiState(sealed 또는 data class), `Factory` 내부 클래스, `viewModelScope.launch` 패턴
5. **Screen**: `ui/screen/XxxScreen.kt`
   - 상단에 `private val ColorX = Color(0xFF...)` 색 정의
   - 시그니처: `viewModel: XxxViewModel? = null, onBack = {}, onNavigate: (String) -> Unit = {}`
   - `@Preview(showBackground = true)` 포함
   - `collectAsStateWithLifecycle()` 사용
6. **Screen sealed class**: `NavGraph.kt` 에 `object Xxx : Screen("xxx")` 추가 (인자 있으면 `createRoute()`)
7. **composable {}**: `NavGraph.kt` 의 NavHost 내부에 ViewModel 주입 + 컴포저블 호출

---

## 16. 주의사항 · TODO

- **`MyRequestsScreen.kt`** 파일은 `ui/screen/` 에 존재하지만 NavGraph 가 같은 파일 안의 placeholder("준비 중입니다", `NavGraph.kt:1011-1059`)를 우선 사용하고 있음. 실제 화면 연결 시 NavGraph 의 `composable(Screen.MyRequests.route)` 블록을 수정해서 `ui.screen.MyRequestsScreen` 으로 교체 필요.
- **Hardcoded base URL**: `RetrofitClient.kt`, `TokenAuthenticator.kt`, `ChatWebSocketManager.kt` 세 곳에 분산. 환경 변경 시 모두 수정.
- **시흥시 동(洞) 좌표**: `HomeViewModel.kt` 에 18개 행정동 GPS가 하드코딩되어 있음.
- **외부 참고 링크**: API 명세서·에러코드는 [CLAUDE.md 외부 참고](../CLAUDE.md#외부-참고) 섹션의 Notion/Swagger 링크.
