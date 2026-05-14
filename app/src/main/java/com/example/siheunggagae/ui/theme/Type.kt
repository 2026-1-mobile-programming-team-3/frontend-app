package com.example.siheunggagae.ui.theme

import android.content.res.AssetManager
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalTextApi::class)
fun suitFontFamily(assets: AssetManager) = FontFamily(
    Font(path = "fonts/SUIT-Regular.ttf",  assetManager = assets, weight = FontWeight.Normal),
    Font(path = "fonts/SUIT-Medium.ttf",   assetManager = assets, weight = FontWeight.Medium),
    Font(path = "fonts/SUIT-SemiBold.ttf", assetManager = assets, weight = FontWeight.SemiBold),
    Font(path = "fonts/SUIT-Bold.ttf",     assetManager = assets, weight = FontWeight.Bold),
)

fun appTypography(assets: AssetManager): Typography {
    val suit = suitFontFamily(assets)
    return Typography(
        displayLarge = TextStyle(fontFamily = suit, fontWeight = FontWeight.Bold,     fontSize = 57.sp, lineHeight = 64.sp),
        titleLarge   = TextStyle(fontFamily = suit, fontWeight = FontWeight.Bold,     fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium  = TextStyle(fontFamily = suit, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
        bodyLarge    = TextStyle(fontFamily = suit, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium   = TextStyle(fontFamily = suit, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
        labelSmall   = TextStyle(fontFamily = suit, fontWeight = FontWeight.Normal,   fontSize = 11.sp, lineHeight = 16.sp),
    )
}
