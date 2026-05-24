# UI/UX 50개 개선 마스터 로드맵

**작성일**: 2026-05-24
**소스 리뷰**: 50개 결함·개선 기회 (UI 디자인 크리틱 + Explore 2회)
**총 예상 작업량**: ~83시간 (10-12 dev days)
**산출 PR 개수**: 8 (phase별)

## 1. 핵심 결정

### 1.1 왜 한 plan이 아닌가
50개를 단일 plan에 넣으면:
- PR이 너무 커서 리뷰 불가
- task-level rollback 불가
- 의존성 충돌 (예: countUp 컴포넌트 만들기 전에 사용처 작성)
- 데모 시연 기회 상실 (phase별로 점진 폴리시 노출 가능)

### 1.2 Phase 의존성 원칙
1. **Phase 0 — Critical Quick Fixes** (foundation 없이 가능, 즉시 가치)
2. **Phase 1 — Design System Foundation** (이후 모든 phase의 빌딩 블록)
3. **Phase 2-6 — Foundation 활용한 폴리시** (phase 간 독립)
4. **Phase 7-8 — 외부 의존성·큰 인프라** (리스크 큼, 별도 분리)

### 1.3 데모 노출 우선순위
**데모 시연자가 가장 먼저 보는 동선**: Splash → Home(첫 화면) → Map → PlaceDetail → My → 채팅/매칭. 이 동선에 등장하는 화면을 phase 2-3에 우선 배치.

---

## 2. Phase 구조

### Phase 0 — Critical Quick Fix Bundle (~6h)
**목적**: 데모/실사용에서 신뢰 깨지는 버그·위계 오류 즉시 정리. foundation 작업 없이 가능한 16개.

| # | 화면 | 결함 | 작업 시간 |
|---|---|---|:--:|
| 17 | Map | SearchCard placeholder Brown400 → Brown700 | 5m |
| 18 | Home | StoreItem 텍스트 위계 (제목 14sp Bold ↔ 서브 16sp SemiBold) 역전 수정 | 10m |
| 23 | SignUp | 비번 힌트 Row → FlowRow | 10m |
| 25 | Chat | `imePadding()` 추가 | 5m |
| 27 | Chat | "⭐ 4.9" 하드코딩 제거, 실 데이터 or 숨김 | 30m |
| 29 | Notif | 읽음·미읽음 padding 통일 (16dp) | 10m |
| 30 | Notif | 탭 검정 → Brown900 통일 | 10m |
| 32 | PetAdd | 나이 상한 (살 30, 개월 36) | 15m |
| 35 | RequestFlow | 과거 날짜 비활성 (`isBefore(LocalDate.now())`) | 30m |
| 38 | MatchDetail | 상태 영문 → 한국어 mapping | 20m |
| 41 | Settings | Switch uncheckedTrack 대비 강화 | 5m |
| 42 | Settings | 탈퇴 시트 `imePadding()` | 10m |
| 43 | NewsDetail | 북마크 onClick 빈 람다 → 로컬 토글 + 아이콘 변화 | 45m |
| 44 | VolunteerApply | "제목" 필드 제거 또는 안내 추가 | 15m |
| 46 | PetHotelCompare | 가격 `₩…/박` 단위 표시 | 20m |
| 47 | Global | AlertDialog 폰트 누락 → `SiheungAlertDialog` wrapper 생성 + 마이그레이션 | 1h 30m |

**산출물**: 단일 PR `fix: phase 0 — critical UI 버그 일괄 수정`
**테스트**: 빌드 + 수동 시각 확인 (각 항목 before/after 1장)
**리스크**: 낮음

---

### Phase 1 — Design System Foundation (~8h)
**목적**: 다음 phase 들이 사용할 재사용 컴포넌트·토큰 정비.

