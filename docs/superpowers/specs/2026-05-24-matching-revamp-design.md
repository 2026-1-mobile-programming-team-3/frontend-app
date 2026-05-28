# 매칭 화면 개선 — 설계 스펙

작성일: 2026-05-24
작업 범위: 시흥가개 안드로이드 클라이언트의 `MatchingScreen` (이동 도움 매칭 리스트) UI/UX 전면 개선. P0+P1 14개 항목.

관련 문서:
- 백엔드 변경 요청: [`docs/backend-requests/2026-05-24-matching-revamp.md`](../../backend-requests/2026-05-24-matching-revamp.md)
- 시안 (브레인스토밍 세션): `.superpowers/brainstorm/257592-*/content/{00,01,02}*.html`

---

## 1. 목표

매칭 화면이 "기능 구현만 된 수준"이라 사용자가 원하는 매칭을 찾기 어려움. 당근마켓/번개장터 류 한국 중고거래·지역 커뮤니티 앱의 정보 밀도와 탐색성을 매칭 도메인(이동 도움 요청)에 옮겨 P0+P1 14개 항목 개선.

핵심 가치:
- **임박순 디폴트 정렬** + **거리·카테고리 필터** 로 "지금 도움 줄 수 있는 가까운 매칭"이 한눈에
- **봉사자 자격 보유자 전용 카테고리** 를 시각·권한으로 분리
- **본인 글 식별 + 끌어올림·수정** 액션을 카드 우상단 ⋮ 메뉴로 자연스럽게

## 2. 스코프

### 2.1 포함 (P0+P1 14항목)
**P0 (6)**
1. pill 탭 셀렉터 (전체 / 모집중 / 검토중 / 진행중 / 완료)
2. 정렬 드롭다운 (임박순 디폴트 / 최신 / 가까운)
3. 거리 필터 sticky 칩 (1km / 3km / 5km / 전체)
4. 본인 글 식별 — userId 기반 + 우상단 ⋮ + meta "· 내가 작성" (시안 A안)
5. 거리 표시 ("도보 8분 · 350m")
6. 임박도 배지 다단계 (6h / 24h / D-1)

**P1 (8)**
7. 카테고리 칩 가로 스크롤 (전체 + 5종)
8. 봉사자 전용 카테고리 시각 구분 (Green600 칩 + ic_award)
9. pull-to-refresh
10. 상단 floating pill "새 요청 N건"
11. 빈 상태 일러스트 + CTA 2개 (거리 넓히기 / 요청 작성)
12. 무한 스크롤 페이지네이션
13. 스켈레톤 카드 3장 로딩
14. 카드 카테고리 아이콘 (썸네일 60×60dp 카테고리 그라디언트)

### 2.2 미포함 (후속)
- 끌어올림(다시 알리기) — ⋮ 메뉴에 UI 자리는 두되 기능 disabled
- 작성자 신뢰 지표 (봉사 뱃지 노출)
- 장기 미해결 강조 stripe
- 키워드 검색 (TopBar 검색 아이콘은 자리만 두고 동작은 후속)
- 다중 카테고리 선택
- 필터/정렬 영속화 (SharedPreferences) — 초기에는 세션 유지만, 영속은 후속

## 3. 백엔드 의존성

전체 목록은 [`docs/backend-requests/2026-05-24-matching-revamp.md`](../../backend-requests/2026-05-24-matching-revamp.md) 참조. 요지:

| # | 필수도 | 항목 |
|---|---|---|
| §1.1 | P0 | `matches.category` 컬럼 + enum |
| §1.2 | P0 | 봉사자 전용 카테고리 작성 권한 검증 |
| §1.3 | P1 | `author_user_id` 응답 |
| §2 | P0 | `GET /matches` 에 `sort`/`category`/`max_distance`/`lat`/`lng` 쿼리 |
| §3 | P0 | `MatchListItem` 응답에 `category`/`latitude`/`longitude`/`distance_m` |
| §4 | P0 | `MatchCreateRequest` 본문에 `category` 필수 |

