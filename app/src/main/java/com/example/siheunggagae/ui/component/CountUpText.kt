package com.example.siheunggagae.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily

/**
 * Int 값이 변경되면 직전 값에서 새 값까지 부드럽게 카운트업.
 *
 * - 기본 1200ms tween · FastOutSlowInEasing
 * - 사용처: 산책지수, 활동 통계, 매장 개수 등 수치 강조 위치
 */
@Composable
fun CountUpText(
    value: Int,
    modifier: Modifier = Modifier,
    suffix: String = "",
    durationMs: Int = 1200,
    style: TextStyle = TextStyle(
        fontFamily = PretendardFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    color: Color = Color(0xFF1E120A),
) {
    val animated by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "countUp",
    )
    Text(text = "$animated$suffix", modifier = modifier, style = style, color = color)
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEFE)
@Composable
private fun CountUpTextPreview() {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(16.dp),
    ) {
        CountUpText(value = 87, suffix = "점")
        CountUpText(value = 12, suffix = "개")
        CountUpText(value = 0, suffix = "건")
    }
}
