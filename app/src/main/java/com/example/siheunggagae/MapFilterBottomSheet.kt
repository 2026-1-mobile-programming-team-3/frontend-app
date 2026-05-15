package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 스펙 컬러 (Figma 실측)
private val TextBlackMf = Color(0xFF1E120A)  // rgba(30,18,10)   — 제목, 라벨, 아이콘
private val Brown700Mf  = Color(0xFF8A6E58)  // rgba(138,110,88) — 닫기 아이콘, 적용 버튼
private val Orange500Mf = Color(0xFFF7A35B)  // rgba(247,163,91) — 체크박스 선택 상태 bg
private val Gray300Mf   = Color(0xFFE8E8E8)  // rgba(232,232,232)— 아이콘 박스 bg, 구분선, 미선택 border

// ─── 데이터 ────────────────────────────────────────────────────────────────────

data class MapFilterEntry(
    val name: String,
    val icon: ImageVector,
    val isSelected: Boolean,
)

// ─── 메인 BottomSheet ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFilterBottomSheet(
    onDismiss: () -> Unit = {},
    onApply: (List<MapFilterEntry>) -> Unit = {},
) {
    val entries = remember {
        mutableStateListOf(
            MapFilterEntry("카페",     Icons.Default.LocalCafe,     isSelected = true),
            MapFilterEntry("식당",     Icons.Default.Restaurant,    isSelected = false),
            MapFilterEntry("공원",     Icons.Default.Park,          isSelected = true),
            MapFilterEntry("동물병원", Icons.Default.LocalHospital, isSelected = false),
            MapFilterEntry("미용",     Icons.Default.ContentCut,    isSelected = false),
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ─── 헤더 ──────────────────────────────────────────────────────────
            // padding top=24, horizontal=24, gap(제목↔구분선)=16
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "지도 보기 설정",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = TextBlackMf,
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Brown700Mf,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                HorizontalDivider(color = Gray300Mf)
            }

            Spacer(Modifier.height(6.dp))

            // ─── 카테고리 항목 5개 ──────────────────────────────────────────────
            // padding horizontal=24, gap=16 — 항목 간 divider 없음(Figma 미사용)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                entries.forEachIndexed { index, entry ->
                    FilterItemRow(
                        entry = entry,
                        onToggle = { entries[index] = entry.copy(isSelected = !entry.isSelected) },
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // ─── 적용 버튼 ─────────────────────────────────────────────────────
            // padding horizontal=24, vertical=12, button h=50, r=16, Brown700 bg
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brown700Mf)
                        .clickable { onApply(entries.toList()) },
                ) {
                    Text(
                        text = "적용하기",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// ─── 카테고리 항목 Row ─────────────────────────────────────────────────────────

@Composable
private fun FilterItemRow(entry: MapFilterEntry, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // 아이콘 박스 + 라벨
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gray300Mf),
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = entry.name,
                    tint = TextBlackMf,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = entry.name,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlackMf,
            )
        }

        // 체크박스: 24×24dp, r=8dp
        FilterCheckboxMf(checked = entry.isSelected)
    }
}

// ─── 커스텀 체크박스 ───────────────────────────────────────────────────────────

@Composable
private fun FilterCheckboxMf(checked: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (checked) Modifier.background(Orange500Mf)
                else Modifier
                    .background(Color.White)
                    .border(1.dp, Gray300Mf, RoundedCornerShape(8.dp))
            ),
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun MapFilterBottomSheetPreview() {
    SiheungGagaeTheme { MapFilterBottomSheet() }
}
