@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.example.siheunggagae.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R

// ─── Pretendard Variable ───────────────────────────────────────────────────────
// res/font/pretendardvariable.ttf — variable font
// FontVariation.Settings로 각 weight 축을 명시해야 실제 굵기가 적용됨

val PretendardFamily = FontFamily(
    Font(
        R.font.pretendardvariable,
        weight = FontWeight.Thin,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(100)),
    ),
    Font(
        R.font.pretendardvariable,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300)),
    ),
    Font(
        R.font.pretendardvariable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.pretendardvariable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.pretendardvariable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.pretendardvariable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
    Font(
        R.font.pretendardvariable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800)),
    ),
)

// ─── Typography — Figma 실측 (BWkXUCXVxj7MMq1K66DG0y) ─────────────────────────
//
// displayMedium  30/800/32  산책지수 점수
// displaySmall   26/800/32  메인 화면 타이틀 (홈·매칭·소식·마이)
// headlineLarge  24/800/33  도움 요청 질문 텍스트
// headlineMedium 20/800/32  서브 페이지 TopBar 타이틀 (알림·설정 등)
// headlineSmall  20/700/24  섹션 헤더 ("주변 매장 24곳")
// titleLarge     18/700/27  카드 제목, 알림 제목
// titleMedium    16/700/24  굵은 버튼 텍스트, 장소명
// titleSmall     16/600/24  뉴스 카드 제목
// bodyLarge      14/700/20  강조 본문
// bodyMedium     14/500/20  일반 본문 (가장 빈번)
// bodySmall      14/400/20  서브텍스트, 날짜
// labelLarge     12/700/16  D-day 배지
// labelMedium    12/600/16  nav 선택 라벨, 필터 태그
// labelSmall     12/500/16  캡션 (거리·날짜·출처)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 48.sp,
        lineHeight    = 48.sp,
        letterSpacing = 0.sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 30.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 26.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 24.sp,
        lineHeight    = 33.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 20.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 20.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 18.sp,
        lineHeight    = 27.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = PretendardFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.sp,
    ),
)