### 점진 적용 (백엔드 일부만 반영된 경우)
- `category` 응답 없음 → 카테고리 칩 "전체" 만 노출 + 카드 카테고리 아이콘 숨김
- `latitude/longitude` 없음 → 거리 표시 동(洞) 명만 폴백
- `sort=imminent` 미지원 → 현재 페이지 안에서만 클라이언트가 재정렬
- `author_user_id` 없음 → 닉네임 비교 fallback (현재 동작 유지)

## 4. 화면 구성 — MatchingScreen 재설계

### 4.1 TopBar
- 26sp ExtraBold "매칭" — 기존 유지
- 우측 액션 2개 (40×40dp 흰 카드 shadow 2dp radius 12dp, Brown700 tint):
  - `ic_search` (검색 — 이번 스코프에서는 진입 시 토스트 "곧 도착해요" + 동작 없음 또는 비활성 회색)
  - `ic_list` (내 요청 목록 — 기존 `Assignment` 자리)

### 4.2 Pill 상태 탭 (가로 스크롤)
- padding h=18 v=12(하단), gap 8dp
- 선택: bg=`#1A1A1A` text=White 13sp 700, padding h=16 v=7, radius 50
- 미선택: bg=White border 1dp `BorderBeige` text=`Brown700` 13sp 500
- 탭별 카운트 동반: "모집중 12" (선택된 status 의 결과 개수). 카운트 0 이면 숫자 생략. "전체"는 카운트 안 붙임.
- 탭 5종:
  - **전체** (status 필터 미지정)
  - **모집중** (RECRUITING)
  - **검토중** (REVIEWING)
  - **진행중** (IN_PROGRESS)
  - **완료** (DONE)

### 4.3 정렬 + 거리 행
- padding h=18 v=10, gap 6dp
- pill 디자인 (bg=White border 1dp BorderBeige radius 50 padding h=12 v=6, font 12sp 700):
  - **임박순 ▾** — 디폴트, 활성 시 border `Brown900`
  - **5km 이내 ▾** — 디폴트 5km
- 탭 시 `ModalBottomSheet` 열림 — 라디오 선택 + "적용" CTA
  - 정렬 옵션: 임박순 / 최신순 / 가까운순(위치 권한 없으면 disabled + 안내)
  - 거리 옵션: 1km / 3km / 5km / 전체

### 4.4 카테고리 칩 (가로 스크롤)
- padding h=18 v=14(하단), gap 6dp
- 6칩: 전체 / 산책동행 / 병원동행 / 장보기 / 이동 / 봉사
- 각 칩에 카테고리 아이콘 12dp inline + 라벨 12sp
- **봉사자 전용 카테고리** (병원동행, 봉사 — 협의 후 enum 확정): bg=`#DCFCE7` text=`#16A34A` border=1dp `#16A34A`
- 선택 시 bg=`#1A1A1A` text=White (봉사자 전용도 동일하게 검정 pill, 단 선택 안 된 상태에서만 Green 강조)
- 카테고리 enum 매핑 (클라이언트):
  - WALK → "산책동행" + `ic_paw`
  - VET → "병원동행" + `ic_stethoscope` (봉사자 전용)
  - SHOPPING → "장보기" + `ic_shopping_cart`
  - MOVE → "이동" + `ic_car`
  - VOLUNTEER → "봉사" + `ic_award` (봉사자 전용)

### 4.5 카드 디자인

```
┌──────────────────────────────────────────────┐
│  ┌────┐  [모집중] [봉사자] [오늘 16:30]      ⋮│
│  │ 🩺 │  정형외과 진료 동행 필요해요          │
│  └────┘  📍 정왕동 · 도보 22분  · 내가 작성  │
│          👥 신청 3명 (Pink500 굵게, 본인 글만)│
└──────────────────────────────────────────────┘
```

- 카드: White bg radius 16dp shadow 1dp, margin h=18 v=6
- **좌측 썸네일** 60×60dp radius 12dp (카테고리 그라디언트 + 흰 SVG 아이콘 28dp):
  - WALK: `OrangeSand → Orange500`
  - VET: `#DCFCE7 → #16A34A`
  - SHOPPING: `PinkSurface → Pink500`
  - MOVE: `#DBEAFE → #388AF5`
  - VOLUNTEER: `MintLight → Green600`
