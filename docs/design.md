# 시흥가개 — 디자인 가이드

이 문서는 **새 기능·화면을 만들 때 따라야 할 디자인 결정**을 담는다.

| 문서 | 역할 |
|---|---|
| [`../CLAUDE.md`](../CLAUDE.md) | 토큰 정의 (컬러 hex, Typography 슬롯, 공통 컴포넌트 스펙) |
| [`screens.md`](screens.md) | 화면별 레이아웃·문구·동작 사전 |
| **이 문서** | spacing/radius/elevation/색 의미·패턴·언제 무엇을 쓰는지 |

---

## 1. 디자인 원칙

1. **따뜻한 갈색 톤이 베이스**. Brown900(#614B3A)은 단순 강조가 아니라 "신뢰·앵커"의 의미. 본문 텍스트 색(#1E120A)과 거의 같이 다룬다.
2. **분홍은 사용자 자신과 긴급도**. PinkSurface 카드 = "내 것" / "검토 필요" / "오늘 일정". Pink500은 강조·요청자·D-day.
3. **주황은 모든 액션·진입점**. "지도 보기", "전체 보기", "신청 →", 1단계 정보 카드 등 사용자가 다음 행동으로 갈 모든 텍스트·아이콘.
4. **초록은 봉사**. 봉사자 신청, 봉사 통계, 봉사 등급, 봉사 활동 진행 중 등.
5. **민트는 위치/지도 전용**. 그라디언트 placeholder, 채팅 상대 아바타 등 "장소·상대성"의 의미.
6. **그림자는 가볍게**. elevation 1·2·4·8 네 단계로 충분. 그 이상은 안 쓴다.
7. **둥근 모서리는 콘텐츠·UI 역할에 따라**. 12·16·20·50dp 네 종 (아래 §3).

---

## 2. Spacing 시스템

8dp 기반 그리드.

| 값 | 용도 |
|---|---|
| **4dp** | 최소 단위. 아이콘과 라벨, 컴팩트 요소 사이 |
| **8dp** | 짧은 gap (예: 칩 사이, BottomBar 내부 패딩) |
| **12dp** | 리스트 아이템 카드 사이, 버튼 vertical gap |
| **16dp** | **기본** — 화면 좌우 padding, 카드 내부 padding, 섹션 사이 |
| **20dp** | 시트 헤더 padding, 등급 카드 padding |
| **24dp** | 큰 섹션 헤더, 시작 화면 가로 padding |
| **28dp** | 등급 표시 같은 강조 padding |
| **32dp** | StartScreen 하단 푸터 등 |
| **48dp** | 빈 상태 spacing, 로고 아래 큰 gap |

#### 규칙

- 화면 좌우 padding: 거의 항상 **16dp**. 예외: 시작 화면(24dp), 빈 상태 메시지(32dp).
- 카드 내부 padding: **16dp** (radius=16dp 카드 기본). radius=20dp 카드는 20dp.
- 섹션 사이: **16~24dp**. 24dp는 시각적 구분이 명확해야 할 때(섹션 헤더 위).
- 같은 종류 아이템 사이: **12dp** (LazyColumn `Arrangement.spacedBy`).

---

## 3. Radius 시스템

| 값 | 용도 | 의미 |
|---|---|---|
| **8dp** | 30×30dp 작은 카드 버튼 (TopBar 보조 아이콘) | 미니 컨테이너 |
| **12dp** | Primary/Outline 버튼, TopBar 카드 버튼(40×40dp), 차단·해제 같은 작은 액션 | 일반 컨트롤 |
| **16dp** | **카드 기본**, 큰 버튼, 입력 필드 | 콘텐츠 컨테이너 |
| **20dp** | BottomSheet topStart/End, NotificationItem(읽지 않음), 등급 카드 | 시트·강조 카드 |
| **24dp** | News Featured 카드, 80dp 원형 아이콘 박스 | 히어로/큰 시각 요소 |
| **50dp** | **Pill** — 칩, 상태 태그, 검색 바, 작은 액션 버튼, 위치 라벨 | 인라인 액션 |

#### 규칙

- "둥근 사각형 vs Pill"이 헷갈리면: **인라인 텍스트 액션은 Pill(50dp), 콘텐츠 컨테이너는 16dp**.
- BottomSheet 는 topStart/End=20dp, bottomStart/End=0dp.
- 원형 아바타·아이콘은 명시적 `CircleShape` 사용 (radius로 흉내 X).

---

## 4. Elevation 시스템

| 값 | 용도 |
|---|---|
| **0dp** | 본문 카드 (flat) — 흰 배경 위 흰 카드를 border 없이 둘 때 |
| **1dp** | 설정 화면 섹션 카드, 검색 바 |
| **2dp** | **카드 기본**, TopBar 뒤로가기 버튼 카드, 매칭/요청 카드 |
| **4dp** | 지도 검색 바, 우측 FAB 컬럼 (떠있는 느낌) |
| **8dp** | BottomNavigationBar (전체 앱 위에 떠있음) |

#### 규칙

- 카드는 거의 모두 elevation=2dp.
- FAB(매칭 화면 +)는 elevation 명시 없이 기본값 (Material3 6dp) 사용.
- 그림자 색 커스텀 안 함 (기본 검정 alpha).

---

## 5. 색상 사용 의미론

CLAUDE.md에 hex는 정의되어 있다. 여기서는 **언제 어떤 색을 쓸지**.

### Brown 계열

| 토큰 | hex | 사용처 |
|---|---|---|
| **Brown900** | #614B3A | Primary 버튼 bg, 본문 강조, 로고, 보낸 메시지 버블, 위치 설정 chip 선택, BottomBar 선택탭 |
| **Brown700** | #8A6E58 | 서브텍스트, 라벨, BottomBar 미선택 텍스트, "취소" 같은 보조 버튼 |
| **Brown400** | #C4A882 | 비활성·미달성 텍스트, BottomBar 미선택 아이콘 |
| **BorderBeige** | #E8D3C2 | 흰 버튼 border, 입력 필드 border, 날짜 chip border |
| **FABBrown** | #9A7B5E | **매칭 화면 FAB 전용** — 다른 곳에서 쓰지 말 것 |

### Pink 계열 (=내 것·요청자·긴급)

| 토큰 | hex | 사용처 |
|---|---|---|
| **Pink500** | #F04268 | "내 요청" 강조, D-day 텍스트, 신청 버튼, 알림 "방금 전", 회원 탈퇴 |
| **PinkSurface** | #FEE7EC | "내 것" 표시 카드 bg, 읽지 않은 알림 bg, 상태 배너, D-day 배너 |
| **TagPinkBg** | (`#FEE7EC` 부근) | 상태 태그 "모집중" |

### Orange 계열 (=액션·진입점)

| 토큰 | hex | 사용처 |
|---|---|---|
| **Orange500** | #F7A35B | "지도 보기"/"전체 보기" 텍스트, 정보 아이콘, 로그인/회원가입 버튼, 진행 단계 표시 |
| **OrangeSand** | #FFEDD4 | 강아지 아이콘 bg, 도움말 Q 박스, 활동 영역 아이콘 박스 |

**Orange500이 Brown900보다 사용 빈도가 높다.** 사용자가 다음 행동으로 갈 가능성이 있으면 거의 Orange.

### Green 계열 (=봉사)

| 토큰 | hex | 사용처 |
|---|---|---|
| **Green600** | #00A63E | 봉사자 자격 신청, 봉사 통계, 봉사 등급, 산책지수 1단계 (80↑) |
| (TagGreenBg) | #F0FDF4 | "봉사 활동" 요약 카드 bg, "완료" chip bg |

### 기타

| 토큰 | hex | 사용처 |
|---|---|---|
| **Blue500** | #388AF5 | 산책지수 2단계 (50–79), 토요일 |
| **OrangeRed** | #EE6A46 | 산책지수 3단계 (0–49) — Orange500과 구분되는 경고 톤 |
| **MintLight** | #D0FEE1 | 지도 placeholder bg, 채팅 상대 아바타 bg |
| **StarYellow** | #FDC700 | 별점 |
| **Gray300** | #E8E8E8 | 구분선, Switch off bg, 미선택 체크박스 border |
| **TagGray** | #F2F2F2 | 매칭 카드 태그 bg |
| **AddGray** | #F4F4F4 | 반려동물 추가 버튼 bg, 채팅 입력창 bg |
| **PlaceholderColor** | #C1AEA0 | TextField placeholder 텍스트 |
| **TextBlack** | #1E120A | 본문 기본 텍스트 |
| **Background** | #FEFEFE | 화면 bg (거의 흰색) |

### 색 선택 결정 트리

1. 사용자가 누르거나 따라가야 할 텍스트·아이콘인가? → **Orange500**
2. "내 것" / "내가 신청한" / 긴급한 정보인가? → **Pink500** / **PinkSurface**
3. 봉사 도메인인가? → **Green600**
4. 본문 강조·고정 표시·기본 버튼인가? → **Brown900**
5. 보조 텍스트·라벨인가? → **Brown700**
6. 위치·지도 관련인가? → **MintLight** 그라디언트

---

## 6. Typography 패턴

CLAUDE.md 슬롯 표에 더해, **실제 사용 패턴**:

| 위치 | 슬롯 | 코드 예시 |
|---|---|---|
| 메인탭 TopBar 제목 | displaySmall (26sp ExtraBold) | "매칭", "마이", "소식" |
| 서브 화면 TopBar 제목 | headlineMedium (20sp ExtraBold) | "알림", "내 봉사 상세" |
| 모달 시트 헤더 | titleLarge (18sp SemiBold) | "지도 보기 설정", "비밀번호 변경" |
| 큰 화면 인사 | 30sp Bold | "반가워요!\n로그인을 진행해 주세요." |
| 도움 요청 질문 | headlineLarge (24sp ExtraBold) | "어떤 반려동물과\n함께 이동하나요?" |
| 카드 제목 | titleLarge (18sp Bold) | 알림 제목 |
| 장소·이름 | titleMedium (16sp Bold) | 봉사자명, 장소명 |
| 본문 | bodyMedium (14sp Medium) | 일반 텍스트 |
| 캡션 | labelSmall (12sp Medium) | 날짜, 거리, 출처 |
| 가격·점수 | displayMedium (30sp ExtraBold) | 산책지수 숫자만 |

#### 규칙

- 폰트는 **항상 PretendardFamily**. `fontFamily = PretendardFamily` 명시.
- `lineHeight` 는 폰트 크기의 약 1.4배(슬롯표 기준). 직접 sp 지정할 때도 그 비율 유지.
- 줄바꿈은 `\n` 으로 명시. 자동 줄바꿈에 의존하지 않음 (제목·인사·질문).

---

## 7. 카드 패턴

### 기본 흰 카드

```kotlin
Card(
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    modifier = Modifier.fillMaxWidth(),
) {
    Column(modifier = Modifier.padding(16.dp)) { ... }
}
```

거의 모든 정보 카드의 기본. 매칭 카드, 알림 카드, 봉사 이력 카드, 통계 카드.

### "내 것" / 강조 카드 (PinkSurface)

```kotlin
modifier = Modifier
    .clip(RoundedCornerShape(16.dp))
    .background(PinkSurface)
    .padding(16.dp)
```

프로필 카드, 상태 배너, "내 요청" 요약 카드, 채팅의 요청 미리보기.

### 그라디언트 카드

| 종류 | 그라디언트 | 사용처 |
|---|---|---|
| 지도 placeholder | MintLight → 연두 (#D0FEE1 → #E0F7FA) | 위치 카드, 미니맵 |
| 즐겨찾기 썸네일 | 민트 → 옅은 청록 | 매장 카드 썸네일 |
| 뉴스 헤더 | OrangeSand → PinkSurface | NewsDetail 헤더 |
| 뉴스 Featured | Brown900 → lighter | NewsScreen 메인 카드 |
| 봉사 모드 thumbnail | 카테고리 색 → lighter | 다양 |

`Brush.linearGradient()` 사용. 방향은 보통 좌상단 → 우하단 또는 위 → 아래.

### 카드 내부 정보 Row

매칭 상세에서 흔히 보임: 아이콘 박스(40~44dp) + 라벨(12sp) + 내용(14sp Bold). 아이콘 박스 bg 색은 정보 종류에 따라:

| 정보 | 박스 bg |
|---|---|
| 일정/날짜 (CalendarMonth) | #FFE4E6 (옅은 분홍) |
| 시간 (Schedule) | #FFF3E0 (옅은 주황) |
| 장소 (LocationOn) | #F0FDF4 (옅은 초록) |
| 반려동물 (Pets) | #FEF3E2 (옅은 살구) |
| 메모 (ChatBubble) | #F4F4F4 (회색) |

---

## 8. 버튼 패턴

### Primary (Brown900)

기본 확정 액션. "저장하기", "변경하기", "지원 현황 보기".

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Brown900)
        .clickable { onClick() }
        .padding(vertical = 16.dp),
    contentAlignment = Alignment.Center,
) {
    Text("저장하기", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
}
```

height=56dp, hPad=24dp, radius=12dp.

### Orange (액션·로그인·회원가입)

진입성·긍정성이 강한 액션. "로그인", "회원가입", "→ 일정 선택" (요청 폼 진행), 프로필 저장.

스펙은 Primary 와 동일, bg만 Orange500.

### Pink (요청자·긴급)

봉사자 시점의 "신청하기", "수락", 회원 탈퇴 등. Pink500 bg.

### Green (봉사 도메인)

"봉사자 자격 신청하기", "봉사자 자격 신청". Green600 bg.

### Outline

부정적·취소·보조 액션. "취소", "닫기", "편집"(프로필 카드 안).

```kotlin
modifier = Modifier
    .clip(RoundedCornerShape(12.dp))
    .border(1.dp, Brown900, RoundedCornerShape(12.dp))
    .background(Color.White)
    .padding(vertical = 14.dp, horizontal = 20.dp)
```

text = Brown900 SemiBold.

### Pill 액션 (50dp)

작은 인라인 액션. "모두 읽음", "리뷰 쓰기", "지도 보기".

```kotlin
modifier = Modifier
    .clip(RoundedCornerShape(50.dp))
    .background(Pink500) // 또는 PinkSurface, Color(0xFFF0FDF4)
    .padding(horizontal = 12.dp, vertical = 6.dp)
```

text는 14sp SemiBold.

### 비활성 상태

`alpha = 0.4f` (입력 미완료) 또는 `Gray300` bg (제출 가능 조건 미충족). **disabled 색을 회색조 새로 만들지 말 것** — 위 두 가지로 통일.

### 로딩 상태

```kotlin
if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
else Text("저장하기")
```

버튼 내부에 20dp 스피너 (색은 텍스트 색과 동일).

---

## 9. 입력 필드 패턴

### 표준 TextField

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    placeholder = { Text("예: 정왕 동물병원", color = PlaceholderColor, fontSize = 16.sp) },
    shape = RoundedCornerShape(16.dp),
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Orange500,
        unfocusedBorderColor = BorderBeige,
        cursorColor = Orange500,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
    ),
    modifier = Modifier.fillMaxWidth(),
)
```

또는 화면별로 직접 `BasicTextField` + Box 조합으로 그리는 곳도 있음 — 둘 다 같은 시각 결과.

- **radius=16dp** (16dp 카드와 같은 형제 컨테이너 느낌)
- **border=1dp BorderBeige** (포커스 시 Orange500)
- **placeholder=PlaceholderColor 16sp** Normal
- **cursor=Orange500**
- **padding=16×14dp** (회원가입·로그인 기준)

### 멀티라인 (메모/신청서)

`minHeight = 100~120dp`, `maxLines = ...`, 우하단에 카운터 텍스트 "N/500".

### 라벨

위에 별도 Text. 14sp SemiBold Brown700.

### 에러 supportingText

13sp Medium Pink500. 필드 바로 아래 좌측 정렬.

---

## 10. 칩 / 태그

### 필터 칩 (선택 가능)

```
공통: 14sp Medium, padding h=16dp v=8dp, RoundedCornerShape(50.dp)
선택:   bg=#1A1A1A, text=White
미선택: bg=White, border=1dp BorderBeige, text=Brown700
```

매칭/소식/지도 화면의 카테고리·상태 필터.

### 상태 태그 (정보용, 클릭 안 함)

```
공통: 12sp Medium SemiBold, padding h=10dp v=4dp, RoundedCornerShape(50.dp)
모집중:  bg=#FEE7EC / text=#E84B6A
검토중:  bg=#FEF3C7 / text=#CA8A04
진행중:  bg=#F0FDF4 / text=#16A34A
완료:    bg=#F3F4F6 / text=#6B7280
```

매칭 카드 우상단, 봉사 활동 이력 카드.

### 카테고리 라벨 칩 (소식·뉴스)

bg=카테고리 색(Orange500/Pink500/Green500/...) + text=White + 14sp Medium.

### 메타 칩 (날짜·거리)

아이콘 13dp + 텍스트 12sp Normal Brown700. 박스 없이 인라인. BorderBeige 박스로 감쌀 때도 있음 (요청 정보 카드 안).

---

## 11. BottomSheet 패턴

### Modal BottomSheet (덮는 모달)

```kotlin
ModalBottomSheet(
    onDismissRequest = { ... },
    sheetState = sheetState,
    containerColor = Color.White,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    dragHandle = null,  // 또는 BottomSheetDefaults.DragHandle()
) {
    Column(modifier = Modifier.padding(16.dp)) { ... }
}
```

- 헤더: 제목(18sp SemiBold) + X 닫기 → HorizontalDivider Gray300
- 본문 padding=16~24dp
- 하단: Primary 버튼 (변경하기/저장/적용하기)
- 사용처: 지도 필터, 위치 설정, 비밀번호 변경, 회원 탈퇴, 첨부 피커, 이메일 충돌, 신청 결과

### BottomSheetScaffold (지도)

`MapScreen` 만 사용. peekHeight=280dp, drag handle 있음, 항상 보임.

---

## 12. AlertDialog 패턴

```kotlin
AlertDialog(
    onDismissRequest = { ... },
    title = { Text("반려동물 삭제", fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
    text = { Text("${pet.name}을(를) 삭제할까요?", fontSize = 14.sp, color = Brown700) },
    confirmButton = {
        TextButton(onClick = { ... }) {
            Text("삭제", color = Pink500, fontWeight = FontWeight.SemiBold)
        }
    },
    dismissButton = {
        TextButton(onClick = { ... }) { Text("취소", color = Brown700) }
    },
    containerColor = Color.White,
    shape = RoundedCornerShape(20.dp),
)
```

확정 버튼 색은 행동 의미에 따라: 삭제/차단=Pink500, 일반 확인=Brown900, 봉사=Green600.

---

## 13. 빈 상태 / 로딩 / 에러

### 빈 상태

- 80dp 원형 아이콘 박스 (의미 색 bg, 0.1f alpha 또는 옅은 색)
- 18sp Bold 제목 (TextBlack)
- 14sp Normal 안내 (Brown700, 2줄 가능)
- 28dp gap 후 액션 버튼 (해당 도메인 색)

```
[VolunteerHistoryScreen] 봉사자 아님 빈 상태 예시:
  80dp #F0FDF4 + VolunteerActivism (Green500 40dp)
  "봉사자 자격을 먼저 신청해주세요"
  "봉사자 자격을 취득하면\n봉사 활동 이력을 확인할 수 있어요"
  [봉사자 자격 신청하기] Green500
```

### 로딩

- 화면 전체 로딩: `LoadingOverlay(isLoading = true)` — 반투명 검정 오버레이 + Orange 스피너
- 부분 로딩: 영역 안 중앙 `CircularProgressIndicator(color = Orange500)` 단일 (40dp 기본)
- 버튼 내부: 20dp 흰 스피너 (§8 참조)

### 에러

- 화면 전체: 중앙 텍스트 14sp Brown700 + "다시 시도" Pill 버튼 (Orange500 또는 Pink500)
- 인라인 (API 호출 결과): `ApiErrorEffect` → SiheungSnackbarHost
- RateLimited(429): `RateLimitBanner` (4초 자동 사라짐)
- 필드 검증: 13sp Medium Pink500 supportingText

---

## 14. 아이콘

### 라이브러리

`androidx.compose.material:material-icons-extended` (Material Symbols). 그 외 커스텀은 `res/drawable/ic_*.xml`.

### 자주 쓰는 아이콘

| 도메인 | 아이콘 | 의미 |
|---|---|---|
| Navigation | `KeyboardArrowLeft` | 뒤로 (TopBar 카드 안) |
| Navigation | `KeyboardArrowRight` | 다음/링크 (16~18dp, Brown400) |
| Navigation | `MoreVert` | 메뉴 드롭다운 |
| Action | `Notifications` | 알림 (TopBar 우측) |
| Action | `Settings` | 설정 (마이 화면) |
| Action | `Search` | 검색 |
| Action | `Share` | 공유 |
| Action | `BookmarkBorder` / `Bookmark` | 즐겨찾기 |
| Action | `Favorite` | 하트 (즐겨찾기 매장) |
| Action | `Add` | FAB / 추가 |
| Info | `LocationOn` | 위치 (12~16dp) |
| Info | `CalendarMonth` | 날짜 |
| Info | `Schedule` | 시간 |
| Info | `Star` | 별점 (StarYellow) |
| Info | `Pets` | 반려동물 |
| Info | `Handshake` | 봉사 |
| Info | `VolunteerActivism` | 봉사 활동 이력 |
| Info | `Eco` / `LocalFlorist` | 봉사 등급 (FRUIT/FLOWER) |
| Info | `ChatBubble` | 채팅 / 메모 |
| Info | `PriorityHigh` | 알림 아이콘 (44dp 원형 안) |
| Info | `CheckCircle` | 완료/성공 |
| Info | `HourglassEmpty` | 검토 중/대기 |
| Info | `PersonOff` | 회원 탈퇴 |

### 아이콘 크기 규약

| 컨텍스트 | 크기 |
|---|---|
| TopBar 카드 안 화살표 | 22dp |
| 카드 본문 메타 (날짜/위치) | 12~14dp |
| Pill 버튼 안 아이콘 | 14dp |
| 큰 정보 아이콘 박스 안 | 24dp (박스=44dp일 때) |
| FAB 아이콘 | 24dp |
| 빈 상태 일러스트 | 40dp (박스=80dp일 때) |

### tint

- 인터랙티브 아이콘: 의미 색 (Pink500/Orange500/Green500/Brown900)
- 비활성: Brown400 또는 Gray300
- 화살표 보조: Brown400 (16~18dp 작은 화살표)
- 별점: StarYellow

---

## 15. 애니메이션

`NavGraph.kt` 에 정의된 Apple HIG 기준 곡선을 **그대로 재사용**.

```kotlin
private val AppleEaseOut   = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private val AppleEaseInOut = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

private const val SCREEN_ENTER_MS = 380   // 화면 진입
private const val SCREEN_EXIT_MS  = 320   // 화면 이탈
private const val SCREEN_FADE_MS  = 280   // fade 단독
private const val DOCK_MORPH_MS   = 320   // BottomBar morph
```

#### 규칙

- 같은 레벨 탭 전환(Shared Axis Z): `fadeIn + scaleIn(0.94f → 1f)` + 반대.
- 푸시(Shared Axis X): `slideInHorizontally(Start) + fadeIn` + 반대.
- 뒤로가기: 같은 X축이지만 `End` 방향.
- BottomBar 의 선택 morph: 320ms tween, AppleEaseOut. spring 쓰지 말 것 — 끝나고 흔들림.
- 화면 내 작은 토글 (Switch/체크박스): Material3 기본 사용.

새 화면 전환을 추가하지 않는 한 위 상수를 다시 사용하면 일관성 유지.

---

## 16. BottomNavigationBar 패턴

`AppBottomBar` 컴포저블 하나가 전역 (NavGraph 안). 새로 그리지 말 것.

- 메인탭 5개 (홈·매칭·지도·소식·마이)에서만 표시. `isTopLevelTabRoute()` 가 판단.
- 선택탭은 검정 pill + 아이콘 + 라벨, 미선택은 아이콘만(Brown400).
- 진입 시 `slideInVertically + fadeIn`, 이탈 시 반대.
- 흰 카드 자체는 `RoundedCornerShape(50.dp)` + elevation=8dp, 좌우 margin=16dp, bottom margin=16dp + navigationBarsPadding.

탭 추가하려면 `bottomNavEntries` 리스트 + `isTopLevelTabRoute` 분기 둘 다 수정.

---

## 17. TopBar 패턴

### 메인탭

```
좌: 화면 제목 (displaySmall 26sp ExtraBold) — 또는 인사 + 로고 (Home)
우: 보조 아이콘 1~2개 (40×40dp 카드 또는 icon-only)
```

뒤로가기 없음 (BottomBar 가 네비게이션 담당).

### 서브 화면

```
좌: 뒤로가기 카드 (40×40dp shadow=2dp radius=12dp, KeyboardArrowLeft 22dp)
중: 화면 제목 (headlineMedium 20sp ExtraBold 또는 18sp SemiBold) — 가운데 정렬
우: 보조 아이콘 1~3개
```

뒤로가기 카드는 Box로 직접 만든다 (IconButton 사용하지 않음 — 카드 그림자가 필요):

```kotlin
Box(
    modifier = Modifier
        .size(40.dp)
        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White)
        .clickable { onBack() },
    contentAlignment = Alignment.Center,
) {
    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "뒤로", tint = TextBlack, modifier = Modifier.size(22.dp))
}
```

### 변형

- 채팅 화면: 좌측에 아바타 + 닉네임 + 서브 텍스트 (3줄 정보)
- 알림 화면: 우측에 "모두 읽음" 텍스트 링크
- 상세 화면: 우측에 BookmarkBorder + Share 등 액션 아이콘 카드

---

## 18. 새 화면 디자인 결정 체크리스트

1. **화면 유형은?** → 메인탭(전체화면, BottomBar 표시) / 서브화면(TopBar 뒤로가기) / 모달 시트
2. **TopBar 좌·중·우 결정** → §17
3. **본문 padding** → 좌우 16dp 거의 항상
4. **콘텐츠 컨테이너** → 카드(흰색 기본 / PinkSurface "내 것") / 그라디언트 헤더 / 직접 배치
5. **빈 상태·로딩·에러 UI** → §13의 3종 패턴
6. **주요 액션 버튼 색** → Primary(Brown900) / Orange(진입) / Pink(요청·긴급) / Green(봉사)
7. **사용 아이콘 결정** → §14 표 참고, 없으면 Material Symbols Extended에서 검색
8. **애니메이션은 자동** → NavGraph가 처리, 화면 내부 트랜지션만 별도 결정 필요 시 §15 곡선 재사용
9. **컬러 토큰 정의** → 화면 파일 상단에 `private val ColorX = Color(0xFF...)` 형태로 (값은 CLAUDE.md에서 가져옴)

---

## 19. 흔히 하는 디자인 실수

- ❌ `Color.Gray` / `Color.LightGray` / `Color.Black` 직접 사용 → ✅ CLAUDE.md 토큰의 hex 사용
- ❌ Material3 기본 `MaterialTheme.colorScheme.primary` 참조 → ✅ 화면 파일의 hex 변수 사용 (현재 코드 컨벤션)
- ❌ 카드를 elevation=8dp 같이 깊게 → ✅ 2dp 또는 4dp만 사용
- ❌ 새 radius 값(10dp, 14dp 등) 도입 → ✅ 12/16/20/50dp 중 선택
- ❌ 버튼을 RoundedCornerShape(8.dp) 로 → ✅ 12.dp Primary 기본
- ❌ 폰트 sp만 지정하고 lineHeight 누락 → ✅ 1.4배 근처로 함께 지정
- ❌ 화면별 색이 미묘하게 다른 hex (예: #614B3A vs #614C3B) → ✅ CLAUDE.md hex 정확히 복사
- ❌ "취소" 버튼을 Primary 스타일로 → ✅ Outline 또는 텍스트 버튼 (Brown700)
- ❌ Snackbar 메시지를 영어로 → ✅ 백엔드도 한국어, 클라이언트 메시지도 모두 한국어
- ❌ `dp` 와 `sp` 혼동 → ✅ 폰트는 sp, 그 외 거리는 dp
