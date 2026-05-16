package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ── 스펙 컬러 (Figma 실측) ────────────────────────────────────────────────────
private val Brown700D     = Color(0xFF8A6E58)
private val Brown400D     = Color(0xFFC4A882)
private val BrownBorderD  = Color(0xFFE8D3C2)
private val Orange500D    = Color(0xFFF7A35B)
private val Pink500D      = Color(0xFFF04268)
private val MintLightD    = Color(0xFFD0FEE1)
private val OrangeSandD   = Color(0xFFFFEDD4)
private val PinkSurfaceD  = Color(0xFFFEE7EC)
private val BackgroundD   = Color(0xFFFEFEFE)
private val TagGrayD      = Color(0xFFF2F2F2)
private val Gray300D      = Color(0xFFE8E8E8)
private val TextBlackD    = Color(0xFF1E120A)
private val Green600D     = Color(0xFF00A63E)

// ── 데이터 ────────────────────────────────────────────────────────────────────

data class MatchingRequestDetail(
    val id: Int,
    val title: String,
    val statusText: String,
    val applicantCount: Int,
    val dDay: Int?,
    val date: String,
    val time: String,
    val destination: String,
    val petInfo: String,
    val memo: String,
    val originName: String,
    val distanceKm: Float,
)

data class VolunteerApplicant(
    val id: Int,
    val name: String,
    val rating: Float,
    val volunteerCount: Int,
    val distanceKm: Float,
    val memo: String?,
    val avatarColor: Color,
)

private val sampleRequest = MatchingRequestDetail(
    id = 1,
    title = "정왕동 실외견 병원 이동 부탁드립니다",
    statusText = "봉사자 모집 중 · 신청 2건",
    applicantCount = 2,
    dDay = 2,
    date = "2026년 5월 10일 (일)",
    time = "오전 10:00 출발 예정",
    destination = "정왕 동물병원",
    petInfo = "파댕이 · 강아지 · 중형",
    memo = "슬개골 탈구 수술 후 경과 검진입니다. 차량 이동이 가능하신 분이면 좋겠어요.",
    originName = "정왕동 자택",
    distanceKm = 0.8f,
)

private val sampleApplicants = listOf(
    VolunteerApplicant(
        id = 1,
        name = "봉사천사",
        rating = 4.9f,
        volunteerCount = 12,
        distanceKm = 1.2f,
        memo = "차량 있고 강아지 좋아해요. 오전 시간대 편합니다!",
        avatarColor = MintLightD,
    ),
    VolunteerApplicant(
        id = 2,
        name = "댕댕러버",
        rating = 4.7f,
        volunteerCount = 5,
        distanceKm = 2.3f,
        memo = null,
        avatarColor = OrangeSandD,
    ),
)

// ── 메인 화면 ──────────────────────────────────────────────────────────────────

@Composable
fun MatchingDetailScreen(
    requestId: Int = 0,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    Scaffold(
        containerColor = BackgroundD,
        topBar = { MatchingDetailTopBar(onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // 상태 배너 + 요청 정보 Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatusBannerD(request = sampleRequest)
                RequestInfoCardD(request = sampleRequest)
            }

            // 경로 Row + 지도 Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RouteRowD(request = sampleRequest)
                MapCardD()
            }

            // 신청한 봉사자 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                VolunteerSectionHeaderD(count = sampleRequest.applicantCount)
                sampleApplicants.forEach { applicant ->
                    VolunteerCardD(applicant = applicant, onNavigate = onNavigate)
                }
            }
        }
    }
}

// ── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun MatchingDetailTopBar(onBack: () -> Unit) {
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
                tint = TextBlackD,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "내 봉사 상세",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackD,
            modifier = Modifier.align(Alignment.Center),
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TopBarIconD(iconRes = R.drawable.ic_more_vert, desc = "더보기")
            TopBarIconD(iconRes = R.drawable.ic_share, desc = "공유")
        }
    }
}

@Composable
private fun TopBarIconD(iconRes: Int, desc: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { },
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = desc, tint = TextBlackD, modifier = Modifier.size(20.dp))
    }
}

// ── 상태 배너 ─────────────────────────────────────────────────────────────────

@Composable
private fun StatusBannerD(request: MatchingRequestDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(PinkSurfaceD)
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = request.statusText,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            color = TextBlackD,
        )
        if (request.dDay != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "D-${request.dDay}",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp,
                    color = Pink500D,
                )
            }
        }
    }
}

// ── 요청 정보 Card ────────────────────────────────────────────────────────────

