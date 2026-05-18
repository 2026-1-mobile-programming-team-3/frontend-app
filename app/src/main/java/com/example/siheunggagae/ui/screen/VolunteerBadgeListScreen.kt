package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.VolunteerBadgeInfo
import com.example.siheunggagae.data.model.VolunteerBadgeTier
import com.example.siheunggagae.ui.viewmodel.VolunteerBadgeViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.flow.MutableStateFlow

// 스펙 컬러
private val Brown900B    = Color(0xFF614B3A)
private val Brown700B    = Color(0xFF8A6E58)
private val Brown400B    = Color(0xFFC4A882)
private val Orange500B   = Color(0xFFF7A35B)
private val Green500B    = Color(0xFF00A63E)
private val Pink500B     = Color(0xFFF04268)
private val Gray300B     = Color(0xFFE8E8E8)
private val BackgroundB  = Color(0xFFFEFEFE)
private val TextBlackB   = Color(0xFF1E120A)

// 등급별 색상 (달성 시)
private val tierColor = mapOf(
    VolunteerBadgeTier.SEED   to Green500B,
    VolunteerBadgeTier.FLOWER to Orange500B,
    VolunteerBadgeTier.FRUIT  to Pink500B,
    VolunteerBadgeTier.TREE   to Brown900B,
)

// 등급별 아이콘
private val tierIconRes = mapOf(
    VolunteerBadgeTier.SEED to R.drawable.ic_psychiatry,
    VolunteerBadgeTier.TREE to R.drawable.ic_nature,
)
private val tierIconVector = mapOf(
    VolunteerBadgeTier.FLOWER to Icons.Default.LocalFlorist,
    VolunteerBadgeTier.FRUIT  to Icons.Default.Eco,
)

private val tierOrder = listOf(
    VolunteerBadgeTier.SEED,
    VolunteerBadgeTier.FLOWER,
    VolunteerBadgeTier.FRUIT,
    VolunteerBadgeTier.TREE,
)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun VolunteerBadgeListScreen(
    viewModel: VolunteerBadgeViewModel? = null,
    onBack: () -> Unit = {},
) {
    val isLoading by remember(viewModel) {
        viewModel?.isLoading ?: MutableStateFlow(false)
    }.collectAsState()

    val badge by remember(viewModel) {
        viewModel?.badge ?: MutableStateFlow(null)
    }.collectAsState()

    Scaffold(
        containerColor = BackgroundB,
        topBar = { BadgeListTopBar(onBack = onBack) },
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange500B)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BadgeSummaryHeader(badge = badge)

                Spacer(Modifier.height(4.dp))

                tierOrder.forEach { tier ->
                    BadgeTierCard(tier = tier, badge = badge)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun BadgeListTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackB,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "봉사 뱃지",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackB,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 현재 등급 요약 헤더 ────────────────────────────────────────────────────────

@Composable
private fun BadgeSummaryHeader(badge: VolunteerBadgeInfo?) {
    val tier = badge?.tier ?: VolunteerBadgeTier.NONE
    val count = badge?.count ?: 0
    val color = tierColor[tier] ?: Gray300B

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color),
        ) {
            if (tier != VolunteerBadgeTier.NONE) {
                TierIcon(tier = tier, size = 28, tint = Color.White)
            } else {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
        Column {
            Text(
                text = if (tier == VolunteerBadgeTier.NONE) "아직 뱃지가 없어요" else "${tier.label} 등급",
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp,
                color = TextBlackB,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "누적 봉사 ${count}건",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700B,
            )
        }
    }
}

// ─── 등급 카드 ─────────────────────────────────────────────────────────────────

@Composable
private fun BadgeTierCard(tier: VolunteerBadgeTier, badge: VolunteerBadgeInfo?) {
    val count = badge?.count ?: 0
    val currentTier = badge?.tier ?: VolunteerBadgeTier.NONE
    val currentTierIdx = tierOrder.indexOf(currentTier).coerceAtLeast(-1)
    val thisTierIdx = tierOrder.indexOf(tier)
    val achieved = thisTierIdx <= currentTierIdx

    val bgColor = if (achieved) tierColor[tier] ?: Gray300B else Gray300B
    val required = tier.requiredCount
    val progress = if (required > 0) (count.toFloat() / required).coerceIn(0f, 1f) else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 아이콘 박스
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor),
        ) {
            TierIcon(tier = tier, size = 36, tint = Color.White)
        }

        Column(modifier = Modifier.weight(1f)) {
            // 등급명 + 달성 여부 chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = tier.label,
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 27.sp,
                    color = TextBlackB,
                )
                AchievedChip(achieved = achieved)
            }

            Spacer(Modifier.height(4.dp))

            // 달성 조건
            val conditionText = when {
                required == 0 -> "기본 달성"
                achieved      -> "봉사 ${required}건 달성"
                else          -> "봉사 ${required}건 필요 (현재 ${count}건)"
            }
            Text(
                text = conditionText,
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp,
                color = Brown700B,
            )

            // 미달성 시 진행 바
            if (!achieved) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = tierColor[tier] ?: Orange500B,
                    trackColor = Gray300B,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

// ─── 달성 여부 Chip ────────────────────────────────────────────────────────────

@Composable
private fun AchievedChip(achieved: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (achieved) Green500B.copy(alpha = 0.12f) else Gray300B.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (achieved) Icons.Default.Check else Icons.Default.Lock,
            contentDescription = null,
            tint = if (achieved) Green500B else Brown400B,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = if (achieved) "달성" else "미달성",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            color = if (achieved) Green500B else Brown400B,
        )
    }
}

// ─── 아이콘 렌더러 ─────────────────────────────────────────────────────────────

@Composable
private fun TierIcon(tier: VolunteerBadgeTier, size: Int, tint: Color) {
    val iv: ImageVector? = tierIconVector[tier]
    val res: Int? = tierIconRes[tier]
    when {
        iv  != null -> Icon(imageVector = iv, contentDescription = tier.label, tint = tint, modifier = Modifier.size(size.dp))
        res != null -> Icon(painter = painterResource(res), contentDescription = tier.label, tint = tint, modifier = Modifier.size(size.dp))
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VolunteerBadgeListScreenPreview() {
    SiheungGagaeTheme { VolunteerBadgeListScreen() }
}
