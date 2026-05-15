package com.example.siheunggagae.ui.theme

import androidx.compose.material3.MaterialTheme
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

@Composable
fun SiheungGagaeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = AppTypography,
        content     = content,
    )
}
