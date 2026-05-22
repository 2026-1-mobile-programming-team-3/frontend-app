# 시흥가개 — 디자인 시스템

"시흥가개" Android 앱(Jetpack Compose).

## 문서 구조

| 문서 | 역할 |
|---|---|
| **이 파일 (CLAUDE.md)** | 컬러·Typography 토큰 (단일 진실 소스), 공통 컴포넌트 요약 |
| [`docs/screens.md`](docs/screens.md) | 화면별 레이아웃·문구·동작 |
| [`docs/architecture.md`](docs/architecture.md) | 레이어 구조·코드 패턴·라우트·빌드 설정 |
| [`docs/design.md`](docs/design.md) | Spacing·Radius·Elevation·색 의미·카드·버튼·칩·애니메이션 패턴 |
| [`docs/api.md`](docs/api.md) | 전체 엔드포인트 카탈로그·에러 분류 |
| [`docs/gotcha.md`](docs/gotcha.md) | 함정·주의사항·변경 전 확인 목록 |

---

## 기술 스택

- Kotlin + Jetpack Compose, minSdk 35 / targetSdk·compileSdk 36 / Java 11
- 아이콘: `androidx.compose.material:material-icons-extended` (Material Symbols)
- 폰트: Pretendard Variable (`res/font/pretendardvariable.ttf`)
- 지도: Kakao Map SDK 2.12.8 (키: `local.properties:kakao.app.key`)
- 네트워크: Retrofit2 + OkHttp3 + Gson (`LOWER_CASE_WITH_UNDERSCORES`)
- 이미지: Coil 3 (compose + svg + okhttp)
- 패키지: `com.example.siheunggagae`

## 코드 작성 규칙

- 모든 파일은 실행 가능한 완성 형태로 작성
- `@Preview(showBackground = true)` 항상 포함
- data class + 더미 데이터 포함해 바로 실행되게
- 지도는 민트 그라디언트 Box로 placeholder 처리
- 화면 간 이동: `(onNavigate: (String) -> Unit)` 파라미터
- 새 화면 추가 절차: [`docs/architecture.md` §15](docs/architecture.md)
- 패키지·파일 컨벤션: [`docs/architecture.md` §14](docs/architecture.md)

## 외부 참고

- API 명세서(에러코드 등): https://www.notion.so/69e13e2ffd0e416285eb9358480c3673?v=f626631ddc034730aa6e48b53c9bcdfa
- 노션 체크리스트: https://www.notion.so/362f3270da67808eaaa9db68e207ed24
- Swagger: https://backend-production-f6c0.up.railway.app/docs

---

## 컬러 팔레트

```kotlin
val TextBlack        = Color(0xFF1E120A) // 본문 기본 검정
val Brown700         = Color(0xFF8A6E58) // 서브텍스트(커피색)
val Brown900         = Color(0xFF614B3A) // 버튼·강조(진한 커피색)
val Background       = Color(0xFFFEFEFE) // 배경
val OrangeSand       = Color(0xFFFFEDD4) // 강아지 아이콘 bg
val Orange500        = Color(0xFFF7A35B) // 황토색 Accent
val OrangeRed        = Color(0xFFEE6A46) // 산책지수 3단계
val MintLight        = Color(0xFFD0FEE1) // 지도 bg
val Green600         = Color(0xFF00A63E) // 산책지수 1단계, 봉사자 신청
val PinkSurface      = Color(0xFFFEE7EC) // 카드 bg(연분홍)
val Pink500          = Color(0xFFF04268) // 강조·태그(진분홍)
val Blue500          = Color(0xFF388AF5) // 산책지수 2단계
val Gray300          = Color(0xFFE8E8E8) // 구분선·미선택 하트
val StarYellow       = Color(0xFFFDC700) // 별점
val BorderBeige      = Color(0xFFE8D3C2) // 흰버튼 테두리, 날짜/시간 채우기
val TagGray          = Color(0xFFF2F2F2) // 매칭 카드 태그 bg
val FABBrown         = Color(0xFF9A7B5E) // 매칭 + FAB 전용
val AddGray          = Color(0xFFF4F4F4) // 반려동물 추가 버튼 bg
val PlaceholderColor = Color(0xFFC1AEA0) // 입력창 placeholder
```

---

## Typography

```kotlin
val PretendardFamily = FontFamily(
    Font(R.font.pretendardvariable, FontWeight.Thin),
    Font(R.font.pretendardvariable, FontWeight.Light),
    Font(R.font.pretendardvariable, FontWeight.Normal),
    Font(R.font.pretendardvariable, FontWeight.Medium),
    Font(R.font.pretendardvariable, FontWeight.SemiBold),
    Font(R.font.pretendardvariable, FontWeight.Bold),
    Font(R.font.pretendardvariable, FontWeight.ExtraBold),
)
```

`AppTypography`(Type.kt) 슬롯 매핑:

