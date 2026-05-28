package com.example.siheunggagae.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 표준 BottomSheet drag 인디케이터 — 32dp × 4dp 베이지 라운드 막대.
 *
 * 사용:
 * ```
 * ModalBottomSheet(dragHandle = { SheetHandle() }, ...)
 * ```
 */
@Composable
fun SheetHandle(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE0D4CC),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFEFEFE)
@Composable
private fun SheetHandlePreview() {
    SheetHandle()
}
