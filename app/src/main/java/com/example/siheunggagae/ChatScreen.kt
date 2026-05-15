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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Handshake
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// ─── Colors ────────────────────────────────────────────────────────────────────
private val BgC          = Color(0xFFFFFFFF)
private val TextBlackC   = Color(0xFF1F130B)
private val Brown700C    = Color(0xFF8B6F59)
private val Brown900C    = Color(0xFF614C3B)
private val Pink500C     = Color(0xFFF14369)
private val MintLightC   = Color(0xFFD1FFE2)
private val Green500C    = Color(0xFF00A73F)
private val PinkSurfaceC = Color(0xFFFEE8ED)
private val Gray300C     = Color(0xFFE9E9E9)
private val InputBgC     = Color(0xFFF3F3F3)
private val PlaceholderC = Color(0xFFC1AFA0)

// ─── Data ──────────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: Int,
    val text: String,
    val time: String,
    val isMe: Boolean,
)

private val sampleMessages = listOf(
    ChatMessage(1, "안녕하세요! 봉사 요청했어요. 5월 10일 오전에 동물병원 이동 부탁드립니다 🙏", "오전 9:35", false),
    ChatMessage(2, "감사해요! 혹시 자동차로 같이 타고 이동하시게요? 괜찮으시잖아요.", "오전 9:36", true),
    ChatMessage(3, "네, 괜찮습니다! 저 차가 SUV라 태우기 넓고 편할 것 같아요 🙂", "오전 9:38", false),
    ChatMessage(4, "정말 감사하죠. 그럼 당일 파댕이 자택에서 출발하고 오전 10시에 알겠습니다!", "오전 9:40", true),
    ChatMessage(5, "네, 파댕이 자택 앞에서 오전 10시에 알겠습니다!", "오전 9:42", false),
)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun ChatScreen(
    userId: Int = 0,
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BgC,
        topBar = { ChatTopBar(onBack = onBack) },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            RequestPreviewCard()
            Spacer(Modifier.height(1.dp))
            ChatMessageList(messages = sampleMessages)
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun ChatTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(BgC)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 뒤로가기 버튼 — Figma: 30x30, r=8
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(BgC)
                .clickable { onBack() },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlackC,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        // 아바타 (40x40 circle, fill=#D1FFE2)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MintLightC),
        ) {
            Text(
                text = "봐",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                color = Green500C,
            )
        }

        Spacer(Modifier.width(12.dp))

        // 닉네임 + 서브 정보
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "봉사천사",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlackC,
            )
            Text(
                text = "⭐ 4.9 · 봉사 12건 · 1.2km",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700C,
            )
        }

        // 수락 버튼 (pill, fill=#F14369)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Pink500C)
                .clickable { }
                .padding(horizontal = 17.dp, vertical = 7.dp),
        ) {
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
}

// ─── 요청 미리보기 카드 ────────────────────────────────────────────────────────

@Composable
private fun RequestPreviewCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 0.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(BgC)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 핸드셰이크 아이콘 박스 (40x40, r=14, fill=#FEE8ED)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PinkSurfaceC),
        ) {
            Icon(
                imageVector = Icons.Default.Handshake,
                contentDescription = null,
                tint = Pink500C,
                modifier = Modifier.size(24.dp),
            )
        }

        // 제목 + 날짜·목적지
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "파댕이 외과 병원 이동 요청",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlackC,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "5월 10일 · 정왕 동물병원",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700C,
            )
        }

        // 검토 중 뱃지 (pill, fill=#FEE8ED, text=#F14369)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(PinkSurfaceC)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "검토 중",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                color = Pink500C,
            )
        }
    }
}

// ─── 메시지 리스트 ─────────────────────────────────────────────────────────────

@Composable
private fun ChatMessageList(messages: List<ChatMessage>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        item { DateDivider(label = "오늘") }
        items(messages) { msg ->
            if (msg.isMe) SentMessageItem(msg) else ReceivedMessageItem(msg)
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

// ─── 날짜 구분자 ───────────────────────────────────────────────────────────────

@Composable
private fun DateDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300C, thickness = 1.dp)
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp,
            color = Brown700C,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300C, thickness = 1.dp)
    }
}

// ─── 받은 메시지 (봉사자) ──────────────────────────────────────────────────────

@Composable
private fun ReceivedMessageItem(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        // 아바타 (32x32 circle, fill=#D1FFE2)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MintLightC),
        ) {
            Text(
                text = "봐",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
                color = Green500C,
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 버블 (white, topStart=4dp)
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 16.dp,
                        )
                    )
                    .background(BgC)
                    .border(
                        1.dp,
                        Gray300C,
                        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = msg.text,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = TextBlackC,
                )
            }

            // 시간
            Text(
                text = msg.time,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700C,
            )
        }
    }
}

// ─── 보낸 메시지 (나) ──────────────────────────────────────────────────────────

@Composable
private fun SentMessageItem(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 버블 (Brown700, topEnd=4dp)
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 4.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 16.dp,
                        )
                    )
                    .background(Brown700C)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = msg.text,
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = Color.White,
                )
            }

            // 시간
            Text(
                text = msg.time,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700C,
            )
        }
    }
}

// ─── 하단 입력창 ───────────────────────────────────────────────────────────────

@Composable
private fun ChatInputBar(inputText: String, onTextChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgC)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 입력 필드 (pill, fill=#F3F3F3)
        BasicTextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50.dp))
                .background(InputBgC)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = TextBlackC,
            ),
            decorationBox = { innerTextField ->
                if (inputText.isEmpty()) {
                    Text(
                        text = "메시지를 입력하세요...",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = PlaceholderC,
                    )
                }
                innerTextField()
            },
        )

        // 전송 버튼 (44x44 circle, fill=#614C3B)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brown900C)
                .clickable { },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "전송",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    SiheungGagaeTheme { ChatScreen() }
}
