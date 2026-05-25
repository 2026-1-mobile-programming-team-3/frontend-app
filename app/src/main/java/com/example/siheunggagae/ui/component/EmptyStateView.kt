package com.example.siheunggagae.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.R
import com.example.siheunggagae.ui.theme.PretendardFamily

/**
 * 화면 빈 상태 표준 컴포저블.
 *
 * 시각 구조:
 *   ┌──────────────┐
 *   │  ⭕ (80dp)    │  ← 원형 컬러 배경 + 아이콘
 *   │              │
 *   │   타이틀      │  ← 16sp ExtraBold
 *   │   부제목      │  ← 13sp, brown700
 *   │              │
 *   │   [CTA 버튼]  │  ← 선택적
 *   └──────────────┘
 */
@Composable
fun EmptyStateView(
    title: String,
    subtitle: String? = null,
    @DrawableRes iconRes: Int = R.drawable.ic_search,
    iconTint: Color = Color(0xFFF7A35B),         // Orange500
    iconBackground: Color = Color(0xFFFFEDD4),    // OrangeSand
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E120A),
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF8A6E58),
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF614B3A))
                    .clickable { onAction() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text(
                    text = actionLabel,
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEFE)
@Composable
private fun EmptyStateViewPreview() {
    EmptyStateView(
        title = "이 영역에 매장이 없어요",
        subtitle = "지도를 옮기거나 확대해 보세요",
        actionLabel = "전체 보기",
        onAction = {},
    )
}
