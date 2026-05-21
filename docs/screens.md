# 시흥가개 — 화면별 스펙

CLAUDE.md의 전역 규칙(컬러·Typography·공통 컴포넌트)을 따른다. 여기는 화면별 레이아웃·문구·동작만 정의한다.

## 메인 탭

### [SplashScreen] 로그인 선택
- 배경: White, 중앙 로고(없으면 "시흥가개" 36sp ExtraBold Brown900)
- 슬로건: "우리 동네 반려동물을 위한 따뜻한 발걸음" 14sp Regular Brown700
- 하단 버튼 2개(세로 gap=12dp, hPad=24dp, bottom=40dp): Primary "로그인하기" / Outline "회원가입하기"

### [HomeScreen] 홈
- TopBar 좌: "안녕하세요, 댕댕이주인님"(12sp Brown700) + "시흥가개"(28sp Bold Brown900)
- TopBar 우: LocationOn + "정왕동" pill(border=BrownBorder, radius=50dp), Notifications 아이콘
- 산책지수: "오늘 산책지수"(14sp Medium Brown700) + "92점으로 좋아요"(36sp Bold, 점수만 색상)
  - 80↑ Green500 / 50–79 Blue400 / 0–49 Orange500
  - 날씨 "맑음 · 18° · 미세먼지 좋음" 12sp Gray
  - D-day 배너: PinkSurface, radius=50dp, "[D-2] 병원 이동 · 신청 2건 검토하기 >"
- 주변 매장: 헤더 "주변 매장 24곳" + "🗺 지도 보기"(Orange500)
  - 지도 Card height=180dp, 민트-하늘 그라디언트, 핀 4개, 좌하단 "정왕동" pill
  - 카테고리 LazyRow: 전체/카페/공원/병원/미용/식당
  - 장소 Row: 번호 원형(32dp, 1번=Pink500/2번↑=Brown900) + 장소명 16sp SemiBold + "업종 · 거리 · ⭐별점" + Favorite
- 반려동물 소식: 헤더 "반려동물 소식" + "📰 전체 보기"
  - 뉴스 카드: 이미지(80×80dp, radius=8dp) + 태그chip + 제목 15sp + 날짜·출처 12sp
  - 소식 아이템: [태그] + 제목 14sp + 날짜 12sp + Divider
  - 하단 배너 Card(PinkSurface): Handshake(Orange500) + "이동 지원 봉사자가 되어 보세요" + "신청 →" 버튼(Brown900)

