# 시흥가개 — 디자인 시스템 & 화면별 스펙

"시흥가개" Android 앱을 Jetpack Compose로 개발 중이다.

## 기술 스택
- Kotlin + Jetpack Compose
- 최소 API 26
- 아이콘: `androidx.compose.material:material-icons-extended` (Material Symbols)
- 폰트: Pretendard Variable (`res/font/pretendardvariable.ttf`) — SUIT 대체 완료

---

## 컬러 팔레트 보정 (PDF 22페이지 기준 정확한 값)

```kotlin
// 기존 값 일부 수정
val TextBlack    = Color(0xFF1E120A)  // 기본 검정 글자색 (Brown900 대신 이걸 본문에 사용)
val Brown700     = Color(0xFF8A6E58)  // 커피색 (서브텍스트)
val Brown900     = Color(0xFF614B3A)  // 진한 커피색 (버튼, 강조)
val Background   = Color(0xFFFEFEFE) // 배경색
val OrangeSand   = Color(0xFFFFEDD4) // 연황토색 (강아지 아이콘 bg)
val Orange500    = Color(0xFFF7A35B) // 황토색 (Accent)
val MintLight    = Color(0xFFD0FEE1) // 연녹색 (지도 bg)
val Green600     = Color(0xFF00A63E) // 녹색 (산책지수 1단계, 봉사자 신청 버튼)
val PinkSurface  = Color(0xFFFEE7EC) // 연분홍 (카드 bg)
val Pink500      = Color(0xFFF04268) // 진분홍 (강조, 태그)
val Gray300      = Color(0xFFE8E8E8) // 연회색 (구분선, 미선택 하트)
val StarYellow   = Color(0xFFFDC700) // 별점 노란색
val BorderBeige  = Color(0xFFE8D3C2) // 흰버튼 테두리, 날짜/시간 선택 채우기색
val TagGray      = Color(0xFFF2F2F2) // 매칭화면 카드 태그 배경
val FABBrown     = Color(0xFF9A7B5E) // 매칭화면 + FAB 버튼
val AddGray      = Color(0xFFF4F4F4) // 도움요청 반려동물 추가 버튼 bg
val PlaceholderColor = Color(0xFFC1AEA0) // 입력창 placeholder 글자색
val Blue500      = Color(0xFF388AF5) // 산책지수 2단계
val OrangeRed    = Color(0xFFEE6A46) // 산책지수 3단계
```
```

---

## 폰트 Typography (Figma 분석 기준)

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

### Typography 슬롯 매핑 (AppTypography in Type.kt)

| 슬롯 | fontSize | fontWeight | lineHeight | 사용처 |
|---|---|---|---|---|
| displayMedium | 30sp | ExtraBold | 32sp | 산책지수 숫자 |
| displaySmall  | 26sp | ExtraBold | 32sp | 메인탭 TopBar 제목 (홈·매칭·소식·마이), 스플래시 로고 제외 |
| headlineLarge | 24sp | ExtraBold | 33sp | 도움요청 질문 텍스트 |
| headlineMedium| 20sp | ExtraBold | 32sp | 서브페이지 TopBar 제목 (알림·설정·반려동물추가 등) |
| headlineSmall | 20sp | Bold      | 24sp | 섹션 헤더 ("주변 매장 24곳", "이동 지원 요청") |
| titleLarge    | 18sp | Bold      | 27sp | 카드 제목, 알림 제목 |
| titleMedium   | 16sp | Bold      | 24sp | 장소명, 봉사자명 |
| titleSmall    | 16sp | SemiBold  | 24sp | 뉴스 카드 제목 |
| bodyLarge     | 14sp | Bold      | 20sp | 뉴스 리스트 제목 |
| bodyMedium    | 14sp | Medium    | 20sp | 본문, 섹션 라벨 |
| bodySmall     | 14sp | Normal    | 20sp | 날짜, 서브텍스트 |
| labelLarge    | 12sp | Bold      | 16sp | 태그 chip |
| labelMedium   | 12sp | SemiBold  | 16sp | 캡션 강조 |
| labelSmall    | 12sp | Medium    | 16sp | 캡션 (거리·날짜·출처) |

### 사용 기준 요약

```
// 스플래시 로고 "시흥가개"    : 36sp, ExtraBold, PretendardFamily, Brown900
// 메인탭 TopBar 제목          : 26sp, ExtraBold, lineHeight=32sp
// 서브페이지 TopBar 제목       : 20sp, ExtraBold, lineHeight=32sp
// 도움요청 질문               : 24sp, ExtraBold, lineHeight=33sp
// 섹션 헤더 (주변 매장 등)    : 20sp, Bold,      lineHeight=24sp
// 섹션 라벨 (계정/알림 등)    : 14sp, SemiBold,  lineHeight=20sp
// 카드 제목                   : 16sp, SemiBold,  lineHeight=24sp
// 산책지수 숫자               : 30sp, ExtraBold, lineHeight=32sp
// 본문                        : 14sp, Medium,    lineHeight=20sp
// 캡션 (거리·날짜 등)         : 12sp, Medium,    lineHeight=16sp
// 태그 chip 텍스트             : 12sp, Medium,    lineHeight=16sp
// BottomBar 선택탭 텍스트      : 13sp, SemiBold,  lineHeight=18sp
```

---

## 공통 컴포넌트 규칙

### 카드
```
shape     = RoundedCornerShape(16.dp)
elevation = 2.dp
bg        = White
```

### 버튼 Primary (로그인하기, 요청 등록하기 등)
```
shape   = RoundedCornerShape(12.dp)
bg      = Brown900 (#3E2A1A)
text    = White, 16sp, SemiBold
height  = 56.dp
hPad    = 24.dp
```

### 버튼 Outline (회원가입하기)
```
shape  = RoundedCornerShape(12.dp)
border = 1.dp Brown900
bg     = White
text   = Brown900
```

### Chip 필터 (전체/카페/공원 등)
```
선택됨 : bg=#1A1A1A, text=White,    shape=RoundedCornerShape(50.dp)
미선택 : bg=White,   border=1dp BrownBorder, text=Brown700
fontSize=14sp Medium, padding=horizontal 16dp vertical 8dp
```

### 태그 (모집중/검토중/진행중/완료)
```
모집중 : bg=TagPinkBg,   text=#E84B6A
검토중 : bg=TagYellowBg, text=#CA8A04
진행중 : bg=TagGreenBg,  text=#16A34A
완료   : bg=TagGrayBg,   text=#6B7280
shape=RoundedCornerShape(50.dp), padding=horizontal 10dp vertical 4dp, 12sp Medium
```

### BottomNavigationBar
```
전체 바  : White 배경, RoundedCornerShape(50.dp), elevation=8dp
           좌우 margin=16dp, 바닥 margin=12dp (떠있는 느낌)
선택된 탭 : 검정(#1A1A1A) pill + 아이콘(24dp) + 텍스트(14sp Medium) 흰색
           pill padding = horizontal 16dp vertical 10dp
미선택 탭 : 아이콘(24dp)만, color=Brown400(#C4A882), 텍스트 없음
탭 5개   : 홈(Home) / 매칭(Handshake) / 지도(Map) / 소식(Newsmode) / 마이(AccountCircle)
```

### TopBar (뒤로가기 있는 화면)
```
뒤로가기: 흰 카드(40x40dp, radius=12dp, elevation=2dp) + KeyboardArrowLeft 아이콘
타이틀 : 가운데 정렬, 18sp SemiBold
```

---

## 화면별 스펙

### [SplashScreen] 로그인 선택
```
배경: White
중앙: 로고 이미지(drawable/logo) — 없으면 "시흥가개" 36sp Bold Brown900
슬로건: "우리 동네 반려동물을 위한 따뜻한 발걸음" 14sp Regular Brown700 가운데
하단 버튼 2개 (세로, gap=12dp, hPad=24dp, bottom=40dp):
  1. "로그인하기"   — Primary 버튼
  2. "회원가입하기" — Outline 버튼
```

### [HomeScreen] 홈
```
TopBar:
  좌: "안녕하세요, 댕댕이주인님" 12sp Regular Brown700
      "시흥가개" 28sp Bold Brown900
  우: [LocationOn + "정왕동" pill (12sp, border=BrownBorder, radius=50dp)]
      [Notifications 아이콘 버튼]

산책지수 섹션:
  "오늘 산책지수" 14sp Medium Brown700
  "92점으로 좋아요" 36sp Bold — "92점"만 Green500, 나머지 검정
    80이상=Green500 / 50-79=Blue400 / 0-49=Orange500
  날씨 "맑음 · 18° · 미세먼지 좋음" 12sp Gray
  D-day 배너: PinkSurface bg, radius=50dp, hPad=16dp vPad=8dp
    "[D-2]  병원 이동 · 신청 2건 검토하기  >" 13sp Medium

주변 매장 섹션:
  헤더: "주변 매장" 18sp Bold + "24곳" Orange500 + "🗺 지도 보기" 우측 13sp Orange500
  지도 Card: height=180dp, 민트-하늘 그라디언트
    Pink500 LocationOn 핀 3개 + 오렌지 핀 1개
    좌하단: LocationOn + "정왕동" pill (White bg, radius=50dp)
  카테고리 LazyRow: 전체/카페/공원/병원/미용/식당, gap=8dp
  장소 리스트 2개:
    번호 원형(32dp): 1번=Pink500, 2번이상=Brown900, 숫자 White 14sp Bold
    장소명 16sp SemiBold + "업종 · 거리 · ⭐ 별점" 12sp Brown700
    Favorite 아이콘: 즐겨찾기됨=Pink500, 안됨=BrownBorder

반려동물 소식 섹션:
  헤더: "반려동물 소식" 18sp Bold + "📰 전체 보기" 우측 Orange500
  뉴스 카드: 이미지(80x80dp radius=8dp) + 카테고리 태그chip + 제목 15sp SemiBold + 날짜·출처 12sp
  소식 아이템: [행사/봉사/지원 태그chip] + 제목 14sp + 날짜 12sp Gray — Divider 구분
  하단 배너 Card: PinkSurface bg
    Handshake 아이콘(Orange500) + "이동 지원 봉사자가 되어 보세요" 14sp Bold
    "신청 →" 버튼 (Brown900 bg, radius=50dp)
```

### [NotificationScreen] 알림
```
TopBar: 뒤로가기 카드 + "알림" 가운데 + "모두 읽음" 우측 14sp Brown700

필터 Chip Row: 전체/매칭/소식/시스템, gap=8dp

알림 아이템 LazyColumn:
  읽지않음: bg=PinkSurface, radius=12dp
  읽음    : bg=White, Divider만
  좌: 원형(44dp, White bg, border=1dp Gray): PriorityHigh 아이콘
    매칭=Pink500 / 봉사=Green500 / 소식=Orange500
  제목: 16sp Bold
  내용: 14sp Regular Brown700, 2줄
  우측 상단 시간: 12sp Brown400, "방금 전"=Pink500
```

### [MatchingScreen] 매칭
```
TopBar: "매칭" 24sp Bold 좌측 + Assignment 아이콘 카드버튼(40x40dp) 우측

요약 카드 Row (gap=12dp, equal weight):
  내 요청  : PinkSurface bg, Assignment(Pink500) + "내 요청" 12sp + "2건 검토 중" 16sp Bold
  봉사 활동: #F0FDF4 bg,    Favorite(Green500) + "봉사 활동"   + "1건 진행 중" 16sp Bold

탭 필터 Row (가로 스크롤):
  전체 8 | 모집중 5 | 검토중 2 | 진행중 1 | 완료
  선택=14sp Bold Black, 미선택=14sp Regular Brown400, "·" 구분자

섹션 헤더: "이동 지원 요청" 18sp Bold + "🗺 지도 보기" 우측 Orange500

요청 카드 LazyColumn (gap=12dp):
  Card(radius=16dp, elevation=2dp):
    1행: 태그chip + "D-2" 우측 14sp Brown700
    2행: 제목 16sp SemiBold (최대 2줄)
    3행: "지역 · 거리 · 날짜" 13sp Brown700
    4행: 반려동물 chip들 (radius=50dp, border=BrownBorder, 12sp) + 신청수 텍스트

FAB: Brown900 bg, Add(White), 56x56dp, radius=16dp, 우하단 margin=16dp
```

### [RequestFlowScreen] 도움 요청하기 (3단계)
```
TopBar: 뒤로가기 카드 + "도움 요청하기" 18sp SemiBold + "1/3" 14sp Brown700

스텝 인디케이터 (3구간 선, 두께 3dp):
  완료/현재: Orange500 | 미완료: Brown900 30% opacity
  구간 아래 텍스트(12sp): 현재=Orange500, 나머지=Brown400
  라벨: "반려동물" / "일정" / "요청 내용"

질문 스타일: 26sp Bold Brown900 (줄바꿈 포함)
서브 설명 : 14sp Regular Brown700, 상단 8dp

--- Step 1: 반려동물 선택 ---
Grid 2열 gap=12dp, Card(radius=16dp):
  아이콘 Box(100x100dp, radius=20dp):
    강아지: #FEF3E2 bg, Pets 아이콘 Orange500
    고양이: #FFE4E6 bg, Pets 아이콘 Pink500
  이름 16sp Bold, 종류·나이·몸무게 13sp Brown700
  선택됨: border=2dp Orange500 + 우상단 체크 뱃지(Orange500 원+White Check)
  미선택: border=1dp BrownBorder

추가 카드: Gray bg, 점선 border, Add(Gray) + "반려동물 추가" 14sp Gray
하단 고정: "→ 일정 선택" Primary 버튼

--- Step 2: 일정 선택 ---
달력:
  헤더 "< 2026년 10월 >" 16sp SemiBold
  요일: 일=Pink500, 토=Blue400, 평일=Brown900, 12sp
  날짜: 16sp, 일=Pink500
  선택됨: #FFF3E0 원형 bg, 텍스트 Orange500

희망 시간:
  "희망 시간" 16sp SemiBold
  입력 필드: border=1dp BrownBorder, radius=12dp, Schedule(Orange500) 좌측
    placeholder "예: 오전 9:30" 14sp Brown400
  "빠른 선택" 12sp Brown700
  시간 chip Grid 4열(09:00~20:00):
    미선택: border=BrownBorder, White bg | 선택: #FFF3E0 bg, Orange500 border+text
하단: "→ 요청 내용 작성" Primary 버튼

--- Step 3: 요청 내용 ---
입력 필드 (라벨 14sp SemiBold, 필드 border=1dp BrownBorder radius=12dp padding=16dp):
  제목 *   : 일반 TextField
  목적지 * : LocationOn(Orange500) 좌측
  메모 *   : minHeight=120dp, 우하단 "0/500" 12sp Brown400
하단: "요청 등록하기" Primary 버튼
```

### [MyRequestsScreen] 내 봉사 요청 목록
```
TopBar: 뒤로가기 + "내 봉사 요청" + "+ 새 요청" 버튼(Pink500 bg, radius=50dp, 14sp White)

필터 Chip Row: 전체/매칭 전/매칭됨/종료됨

요청 카드 (MatchingScreen 카드 + 추가):
  "🗨 새 메시지 1" 우측 상단 (Pink500, chat_bubble 아이콘)
  "봉사자 2명 신청" 우측 상단 Pink500
  하단 위치 chip: LocationOn(Brown700) + 장소명, Brown100 bg, radius=50dp
  완료 카드: ⭐⭐⭐⭐⭐ Row + "후기 보기" 버튼(Brown100 bg)
```

### [MapScreen] 지도
```
전체화면 지도 placeholder: 민트-White 그라디언트 Box(fillMaxSize)
  핑크 LocationOn 핀 2개 + 오렌지 핀 1개
  파란 원형 dot(지름 16dp, 내 위치)

오버레이:
1. 검색 Card(상단, radius=50dp, elevation=4dp, height=48dp, margin=16dp):
   Search(Brown400) + "매장 · 병원 · 공원 검색" placeholder

2. 카테고리 LazyRow (검색창 아래 8dp): 전체/카페/공원/병원
   chip: White bg(미선택) / 검정(선택), elevation=2dp, gap=8dp

3. 우측 FAB 3개 세로(margin=16dp):
   Card(40x40dp radius=12dp elevation=4dp): MyLocation / Layers / Refresh, Brown700

하단 BottomSheet (radius topStart/End=20dp):
  드래그 핸들(회색 바, 가운데)
  "주변 매장 24곳" 18sp Bold + "↕ 거리순" 우측
  LazyColumn 장소 리스트 (홈화면과 동일 형식)
```

### [PlaceDetailScreen] 장소 상세
```
TopBar (그라디언트 위): 뒤로가기 카드 + BookmarkBorder + Share 아이콘 카드들(우측)

상단 배너: 민트-연두 그라디언트, height=200dp

장소 정보:
  장소명: 28sp Bold Brown900
  "업종 · 거리": 14sp Brown700
  ⭐(Gold) + "4.9" 22sp Bold + "(후기 128)" 14sp Brown700

정보 Row 3개:
  아이콘 Box(44x44dp, radius=12dp, border=1dp BrownBorder)
  라벨 12sp Brown400 + 내용 14sp Bold Brown900
  우측: "영업 중" 초록 pill / "복사" 아웃라인 버튼(radius=50dp, 12sp)

위치 Card(radius=16dp, height=160dp): 민트 그라디언트 + 핑크 핀 + 장소명 pill

리뷰 섹션:
  "리뷰" 18sp Bold
  "4.8" 28sp Bold + ⭐⭐⭐⭐⭐(Gold) + "총 128개의 리뷰" 13sp Brown700
  리뷰 Card(radius=12dp):
    아바타 원형(36dp, 이름 첫글자, 랜덤 배경색)
    닉네임 14sp SemiBold + ⭐행 + "N일 전" 12sp Brown400
    리뷰 내용 14sp Regular
```

### [NewsScreen] 소식
```
TopBar: "소식" 24sp Bold 좌측

카테고리 LazyRow: 전체/정책/행사/봉사/지원, gap=8dp

메인 뉴스 Card: 이미지(fullWidth height=160dp radius=12dp) + 태그 + 제목 16sp SemiBold + 날짜·출처 12sp

소식 리스트:
  [태그chip] + 제목 14sp SemiBold + 날짜 12sp Brown400
  Divider(1dp, Brown100) 구분
```

### [MyScreen] 마이
```
TopBar: "마이" 24sp Bold + Settings 아이콘 우측

프로필 Card(PinkSurface bg, radius=16dp):
  원형 이미지(56dp) + 닉네임 16sp Bold + LocationOn+지역명 13sp Brown700
  "편집" 아웃라인 버튼(radius=50dp, 13sp)

내 반려동물:
  라벨 "내 반려동물" 14sp SemiBold Brown700
  Card: Pets(Orange500, 연오렌지 bg 40dp) + "파댕이" 15sp Bold + "강아지·3살·수컷" 12sp
  "전체 보기" 아웃라인 버튼 우측

활동 통계 Row (3개, equal weight, gap=8dp):
  Card: 숫자 24sp Bold (내요청=Pink500/봉사=Green500/즐겨찾기=Orange500) + 라벨 12sp Brown700

봉사 뱃지 Card(radius=16dp):
  "🌱 봉사 등급" 15sp SemiBold + "전체 보기 >" Orange500 13sp 우측
  "새싹 등급 · 누적 2건" 13sp + "꽃까지 3건" Brown400 우측
  ProgressBar(높이 6dp, radius=3dp, Orange500, 진행 2/5)
  뱃지 Row 4개:
    새싹: Green500 bg / 꽃: 베이지 / 열매: 핑크 / 나무: Gray (원형 48dp)
    이름 12sp Bold + 조건 11sp Brown400

내 기록:
  VolunteerActivism(Green500) + "봉사 활동 이력" + KeyboardArrowRight
  Favorite(Pink500)           + "즐겨찾기 매장"  + KeyboardArrowRight

설정:
  "알림 설정" / "지역 설정(서브:정왕동)" / "개인정보 및 보안" / "앱 정보(서브:v3.0.0)"
  "봉사자 자격 신청" — 텍스트 Green500
  각 항목 14sp Medium + KeyboardArrowRight, Divider 구분

"로그아웃" Outline 버튼(fullWidth, 상단 margin=16dp)
```

---

## 코드 작성 공통 규칙
- 모든 파일은 실행 가능한 완성 형태로 작성
- `@Preview` 어노테이션 항상 포함 (showBackground=true)
- data class + 더미 데이터 포함해서 바로 실행되게
- 지도는 민트 그라디언트 Box로 placeholder 처리
- 화면 간 이동: `(onNavigate: (String) -> Unit)` 파라미터로 처리


# CLAUDE.md 추가 내용 — 빠진 화면 스펙

기존 CLAUDE.md 파일 맨 아래에 이 내용을 붙여넣으세요.

---



---

## 추가 화면 스펙

### [MatchingDetailScreen] 매칭 상세 — 내 봉사 상세 (요청자 시점)
```
TopBar: 뒤로가기 + "내 봉사 상세" 가운데 + MoreVert + Share 아이콘 우측

상태 배너 (PinkSurface bg, fullWidth, padding=16dp):
  좌: "봉사자 모집 중 · 신청 2건" 14sp Brown700
  우: "D-2" 14sp Orange500
  (상태에 따라: 진행중="진행 중", 완료="완료됨" 등으로 변경)

요청 정보 Card (radius=16dp, elevation=2dp):
  제목: 20sp Bold TextBlack, 2줄까지
  정보 Row 5개 (아이콘Box(36x36dp radius=12dp) + 라벨12sp + 내용14sp Bold):
    CalendarMonth (핑크 bg #FFE4E6): "일정" + "2026년 5월 10일 (일)"
    Schedule (오렌지 bg #FFF3E0): "시간" + "오전 10:00 출발 예정"
    LocationOn (초록 bg #F0FDF4): "목적지" + "정왕 동물병원"
    Pets (브라운 bg #FEF3E2): "반려동물" + chip("파댕이 · 강아지 · 중형", BorderBeige bg)
    ChatBubble (회색 bg #F4F4F4): "요청 메모" + 내용 14sp Regular
  각 Row 사이 Divider(1dp, Gray300)

경로 Row (padding=16dp):
  LocationOn(Brown700) + "정왕동 자택" + "→" + "정왕 동물병원" + "0.8km" 우측
  Divider 위아래

경로 지도 Card (height=160dp, 민트 그라디언트):
  핑크 핀(출발) ———— 오렌지 핀(도착) 연결선
  좌하단: "🗺 지도에서 보기" 버튼(White bg, radius=50dp, 14sp)

신청한 봉사자 섹션:
  "👥 신청한 봉사자" 18sp Bold + "2명" 우측 Orange500 16sp Bold

봉사자 카드 (각각 Card radius=12dp):
  상단 Row: 원형 아바타(44dp) + 닉네임 16sp Bold + ⭐별점 + "봉사 N건" + "📍N.Nkm"
  메모: 14sp Regular Brown700 (없으면 "메모 없음" Gray)
  하단 버튼 Row (gap=8dp):
    "채팅" 아웃라인 버튼(radius=50dp, Brown900)
    "✓ 수락" Primary 버튼(Pink500 bg, radius=50dp)
    "거절" 아웃라인 버튼(border=Pink500, text=Pink500, radius=50dp)
```

### [MatchingPublicDetailScreen] 이동 지원 요청 상세 — 봉사자 시점
```
TopBar: 뒤로가기 + "이동 지원 요청" 가운데 + BookmarkBorder + Share 아이콘

상태 배너: MatchingDetailScreen과 동일

요청 정보 Card: MatchingDetailScreen과 동일 구조

경로 + 지도 Card (동일):
  지도 위 하단 오버레이 버튼 2개 (가로 Row):
    ChatBubble 아이콘 버튼 (White bg, 40x40dp, radius=12dp, elevation=2dp)
    "봉사 신청하기" 버튼 (Pink500 bg, radius=50dp, fullWidth, 16sp White Bold)
  좌하단: "🗺 지도에서 보기" 버튼

요청자 정보 섹션:
  "요청자 정보" 16sp SemiBold Brown700
  Card (radius=12dp):
    원형 아바타(44dp, Green500 bg, 이름 첫글자)
    닉네임 16sp Bold + "⭐4.9 · 요청 6건 · 📍1.2km" 13sp Brown700
    우측: "🗨 채팅" 아웃라인 버튼(radius=50dp, 13sp)
```

### [ChatScreen] 채팅
```
TopBar:
  뒤로가기 + 원형 아바타(36dp, Green500 bg, 첫글자) + 닉네임 16sp Bold
  서브: "⭐4.9 · 봉사 12건 · 1.2km" 12sp Brown700
  우측: "수락" 버튼(Pink500 bg, radius=50dp, 14sp White) — 상황에 따라 표시

요청 미리보기 Card (상단, PinkSurface bg, radius=12dp):
  Handshake 아이콘(Orange500, 40x40dp 연오렌지 bg) + 제목(14sp SemiBold, 말줄임) + "검토 중" 태그
  날짜 · 목적지 12sp Brown700

날짜 구분자: "오늘" 12sp Gray, 가운데 정렬, Divider 양옆

메시지 버블:
  상대방(봉사자): 좌측, 연두색(#F0FDF4) bg, radius=topStart=4dp 나머지=16dp
    아바타(32dp) + 버블 + 시간(12sp Gray) 순서
  나(요청자): 우측, Brown900 bg, White 텍스트, radius=topEnd=4dp 나머지=16dp
    시간(12sp Gray) + 버블 순서
  버블 padding=12dp, 텍스트 15sp, maxWidth=75%

하단 입력창 (White bg, border-top=1dp Gray300):
  TextField (radius=50dp, border=1dp Gray300, placeholder "메시지를 입력하세요..." 14sp PlaceholderColor)
  Send 아이콘 버튼 (Brown900 원형 bg, 40x40dp, White Send 아이콘)
```

### [SettingsScreen] 설정
```
TopBar: 뒤로가기 + "설정" 18sp SemiBold 가운데

섹션별 Card (radius=16dp, elevation=1dp) + 섹션 라벨:

[계정] 라벨 14sp SemiBold Brown700:
  Card:
    AccountCircle(Orange500) | "프로필 편집" | KeyboardArrowRight
    Divider
    Pets(Orange500) | "반려동물 정보" | KeyboardArrowRight
    Divider
    SocialLeaderboard(Orange500) | "봉사 이력" | "3건" Orange500 + KeyboardArrowRight

[알림] 라벨:
  Card:
    Notifications(Orange500) | "매칭 알림" | Switch(켜짐=Brown900)
    Divider
    Campaign(Orange500) | "공지 알림" | Switch(켜짐=Brown900)
    Divider
    ChatBubble(Orange500) | "리뷰 알림" | Switch(꺼짐=Gray)

[앱] 라벨:
  Card:
    LocationOn(Orange500) | "위치 설정" | "정왕동" Orange500 + KeyboardArrowRight
    Divider
    Mobile(Orange500) | "버전" | "1.0.0" Orange500 (클릭 불가)

[기타] 라벨:
  Card:
    Help(Orange500) | "도움말" | KeyboardArrowRight
    Divider
    Lock(Orange500) | "개인정보 처리 방침" | KeyboardArrowRight
    Divider
    Logout(Pink500) | "로그아웃" (텍스트 Pink500) — KeyboardArrowRight 없음

Switch 스타일:
  켜짐: Brown900 배경, 흰 원형 thumb
  꺼짐: Gray300 배경, 흰 원형 thumb
```

### [PetListScreen] 내 반려동물 목록
```
TopBar: 뒤로가기 + "내 반려동물" 가운데 + MoreVert 아이콘 우측(카드버튼)

서브헤더: "내 반려동물 2마리" 14sp SemiBold Brown700

Card (radius=16dp, elevation=2dp):
  각 동물 Row (padding=16dp):
    아이콘 Box(56x56dp, radius=16dp):
      강아지: OrangeSand(#FEF3E2) bg, Pets 아이콘 Orange500
      고양이: PinkSurface(#FEE7EC) bg, Pets 아이콘 Pink500
    이름 16sp Bold TextBlack
    "강아지 · 말티즈 · 3살 · 3.2kg" 13sp Brown700
    우측: MoreVert 아이콘(Brown700)
    항목 사이 Divider(1dp, Gray300)
```

### [PetAddScreen] 반려동물 추가
```
TopBar: 뒤로가기 + "반려동물 추가" 18sp SemiBold 가운데

미리보기 Card (OrangeSand bg #FEF3E2, radius=16dp, padding=16dp):
  Pets 아이콘(60dp, White bg, radius=16dp, Orange500)
  "미리보기" 12sp Brown700
  이름(입력 전: "이름을 입력하세요") 18sp Bold TextBlack
  "강아지 · 1살 · 수컷" 14sp Brown700 (입력 따라 실시간 변경)

[기본 정보] 섹션 라벨 14sp SemiBold Brown700:
  Card (radius=16dp):
    Row: "이름" 14sp SemiBold | 우측 TextField(placeholder "예: 파댕이", border=none, 우측 정렬)
    Divider
    Row: "종류" 14sp SemiBold | 우측 Chip Row: [강아지] [고양이] [기타]
      선택됨: Brown900 bg White text, 미선택: border=BrownBorder bg=White
    Divider
    Row: "품종(선택)" | 우측 TextField(placeholder "예: 말티즈")

[상세 정보] 섹션:
  Card:
    Row: "나이" | 우측: [-] [숫자] [+] 버튼 + [개월] [살] chip
    Divider
    Row: "성별" | 우측: [암컷] [수컷] chip
    Divider
    Row: "중성화" | 우측: Switch

[특징 및 주의사항]:
  TextField (multiline, minHeight=100dp, border=1dp BrownBorder, radius=12dp)
  placeholder: "알러지, 질환, 성격 등 참고할 만한 내용을 자유롭게 적어주세요."
  우하단 "0/300" 카운터 12sp Brown400

하단: "저장하기" Primary 버튼
```

### [VolunteerApplyScreen] 봉사자 자격 신청
```
TopBar: 뒤로가기 + "봉사자 자격 신청" 18sp SemiBold 가운데

제목 섹션:
  "반려동물과 함께하는" 24sp Bold TextBlack
  "봉사자로 활동해보세요" 24sp Bold (봉사자= Green600 #00A63E, 나머지 TextBlack)

입력 필드:
  "제목 *" 14sp SemiBold Brown700
  TextField (radius=12dp, border=1dp BorderBeige, placeholder "예: 봉사자 자격 요청드립니다." 14sp PlaceholderColor)

  "신청서 *" 14sp SemiBold Brown700
  TextField (multiline, minHeight=120dp, radius=12dp, border=1dp BorderBeige)
  placeholder: "시흥가개에 전달할 내용을 입력하세요.\n예: 신청서의 예시 문장이\n뭐가있지" 14sp PlaceholderColor
  우하단 "0/500" 12sp Brown400

하단: "신청하기" 버튼 (Green600 #00A63E bg, White 텍스트, radius=12dp, fullWidth, height=56dp)
```

### [NewsDetailScreen] 소식 상세
```
TopBar: 뒤로가기 + BookmarkBorder + Share 아이콘(카드버튼) 우측

상단 헤더 영역 (연살구-연분홍 그라디언트 bg, padding=24dp):
  카테고리 태그chip (Orange500 bg, White text, 14sp) — "지원"
  제목: 28sp Bold TextBlack (2줄)
  "시흥시청 동물보호과 · 4월 10일" 13sp Brown700

해시태그 Row: [#정책] [#시흥시] [#중성화] — chip(border=BrownBorder, radius=50dp, 13sp Brown700)

본문 내용: 15sp Regular TextBlack, lineHeight=24sp

본문 이미지: fullWidth, radius=12dp, height=200dp

정보 박스 Card (radius=12dp, bg=Background, padding=16dp):
  bullet 리스트:
  "· 신청 기간: 2026.04.10 - 2026.05.15"
  "· 지원 대상: 시흥시 거주 실외 사육견 소유자"
  "· 지원 내용: 수술비 전액 및 내장형 칩 등록 지원"
  14sp Regular TextBlack

추가 본문

관련 소식 섹션:
  "관련 소식" 16sp SemiBold Brown900
  관련 Card 2개 (radius=12dp, PinkSurface bg):
    PriorityHigh 아이콘(Orange500, 36dp 원형) + 카테고리 12sp + 제목 14sp SemiBold
```

### [MapFilterBottomSheet] 지도 필터 모달 (21페이지)
```
ModalBottomSheet (radius topStart/End=20dp, dragHandle 있음):
  헤더 Row: "지도 보기 설정" 18sp SemiBold + X 닫기 버튼(우측, 24dp)
  Divider

  카테고리 항목 5개:
    Row (padding=16dp):
      아이콘 Box(40x40dp, radius=12dp, Gray300 bg): 카테고리 아이콘 Brown700
      카테고리명 16sp Medium TextBlack
      우측: Checkbox
        선택됨: Orange500 bg + White Check 아이콘, radius=6dp
        미선택: border=1dp Gray300, radius=6dp, White bg
    항목: 카페(LocalCafe) / 식당(ForkSpoon) / 공원(Forest) / 동물병원(HealthCross) / 미용(ContentCut)
    Divider 구분

  하단: "적용하기" Primary 버튼 (Brown900 bg, fullWidth)
```

---

## Navigation Routes 추가

```kotlin
object MatchingDetail    : Screen("matching_detail/{requestId}")
object MatchingPublicDetail : Screen("matching_public_detail/{requestId}")
object Chat              : Screen("chat/{userId}")
object Settings          : Screen("settings")
object PetList           : Screen("pet_list")
object PetAdd            : Screen("pet_add")
object VolunteerApply    : Screen("volunteer_apply")
object NewsDetail        : Screen("news_detail/{newsId}")
```