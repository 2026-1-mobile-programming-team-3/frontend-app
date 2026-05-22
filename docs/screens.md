# 시흥가개 — 화면별 스펙

CLAUDE.md의 전역 규칙(컬러·Typography·공통 컴포넌트), 코드 패턴은 [`architecture.md`](architecture.md) 참고. 여기는 화면별 레이아웃·문구·동작만 정의한다.

화면 파일은 모두 `app/src/main/java/com/example/siheunggagae/ui/screen/`. 각 화면 파일 상단에 `private val ColorX = Color(0xFF...)` 형태로 색을 직접 정의하고 있으며 값은 CLAUDE.md 토큰과 동일.

---

## 진입 플로우

### [AutoSplashScreen] 자동 스플래시

- TopBar 없음, fullScreen White bg
- 중앙 로고: `assets/logo.svg` AsyncImage, 200dp
- LaunchedEffect: 최소 1500ms 표시 + 토큰 유효성 검사 (AuthRepository.refresh)
  - 토큰 유효 → `onHome()` (Home으로, 스택 전체 삭제)
  - 무효/없음 → `onStartScreen()` (Splash로)

### [StartScreen] 시작 화면 (route=`splash`)

- 배경: White, 중앙 Column
- 로고: `assets/logo.svg` AsyncImage 180dp
- Spacer 48dp
- "로그인하기" 버튼: fullWidth, vPad=14dp, bg=Brown700(#8A6E58), text=White 18sp Bold, radius=16dp
- Spacer 12dp
- "회원가입하기" 버튼: fullWidth, vPad=14dp, bg=White, border=1dp BorderBeige, text=TextBlack 18sp Bold, radius=16dp
- 하단 중앙: "v3.0.0" 12sp Gray80, bottomPad=32dp

### [LoginScreen] 로그인

- TopBar: 뒤로가기 카드(40×40dp) + "로그인" 20sp ExtraBold, 우측 비움
- 본문 hPad=24dp
  - 제목: "반가워요!\n로그인을 진행해 주세요." 30sp Bold
  - 이메일 TextField: border=1dp BorderBeige, radius=16dp, padding 16×14dp, cursor=Orange500, placeholder=PlaceholderColor 16sp
  - 비밀번호 TextField: 동일 스펙 + Visibility 토글 아이콘
  - 에러 메시지: 13sp Medium Pink500 (있을 때만)
- 하단: "로그인" 버튼 Orange500 fullWidth height=56dp radius=16dp (Loading 시 60% alpha + 스피너)
- 하단 링크 Row: "계정이 없으신가요?  회원가입" — 회원가입은 Orange500
- ViewModel: AuthViewModel (Idle/Loading/Success/Error/RateLimited)

### [SignUpScreen] 회원가입

- TopBar: 뒤로가기 + "회원가입" 20sp ExtraBold
- 본문 hPad=24dp (스크롤 가능, 섹션 간 20dp gap)
  - 제목: "시흥가개에\n오신 것을 환영해요!" 30sp Bold
  - 이메일 *: RegTextField (LoginScreen과 동일 스펙)
  - 닉네임 *: RegTextField
  - 비밀번호 *: RegPasswordTextField (Visibility 토글)
    - 검증 힌트 Row 4개: 8자 이상 / 영문 / 숫자 / 특수문자 — 체크 원형(Green600 충족 / Gray300 미충족)
  - 비밀번호 확인 *: 동일, 불일치 시 에러 텍스트
  - 휴대폰 (선택)
  - 시 (선택) / 동 (선택)
  - 약관 동의 Row: 22dp 원형 체크박스(체크=Orange500 + White 체크 아이콘) + "이용약관 및 개인정보처리방침에 동의합니다" 14sp Medium
- 하단: "회원가입하기" Orange500 fullWidth height=56dp radius=16dp + "이미 계정이 있으신가요?  로그인" (로그인=Orange500)
- 이메일 충돌 ModalBottomSheet: "이미 가입된 이메일이에요" 20sp ExtraBold + 본문 + "로그인하러 가기"(Orange500) / "취소"(Outline)
- ViewModel: AuthViewModel

---

## 메인 탭

### [HomeScreen] 홈

- TopBar 좌: Column "안녕하세요, [닉네임]님"(12sp Brown700) + "시흥가개"(26sp ExtraBold, "시흥"=Black "가개"=StarYellow)
- TopBar 우: Row
  - 위치 pill: LocationOn(Orange500) + 동 이름, border=BrownBorder, radius=50dp
  - Notifications 아이콘 + 읽지 않음 뱃지(0보다 클 때)
- **WalkIndexSection**
  - "오늘 산책지수"(14sp Medium Brown700) + "[점수]점으로 [라벨]" (점수만 색상)
    - 80↑=Green500(#22C55E) / 50–79=Blue400 / 0–49=Orange600
  - 날씨: "[맑음] · [18]° · 미세먼지 [좋음]" 12sp Gray
  - D-day 배너(검토 대기 매칭 있을 때만): PinkSurface, radius=50dp, "[D-N] 신청 N건 검토하기 >"
- **NearbyStoresSection**
  - 헤더: "주변 매장 N곳" headlineSmall + "🗺 지도 보기" Orange500
  - 미니 지도 Card: height=180dp, radius=16dp, 민트-하늘 그라디언트 + 핀 4개, 좌하단 동 pill
  - 카테고리 LazyRow: 전체/카페/공원/병원/미용/식당 (선택=#1A1A1A bg White text / 미선택=White border=BrownBorder)
  - 장소 Row: 번호 원형(32dp, 1번=Pink500 / 2번↑=Brown900) + 장소명 16sp SemiBold + "[업종] · [거리] · ⭐[별점]" + Favorite 아이콘
- **PetNewsSection**
  - 헤더: "반려동물 소식" + "📰 전체 보기" Orange500
  - 메인 뉴스 카드: 그라디언트(Brown→lighter) 180×160dp + 카테고리 chip + 제목 20sp White + "[출처] · [날짜]" White 80%
  - 2×2 Grid + 리스트 아이템: [태그] + 제목 14sp + 날짜 12sp + Divider
- **봉사 배너**: PinkSurface, 16dp padding, Handshake(Orange500) + "이동 지원 봉사자가 되어 보세요" + "신청 →"(Brown900 → VolunteerApply)
- ViewModel: HomeViewModel (`data class HomeUiState`)

### [MatchingScreen] 매칭

- TopBar: "매칭" 26sp ExtraBold + Assignment 아이콘 카드버튼(40×40dp, → MyRequests)
- 요약 카드 Row(gap=12dp, equal weight)
  - 내 요청(PinkSurface): Assignment(Pink500) + "내 요청" + "N건 검토 중" 16sp Bold
  - 봉사 활동(#F0FDF4): Favorite(Green500) + "봉사 활동" + "N건 진행 중"
- 탭 필터: 전체 / 모집중 / 검토중 / 진행중 / 완료 (선택=Bold Black, 미선택=Brown400)
- 섹션 헤더: "이동 지원 요청" + "🗺 지도 보기" Orange500
- 요청 카드(radius=16dp, elevation=2dp): 우상단 상태 chip + 날짜 / 제목 16sp SemiBold (2줄) / "[지역] · [거리] · [날짜·시간]" 13sp / "신청 N건"(있을 때, Orange500)
- 카드 롱프레스 모달: 북마크 / 공유 / 신고 / 숨김 / 사용자 차단 / 숨김+차단
- FAB: 56×56dp, radius=16dp, bg=FABBrown(#9A7B5E), Add 아이콘, 우하단 margin=16dp (→ RequestFlow)
- ViewModel: MatchingViewModel (`sealed MatchingUiState`)

### [MapScreen] 지도

- 전체화면 KakaoMap (`MapViewWrapper`로 래핑). 권한 미허용 시 민트-White 그라디언트 placeholder + 핀 3개 + 파란 dot(내 위치)
- 상단 검색 Card: radius=50dp, elevation=4dp, height=48dp, Search 아이콘 + "매장 · 병원 · 공원 검색" placeholder (탭 시 검색 오버레이)
- 카테고리 LazyRow (상단 검색 바로 아래): 전체 / 카페 / 공원 / 병원 / 미용 / 식당 (선택=Black bg White text elev=2dp / 미선택=White border)
- 좌상단 VolunteerToggleChip (사용자 role=VOLUNTEER일 때): "봉사요청 보기" — ON=Blue bg / OFF=White, radius=50dp
- 우측 FAB 컬럼(40×40dp, radius=12dp, elev=4dp): MyLocation / Layers(필터 모달) / Refresh
- 하단 BottomSheetScaffold: peek=280dp, radius topStart/End=20dp, drag handle
  - 헤더: "주변 매장 N개" + "↕ 거리순"
  - 장소 LazyColumn (일반 모드) 또는 봉사 요청 LazyColumn (봉사 모드)
- **StoreDetailSheet** (마커 탭 시): 민트→연두 그라디언트 헤더 + 장소명 + "[업종] · [거리] · ⭐[별점]" + Favorite + "상세 정보 보기"(Brown900 → PlaceDetail)
- **MapSearchOverlay**: 뒤로가기 + 검색 입력 + 결과 리스트
- 마커: 줌 14↑ 개별 마커(카테고리 이모지 + 매장명) / 줌 11–13 그리드 클러스터
- ViewModel: MapViewModel
- 라우트 인자: `volunteerMode`, `focusLat`, `focusLng`, `focusStoreId` (모두 옵션)

### [NewsScreen] 소식

- TopBar: "소식" 26sp ExtraBold + BookmarkBorder + Notifications(읽지않음 뱃지)
- NewsSearchBar: radius=50dp height=48dp, Search 아이콘 + placeholder, elev=1dp
- 카테고리 LazyRow (텍스트만): 전체 / 정책 / 행사 / 봉사 / 지원 (선택=Bold Black, 미선택=Brown700)
- **FeaturedNewsCard**: 180×height, radius=24dp, Brown900→lighter 그라디언트 + 카테고리 chip + 제목 20sp White + "[출처] · [날짜]" White 80%
- **NewsGridRow**: 2열, 100×height 카테고리 색 그라디언트 + 카테고리 12sp + 제목 14sp Bold (2줄) + 날짜 12sp
- **NewsListRow**: White Card, "[카테고리]" 16sp Bold 40dp width + 제목 16sp Normal (1줄) + 날짜 12sp Brown700 + Divider
- LazyColumn bottomPadding=96dp

### [MyScreen] 마이

- TopBar: "마이" 26sp ExtraBold + Settings 카드버튼(36×36dp)
- 프로필 Card(PinkSurface): 원형 이미지(56dp, Orange500 bg) + 닉네임 18sp Bold + LocationOn+동명 14sp + "편집" Outline (→ ProfileEdit)
- 내 반려동물 섹션: "내 반려동물" 라벨 / Pets 아이콘(40dp, 강아지=OrangeSand+Orange500 / 고양이=PinkSurface+Pink500) + 이름 16sp Bold + "[종류] · [나이] · [성별]" 12sp + "전체 보기" Outline (→ PetList)
- 활동 통계 Row 3개(equal weight): 숫자 30sp Bold (내요청=Pink500 / 봉사=Green500 / 즐겨찾기=Orange500) + 라벨 12sp
- 봉사 뱃지 Card: "봉사 등급" 라벨 + 뱃지 4개 Row(새싹/꽃/열매/나무, 등급별 색)
- 내 기록 섹션: "내 매장 요청" (→ MyStoreRequests) / "봉사 활동 이력" (→ VolunteerHistory) / "즐겨찾기 매장" (→ FavoriteStores) — 아이콘+텍스트+화살표
- 설정 섹션: 알림 설정 / 지역 설정 / 개인정보 및 보안 / 앱 정보(v3.0.0) / "봉사자 자격 신청"(Green500, → VolunteerApply)
- 하단: "로그아웃" Outline 버튼(Brown700 border)
- ViewModel: MyViewModel
- onResume마다 `fetchData()` 호출 (다른 화면에서 돌아올 때 새로고침)

---

## 서브 — 알림 / 요청

### [NotificationScreen] 알림

- TopBar: 뒤로가기 + "알림" 20sp ExtraBold + "모두 읽음"(우, 14sp SemiBold Brown700)
- 탭 필터: 전체 / 매칭 / 소식 / 시스템 (선택=Black bg White / 미선택=White border BrownBorder)
- 아이템 Row:
  - 안 읽음: bg=PinkSurface, radius=20dp, 전체 padding
  - 읽음: bg=White + 아래 HorizontalDivider
  - 좌 원형(44dp, White border) PriorityHigh: MATCH=Pink500 / VOLUNTEER·REVIEW·NEWS·POLICY=Orange500 / SYSTEM=Green500
  - 제목 16sp Bold + 본문 14sp (2줄) + 우상단 시간(12sp, "방금 전"=Pink500 그 외=Brown400)
- LazyColumn: 무한 스크롤 (`loadNextPage`)
- 시간 포맷: `formatRelativeTime()` — ISO timestamp → "N분 전" 등
- 알림 클릭 시 `item.link` deeplink 파서 (`handleNotificationDeeplink`): `siheunggagae://store-request/{id}` → StoreRequestDetail
- ViewModel: NotificationViewModel — `selectTab`, `markRead`, `markAllRead`, `loadNextPage`, `refresh`

### [RequestFlowScreen] 도움 요청하기 (3단계, 신규+수정 공용)

- 라우트 인자 `requestId`(0=신규, >0=수정)
- TopBar: 뒤로가기 + "도움 요청하기" 20sp ExtraBold + "S/3" 14sp SemiBold (우)
- 스텝 인디케이터: 3구간 선(두께 3dp), 완료/현재=Orange500, 미완료=Brown900 30%
  - 라벨: "반려동물" / "일정" / "요청 내용" (현재 단계=Bold Orange500, 그 외=Normal Brown700)
- 질문 텍스트: 24sp ExtraBold (단계별로 다름), 서브 14sp

#### Step 1 — 반려동물 선택

- 질문: "어떤 반려동물과\n함께 이동하나요?"
- Grid 2열(gap=12dp) PetCard
  - 아이콘 Box(100×100dp): 강아지=#FEF3E2+Orange500 / 고양이=#FFE4E6+Pink500
  - 선택됨: border=2dp Orange500 + 우상단 체크 뱃지
  - 미선택: border=1dp BrownBorder
- AddPetCard: 점선 border GrayText.copy(0.4f), Gray bg, "반려동물 추가" (→ PetAdd)
- 에러: RequestUiState.Error일 때 메시지 표시
- 하단 버튼: "→ 일정 선택"

#### Step 2 — 일정 선택

- 질문: "언제 도움이\n필요한가요?"
- 달력: "< 2026년 10월 >" 16sp 좌우 화살표, 요일 헤더(일=Pink500, 토=Blue400, 평일=Brown900)
  - 선택된 날: 원형 #FFF3E0 bg + Orange500 Bold
- 희망 시간 TextField: Schedule 아이콘(Orange500), border=BrownBorder, placeholder "예: 14:30"
- 빠른 선택 chip Grid 4열 (12개, 09:00~20:00): 선택=#FFF3E0 bg Orange500 text / 미선택=White Brown700
- 하단: "→ 요청 내용 작성"

#### Step 3 — 요청 내용

- 질문: "요청 내용이\n무엇인가요?"
- 라벨 14sp SemiBold, 필드 border=1dp BrownBorder radius=12dp padding=16dp
- 제목 * TextField: placeholder "예: 정왕동 실외견 이동 부탁드립니다."
- 목적지 * TextField: LocationOn(Orange500) + placeholder "예: 정왕 동물병원"
- 메모 * 멀티라인 TextField: minHeight=120dp + placeholder + 카운터 "N/500" 우하단
- 하단: "수정 완료하기" 또는 "요청 등록하기" (Brown700, 비활성=Gray + 제출 중 스피너)
- SnackbarHost: `SiheungSnackbarHost`로 성공/에러
- ViewModel: RequestViewModel (`viewModelStoreOwner = backStackEntry`)

### [MyRequestsScreen] 내 봉사 요청 목록 ⚠️

- 화면 파일은 `ui/screen/MyRequestsScreen.kt`에 있으나 **NavGraph에서 사용 중인 것은 같은 파일 안의 placeholder** (`NavGraph.kt:1011-1059`, "준비 중입니다" 텍스트만 표시)
- 실제 화면 연결 시 NavGraph의 `composable(Screen.MyRequests.route)` 블록을 수정 필요
- 파일에 정의된 의도된 스펙(추후 연결될 때):
  - TopBar: 뒤로가기 + "내 봉사 요청" + "+ 새 요청" 버튼(Pink500, radius=50dp)
  - 필터 Chip: 전체 / 매칭 전 / 매칭됨 / 종료됨
  - 카드(radius=16dp elev=2dp): 우상단 상태 chip + 날짜 / 제목 16sp SemiBold (1줄 ellipsis) / "[지역] · [날짜·시간]" 13sp Brown700
  - ViewModel: MyRequestsViewModel

---

## 서브 — 매칭 / 채팅

### [MatchingDetailScreen] 내 봉사 상세 (요청자 시점)

- TopBar: 뒤로가기 + "내 봉사 상세" 20sp ExtraBold + 편집/삭제 아이콘 (Pink500/TextBlack)
- **상태 배너**: fullWidth bg=PinkSurface radius=50dp, 상태 텍스트 14sp Medium
- **요청 정보 Card**: 제목 20sp Bold (2줄) / 정보 Row 5개 (Divider 구분, 아이콘 Box 컬러: CalendarMonth=#FFE4E6 / Schedule=#FFF3E0 / LocationOn=#F0FDF4 / Pets=#FEF3E2 + chip BorderBeige / ChatBubble=#F4F4F4)
- **지원자 현황 섹션**: "지원자 현황 (N명)" — 비어있으면 빈 상태
  - 봉사자 카드: 아바타(40dp MintLight bg, Green600 이니셜) + 이름 14sp Bold + 메모 12sp (2줄, 없으면 "메모 없음" Gray) + "채팅하기"(Pink500 radius=50dp → Chat)
- ViewModel: MatchDetailViewModel (Loading/Error/Success/DeleteSuccess)

### [MatchingPublicDetailScreen] 이동 지원 요청 (봉사자 시점)

- TopBar: 뒤로가기 + "이동 지원 요청" 20sp ExtraBold + BookmarkBorder + Share
- 상태 배너 / 요청 정보 Card: MatchingDetailScreen과 동일
- **경로 Row**: LocationOn + "출발지 인근" → "[목적지]" pill (White bg radius=50dp)
- **경로 지도 Card**: height=192dp, 민트→연두 그라디언트, 핑크/오렌지 핀, 좌하단 "지도에서 보기"(White bg)
- **요청자 정보 Card**: 아바타(48dp Mint) + 닉네임 16sp Bold + "⭐ 4.9 · [등급]" 12sp + "🗨 채팅" Outline Brown700
- **하단 바 (분기)**
  - isMyRequest: "지원 현황 및 목록 보기"(Brown900 radius=50dp fullWidth)
  - !isApplied: 채팅 원형버튼(44dp White border) + "봉사 신청하기"(Pink500 radius=50dp fullWidth)
  - isApplied: "요청자와 채팅하기"(Pink500 radius=50dp fullWidth, ChatBubble + 텍스트)
- **신청 다이얼로그**: OutlinedTextField로 신청 메시지 + 확인/취소
- ViewModel: MatchDetailViewModel

### [ChatScreen] 채팅

- 라우트 인자 `matchId`, `applicationId`
- TopBar: 뒤로가기 + 아바타(40dp MintLight, Green500 이니셜) + 닉네임 16sp Bold + 서브 "⭐4.9 · [등급] · [거리]" 13sp + 우측 "수락"(Pink500 radius=50dp, 검토 중일 때만)
- **요청 미리보기 Card** (상단, PinkSurface, radius=12dp): Handshake(Orange500) + 제목 14sp Bold (말줄임) + 상태 태그 + "[날짜] · [목적지]"
- 날짜 구분자: "오늘" 12sp Gray, Divider 양옆
- 메시지 LazyColumn (자동 스크롤 to bottom)
  - 상대(좌): MintLight 아바타(32dp), 버블 White border=1dp Gray300, radius topStart=4dp/나머지 16dp, 시간 12sp
  - 나(우): Brown900 bg White 텍스트, radius topEnd=4dp/나머지 16dp, 시간 12sp
  - padding=12dp, 텍스트 15sp, maxWidth=75%
- 입력바: TextField radius=50dp border=1dp Gray300 placeholder "메시지를 입력하세요..." + Send 원형 버튼(Brown900, 40×40dp)
- ViewModel: ChatViewModel — `initChatRoom()`, `sendTextMessage()`, `acceptVolunteer()`
- WebSocket: `ChatWebSocketManager` (applicationId 단위 연결)

---

## 서브 — 마이 / 설정

### [SettingsScreen] 설정

- 라우트 인자 `section`: `null` / `notifications` / `location` / `privacy` — 진입 시 해당 ModalBottomSheet 자동 열림
- TopBar: 뒤로가기 카드(40×40dp) + "설정" 20sp ExtraBold
- 본문 (스크롤):
  - **계정** 섹션 Card(radius=16dp elev=1dp): 프로필 편집(→ ProfileEdit) / 반려동물 정보(→ PetList) / 봉사 이력(→ VolunteerHistory)
    - 각 항목: 40dp OrangeSand 아이콘 박스 + 라벨 + 화살표 + Divider(#F3F4F6)
  - **알림** 섹션: 매칭 알림 / 공지 알림 / 리뷰 알림 — Switch (켜짐=Brown900 / 꺼짐=Gray300, thumb=White)
  - **앱** 섹션: 위치 설정(우측에 현재 동 Orange500 또는 "미설정") / 버전(우측 versionName Orange500, 클릭 불가)
  - **기타** 섹션: 도움말(→ Help) / 비밀번호 변경(시트) / 차단 관리(→ BlockManage) / 회원 탈퇴(PersonOff Pink500, 텍스트 Pink500, 시트)
- **위치 설정 시트**: "활동 지역 설정" + 부제 + TextField(에러 supportingText) + "빠른 선택" + 동 chip Grid 4열(선택=Brown900 bg White, 미선택=White border BrownBorder) + "저장"(Brown900)
- **비밀번호 변경 시트**:
  - 성공 상태: Green bg + 체크 + "비밀번호가 변경되었어요."
  - 폼: PasswordField 3개(현재/새/확인) + 에러 supportingText + "변경하기"
- **회원 탈퇴 시트**: "회원 탈퇴" Pink500 / PinkSurface 경고 박스(영구 삭제 안내) / PasswordField "비밀번호 확인" / TextField "탈퇴 사유 (선택)" 100dp 4줄 / "탈퇴하기" Pink500
- ViewModel: NotificationSettingsViewModel + LocationSettingsViewModel + AccountSettingsViewModel (3개 동시 주입)

### [PetListScreen] 내 반려동물 목록

- TopBar: 뒤로가기 카드(40×40dp shadow=2dp) + "내 반려동물" 18sp SemiBold
- 서브헤더: "내 반려동물 N마리" 14sp SemiBold Brown700
- 상태 분기: Loading(스피너 Orange500) / Error(메시지 + retry) / Success empty(센터 텍스트) / Success list
- Card(radius=16dp elev=2dp):
  - Row(padding=16dp): 아이콘 Box(56×56dp radius=16dp, 강아지=OrangeSand+Orange500 / 고양이=PinkSurface+Pink500) + 이름 16sp Bold + "[종류] · [품종] · [N]살 · [N]kg" 13sp Brown700 + 우측 MoreVert(DropdownMenu: 수정/삭제)
  - 아이템 사이 HorizontalDivider Gray300
- FAB: 56×56dp Brown900 radius=16dp Add 아이콘 (→ PetAdd)
- AlertDialog 삭제 확인: "반려동물 삭제" / "[이름]을(를) 삭제할까요?" / 삭제(Pink500) / 취소
- ViewModel: PetListViewModel (onResume마다 `fetchPets()`)

### [PetAddScreen] 반려동물 추가/수정

- 라우트 인자 `petId`(-1=신규)
- TopBar: 뒤로가기 + "반려동물 추가" 또는 "반려동물 수정" 18sp SemiBold
- **미리보기 Card** (bg=OrangeSand): 60×60dp White Box + Pets(Orange500) 또는 사진 + 우하단 카메라 뱃지(20×20dp Brown900 원형) + "미리보기" 12sp + 이름 + "[종류] · [N]살 · [성별]"(실시간)
- **기본 정보 Card** (White radius=16dp):
  - 이름 TextField (우측 정렬)
  - 종류 chip Row (강아지/고양이/기타, 선택=Brown900 / 미선택=White border)
  - 품종 (선택) TextField (우측 정렬)
- **상세 정보 Card**:
  - 나이 컨트롤: [−] 버튼 + 숫자 + [+] 버튼 + 개월/살 chip
  - 성별 chip: 암컷/수컷
  - 중성화 Switch (켜짐=Brown900)
- **특징 및 주의사항**: multiline TextField minHeight=100dp + 카운터 "N/300"
- 하단: "저장하기" Brown900 fullWidth height=56dp (비활성=40% alpha + 저장 중 스피너)
- ViewModel: PetAddViewModel (isEditMode 자동 판단)

### [MyStoreRequestsScreen] 내 매장 요청

- 라우트: `my_store_requests` (인자 없음). 마이페이지 "내 기록" 섹션 "내 매장 요청" 항목에서 진입.
- TopBar: 뒤로가기 카드(40×40dp shadow=2dp) + "내 매장 요청" 18sp SemiBold
- 서브헤더: "전체 N건" 14sp SemiBold Brown700
- 필터 칩: 전체 / 대기(PENDING) / 승인(APPROVED) / 반려(REJECTED) — 14sp Medium, h=16dp v=8dp
- 카드 row (radius=16dp elev=1dp): 카테고리 아이콘 + "추가"/"수정" pill(OrangeSand) + 상태 태그(검토중/승인/반려) + 매장명(15sp Bold) + "[카테고리] · [date] 신청|처리" 12sp Brown700 + ic_chevron_right
- FAB: 56dp FABBrown(#9A7B5E) + ic_plus → StoreRequestForm(ADD)
- 빈 상태: 80dp OrangeSand 원형 + ic_store 40dp + "아직 요청이 없어요" + "+ 매장 추가 요청" CTA
- 무한 스크롤 (page 1부터 size=20)
- ViewModel: MyStoreRequestsViewModel — refresh, loadMore, setFilter

### [StoreRequestFormScreen] 매장 추가/수정 요청

- 라우트: `store_request_form?type={ADD/UPDATE}&storeId={?}&requestId={?}` — type=ADD/UPDATE 필수. storeId=UPDATE 대상, requestId=재제출 prefill 소스.
- TopBar: 뒤로가기 + 제목 (ADD="매장 추가 요청" / UPDATE="매장 정보 수정")
- 본문 (단일 스크롤): 헤더 22sp ExtraBold + 안내 → 매장명 TextField → 카테고리 2×2 grid (ic_coffee/ic_utensils/ic_trees/ic_hotel) → 위치 큰 미리보기(140dp, ic_map_pin, 탭 시 MapPinPicker 진입) → 동반 가능 Switch → (PET_HOTEL일 때만) 가격 플랜 행 리스트 → 전화/영업시간 → 매장 사진 가로스크롤 (0/5) → 증빙 자료 (0/10, 필수) → 관리자 메모(0/1000)
- 사진/증빙: GetMultipleContents 런처. 사진 image/*, 증빙 */*. 실제 업로드 없이 placeholder URL stub (`https://placeholder.local/uploads/<uuid>`).
- 검증(엄격): proof_urls 1개 이상 필수, PET_HOTEL은 plans 1개 이상 + planName 중복/공백/0가격 차단.
- 하단 CTA: "요청 제출하기" 56dp Brown900 / 비활성 시 alpha 0.4
- 결과: POST /maps/store-requests. 성공 시 popBackStack → StoreRequestDetail 이동.
- prefill: UPDATE 모드 → GET /maps/stores/{id}로 매장 정보 → 폼 채움(카테고리는 백엔드 응답에 없어 사용자가 재선택). 재제출 모드 → GET /maps/store-requests/{id}로 이전 payload 가져와서 채움.
- 위치 결과 회수: SavedStateHandle (RESULT_LAT, RESULT_LNG, RESULT_ADDRESS).
- ViewModel: StoreRequestFormViewModel — submit, applyPickedLocation, addPlan/updatePlanName/updatePlanPrice/removePlan, addPhotos/removePhoto, addProofs/removeProof, setName/setCategory/setIsPetAllowed/setPhone/setHours/setMessage

### [StoreRequestDetailScreen] 매장 요청 상세

- 라우트: `store_request_detail/{requestId}`. 마이 목록 또는 SYSTEM 알림 deeplink에서 진입.
- TopBar: 뒤로가기 + "요청 상세" 18sp SemiBold
- 상태 헤더 카드 (그라디언트 bg + border 1dp):
  - PENDING: #FEF3C7→#FFEDD4 / Orange500 border / ic_clock + "검토 중" #CA8A04 + 안내 + 신청일
  - APPROVED: #F0FDF4→#D0FEE1 / Green600 border / ic_check_circle + "승인되었어요" #16A34A + 안내 + 처리일 + 관리자 메모
  - REJECTED: #FEE7EC→#FEFEFE / Pink500 border / ic_alert_circle + "반려되었어요" #E84B6A + 사유 White 카드 + 처리일
- (APPROVED) 등록된 매장 미니 카드: 카테고리 그라디언트 박지 + 매장명 + chevron → PlaceDetail
- (REJECTED) 안내 박스: ic_lightbulb + "다시 작성하면 기존 내용이 채워진 채로 폼이 열려요"
- 제출 내용 카드: 유형/매장명/카테고리/주소/사진 수/증빙 수/(PET_HOTEL이면) 가격 플랜 수
- 하단 CTA:
  - PENDING: "요청 취소" Pink500 Outline → AlertDialog → DELETE
  - APPROVED: "매장 상세 보기" Brown900 → PlaceDetail
  - REJECTED: "다시 작성하기" Brown900 → StoreRequestForm(prefill from this requestId)
- ViewModel: StoreRequestDetailViewModel — refresh, cancelRequest (Idle/Cancelling/Cancelled/Failed)

### [MapPinPickerScreen] 위치 핀 선택

- 라우트: `map_pin_picker?lat={?}&lng={?}` — 초기 좌표 옵션. 없으면 시흥시청 기본.
- 풀스크린 KakaoMap (MapViewWrapper 재사용) + 중앙 고정 핀(ic_map_pin Orange500)
- 상단: 투명 그라디언트 TopBar + 뒤로가기 카드 + "위치 선택" 18sp SemiBold
- 하단 시트: "선택된 위치" 라벨 + ic_map_pin 박지 + 주소(reverse geocode `/api/v1/geo/reverse`) + 좌표 + "이 위치로 설정" Brown900 CTA
- Reverse geocode debounce 200ms. 실패 시 "주소를 가져올 수 없어요".
- 카메라 이동 종료 시 `setOnCameraMoveEndListener`에서 viewModel.onCameraIdle(lat, lng)
- 결과 반환: previousBackStackEntry.savedStateHandle에 `picked_lat`, `picked_lng`, `picked_address` 저장 후 popBackStack
- ViewModel: MapPinPickerViewModel — onCameraIdle, 자동 reverse geocode

### [ProfileEditScreen] 프로필 편집

- TopBar: 뒤로가기 카드(40×40dp shadow=2dp) + "프로필 편집" 18sp SemiBold
- 본문 (스크롤):
  - 아바타: 88dp 원형 Orange500 bg, 탭 시 이미지 피커, 우하단 카메라 뱃지(28dp Brown900 원형)
  - 닉네임 *: TextField (border=1dp, radius=16dp, padding=16dp)
  - 휴대폰 (선택)
  - 시 (선택) / 동 (선택)
  - 각 필드: 에러 텍스트(fieldErrors), 닉네임 충돌 별도 에러
- 하단: "저장하기" Orange500 fullWidth height=56dp (저장 중 스피너)
- ViewModel: ProfileEditViewModel (Loading/Loaded/Saving/SaveSuccess/Error/FieldErrors/NicknameConflict)

### [FavoriteStoresScreen] 즐겨찾기 매장

- TopBar: 뒤로가기 + "즐겨찾기 매장" 20sp ExtraBold
- 상태 분기: Loading(스피너) / Error(메시지 + retry) / Success empty(아이콘 + 텍스트) / Success list
- FavoriteStoreCard Row:
  - 썸네일 72×72dp 그라디언트(#D0FEE1→#E0F7FA), radius=12dp, 카테고리 아이콘
  - 매장명 16sp SemiBold + "[업종] · [거리] · ⭐[별점]" 12sp + Favorite 하트(즐겨찾기 시 Pink500, 해제 시 Brown400)
- 카드 탭 → PlaceDetail
- ViewModel: FavoriteStoresViewModel

### [PlaceDetailScreen] 장소 상세

- 라우트 인자 `placeId`(Int), `lat`/`lng`(옵션)
- TopBar: 뒤로가기(30×30dp shadow=1dp radius=8dp) + Favorite 아이콘(30×30dp, 즐겨찾기 시 Pink500 / 해제 시 BrownBorder) + Share(30×30dp)
- 본문 LazyColumn:
  1. **Banner**: 150dp 민트→연두 그라디언트 + LocationOn 48dp Pink500
  2. **장소 정보**: 장소명 28sp Bold / Star(StarYellow) + 별점 16sp Bold + "(후기 N개)" 16sp
  3. **정보 섹션**:
     - 영업시간 + 상태 chip ("영업 중" Green / "영업 마감" Gray, `isOpenNow()` 기반)
     - 주소 + 복사 버튼(Gray100 radius=50dp)
     - 전화 + 복사 + "전화하기"(border=1dp BrownBorder)
  4. **위치 Card**: height=140dp, 좌표 있으면 KakaoMap, 없으면 그라디언트 placeholder, 좌하단 동 pill
  5. **리뷰 섹션**:
     - 헤더: "리뷰" 14sp + "리뷰 쓰기"(Pink500 radius=50dp)
     - 평점 28sp + 별 표시 + "총 N개의 리뷰" 12sp
     - 리뷰 Card: 40dp 컬러 아바타(닉네임 길이 기반) + 닉네임 14sp SemiBold + 별점 + 날짜(우, 12sp) + 본문 + 반려동물 출입 chip
     - "리뷰 더 보기"(border=1dp BrownBorder) — 더 있을 때만
- **ReviewWriteSheet** ModalBottomSheet: 별 5개(StarYellow) + 반려동물 출입 토글(가능/불가) + 본문 OutlinedTextField 120dp + 카운터 + "등록"(Pink500, 별점=0 또는 본문 빈 경우 비활성, 제출 중 스피너)
- "정보 수정 요청" Outline 버튼: 로그인 + (is_owner=true OR owner_user_id=null) 일 때만 활성. 매장 owner 클레임 또는 정보 수정 요청 진입 (→ StoreRequestForm UPDATE)
- ViewModel 없음 — `LaunchedEffect` 안에서 `RetrofitClient` 직접 호출

---

## 서브 — 봉사

### [VolunteerApplyScreen] 봉사자 자격 신청

- TopBar: 뒤로가기 + "봉사자 자격 신청" 20sp ExtraBold
- 본문 (스크롤):
  - 제목: "반려동물과 함께하는" / "봉사자로 활동해보세요" 24sp Bold ("봉사자"=Green600)
  - "제목" TextField (UI only, 전송 안 함)
  - "신청서 *" 멀티라인 TextField minHeight=120dp + 카운터 "N/500"
  - 첨부 (선택, 최대 5개): 안내 텍스트 + "파일 또는 사진 첨부하기" 버튼
    - FlowRow chip: 16dp 아이콘(Image/Description) + 파일명 12sp Brown700 (maxWidth=140dp) + 닫기
  - **AttachPickerSheet**: 사진/이미지(Orange bg 아이콘) / 파일(Green bg 아이콘, PDF·문서)
- 하단: 에러 텍스트(있을 때) + "신청하기" Green600 fullWidth height=56dp (비활성=Gray300, 제출 중 스피너)
- **AlreadyPendingSheet**: HourglassEmpty(Brown400 48dp) + "이미 검토중인 신청이 있어요" + 본문 + "확인"(Brown700)
- **SuccessSheet**: CheckCircle(Green600 48dp) + "신청이 완료되었어요" + 본문 + "확인"(Green600)
- **ErrorSheet**: HourglassEmpty(Pink500 48dp) + "오류가 발생했어요" + 메시지 + "닫기"(Pink500)
- ViewModel: VolunteerApplyViewModel (event: Idle/Submitting/Success/AlreadyPending/Error)

### [VolunteerBadgeListScreen] 봉사 뱃지

- TopBar: 뒤로가기 카드(40×40dp shadow=2dp) + "봉사 뱃지" 20sp ExtraBold
- 본문 (스크롤):
  - **BadgeSummaryHeader** Row: 56dp 원형 아이콘 박스(현재 등급 색) + "[등급명] 등급" 18sp Bold(미달=「아직 뱃지가 없어요」) + "누적 봉사 N건" 14sp Brown700
    - bg=등급색 alpha=0.1f, radius=20dp, padding=20dp
  - **BadgeTierCard** Row × 4 (SEED/FLOWER/FRUIT/TREE, radius=20dp White):
    - 72dp 아이콘 박스(달성=등급색 / 미달성=Gray300) radius=20dp
    - 등급명 18sp Bold + AchievedChip ("달성" Green500 / "미달성" Gray)
    - 조건 텍스트 13sp Brown700: "기본 달성" / "봉사 N건 달성" / "봉사 N건 필요 (현재 M건)"
    - 미달성 시: LinearProgressIndicator(height=5dp radius=3dp, 등급색 / track=Gray300)
- 등급별 색: SEED=Green500 / FLOWER=Orange500 / FRUIT=Pink500 / TREE=Brown900
- 등급별 아이콘: SEED=ic_psychiatry / FLOWER=LocalFlorist / FRUIT=Eco / TREE=ic_nature
- ViewModel: VolunteerBadgeViewModel

### [VolunteerHistoryScreen] 봉사 활동 이력

- TopBar: 뒤로가기 카드(40×40dp shadow=2dp) + "봉사 활동 이력" 20sp ExtraBold
- 상태 분기:
  - **Loading**: 센터 CircularProgressIndicator Orange500
  - **NotVolunteer (빈 상태)**: 80dp 원형 #F0FDF4 bg + VolunteerActivism(Green500 40dp) + "봉사자 자격을 먼저 신청해주세요" 18sp Bold + 안내 14sp Brown700 + "봉사자 자격 신청하기"(Green500 radius=12dp, → VolunteerApply)
  - **Error**: 센터 메시지 14sp Brown700
  - **Success**:
    - **VolunteerStatsCard** (radius=20dp elev=2dp White): Row SpaceEvenly 3개 + 가운데 1dp Divider Gray300
      - 누적 봉사 (Green500 / 22sp Bold) / 봉사 시간 (Orange500) / 평균 평점 (StarYellow)
    - 매칭 비었으면 "아직 완료된 봉사 활동이 없어요" 14sp Brown400
    - **MatchHistoryCard** (radius=16dp elev=2dp): "완료" chip(#F0FDF4 bg, Green500) + ⭐별점(있을 때) / 제목 16sp SemiBold (2줄) / Divider / 메타 Row (CalendarMonth + 날짜, LocationOn + 주소) + 우측 KeyboardArrowRight (Brown400)
- ViewModel: VolunteerHistoryViewModel (Loading/NotVolunteer/Error/Success)

---

## 서브 — 소식

### [NewsDetailScreen] 소식 상세

- 라우트 인자 `newsId`(String)
- TopBar: 뒤로가기 + BookmarkBorder + Share
- **헤더**: OrangeSand→PinkSurface 그라디언트, padding=24dp
  - 카테고리 chip(Orange500 bg radius=50dp 14sp Medium White)
  - 제목 26sp Bold (2줄)
  - "[출처] · [날짜]" 13sp Brown700
- 본문 16sp Normal Brown700 lineHeight=26sp, padding 16–24dp
- 공식 링크 Card: Gray bg, bullet 텍스트
- **관련 소식 섹션**: "관련 소식" 라벨 + Card(PinkSurface bg) + PriorityHigh(36dp 원형 OrangeSand) + 카테고리 + 제목 14sp (→ NewsDetail)
- LaunchedEffect: 상세 로드 + 카테고리별 관련 소식 collect

---

## 서브 — 기타

### [BlockManageScreen] 차단 관리

- TopBar: 뒤로가기 카드(40×40dp shadow=2dp) + "차단 관리" 20sp ExtraBold
- 상태 분기: Loading(스피너 Orange500) / Error(메시지 + retry Orange500 radius=50dp) / Success
- LazyColumn — **BlockedUserCard** Row:
  - 44dp 원형 아바타 (이니셜)
  - 닉네임 16sp Bold
  - 차단 날짜 12sp
  - "차단 해제" 버튼(PinkSurface bg, radius=50dp)
- AlertDialog "차단 해제": 제목 + "[닉네임]" 메시지 + 확인(Pink500) / 취소
- ViewModel: BlockManageViewModel — `unblockUser()`

### [HelpScreen] 도움말

- TopBar: 뒤로가기 + "도움말" 20sp ExtraBold
- 본문: 단일 Card
- FaqRow (7개 FAQ 하드코딩, 펼치기/접기):
  - Q 박스 40×40dp OrangeSand + "Q" Orange500
  - 질문 15sp Medium + 화살표
  - 펼치면: 좌측 40dp spacer + 답변 14sp Normal Brown700
- 상태 없음 (ViewModel 미사용)

### [PrivacyPolicyScreen] 개인정보 처리 방침

- TopBar: 뒤로가기 카드(40×40dp shadow=2dp) + "개인정보 처리 방침" 20sp ExtraBold
- 본문: 전체 WebView
  - URL: `https://backend-production-f6c0.up.railway.app/privacy`
  - 설정: JavaScript 활성화, DOM Storage 활성화, 캐시 LOAD_DEFAULT
- 로딩: CircularProgressIndicator Orange500
- 에러: 센터 Column에 에러 메시지 + 재시도 안내

---

## 모달

### [MapFilterBottomSheet] 지도 필터 모달

- ModalBottomSheet: radius topStart/End=20dp, White, dragHandle=null
- 헤더 Row: "지도 보기 설정" 16sp SemiBold + X 닫기(14dp Brown700)
- HorizontalDivider Gray300
- 카테고리 항목 5개 Row(padding=16dp):
  - 40×40dp Gray300 bg radius=10dp 아이콘 박스
  - 라벨 16sp Medium
  - 체크박스 24×24dp radius=8dp (체크=Orange500 / 미체크=White border=1dp Gray300)
- 카테고리: 카페(LocalCafe) / 식당(ForkSpoon) / 공원(Forest) / 동물병원(HealthCross) / 미용(ContentCut)
- 하단: "적용하기" Brown700 fullWidth height=50dp radius=16dp 16sp Bold White

---

## 화면 ↔ 라우트 매핑

전체 라우트 목록은 [`architecture.md` §7.1](architecture.md#71-라우트-정의--navgraphkt) 참고.
