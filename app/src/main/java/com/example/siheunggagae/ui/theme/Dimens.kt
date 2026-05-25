package com.example.siheunggagae.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 시흥가개 디자인 토큰 — Spacing, Radius, Icon, Elevation.
 * 모든 화면이 이 토큰을 사용하면 미세 일관성 깨짐 방지.
 */
object Dimens {
    // ─── Spacing ───────────────────────────────────────────────────────────
    /** 화면 좌우 공통 padding (이전엔 16/20 혼재) */
    val screenH = 20.dp
    /** 화면 위·아래 padding */
    val screenV = 16.dp
    /** 카드 내부 padding */
    val cardPad = 16.dp
    /** 섹션 간 vertical gap */
    val sectionGap = 24.dp
    /** 리스트 아이템 vertical padding */
    val itemV = 14.dp
    /** 작은 간격 */
    val s = 4.dp
    val m = 8.dp
    val l = 12.dp
    val xl = 16.dp
    val xxl = 24.dp

    // ─── Radius ────────────────────────────────────────────────────────────
    val radiusS = 8.dp
    val radiusM = 12.dp
    val radiusL = 16.dp
    val radiusXL = 20.dp
    /** Pill / fully rounded */
    val radiusPill = 50.dp

    // ─── Icon ──────────────────────────────────────────────────────────────
    val iconS = 16.dp
    val iconM = 20.dp
    val iconL = 24.dp
    val iconXL = 32.dp

    // ─── Elevation ─────────────────────────────────────────────────────────
    val elevS = 1.dp
    val elevM = 2.dp
    val elevL = 4.dp
    val elevXL = 8.dp
}