### [MatchingScreen] 매칭
- TopBar: "매칭" 24sp Bold + Assignment 아이콘 카드버튼(40×40dp)
- 요약 카드 Row(gap=12dp, equal weight)
  - 내 요청(PinkSurface): Assignment(Pink500) + "내 요청" + "2건 검토 중" 16sp Bold
  - 봉사 활동(#F0FDF4): Favorite(Green500) + "봉사 활동" + "1건 진행 중"
- 탭 필터: 전체 8 · 모집중 5 · 검토중 2 · 진행중 1 · 완료 (선택=Bold Black, 미선택=Brown400)
- 섹션 헤더: "이동 지원 요청" + "🗺 지도 보기"
- 요청 카드(radius=16dp, elevation=2dp): 태그chip + "D-2" / 제목 16sp / 지역·거리·날짜 / 반려동물 chip + 신청수
- FAB: Brown900, Add, 56×56dp, radius=16dp, 우하단 margin=16dp

### [NewsScreen] 소식
- TopBar "소식" 24sp Bold
- 카테고리 LazyRow: 전체/정책/행사/봉사/지원
- 메인 뉴스 Card: 이미지 fullWidth height=160dp + 태그 + 제목 16sp SemiBold + 날짜·출처
- 소식 리스트: [태그] + 제목 14sp + 날짜 12sp Brown400, Divider 구분

### [MyScreen] 마이
- TopBar "마이" 24sp Bold + Settings 아이콘
- 프로필 Card(PinkSurface): 원형 이미지(56dp) + 닉네임 + LocationOn+지역명 + "편집" Outline
- 내 반려동물 Card: Pets(Orange500, 연오렌지 bg 40dp) + 이름 15sp Bold + "강아지·3살·수컷" + "전체 보기" Outline
- 활동 통계 Row(3개): 숫자 24sp(내요청=Pink500/봉사=Green500/즐겨찾기=Orange500) + 라벨 12sp
- 봉사 뱃지 Card: "🌱 봉사 등급" + 진행 표시 + ProgressBar(6dp, Orange500) + 뱃지 Row 4개(새싹/꽃/열매/나무)
- 내 기록: VolunteerActivism "봉사 활동 이력" / Favorite "즐겨찾기 매장"
- 설정 리스트: 알림 설정 / 지역 설정(정왕동) / 개인정보 및 보안 / 앱 정보(v3.0.0) / "봉사자 자격 신청"(Green500)
- 하단: "로그아웃" Outline 버튼

## 서브 화면

### [NotificationScreen] 알림
- TopBar: 뒤로가기 + "알림" + "모두 읽음"(우, 14sp Brown700)
- 필터 Chip: 전체/매칭/소식/시스템
- 아이템: 읽지않음=PinkSurface bg radius=12dp / 읽음=White + Divider
  - 좌 원형(44dp) PriorityHigh: 매칭=Pink500 / 봉사=Green500 / 소식=Orange500
  - 제목 16sp Bold + 내용 14sp 2줄 + 우상단 시간(12sp, "방금 전"=Pink500)

### [RequestFlowScreen] 도움 요청하기 (3단계)
- TopBar: 뒤로가기 + "도움 요청하기" + "1/3"
- 스텝 인디케이터: 3구간 선(두께 3dp), 완료/현재=Orange500, 미완료=Brown900 30%
  - 라벨: 반려동물 / 일정 / 요청 내용
- 질문: 26sp Bold Brown900, 서브 14sp Brown700

**Step 1 — 반려동물 선택**
- Grid 2열(gap=12dp), Card(radius=16dp)
- 아이콘 Box(100×100dp): 강아지(#FEF3E2 + Orange500) / 고양이(#FFE4E6 + Pink500)
- 선택됨: border=2dp Orange500 + 우상단 체크 뱃지 / 미선택: border=1dp BrownBorder
- 추가 카드: Gray bg, 점선 border, "반려동물 추가"
- 하단: "→ 일정 선택" Primary

**Step 2 — 일정 선택**
- 달력: "< 2026년 10월 >" 16sp, 일=Pink500/토=Blue400/평일=Brown900, 선택=#FFF3E0 원형
- 희망 시간 TextField(border=BrownBorder, Schedule 아이콘, placeholder "예: 오전 9:30")
- 빠른 선택 chip Grid 4열(09:00~20:00), 선택=#FFF3E0 bg + Orange500
- 하단: "→ 요청 내용 작성" Primary

**Step 3 — 요청 내용**
- 라벨 14sp SemiBold, 필드 border=1dp BrownBorder radius=12dp padding=16dp
- 제목 * / 목적지 *(LocationOn) / 메모 *(minHeight=120dp, "0/500" 카운터)
- 하단: "요청 등록하기" Primary

### [MyRequestsScreen] 내 봉사 요청 목록
- TopBar: 뒤로가기 + "내 봉사 요청" + "+ 새 요청" 버튼(Pink500, radius=50dp)
- 필터 Chip: 전체/매칭 전/매칭됨/종료됨
- 카드: MatchingScreen 카드 + "🗨 새 메시지 N"(Pink500), "봉사자 N명 신청" 우상단
  - 하단 위치 chip(LocationOn + 장소명, Brown100 bg)
  - 완료 카드: ⭐⭐⭐⭐⭐ + "후기 보기" 버튼(Brown100)

### [MapScreen] 지도
- 전체화면 placeholder: 민트-White 그라디언트 fillMaxSize, 핀 3개 + 파란 dot(내 위치, 16dp)
- 검색 Card 상단(radius=50dp, elevation=4dp, height=48dp): Search + "매장 · 병원 · 공원 검색"
- 카테고리 LazyRow: 전체/카페/공원/병원(선택=검정 / 미선택=White, elevation=2dp)
- 우측 FAB 3개(40×40dp, radius=12dp): MyLocation / Layers / Refresh
- 하단 BottomSheet(radius topStart/End=20dp): 드래그 핸들 + "주변 매장 24곳" + "↕ 거리순" + 장소 LazyColumn

### [PlaceDetailScreen] 장소 상세
- TopBar: 뒤로가기 + BookmarkBorder + Share(우, 카드버튼)
- 상단 배너: 민트-연두 그라디언트, height=200dp
- 장소 정보: 장소명 28sp Bold Brown900 / 업종·거리 14sp / ⭐(Gold) + "4.9" 22sp + "(후기 128)"
- 정보 Row 3개: 아이콘 Box(44×44dp, border=1dp BrownBorder) + 라벨 12sp + 내용 14sp Bold
  - 우측 "영업 중" 초록 pill / "복사" Outline 버튼
- 위치 Card(radius=16dp, height=160dp): 민트 그라디언트 + 핑크 핀 + 장소명 pill
- 리뷰 섹션: "4.8" 28sp + ⭐⭐⭐⭐⭐ + "총 128개의 리뷰"
  - 리뷰 Card: 아바타 원형(36dp) + 닉네임 14sp + ⭐ + "N일 전" + 내용 14sp

### [MatchingDetailScreen] 내 봉사 상세 (요청자 시점)
- TopBar: 뒤로가기 + "내 봉사 상세" + MoreVert + Share
- 상태 배너(PinkSurface, fullWidth, padding=16dp): "봉사자 모집 중 · 신청 2건" + "D-2"(Orange500)
- 요청 정보 Card: 제목 20sp Bold (2줄까지), 정보 Row 5개(Divider 구분)
  - CalendarMonth(#FFE4E6) 일정 / Schedule(#FFF3E0) 시간 / LocationOn(#F0FDF4) 목적지 / Pets(#FEF3E2) 반려동물 chip(BorderBeige) / ChatBubble(#F4F4F4) 요청 메모
- 경로 Row: LocationOn + "출발지" → "도착지" + 거리(우)
- 경로 지도 Card(height=160dp, 민트 그라디언트): 핀 연결선, 좌하단 "🗺 지도에서 보기"
- 신청한 봉사자 섹션: "👥 신청한 봉사자" + "N명"(Orange500)
- 봉사자 카드: 아바타(44dp) + 닉네임 16sp + ⭐별점 + "봉사 N건" + "📍N.Nkm"
  - 메모 14sp(없으면 "메모 없음" Gray)
  - 하단 버튼 Row: "채팅" Outline / "✓ 수락" Primary(Pink500) / "거절"(border/text=Pink500)

### [MatchingPublicDetailScreen] 이동 지원 요청 (봉사자 시점)
- TopBar: 뒤로가기 + "이동 지원 요청" + BookmarkBorder + Share
- 상태 배너 / 요청 정보 Card / 경로 + 지도 Card: MatchingDetailScreen과 동일
- 지도 위 오버레이 Row: ChatBubble 아이콘 버튼(White, 40×40dp) + "봉사 신청하기" Primary(Pink500, fullWidth)
- 요청자 정보 Card: 아바타(44dp Green500) + 닉네임 16sp + "⭐4.9 · 요청 6건 · 📍1.2km" + "🗨 채팅" Outline

### [ChatScreen] 채팅
- TopBar: 뒤로가기 + 아바타(36dp Green500) + 닉네임 16sp + 서브 "⭐4.9 · 봉사 12건 · 1.2km"
  - 우측 "수락" 버튼(Pink500, radius=50dp) — 상황에 따라
- 요청 미리보기 Card(상단, PinkSurface, radius=12dp): Handshake(Orange500) + 제목(말줄임) + "검토 중" 태그 + 날짜·목적지
- 날짜 구분자: "오늘" 12sp Gray, Divider 양옆
- 메시지 버블
  - 상대(좌): 연두(#F0FDF4) bg, radius=topStart=4dp/나머지 16dp, 아바타(32dp) + 버블 + 시간
  - 나(우): Brown900 bg, White 텍스트, radius=topEnd=4dp/나머지 16dp, 시간 + 버블
  - padding=12dp, 텍스트 15sp, maxWidth=75%
- 입력창: TextField(radius=50dp, border=1dp Gray300, placeholder "메시지를 입력하세요...") + Send 버튼(Brown900 원형, 40×40dp)

### [SettingsScreen] 설정
- TopBar: 뒤로가기 + "설정"
- 섹션별 Card(radius=16dp, elevation=1dp) + 라벨 14sp SemiBold Brown700
- 계정: 프로필 편집 / 반려동물 정보 / 봉사 이력("3건" Orange500)
- 알림: 매칭 알림(Switch 켜짐) / 공지 알림(켜짐) / 리뷰 알림(꺼짐)
- 앱: 위치 설정("정왕동") / 버전("1.0.0", 클릭 불가)
- 기타: 도움말 / 개인정보 처리 방침 / 로그아웃(Pink500, KeyboardArrowRight 없음)
- Switch: 켜짐=Brown900 bg / 꺼짐=Gray300 bg, thumb=White

### [PetListScreen] 내 반려동물 목록
- TopBar: 뒤로가기 + "내 반려동물" + MoreVert(우, 카드버튼)
- 서브헤더: "내 반려동물 N마리"
- Card(radius=16dp): 각 동물 Row(padding=16dp, 사이 Divider)
  - 아이콘 Box(56×56dp, radius=16dp): 강아지=OrangeSand+Orange500 / 고양이=PinkSurface+Pink500
  - 이름 16sp Bold + "강아지 · 말티즈 · 3살 · 3.2kg" 13sp + 우측 MoreVert

### [PetAddScreen] 반려동물 추가
- TopBar: 뒤로가기 + "반려동물 추가"
- 미리보기 Card(OrangeSand): Pets(60dp, White bg, Orange500) + "미리보기" + 이름(기본 "이름을 입력하세요") + "강아지 · 1살 · 수컷"(실시간)
- [기본 정보] Card: 이름 TextField / 종류 chip(강아지/고양이/기타) / 품종(선택) TextField
- [상세 정보] Card: 나이([-][숫자][+] + 개월/살 chip) / 성별(암컷/수컷) / 중성화 Switch
- 특징 및 주의사항: multiline TextField(minHeight=100dp, "0/300")
- 하단: "저장하기" Primary

### [VolunteerApplyScreen] 봉사자 자격 신청
- TopBar: 뒤로가기 + "봉사자 자격 신청"
- 제목: "반려동물과 함께하는" / "봉사자로 활동해보세요"(24sp Bold, "봉사자"=Green600)
- "제목 *" TextField(border=BorderBeige)
- "신청서 *" multiline TextField(minHeight=120dp, "0/500")
- 하단: "신청하기" 버튼(Green600 bg, White, radius=12dp, fullWidth, height=56dp)

### [NewsDetailScreen] 소식 상세
- TopBar: 뒤로가기 + BookmarkBorder + Share
- 헤더(연살구-연분홍 그라디언트, padding=24dp): 카테고리 태그chip(Orange500) + 제목 28sp Bold(2줄) + "출처 · 날짜"
- 해시태그 Row: chip(border=BrownBorder, radius=50dp, 13sp Brown700)
- 본문 15sp Regular, lineHeight=24sp / 이미지 fullWidth height=200dp radius=12dp
- 정보 박스 Card(bg=Background): bullet 리스트 14sp Regular
- 관련 소식: Card(PinkSurface) + PriorityHigh(Orange500, 36dp 원형) + 카테고리 + 제목 14sp

### [MapFilterBottomSheet] 지도 필터 모달
- ModalBottomSheet(radius topStart/End=20dp, dragHandle)
- 헤더 Row: "지도 보기 설정" 18sp SemiBold + X 닫기 + Divider
- 카테고리 항목 5개(Row padding=16dp, Divider): 아이콘 Box(40×40dp, Gray300) + 이름 16sp + Checkbox(선택=Orange500+체크)
  - 카페(LocalCafe) / 식당(ForkSpoon) / 공원(Forest) / 동물병원(HealthCross) / 미용(ContentCut)
- 하단: "적용하기" Primary(Brown900, fullWidth)