@Composable
private fun RequestInfoCardD(request: MatchingRequestDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 제목 — fs=20 fw=700 lh=28
        Text(
            text = request.title,
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp,
            color = TextBlackD,
        )

        // 일정 — fw=700 (Figma 실측)
        InfoRowD(
            iconBg = PinkSurfaceD,
            iconRes = R.drawable.ic_calendar_today,
            iconTint = Pink500D,
            label = "일정",
            value = request.date,
            valueFontWeight = FontWeight.Bold,
        )
        HorizontalDivider(color = Gray300D)

        // 시간 — fw=500
        InfoRowD(
            iconBg = OrangeSandD,
            iconRes = R.drawable.ic_schedule,
            iconTint = Orange500D,
            label = "시간",
            value = request.time,
        )
        HorizontalDivider(color = Gray300D)

        // 목적지 — fw=500
        InfoRowD(
            iconBg = MintLightD,
            iconRes = R.drawable.ic_location_on,
            iconTint = Green600D,
            label = "목적지",
            value = request.destination,
        )
        HorizontalDivider(color = Gray300D)

        // 반려동물 — chip 형태
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconBoxD(bg = BrownBorderD) {
                Icon(painter = painterResource(R.drawable.ic_pets), null, tint = Brown700D, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "반려동물",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = Brown700D,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(OrangeSandD)
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = request.petInfo,
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        color = Brown700D,
                    )
                }
            }
        }
        HorizontalDivider(color = Gray300D)

        // 요청 메모 — fs=14 fw=400 lh=20
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconBoxD(bg = Gray300D) {
                Icon(painter = painterResource(R.drawable.ic_chat_bubble), null, tint = TextBlackD, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "요청 메모",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = Brown700D,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = request.memo,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = TextBlackD,
                )
            }
        }
    }
}

@Composable
private fun InfoRowD(
    iconBg: Color,
    iconRes: Int,
    iconTint: Color,
    label: String,
    value: String,
    valueFontWeight: FontWeight = FontWeight.Medium,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconBoxD(bg = iconBg) {
            Icon(painter = painterResource(iconRes), null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700D,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = valueFontWeight,
                lineHeight = 24.sp,
                color = TextBlackD,
            )
        }
    }
}

@Composable
private fun IconBoxD(bg: Color, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg),
    ) { content() }
}

// ── 경로 Row ──────────────────────────────────────────────────────────────────

@Composable
private fun RouteRowD(request: MatchingRequestDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500D, modifier = Modifier.size(16.dp))
        Text(
            text = request.originName,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = TextBlackD,
        )
        Text(
            text = "→",
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            color = Brown400D,
        )
        Text(
            text = request.destination,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = TextBlackD,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${"%.1f".format(request.distanceKm)}km",
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = Brown700D,
        )
    }
}

// ── 경로 지도 Card ────────────────────────────────────────────────────────────

@Composable
private fun MapCardD() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(MintLightD, Color(0xFFE8FAF0), Color(0xFFF4FDFB)))
            ),
    ) {
        // 출발 핀
        Icon(
            painter = painterResource(R.drawable.ic_location_on), null, tint = Pink500D,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center)
                .offset((-40).dp, (-20).dp),
        )
        // 도착 핀
        Icon(
            painter = painterResource(R.drawable.ic_location_on), null, tint = Orange500D,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.Center)
                .offset(40.dp, 20.dp),
        )

        // 지도에서 보기 버튼 — fill=#FFFFFF pill
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_map), null, tint = TextBlackD, modifier = Modifier.size(14.dp))
            Text(
                text = "지도에서 보기",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = TextBlackD,
            )
        }
    }
}

// ── 신청한 봉사자 헤더 ────────────────────────────────────────────────────────

@Composable
private fun VolunteerSectionHeaderD(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_group), null, tint = Orange500D, modifier = Modifier.size(24.dp))
            Text(
                text = "신청한 봉사자",
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp,
                color = TextBlackD,
            )
        }
        Text(
            text = "${count}명",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp,
            color = Orange500D,
        )
    }
}

// ── 봉사자 Card ───────────────────────────────────────────────────────────────

@Composable
private fun VolunteerCardD(
    applicant: VolunteerApplicant,
    onNavigate: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 상단: 아바타 + 이름/정보
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(applicant.avatarColor),
            ) {
                Text(
                    text = applicant.name.first().toString(),
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlackD,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = applicant.name,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlackD,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "⭐ ${applicant.rating} · 봉사 ${applicant.volunteerCount}건 · 📍 ${applicant.distanceKm}km",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = Brown700D,
                )
            }
        }

        // 메모 박스 — fill=#F2F2F2 r=16
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TagGrayD)
                .padding(12.dp),
        ) {
            Text(
                text = applicant.memo ?: "메모 없음",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700D,
            )
        }

        // 버튼 Row: 채팅 | 수락 | 거절
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 채팅 — outline Brown700
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .border(1.dp, Brown700D, RoundedCornerShape(50.dp))
                    .clickable { onNavigate(Screen.Chat.createRoute(applicant.id)) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    text = "채팅",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = Brown700D,
                )
            }

            // 수락 — fill=Pink500 + 체크 아이콘
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Pink500D)
                    .clickable { }
                    .padding(vertical = 10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(painter = painterResource(R.drawable.ic_check), null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(
                        text = "수락",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        color = Color.White,
                    )
                }
            }

            // 거절 — border+text Pink500
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .border(1.dp, Pink500D, RoundedCornerShape(50.dp))
                    .background(BackgroundD)
                    .clickable { }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    text = "거절",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = Pink500D,
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MatchingDetailScreenPreview() {
    SiheungGagaeTheme { MatchingDetailScreen() }
}
