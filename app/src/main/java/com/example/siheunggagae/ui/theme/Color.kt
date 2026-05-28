package com.example.siheunggagae.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary: Brown (브라운) ────────────────────────────────────────────────────
val Brown10  = Color(0xFF3D1F00)
val Brown20  = Color(0xFF5C2E00)
val Brown40  = Color(0xFF7B4F2E)   // Primary
val Brown60  = Color(0xFFA0522D)   // Body text accent
val Brown80  = Color(0xFFC49A78)
val Brown90  = Color(0xFFF5E6D8)   // primaryContainer

// ── Secondary: Pink (핑크) ────────────────────────────────────────────────────
val Pink10   = Color(0xFF5E0020)
val Pink40   = Color(0xFFE91E63)   // D-day 강조, 신청 버튼
val Pink60   = Color(0xFFFF6B8A)   // 위치 핀 accent
val Pink80   = Color(0xFFFFB3C1)
val Pink90   = Color(0xFFFFF0F3)   // secondaryContainer (연분홍 배경)

// ── Tertiary: Orange (오렌지) ─────────────────────────────────────────────────
val Orange10 = Color(0xFF4A1800)
val Orange40 = Color(0xFFF97316)   // 지도보기, 전체보기 등 액션 텍스트
val Orange80 = Color(0xFFFFBD87)
val Orange90 = Color(0xFFFFF3E0)   // tertiaryContainer

// ── Neutral (회색 계열) ───────────────────────────────────────────────────────
val Gray10   = Color(0xFF111827)   // 본문 제목
val Gray20   = Color(0xFF374151)   // 본문 서브
val Gray40   = Color(0xFF6B7280)   // caption, placeholder
val Gray80   = Color(0xFFD1D5DB)   // 비활성 아이콘
val Gray90   = Color(0xFFF3F4F6)   // divider, chip background
val Gray95   = Color(0xFFF9F9F9)   // 스크린 배경

// ── Semantic: 산책지수 ─────────────────────────────────────────────────────────
val WalkGood   = Color(0xFF22C55E)  // 80점 이상 → 초록
val WalkNormal = Color(0xFF3B82F6)  // 50~79점 → 파랑
val WalkBad    = Color(0xFFF97316)  // 49점 이하 → 주황

// ── Map (지도 미리보기) ────────────────────────────────────────────────────────
val MapSky   = Color(0xFFE0F7FA)
val MapMint  = Color(0xFFB2EBF2)
val MapDeep  = Color(0xFF80DEEA)

// ── Basic ─────────────────────────────────────────────────────────────────────
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// ── CLAUDE.md 단일 진실 토큰 (디자인 시스템 정합) ─────────────────────────────
// 화면 인라인으로 흩어져 있던 raw hex를 토큰으로 추출해서 공통화.
// 기존 Brown40/Pink40/Orange40 등은 호환을 위해 유지, 신규 코드는 아래 토큰 사용.
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