- **카드 row1 (칩들)**:
  - 상태 칩 (모집중/검토중/진행중/완료) — bg/fg 색 CLAUDE.md 패턴 따름
  - 봉사자 전용 카테고리면 "🏅 봉사자" 칩 (bg `#00A63E` text White + `ic_award`)
  - 임박도 배지 (6h/24h/D-1) — 4.6 참조
- **제목**: 15sp 800 TextBlack, 1줄 ellipsis
- **meta 라인 1**: 11sp Brown700
  - `ic_map_pin` 동 명 + "도보 N분" (또는 "1.2km" 1km 이상)
  - 본인 글이면 우측 끝에 "· 내가 작성" 11sp Brown700 600
- **meta 라인 2** (조건부): 신청자 수 11sp
  - 본인 글: `ic_users` "신청 N명" Pink500 800
  - 다른 사람 글: `ic_users` "신청 N" Brown700 500 (작게)
- **우상단 ⋮ 메뉴** (28×28dp tap area Brown700):
  - 본인 글: 수정 / 끌어올림(다시 알리기, 이번 스코프 disabled) / 삭제
  - 다른 사람 글: 공유하기 / 신고하기
  - ModalBottomSheet 로 표시
- **완료(DONE) 카드**: 전체 opacity 0.55 (당근 "거래완료" 패턴)

### 4.6 임박도 배지 (4.5의 row1 마지막 칩)

기준: `desired_date + desired_time` 이 현재 시각에서 얼마나 가까운지.

| 범위 | 라벨 | 배경 | 텍스트 |
|---|---|---|---|
| 과거 | (배지 없음) | — | — |
| 현재~6h | "마감 임박 · Nh" 또는 "마감 임박 · Nm" | `#FEE7EC` | `#F04268` |
| 6h~24h | "오늘 HH:MM" | `#FFEDD4` | `#F7A35B` |
| 24h~48h | "내일 · D-1" | `#F2F2F2` | `#8A6E58` |
| 48h~ | (배지 없음, 날짜만 카드 안쪽 표기) | — | — |

각 배지에 `ic_clock` 10dp inline. 폰트 10sp 700.

### 4.7 FAB (글쓰기)
- 기존 유지: 56dp `FABBrown(#9A7B5E)` radius 16 + `ic_plus` 24dp White
- 우하단 inset: right 20, bottom (BottomNav 위) — `MatchingScreen.kt` 기존 위치 유지

### 4.8 새 N건 floating pill (P1.⑩)
- pull-to-refresh 또는 onResume 후 신규 매칭이 있을 때 화면 상단(카테고리 칩 아래 12dp) 에 띄움
- 디자인: Brown900 bg radius 50 padding h=18 v=10 + `ic_refresh` 14dp + "새 요청 N건 · 탭하여 보기"
- 탭 시: LazyColumn scrollToItem(0) + 데이터 갱신 + pill 사라짐
- 자동 사라짐: 8초 후 또는 사용자 스크롤 시
- 새 매칭 감지 로직: refresh 응답의 `match_id` set 과 직전 set 차이

### 4.9 빈 상태 (P1.⑪)
- 96dp 원형 `OrangeSand` bg + `ic_empty_paw`(또는 `ic_paw`) 40dp `Orange500`
- "근처에 요청이 없어요" 16sp 800 TextBlack
- "거리를 더 넓게 보거나\n직접 요청을 작성해 보세요" 13sp Brown700 line-height 1.5
- CTA 2개 (가로 배치, gap 8dp):
  - "거리 넓히기" — Outline Brown900 (기존 거리 + 5km 자동 확장 또는 시트 열기)
  - "요청 작성" — Brown900 fill White text → FAB 와 동일 액션
- 빈 상태 조건: API 응답 items 비어 있고 로딩 끝났을 때

### 4.10 스켈레톤 카드 (P1.⑬)
- 최초 진입 또는 refresh 시 응답 도착 전 카드 3장 placeholder:
  - 썸네일 자리: `#F4F4F4` 60×60 radius 12
  - 라인 3개: 50% / 80% / 60% 너비, height 14dp/14dp/10dp, `#F4F4F4`, radius 6
- opacity 0.7 / 0.5 / 0.3 점진 fade (시안 톤)