| 작업 | 산출 파일 | 시간 |
|---|---|:--:|
| Dimens 토큰 (#20) | `ui/theme/Dimens.kt` — `screenH`, `cardPad`, `sectionGap`, `iconS/M/L`, `radiusS/M/L` | 30m |
| ShimmerBox 컴포넌트 (#7 기반) | `ui/component/ShimmerBox.kt` — Brush translateX 1.2s | 1h |
| CountUpText 컴포저블 (#1, #8 재사용) | `ui/component/CountUpText.kt` — `animateIntAsState` 래퍼 | 30m |
| AsyncImageWithFallback (#10, #26, #31) | `ui/component/AppAsyncImage.kt` — placeholder/error 일관 처리 | 1h |
| EmptyStateView (#13, #28) | `ui/component/EmptyStateView.kt` — 원형 아이콘 배경 + 제목 + 부제 + 선택적 CTA | 1h |
| SiheungBottomSheetHandle (#45) | `ui/component/SheetHandle.kt` — 32dp×4dp 베이지 인디케이터 | 15m |
| StatusLabels (#38 확장) | `ui/util/StatusLabels.kt` — `String.toStatusLabel()` etc. | 30m |
| CategoryVisual (#3, #37, Map 작업의 중복 제거) | `ui/util/CategoryVisual.kt` — emoji/color/gradient 일괄 매핑 | 1h |
| SiheungAlertDialog (#47에서 도출) | `ui/component/SiheungAlertDialog.kt` — Pretendard 강제 적용 | 30m |
| **단위 테스트** | `StatusLabelsTest`, `CategoryVisualTest` | 1h |

**산출물**: 단일 PR `feat: design system foundation`
**테스트**: 컴포넌트 단위 테스트 + Preview 모두 추가
**리스크**: 낮음 (신규 파일만, 기존 화면 영향 없음)

---

### Phase 2 — Home & My Hero (~10h)
**목적**: 데모 첫 화면(Home, My 탭)에서 즉시 wow 모먼트 발생.

| # | 작업 | 의존 | 시간 |
|---|---|---|:--:|
| 1 | 산책지수 카운트업 (0 → walkScore, 1.2s) | Phase 1 CountUpText | 1h |
| 5 | WalkIndex 타이포 위계 (레이블 14sp Medium, 점수 52sp ExtraBold) | — | 30m |
| 8 | My StatCard 카운트업 + 아이콘 3종 (요청·봉사·즐겨찾기) | Phase 1 CountUpText | 1h 30m |
| 10 | Home PetNewsSection 메인 카드 AsyncImage 적용 | Phase 1 AppAsyncImage | 1h |
| 12 | My ProfileCard 그라디언트 + 발바닥 워터마크 + "{dong}에서 활동 중" | — | 1h 30m |
| 15 | Home 미니맵 클릭 피드백 (`appleTapScale` + haptic + 우상단 CTA pill) | — | 1h |
| 19 | My VolunteerBadge 진행바 10dp + 퍼센트 overlay + 달성/미달성 차별화 | — | 2h |
| **수동 검증** | Home·My 진입 영상 캡처 (gif) | | 30m |

**산출물**: PR `feat: home·my hero & 카운트업 폴리시`
**리스크**: 낮음 (UI 표면만)

---

### Phase 3 — Detail Hero & Card Interaction (~10h)
**목적**: 매장 상세, 매칭 카드의 시각 깊이.

| # | 작업 | 의존 | 시간 |
|---|---|---|:--:|
| 3 | Map StoreDetailSheet 히어로: 카테고리 그라디언트 + 대형 아이콘 (72dp) | Phase 1 CategoryVisual | 1h 30m |
| 4 | PlaceDetail 히어로 강화: 160dp → 220dp, 워터마크 패턴 (0.12 alpha) + 전경 아이콘 | Phase 1 CategoryVisual | 2h |
| 14 | PlaceDetail LocationCard 미니맵 100dp → 160dp + "지도에서 보기 →" pill | — | 1h |
| 6 | Matching MatchCardR `appleTapScale` + haptic | — | 30m |
| 33 | MatchReview 별점 단계별 이모지 + 텍스트 + scale punch | — | 2h |
| **고급**: 매장 즐겨찾기 하트 폭발 (scale 1→1.5→1 + 미니 파티클 3개) | — | 1h 30m |
| **수동 검증** | 캡처 + 영상 | | 1h 30m |

**산출물**: PR `feat: 매장·매칭 상세 hero 폴리시`
**리스크**: 낮음~중간 (PlaceDetail hero는 기존 layout 영향 있음, regression 주의)

---

### Phase 4 — Brand Entry (Splash·Login·SignUp) (~7h)
**목적**: 첫 진입 화면 (스플래시·로그인·회원가입) 브랜드 임팩트.

| # | 작업 | 의존 | 시간 |
|---|---|---|:--:|
| 2 | Splash 진입: OrangeSand 배경 + 로고 scale 0.7→1.0 spring + 슬로건 fade-in + 하단 LinearProgressIndicator | — | 2h |
| 11 | Login 헤드라인: 상단 1/3 OrangeSand 그라디언트 + 로고 + "🐾" 슬로건 추가 | — | 1h |
| 21 | SignUp 거주지 드롭다운: 14개 동 칩 자동완성 (`SiheungRegions` 활용) | — | 1h 30m |
| 22 | SignUp 약관 키워드 링크: `buildAnnotatedString` + 약관 ModalBottomSheet | — | 1h 30m |
| **수동 검증** | 진입 영상 | | 1h |

**산출물**: PR `feat: 브랜드 진입 화면 폴리시`
**리스크**: 낮음

---

### Phase 5 — Empty States · Skeletons · Filters (~7h)
**목적**: 화면 전반 빈 상태·로딩·필터 일관성 적용 (Phase 1 컴포넌트 활용).

| # | 작업 | 의존 | 시간 |
|---|---|---|:--:|
| 13 | Map 빈 상태를 `EmptyStateView`로 교체 | Phase 1 | 20m |
| 28 | Notif 빈 상태 아이콘 → `ic_notifications_off` + `EmptyStateView` | Phase 1 | 20m |
| 45 | MapFilter dragHandle 커스텀 (`SheetHandle`) | Phase 1 | 10m |
| 9 | News CategoryFilter pill 스타일 (Map/Matching 동일) | — | 1h |
| 16 | News SearchBar 실동작 (MapSearchOverlay 패턴 복사 → 제목 필터) | — | 2h |
| **ShimmerBox 적용** | Matching/Home/Map/News/Favorite/My 6 화면에 기존 회색 박스 교체 | Phase 1 | 2h 30m |
| **수동 검증** | 각 화면 로딩·빈 상태 캡처 | | 30m |

**산출물**: PR `feat: 빈 상태·로딩·필터 일관성`
**리스크**: 중간 (6개 화면 교차 변경)

---

### Phase 6 — Lists & Interactions (~10h)
**목적**: 리스트 표시·상호작용 폴리시.

| # | 작업 | 의존 | 시간 |
|---|---|---|:--:|
| 24 | Chat 시간 포맷 컨텍스트 분기 (오늘 = `HH:mm`, 그 외 = `M.d HH:mm`) | — | 30m |
| 26 | Chat 상대 아바타 실 이미지 (UiState.opponentProfileUrl 추가) | Phase 1 AppAsyncImage | 1h 30m |
| 31 | PetList 반려동물 사진 표시 (PetResponse.photoUrl 활용) | Phase 1 AppAsyncImage | 1h |
| 36 | Favorite 즐겨찾기 해제 Undo Snackbar | — | 1h 30m |
| 37 | Favorite 썸네일 카테고리별 그라디언트 | Phase 1 CategoryVisual | 1h |
| 39 | MatchDetail 수락 버튼 로딩 + Snackbar | — | 1h 30m |
| 40 | ProfileEdit 저장 성공 Snackbar 1s + onBack | — | 1h |
| **수동 검증** | | | 2h |

**산출물**: PR `feat: 리스트·상호작용 폴리시`
**리스크**: 중간

---

### Phase 7 — External API Integration (~6h)
**목적**: 더미 데이터 → 실제 API 연동.

| # | 작업 | 의존 | 시간 |
|---|---|---|:--:|
| 34 | RequestFlow 장소 검색 Kakao Local API (`/v2/local/search/keyword.json`) 연동, debounce 300ms | — | 4h |
| **테스트** | Kakao Local API 단위 테스트 (Mock OkHttp) | | 1h |
| **수동 검증** | 검색 영상 | | 1h |

**산출물**: PR `feat: 도움요청 장소 검색 Kakao API 연동`
**리스크**: 중간 (외부 API 키 관리·rate limit)
**선행 작업**: `local.properties`에 Kakao REST API key 추가 (이미 Map SDK key 있음, REST key 별도 필요할 수 있음)

---

### Phase 8 — Infrastructure (Large) (~30h, 선택적)
**목적**: 큰 영역 인프라 — 점진 마이그레이션 권장. 데모 임박이면 phase 8을 stretch goal 처리.

#### Phase 8a — Pull-to-Refresh (#48, ~4h)
Material3 `PullToRefreshBox`를 NotificationScreen, FavoriteStoresScreen, MatchingScreen, NewsScreen에 적용. ViewModel `refresh()` 메서드 통일.

#### Phase 8b — 다크 모드 (#49, ~12h)
1. `Color.kt`에 dark 변형 토큰 정의
2. `Theme.kt`에 `darkColorScheme` 구성
3. 주요 화면 6개 (Home, Map, PlaceDetail, Matching, My, Splash)부터 `MaterialTheme.colorScheme.surface/onSurface` 점진 적용
4. 나머지 화면은 fallback로 자동 반전 시도

#### Phase 8c — Shared Element Transition (#50, ~14h)
1. Compose 1.7+ `SharedTransitionLayout` 도입 검증 (현재 Compose 버전 확인 필요)
2. Home → PlaceDetail hero 카드 1건만 우선 적용
3. 검증 후 Matching → MatchingDetail 카드 적용

**리스크**: 큼 — 회귀 가능성 높아 각 sub-phase별 별도 PR + 베타 테스트 권장.

---

## 3. 글로벌 의존성 그래프

```
Phase 0  →  (독립)
Phase 1  →  필요 컴포넌트 사전 제작
   ↓
Phase 2, 3, 5, 6  →  Phase 1 컴포넌트 활용
Phase 4  →  (독립, Phase 1 없이도 가능하나 권장)
Phase 7  →  (외부 API, 독립)
Phase 8  →  대규모 인프라, 모든 phase 후 권장
```

병렬 가능성: Phase 2 ↔ 3 ↔ 4 ↔ 7 은 서로 독립이라 동시 작업 가능 (별도 브랜치).

## 4. 진행 방식

각 phase는:
1. `docs/superpowers/plans/2026-05-24-ui-ux-phase-N-<name>.md` plan 작성
2. subagent-driven-development로 task 단위 구현 + 2단계 리뷰
3. 커밋 후 다음 phase
4. 각 phase 완료 후 최종 cumulative 리뷰

## 5. 리스크·완화

| 리스크 | 완화 |
|---|---|
| Phase 1 컴포넌트가 phase 2-6 요구와 안 맞음 | Phase 2의 첫 task에서 컴포넌트 사용 검증, 필요 시 Phase 1 fix 우선 |
| 50개 작업 중 회귀 발생 | phase별 작은 PR로 bisect 용이 |
| 폰트·색·스타일 일관성 깨짐 | Phase 1의 `SiheungAlertDialog`, `CategoryVisual` 등 wrapper로 강제 |
| 데모 일정 임박 | Phase 0+2+3 까지가 최소 데모 만족도. 그 뒤는 stretch |

## 6. 데모 최소 라인

만약 데모 D-7일이라면:
- D-7~D-6: Phase 0 + Phase 1
- D-5~D-4: Phase 2
- D-3~D-2: Phase 3
- D-1: 캡처·영상 만들기

Phase 4~8은 데모 후 진행.

## 7. 시작 명령

이 문서 승인 후 **Phase 0 plan 작성 → subagent-driven 실행**으로 즉시 진입.
