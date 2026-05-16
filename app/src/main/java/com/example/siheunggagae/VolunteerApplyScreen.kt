package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 스펙 컬러
private val Brown700VA    = Color(0xFF8A6E58)
private val Brown400VA    = Color(0xFFC4A882)
private val BorderBeigeVA = Color(0xFFE8D3C2)
private val Green600VA    = Color(0xFF00A63E)
private val PlaceholderVA = Color(0xFFC1AEA0)
private val BackgroundVA  = Color(0xFFFEFEFE)
private val TextBlackVA   = Color(0xFF1E120A)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun VolunteerApplyScreen(onBack: () -> Unit = {}) {
    var titleInput by remember { mutableStateOf("") }
    var applyInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundVA,
        topBar = { VolunteerApplyTopBar(onBack = onBack) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Green600VA)
                        .clickable { },
                ) {
                    Text(
                        text = "신청하기",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp,
                        color = Color.White,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            HeadlineSection()

            InputSection(
                label = "제목 *",
                content = {
                    ApplyTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = "예: 봉사자 자격 요청드립니다.",
                        singleLine = true,
                    )
                },
            )

            InputSection(
                label = "신청서 *",
                content = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ApplyTextField(
                            value = applyInput,
                            onValueChange = { if (it.length <= 500) applyInput = it },
                            placeholder = "시흥가개에 전달할 내용을 입력하세요.\n예: 신청서의 예시 문장이\n뭐가있지",
                            singleLine = false,
                            minHeight = 120,
                            bottomPadding = 24,
                        )
                        Text(
                            text = "${applyInput.length} / 500",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp,
                            color = PlaceholderVA,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 8.dp),
                        )
                    }
                },
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun VolunteerApplyTopBar(onBack: () -> Unit) {
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
                tint = TextBlackVA,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "봉사자 자격 신청",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackVA,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 제목 섹션 ─────────────────────────────────────────────────────────────────

@Composable
private fun HeadlineSection() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "반려동물과 함께하는",
            fontFamily = PretendardFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 33.sp,
            color = TextBlackVA,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontFamily = PretendardFamily, color = Green600VA, fontWeight = FontWeight.ExtraBold)) {
                    append("봉사자")
                }
                withStyle(SpanStyle(fontFamily = PretendardFamily, color = TextBlackVA, fontWeight = FontWeight.ExtraBold)) {
                    append("로 활동해보세요")
                }
            },
            fontFamily = PretendardFamily,
            fontSize = 24.sp,
            lineHeight = 33.sp,
        )
    }
}

// ─── 공통 입력 섹션 ────────────────────────────────────────────────────────────

@Composable
private fun InputSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            color = Brown700VA,
        )
        content()
    }
}

// ─── 공통 TextField ────────────────────────────────────────────────────────────

@Composable
private fun ApplyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    minHeight: Int = 0,
    bottomPadding: Int = 0,
) {
    val verticalPad = if (minHeight > 0) 14 else 0

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = TextStyle(
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = TextBlackVA,
        ),
        cursorBrush = SolidColor(Green600VA),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (minHeight > 0) Modifier.heightIn(min = minHeight.dp) else Modifier)
            .border(1.dp, BorderBeigeVA, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .then(if (bottomPadding > 0) Modifier.padding(bottom = bottomPadding.dp) else Modifier),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    color = PlaceholderVA,
                )
            }
            inner()
        },
    )
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VolunteerApplyScreenPreview() {
    SiheungGagaeTheme { VolunteerApplyScreen() }
}
