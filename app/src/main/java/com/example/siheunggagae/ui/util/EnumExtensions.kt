package com.example.siheunggagae.ui.util

import androidx.compose.ui.graphics.Color
import com.example.siheunggagae.data.model.MatchStatus
import com.example.siheunggagae.data.model.NotificationCategory

// ─── MatchStatus ──────────────────────────────────────────────────────────────

val MatchStatus.textColor: Color get() = when (this) {
    MatchStatus.WAITING  -> Color(0xFFE84B6A)  // 모집중 — Pink
    MatchStatus.MATCHING -> Color(0xFFCA8A04)  // 검토중 — Yellow
    MatchStatus.PROGRESS -> Color(0xFF16A34A)  // 진행중 — Green
    MatchStatus.DONE     -> Color(0xFF6B7280)  // 완료   — Gray
}

val MatchStatus.bgColor: Color get() = when (this) {
    MatchStatus.WAITING  -> Color(0xFFFECDD3)  // TagPinkBg
    MatchStatus.MATCHING -> Color(0xFFFFEDD4)  // TagYellowBg
    MatchStatus.PROGRESS -> Color(0xFFDCFCE7)  // TagGreenBg
    MatchStatus.DONE     -> Color(0xFFF3F4F6)  // TagGrayBg
}

// ─── NotificationCategory ────────────────────────────────────────────────────

val NotificationCategory.label: String get() = when (this) {
    NotificationCategory.VOLUNTEER -> "봉사"
    NotificationCategory.MATCH     -> "매칭"
    NotificationCategory.REVIEW    -> "리뷰"
    NotificationCategory.NEWS      -> "소식"
    NotificationCategory.POLICY    -> "정책"
    NotificationCategory.SYSTEM    -> "시스템"
}

val NotificationCategory.accentColor: Color get() = when (this) {
    NotificationCategory.VOLUNTEER,
    NotificationCategory.MATCH     -> Color(0xFFF04268)  // Pink500
    NotificationCategory.NEWS,
    NotificationCategory.POLICY    -> Color(0xFFF7A35B)  // Orange500
    NotificationCategory.REVIEW    -> Color(0xFFFDC700)  // StarYellow
    NotificationCategory.SYSTEM    -> Color(0xFF00A63E)  // Green600
}
