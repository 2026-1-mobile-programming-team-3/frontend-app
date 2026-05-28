# PlaceDetail UI 개선 + 펫호텔 가격표

**작성일:** 2026-05-24  
**대상 파일:** `app/src/main/java/com/example/siheunggagae/ui/screen/PlaceDetailScreen.kt`  
**관련 모델:** `data/model/MapModels.kt`, `data/model/PetHotelModels.kt`

---

## 배경

PlaceDetailScreen 의 상단 배너가 단순 그라디언트 + 아이콘으로만 구성돼 있고, 매장명·별점이 배너 아래 별도 블록에 나뉘어 있어 정보 밀도가 낮다. 펫호텔 카테고리(`PET_HOTEL`)의 경우 백엔드가 `plans` 필드를 이미 내려주고 있으나 클라이언트 모델에 매핑이 없어 가격 정보를 표시하지 못하고 있다.

---

## 변경 범위

### 1. 데이터 모델 수정 (`MapModels.kt`)

`StoreDetailResponse`에 두 필드 추가:

```kotlin
@SerializedName("category")
val category: String? = null,

@SerializedName("plans")
private val _plans: List<PetHotelPlan>? = null,
```
```kotlin
val plans: List<PetHotelPlan> get() = _plans ?: emptyList()
```

- `PetHotelPlan`은 `PetHotelModels.kt`에 이미 존재하므로 재사용
- `category`는 `StoreCategory.apiValue`(`"PET_HOTEL"` 등) 문자열과 동일

---

### 2. PlaceDetailScreen 레이아웃 구조

기존의 "배너 → 기본정보 블록 → 지도 카드 → 후기" 순서를 아래로 재편:

```
[히어로 헤더]
  - statusBarsPadding 포함, 높이 160dp
  - 배경: 기존 민트 그라디언트 유지 (MapMintPL → #B2DFBF → #D8F2DC)
  - 다크 오버레이: to-top 그라디언트 (rgba(30,18,10,0.50) → transparent)
  - 플로팅 버튼 (좌: 뒤로, 우: 즐겨찾기·공유) — 기존 카드 스타일 유지
  - 오버레이 위 콘텐츠 (좌하단 정렬):
      · 카테고리 배지 — 반투명 pill (White 25% + border 50%)
      · 매장명 — 22sp ExtraBold White
      · 별점 행 — 별 아이콘(StarYellowPL) + 숫자 + 후기 수 + 영업 상태 pill

[섹션 카드 스택]  ← LazyColumn items, 카드 간 8dp 간격, 좌우 margin 12dp
  카드 1. 💰 요금 플랜    (조건부: category == "PET_HOTEL" && plans.isNotEmpty())
  카드 2. 📍 위치 & 연락처
  카드 3. 💬 후기
```

공통 카드 스타일:
- `RoundedCornerShape(16.dp)`, bg=White, elevation=2dp
- 헤더 행: 제목(14sp Bold TextBlack) + 우측 액션 텍스트(선택)
- 헤더 아래 1dp 구분선 (DividerPL)

---

### 3. 요금 플랜 카드 (`PET_HOTEL` 전용)

**표시 조건:** `s.category == "PET_HOTEL" && s.plans.isNotEmpty()`

**구성:**
- 헤더: `"💰 요금 플랜"` (좌) / 플랜 수 카운트 `"N개"` (우, Brown700)
- 플랜 행: `plans.sortedBy { it.displayOrder ?: 0 }` 적용 후:
  - 항상 첫 3개 표시
  - 4개 이상이면 `"+ N개 더 보기"` 텍스트 버튼 (Brown900, SemiBold 13sp)
  - 버튼 클릭 → 전체 펼침 (접기 기능 없음)
- 각 플랜 행:
  - 좌: `plan.planName` (13sp Medium TextBlack)
  - 우: `"%,d원".format(plan.priceKrw)` (13sp Bold Brown900)
  - 행 배경: Gray100PL (`#F4F4F4`), radius=10dp, vertical padding=9dp
- 최하단 캡션: `"* 가격은 업체 등록 기준이며 실제와 다를 수 있습니다."` (11sp Normal Brown400)

---

### 4. 위치 & 연락처 카드

기존 정보 행 + 지도 + 액션 버튼을 하나의 카드로 통합:

- **지도 플레이스홀더**: 높이 100dp, `RoundedCornerShape(12.dp)`, 민트 그라디언트 — 기존 MapView 임베드 동일하게 유지
- 지도 아래 주소 텍스트 (13sp Normal)
- 영업시간 행 (아이콘 + 텍스트 + 영업상태 chip)
- 전화번호 행 (아이콘 + 텍스트)
- 액션 버튼 3개: `📞 전화` / `🧭 지도에서 보기` / `📋 주소 복사` — Outline 버튼 스타일, `RoundedCornerShape(10.dp)`

---

### 5. 후기 카드

기존 후기 섹션과 동일한 로직, 카드 래퍼만 추가:

- 헤더: `"💬 후기"` + 카운트 / 우측 `"후기 쓰기 +"` (Pink500)
- 별점 요약 행 유지 (28sp 별점 숫자 + 5스타 시각화)
- 리뷰 카드 목록 (기존 3개 → 더 보기 +5 패턴 유지)
- 리뷰 작성 BottomSheet — 변경 없음

---

### 6. 제거 항목

- 기존 "장소 기본 정보" 흰 블록 (히어로로 통합)
- `HorizontalDivider` 섹션 구분자 (카드 카드 간격으로 대체)
- 기존 단독 "내 매장 정보 수정" / "이 매장 클레임하기" 버튼 블록 → 위치 & 연락처 카드 하단으로 흡수 (조건부)

---

## 미구현 / 보류

- 실제 사진 갤러리 (`photoUrls`) — 이번 범위 외, 히어로는 계속 그라디언트 플레이스홀더
- 성수기 가격 토글 — `PetHotelPlan` 구조에 해당 필드 없음, 보류

---

## 구현 체크리스트

- [ ] `StoreDetailResponse`에 `category`, `plans` 필드 추가
- [ ] 히어로 헤더 리팩터 (오버레이 + 매장명/별점/배지 이동)
- [ ] 요금 플랜 카드 컴포저블 작성 (`PlanCard`)
- [ ] 위치 & 연락처 카드 컴포저블 작성 (`LocationCard`)
- [ ] 후기 카드 래퍼 적용 (기존 로직 유지, 카드 감싸기)
- [ ] `LazyColumn` 아이템 순서 재조정
- [ ] `@Preview` 업데이트 (펫호텔 더미 데이터 포함)
