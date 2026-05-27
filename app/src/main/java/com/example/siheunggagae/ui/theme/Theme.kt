package com.example.siheunggagae.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    // Primary — Brown
    primary            = Brown40,
    onPrimary          = White,
    primaryContainer   = Brown90,
    onPrimaryContainer = Brown10,

    // Secondary — Pink
    secondary            = Pink40,
    onSecondary          = White,
    secondaryContainer   = Pink90,
    onSecondaryContainer = Pink10,

    // Tertiary — Orange
    tertiary            = Orange40,
    onTertiary          = White,
    tertiaryContainer   = Orange90,
    onTertiaryContainer = Orange10,

    // Background / Surface
    background       = Gray95,
    onBackground     = Gray10,
    surface          = White,
    onSurface        = Gray10,
    surfaceVariant   = Gray90,
    onSurfaceVariant = Gray20,

    // Outline
    outline        = Gray80,
    outlineVariant = Gray90,
)

// 현재 미사용 - 다크모드 본격 디자인 후 활성화
@Suppress("unused")
private val DarkColorScheme = darkColorScheme(
    // Primary — 베이지 (다크 배경 위 잘 보임)
    primary            = Brown80,
    onPrimary          = Brown10,
    primaryContainer   = Brown20,
    onPrimaryContainer = Brown90,

    // Secondary — Pink (유지)
    secondary            = Pink60,
    onSecondary          = Brown10,
    secondaryContainer   = Pink10,
    onSecondaryContainer = Pink80,

    // Tertiary — Orange (유지)
    tertiary            = Orange80,
    onTertiary          = Orange10,
    tertiaryContainer   = Orange10,
    onTertiaryContainer = Orange90,

    // Background / Surface — 매우 어두운 갈색 계열
    background       = Brown10,          // 0xFF3D1F00 어두운 갈색
    onBackground     = Gray90,
    surface          = Brown20,          // 카드 톤
    onSurface        = Gray90,
    surfaceVariant   = Brown40,
    onSurfaceVariant = Gray90,

    // Outline
    outline        = Brown60,
    outlineVariant = Brown40,
)

@Composable
fun SiheungGagaeTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    // 다크모드 본격 디자인 전까지는 light 강제 (DarkColorScheme은 후속 단계에서 활성화)
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = AppTypography,
    ) {
        // 전 화면 글로벌 Pretendard 강제:
        // - MaterialTheme.typography 가 PretendardFamily 를 매핑해 일반 Text 는 자동 상속되나,
        //   Button/TextField/SegmentedButton 등 LocalTextStyle 을 자체 override 하는 컴포넌트
        //   안에서는 fontFamily 가 떨어질 위험이 있어 안전망으로 한 번 더 명시.
        // - fontFamily 미명시 화면(Map/Pet/Store/StoreRequest 등) 도 한 곳에서 일괄 보강.
        ProvideTextStyle(
            value = LocalTextStyle.current.copy(fontFamily = PretendardFamily),
            content = content,
        )
    }
}