### 4.11 봉사자 권한 안내 배너 (P1.⑧)
비봉사자 사용자가 봉사자 전용 카테고리 칩 선택 시 카테고리 칩 행 바로 아래에 노출:

- bg `#F0FDF4` border 1dp `Green600` radius 12 padding h=14 v=10 margin h=18 bottom=12
- 좌측 `ic_award` 20dp Green600
- 본문: 제목 "봉사자 전용 카테고리" 12sp 800 Green600 / 보조 "병원동행 요청은 봉사자 자격 보유자만 작성·신청할 수 있어요. 신청하려면 봉사자 자격 신청을 먼저 해 주세요." 11sp Brown700 line-height 1.5
- 우측 CTA "자격 신청" — bg Green600 White text 11sp 700 padding h=10 v=5 radius 8
  - 탭 시 `Screen.VolunteerApply` 라우트로 navigate
- 비봉사자 + 봉사자 전용 카테고리 선택 시에만. 카테고리 해제하면 사라짐.
- 카드는 그대로 노출 (탐색은 가능). 신청·작성 시 별도 차단(상세 화면에서 처리).

## 5. 데이터 모델 (클라이언트)

`MatchModels.kt` 확장:

```kotlin
enum class MatchCategory { WALK, VET, SHOPPING, MOVE, VOLUNTEER }

// MatchCategory 의 봉사자 전용 여부 판별
fun MatchCategory.requiresVolunteerRole(): Boolean = when (this) {
    MatchCategory.VET, MatchCategory.VOLUNTEER -> true
    else -> false
}

data class MatchListItem(
    val matchId: Int,
    val title: String,
    val address: String,
    val desiredDate: String,
    val desiredTime: String,
    val status: MatchStatus,
    val applicationsCount: Int,
    val authorNickname: String,
    val createdAt: String,
    // ── 신규 ──
    @SerializedName(value = "category", alternate = ["matchCategory"])
    val category: MatchCategory? = null,    // 백엔드 미지원 시 null
    @SerializedName(value = "author_user_id", alternate = ["authorUserId"])
    val authorUserId: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName(value = "distance_m", alternate = ["distanceM"])
    val distanceM: Double? = null,
)
```

기존 필드는 모두 유지 (이전 응답과의 backward compat).

## 6. ViewModel — `MatchingViewModel`

### 6.1 상태

```kotlin
enum class MatchSort { IMMINENT, RECENT, NEAREST }
enum class DistanceFilter { KM1, KM3, KM5, ALL }

sealed class MatchingUi {
    object Loading : MatchingUi()
    data class Success(
        val items: List<MatchListItem>,
        val statusTabCounts: Map<MatchStatus?, Int>,  // null = 전체
        val selectedStatus: MatchStatus?,
        val selectedCategory: MatchCategory?,
        val sort: MatchSort,
        val distance: DistanceFilter,
        val hasMore: Boolean,
        val newCount: Int,           // 새 N건 floating pill 카운트
        val isRefreshing: Boolean,   // pull-to-refresh 진행 중
    ) : MatchingUi()
    data class Error(val message: String) : MatchingUi()
}
```

### 6.2 액션
- `setStatus(status: MatchStatus?)` — 탭 선택
- `setCategory(category: MatchCategory?)` — 카테고리 칩 선택
- `setSort(sort: MatchSort)` — 정렬 시트 적용
- `setDistance(distance: DistanceFilter)` — 거리 시트 적용
- `refresh()` — pull-to-refresh
- `loadMore()` — 무한 스크롤
- `dismissNewCount()` — floating pill 닫기 또는 탭 → scrollToTop + refresh

### 6.3 거리 계산 / 위치 처리
- `LocationProvider.getLocationOrNull()` 결과로 lat/lng 보유 (이미 다른 화면에서 사용 중)
- 위치 권한 없거나 실패 → `sort = NEAREST` 비활성, `distance = ALL` 강제
- 정렬·필터 쿼리 호출 시 lat/lng 백엔드에 전달

### 6.4 임박도 / "도보 분" 계산 헬퍼

