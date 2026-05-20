package com.example.siheunggagae.ui.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

private val Brown700H   = Color(0xFF8A6E58)
private val Orange500H  = Color(0xFFF7A35B)
private val OrangeSandH = Color(0xFFFFEDD4)
private val Gray300H    = Color(0xFFE8E8E8)
private val BackgroundH = Color(0xFFFEFEFE)
private val TextBlackH  = Color(0xFF1E120A)

private data class FaqItem(val question: String, val answer: String)

private val faqItems = listOf(
    FaqItem(
        question = "시흥가개는 어떤 서비스인가요?",
        answer = "시흥가개는 시흥시 반려동물 보호자와 이동 봉사자를 연결하는 서비스예요. 병원 방문, 미용 등 반려동물 이동이 필요한 보호자가 봉사자의 도움을 받을 수 있어요.",
    ),
    FaqItem(
        question = "이동 지원 봉사자는 어떻게 신청하나요?",
        answer = "마이 탭 → '봉사자 자격 신청'에서 신청서를 작성해 주세요. 검토 후 승인되면 봉사자로 활동할 수 있어요.",
    ),
    FaqItem(
        question = "봉사 신청은 어떻게 하나요?",
        answer = "매칭 탭에서 원하는 이동 지원 요청 글을 선택하고 '봉사 신청하기' 버튼을 눌러주세요. 요청자가 수락하면 매칭이 완료돼요.",
    ),
    FaqItem(
        question = "이동 지원 요청은 어떻게 등록하나요?",
        answer = "매칭 탭 우측 하단 '+' 버튼을 눌러 반려동물, 일정, 요청 내용을 단계별로 입력하면 돼요.",
    ),
    FaqItem(
        question = "차단한 사용자는 어떻게 관리하나요?",
        answer = "설정 → 기타 → '차단 관리'에서 차단된 사용자 목록을 확인하고 차단을 해제할 수 있어요.",
    ),
    FaqItem(
        question = "알림이 오지 않아요.",
        answer = "설정 → 알림 섹션에서 각 알림 항목이 켜져 있는지 확인해 주세요. 기기 설정에서 앱 알림이 허용되어 있는지도 확인해 주세요.",
    ),
    FaqItem(
        question = "문의 및 신고는 어디에 하나요?",
        answer = "게시글 또는 사용자 프로필에서 '신고하기'를 이용해 주세요. 앱 문의는 시흥시청 동물보호과(031-310-2114)로 연락 주세요.",
    ),
)

@Composable
fun HelpScreen(onBack: () -> Unit = {}) {
    Scaffold(
        containerColor = BackgroundH,
        topBar = { HelpTopBar(onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "자주 묻는 질문",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Brown700H,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )

            androidx.compose.material3.Card(
                shape = RoundedCornerShape(16.dp),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    faqItems.forEachIndexed { idx, item ->
                        FaqRow(item = item)
                        if (idx < faqItems.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFFF3F4F6),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FaqRow(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangeSandH),
            ) {
                Text(
                    text = "Q",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange500H,
                )
            }
            Text(
                text = item.question,
                fontFamily = PretendardFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                color = TextBlackH,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray300H,
                modifier = Modifier.size(20.dp),
            )
        }

        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.size(40.dp))
                Text(
                    text = item.answer,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp,
                    color = Brown700H,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HelpTopBar(onBack: () -> Unit) {
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
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackH,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "도움말",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackH,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HelpScreenPreview() {
    SiheungGagaeTheme { HelpScreen() }
}