| 슬롯 | size | weight | lineHeight | 사용처 |
|---|---|---|---|---|
| displayMedium  | 30 | ExtraBold | 32 | 산책지수 숫자 |
| displaySmall   | 26 | ExtraBold | 32 | 메인탭 TopBar 제목(홈·매칭·소식·마이) |
| headlineLarge  | 24 | ExtraBold | 33 | 도움요청 질문 |
| headlineMedium | 20 | ExtraBold | 32 | 서브페이지 TopBar 제목(알림·설정·봉사 등) |
| headlineSmall  | 20 | Bold      | 24 | 섹션 헤더("주변 매장 24곳" 등) |
| titleLarge     | 18 | Bold      | 27 | 카드 제목, 알림 제목 |
| titleMedium    | 16 | Bold      | 24 | 장소명, 봉사자명 |
| titleSmall     | 16 | SemiBold  | 24 | 뉴스 카드 제목 |
| bodyLarge      | 14 | Bold      | 20 | 뉴스 리스트 제목 |
| bodyMedium     | 14 | Medium    | 20 | 본문, 섹션 라벨 |
| bodySmall      | 14 | Normal    | 20 | 날짜, 서브텍스트 |
| labelLarge     | 12 | Bold      | 16 | 태그 chip |
| labelMedium    | 12 | SemiBold  | 16 | 캡션 강조 |
| labelSmall     | 12 | Medium    | 16 | 캡션(거리·날짜·출처) |

예외:
- 스플래시 로고 "시흥가개": 36sp ExtraBold Brown900
- BottomBar 선택탭 텍스트: 13sp SemiBold lineHeight=18sp

---

## 공통 컴포넌트

상세 패턴·코드 예시는 [`docs/design.md`](docs/design.md) 참고.

### 카드
`RoundedCornerShape(16.dp)`, elevation=2dp, bg=White.

### 버튼 Primary
`RoundedCornerShape(12.dp)`, bg=Brown900, text=White 16sp SemiBold, height=56dp, hPad=24dp.

### 버튼 Outline
`RoundedCornerShape(12.dp)`, border=1dp Brown900, bg=White, text=Brown900.

### Chip 필터
- 공통: 14sp Medium, padding h=16dp v=8dp, `RoundedCornerShape(50.dp)`
- 선택: bg=#1A1A1A, text=White
- 미선택: bg=White, border=1dp BorderBeige, text=Brown700

### 태그 (상태)
- 공통: 12sp Medium, padding h=10dp v=4dp, `RoundedCornerShape(50.dp)`
- 모집중: bg=#FEE7EC / text=#E84B6A
- 검토중: bg=#FEF3C7 / text=#CA8A04
- 진행중: bg=#F0FDF4 / text=#16A34A
- 완료:   bg=#F3F4F6 / text=#6B7280

### BottomNavigationBar (탭 5개: 홈·매칭·지도·소식·마이)
- 바: White bg, `RoundedCornerShape(50.dp)`, elevation=8dp, 좌우 margin=16dp, 바닥 margin=16dp + navigationBarsPadding
- 선택탭: 검정(#1A1A1A) pill + 아이콘(24dp) + 텍스트(13sp SemiBold) White, pill padding h=16dp v=10dp
- 미선택탭: 아이콘(24dp)만, Brown400(#C4A882), 텍스트 없음

### TopBar (서브 화면)
- 뒤로가기: 흰 카드(40×40dp, radius=12dp, elevation=2dp) + KeyboardArrowLeft 22dp
- 타이틀: 가운데 정렬 — 20sp ExtraBold (알림·설정·봉사 등) 또는 18sp SemiBold (내 반려동물·프로필 편집 등)

### Switch
- 켜짐: Brown900 bg, White thumb
- 꺼짐: Gray300 bg, White thumb

---

## Navigation Routes

전체 라우트 정의·인자·화면 매핑은 [`docs/architecture.md` §7.1](docs/architecture.md) 참고.

```kotlin
object AutoSplash           : Screen("auto_splash")          // 토큰 확인 후 Home 또는 Splash 분기
object Splash               : Screen("splash")               // 로그인/회원가입 선택
object Login                : Screen("login")
object SignUp               : Screen("signup")
object Home                 : Screen("home")
object Matching             : Screen("matching")
object Map                  : Screen("map")                  // ?volunteerMode / ?focusLat / ?focusLng / ?focusStoreId
object News                 : Screen("news")
object My                   : Screen("my")
object Notification         : Screen("notification")
object RequestFlow          : Screen("request_flow")         // ?requestId (0=신규, >0=수정)
object MyRequests           : Screen("my_requests")          // ⚠️ NavGraph placeholder "준비 중"
object PlaceDetail          : Screen("place_detail/{placeId}")
object MatchingDetail       : Screen("matching_detail/{requestId}")
object MatchingPublicDetail : Screen("matching_public_detail/{requestId}")
object Chat                 : Screen("chat/{matchId}/{applicationId}")
object Settings             : Screen("settings")             // ?section=notifications|location|privacy
object PetList              : Screen("pet_list")
object PetAdd               : Screen("pet_add")              // ?petId (-1=신규)
object VolunteerApply       : Screen("volunteer_apply")
object VolunteerBadgeList   : Screen("volunteer_badge_list")
object VolunteerHistory     : Screen("volunteer_history")
object ProfileEdit          : Screen("profile_edit")
object NewsDetail           : Screen("news_detail/{newsId}") // newsId는 String
object FavoriteStores       : Screen("favorite_stores")
object BlockManage          : Screen("block_manage")
object Help                 : Screen("help")
object Privacy              : Screen("privacy")              // WebView
```