```kotlin
internal fun computeImminence(
    desiredDate: String,    // "yyyy-MM-dd"
    desiredTime: String,    // "HH:mm"
    now: Instant = Instant.now(),
): Imminence?

enum class Imminence { CRITICAL_6H, TODAY_24H, TOMORROW_D1 }
// 그 외 null

internal fun walkingMinutes(distanceM: Double): Int =
    (distanceM / 67.0).toInt().coerceAtLeast(1)  // 평균 보행 67m/min
```

### 6.5 새 N건 감지
- `refresh()` 호출 결과의 `matchId` set 과 직전 set 차이를 `newCount` 로
- 자동 트리거: onResume 마다 refresh (기존 동작 유지)

## 7. 본인 글 식별 (시안 A안)

- `authorUserId` 가 응답에 있으면 `authorUserId == currentUserId` 로 비교 (백엔드 §1.3 의존)
- 없으면 fallback: `authorNickname == myNickname` (기존 동작)
- 본인 글 UI 변경:
  - **좌측 stripe 제거** (이전 시안에서 제거됨)
  - 카드 meta 라인에 "· 내가 작성" 추가
  - 우상단 ⋮ 메뉴: 수정 / 끌어올림(disabled this scope) / 삭제
  - 신청자 카운트 Pink500 800 (기존 유지)

## 8. 카드 클릭 / ⋮ 메뉴 흐름
- **카드 본체 탭**:
  - 본인 글이면 `Screen.MatchingDetail.createRoute(matchId)` (기존)
  - 다른 사람 글이면 `Screen.MatchingPublicDetail.createRoute(matchId)` (기존)
- **⋮ 탭**:
  - 본인 글: ModalBottomSheet → "수정" → MatchingDetail / "끌어올림" disabled / "삭제" 확인 다이얼로그 → DELETE API
  - 다른 사람 글: ModalBottomSheet → "공유하기" Intent / "신고하기" (TBD, disabled this scope)

## 9. 라우트 / NavGraph
기존 라우트 변경 없음. `Screen.Matching` 의 onMyRequestsClick 만 유지.

## 10. 색·타이포·아이콘 토큰

CLAUDE.md 의 토큰 그대로:
- TextBlack `#1E120A` / Brown700 `#8A6E58` / Brown900 `#614B3A`
- OrangeSand `#FFEDD4` / Orange500 `#F7A35B`
- PinkSurface `#FEE7EC` / Pink500 `#F04268`
- Green600 `#00A63E` (봉사자 전용)
- StarYellow / BorderBeige / TagGray 등

신규 아이콘 (Lucide stroke vector drawable):
- `ic_paw` — 산책동행
- `ic_stethoscope` — 병원동행
- `ic_shopping_cart` — 장보기
- `ic_car` — 이동
- `ic_award` — 봉사 (이미 P5 에서 생성됨, 재활용)
- `ic_users` — 신청자 카운트
- `ic_clock` — 임박 배지 (이미 있음)
- `ic_chevron_down` — 정렬·거리 칩 (이미 있음)
- `ic_more_horizontal` — 카드 우상단 ⋮ (현재 `ic_more_vert` 있음 — 재활용 또는 신규)
- `ic_refresh` — 새 N건 pill (이미 있음)

총 **5개 신규 아이콘** (paw / stethoscope / shopping_cart / car / users), 나머지 재활용.

## 11. 에러 처리

| 상황 | 처리 |
|---|---|
| 401 토큰 만료 | 기존 `TokenAuthenticator` 위임 |
| 5xx / Network | "매칭을 불러오지 못했어요" 에러 상태 + "다시 시도" |
| 빈 응답 | 빈 상태 UI (§4.9) |
| 위치 권한 거부 | "가까운순" 비활성 + "정렬 사용을 위해 위치 권한이 필요해요" 안내 |
| 봉사자 권한 없는 사용자가 봉사자 전용 카테고리 매칭 작성 시 | 백엔드 403 응답 → 폼에서 "봉사자 자격 신청이 필요해요" + VolunteerApply 진입 CTA |

## 12. 테스트 전략

단위 테스트:
1. `computeImminence(date, time, now)` — 5h 전 → CRITICAL_6H, 12h 전 → TODAY_24H, 30h 전 → TOMORROW_D1, 72h 전 → null
2. `walkingMinutes(distanceM)` — 67m → 1, 500m → 7, 2000m → 29
3. `MatchCategory.requiresVolunteerRole()` — VET/VOLUNTEER true, 나머지 false
4. ViewModel `setCategory(VET)` 비봉사자 사용자 → `requiresVolunteerWarning = true` (UI 배너 노출)
5. 새 N건 감지 — diff set 정확
6. JSON 역직렬화 — 새 필드(category/authorUserId/distance_m) 있을 때 / 없을 때 default

