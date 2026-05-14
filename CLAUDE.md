# 시흥가개 프로젝트 — Claude 공통 컨텍스트

나는 "시흥가개"라는 반려동물 커뮤니티 Android 앱을 Jetpack Compose로 개발 중이야.

## 기술 스택
- 언어: Kotlin + Jetpack Compose
- 최소 API: 26 이상
- 아이콘: Material Symbols (`androidx.compose.material:material-icons-extended`)
- 폰트: SUIT (또는 Pretendard)

---

## 컬러 팔레트

| 용도 | Hex |
|------|-----|
| Primary (브라운) | #6B4F3A |
| Primary Dark | #3E2A1A |
| Accent / Active | #F97316 (오렌지) |
| 강조 텍스트 (분홍) | #E84B6A |
| 산책지수 Good | #22C55E (초록) |
| 산책지수 Normal | #60A5FA (파랑) |
| 산책지수 Bad | #FB923C (주황) |
| Background | #FFFFFF |
| Card Surface | #FFFFFF (elevation 2dp) |
| Chip 미선택 배경 | #F5F0EB |
| 알림 읽지않음 배경 | #FFF0F3 |
| Tag 모집중 | 배경 #FECDD3 / 텍스트 #E84B6A |
| Tag 검토중 | 배경 #FEF9C3 / 텍스트 #CA8A04 |
| Tag 진행중 | 배경 #DCFCE7 / 텍스트 #16A34A |
| Tag 완료 | 배경 #F3F4F6 / 텍스트 #6B7280 |

---

## 공통 컴포넌트 규칙

- **카드**: `RoundedCornerShape(16.dp)`, elevation 2.dp
- **Primary 버튼**: `RoundedCornerShape(50.dp)`, 배경 #3E2A1A, 텍스트 흰색
- **Outline 버튼**: `RoundedCornerShape(50.dp)`, 배경 흰색, 테두리 #3E2A1A
- **Chip (선택됨)**: 배경 #1A1A1A, 텍스트 흰색
- **Chip (미선택)**: 배경 흰색, 테두리 #D4C4B8
- **BottomNavigationBar**: 5개 탭 — 홈, 매칭, 지도, 소식, 마이
  - 선택됨: 검정 pill 배경 + 아이콘 + 텍스트 (흰색)
  - 미선택: 아이콘만 (#C4A882)

---

## 아이콘 대응표 (Material Symbols)

| 기능 | 아이콘 이름 |
|------|------------|
| 홈 탭 | `Home` |
| 매칭 탭 | `Handshake` |
| 지도 탭 | `Map` |
| 소식 탭 | `Newsmode` |
| 마이 탭 | `AccountCircle` |
| 위치 | `LocationOn` |
| 알림 | `Notifications` |
| 즐겨찾기 (빈) | `FavoriteBorder` |
| 즐겨찾기 (채움) | `Favorite` |
| 설정 | `Settings` |
| 뒤로가기 | `KeyboardArrowLeft` |
| 공유 | `Share` |
| 북마크 | `BookmarkBorder` |
| 추가 | `Add` |
| 정렬/필터 | `SwapVert` |
| 검색 | `Search` |
| 전화 | `Call` |
| 별점 | `Star` |
| 시계/시간 | `Schedule` |
| 달력 | `CalendarMonth` |
| 반려동물 | `Pets` |

---

## 화면 목록 (Navigation Routes)

| 화면 | route |
|------|-------|
| 스플래시/로그인 | `splash` |
| 홈 | `home` |
| 알림 | `notification` |
| 매칭 | `matching` |
| 도움 요청하기 (3단계) | `request_flow` |
| 내 봉사 요청 목록 | `my_requests` |
| 지도 | `map` |
| 소식 | `news` |
| 장소 상세 | `place_detail/{placeId}` |
| 마이 | `my` |

---

## 코드 작성 규칙

- 모든 코드는 **실행 가능한 완성된 형태**로 작성
- `@Preview` 어노테이션 항상 포함
- data class, ViewModel, dummy data 포함해서 바로 실행되게
- 지도 영역은 실제 SDK 대신 **민트 그라디언트 Box로 placeholder** 처리
