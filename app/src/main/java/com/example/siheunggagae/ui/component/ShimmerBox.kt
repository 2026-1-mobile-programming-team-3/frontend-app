package com.example.siheunggagae.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 로딩 스켈레톤. 베이지(#E8D3C2) → 화이트 → 베이지 그라디언트가 좌→우로 1.2s 무한 슬라이드.
 *
 * 사용:
 * ```
 * ShimmerBox(Modifier.height(20.dp).fillMaxWidth(0.6f))
 * ```
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    val widthPx = with(LocalDensity.current) { 600.dp.toPx() }
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEAE0D6),
            Color(0xFFFFFAF7),
            Color(0xFFEAE0D6),
        ),
        start = Offset(x = -widthPx + translate * 2 * widthPx, y = 0f),
        end   = Offset(x = translate * 2 * widthPx, y = 0f),
    )
    Box(modifier = modifier.clip(shape).background(brush))
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEFE)
@Composable
private fun ShimmerBoxPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ShimmerBox(Modifier.size(60.dp), shape = RoundedCornerShape(12.dp))
        Box(Modifier.height(12.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.7f).height(16.dp))
        Box(Modifier.height(8.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.5f).height(12.dp))
    }
}