UI 테스트: manual smoke (Compose UI test 인프라 미구비)
- pill 탭 선택 변화
- 정렬·거리 시트 선택 적용
- 카테고리 칩 선택 변화 (봉사자 전용 칩 → 비봉사자 안내 배너 노출)
- pull-to-refresh → 새 N건 pill → 탭 → scroll top
- 무한 스크롤 (3페이지 이상 fetch)
- 본인 글 ⋮ 메뉴 (수정/삭제) → API 흐름
- 빈 상태 CTA 두 개

## 13. 점진 적용 / 마이그레이션

백엔드 변경이 단계적으로 들어올 경우 클라이언트 가드:

- `category == null` (응답에서 빠짐) → 카테고리 칩 row 자체 숨김. 카드 카테고리 아이콘은 enum 추론 안 되므로 회색 그라디언트 + 일반 가방/사람 아이콘 폴백.
- `latitude/longitude == null` → 거리 표시 동(洞) 명만 ("정왕동" 만), "도보 N분" 부분 생략.
- `authorUserId == null` → nickname 비교 fallback.
- `GET /matches?sort=imminent` 응답이 정렬 안 됨 (서버가 무시) → 클라이언트가 현재 페이지 안에서만 재정렬.

## 14. 향후 (이번 스코프 외)

- 끌어올림(다시 알리기) — 24h cooldown, 백엔드 `/matches/{id}/bump` 엔드포인트 신설
- 작성자 봉사 뱃지 노출 (VolunteerBadgeList 데이터 재활용)
- 키워드 검색 — TopBar 검색 진입
- 필터·정렬 영속화 (SharedPreferences)
- 다중 카테고리 선택
- 장기 미해결 강조 (48h+ 신청자 0)
- 광고·홍보 자리 (당근식 — 분명한 구분으로)

## 15. Gotchas

- **카테고리 enum 백엔드 협의 필수** — 클라이언트 잠정 가정(WALK/VET/SHOPPING/MOVE/VOLUNTEER). 다르면 enum mapping 변경.
- **봉사자 전용 카테고리 정의** — 클라이언트 잠정 VET·VOLUNTEER. 백엔드 협의 후 `requiresVolunteerRole()` 매핑 조정.
- **임박도 계산의 timezone** — `desired_date + desired_time` 이 UTC 인지 KST 인지 백엔드 확인. KST 가정.
- **새 N건 pill 자동 사라짐 8초** — 사용자 스크롤 시 즉시 사라짐(중복 방지)
- **거리 계산 호출량** — 현재 페이지 매장마다 lat/lng 비교는 클라이언트 계산. 무한 스크롤로 100건 넘어도 부담 없음. 다만 백엔드 `distance_m` 응답이 있으면 그걸 우선 사용.
- **본인 글 ⋮ 끌어올림 disabled** — 이번 스코프 외이므로 메뉴 항목은 보이되 회색 + "곧 추가될 기능이에요" 토스트.
- **봉사자 카테고리 칩의 비봉사자 안내 배너** — 칩 색은 평소 Green border + 선택 시 검정 pill 로 통일. 안내 배너에서만 Green 강조.

---

## 부록 — 작업 단위 (구현 시 분리할 PR 후보)

writing-plans 에서 세부 도출. 대략적 묶음:

1. 데이터 모델 + ViewModel + 단위 테스트 (computeImminence / walkingMinutes / requiresVolunteerRole / new diff)
2. 카드 디자인 (썸네일 + 칩 + ⋮ 메뉴 + 임박 배지 + 본인 식별)
3. pill 탭 + 정렬·거리 시트 + 카테고리 칩
4. pull-to-refresh + 새 N건 pill + 무한 스크롤 + 스켈레톤
5. 빈 상태 + 봉사자 안내 배너 + 위치 권한 처리
6. 신규 5개 Lucide 아이콘 vector drawable
