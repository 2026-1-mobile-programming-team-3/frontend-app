package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.example.siheunggagae.data.model.ChatMessageItem
import com.example.siheunggagae.data.model.MatchDetailResponse
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.ChatUiState
import com.example.siheunggagae.ui.viewmodel.ChatViewModel

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

@Composable
fun ChatScreen(
    matchId: Int,
    applicationId: Int,
    viewModel: ChatViewModel,
    // ❌ 삭제: myUserId: Int = 31 (하드코딩 파라미터 완전 차단)
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    var inputText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // 채팅방 진입 시 비동기 파이프라인 가동 트리거
    LaunchedEffect(matchId, applicationId) {
        viewModel.initChatRoom(matchId, applicationId)
    }

    Scaffold(
        containerColor = BgC,
        topBar = {
            val state = uiState as? ChatUiState.Success
            ChatTopBar(
                title = state?.opponentNickname ?: "대화방",
                showAcceptBtn = state?.isMyRequest ?: false,
                onBack = onBack,
                onAcceptClick = {
                    viewModel.acceptVolunteer {
                        onBack()
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendTextMessage(inputText)
                        inputText = ""
                    }
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ChatUiState.Loading -> {
                    CircularProgressIndicator(color = Pink500C, modifier = Modifier.align(Alignment.Center))
                }
                is ChatUiState.Error -> {
                    Text(text = state.message, color = Pink500C, modifier = Modifier.align(Alignment.Center), fontFamily = PretendardFamily)
                }
                is ChatUiState.Success -> {
                    // 새 메시지 도달 시 하단 자동 스크롤 보증 로직
                    LaunchedEffect(state.messages.size) {
                        if (state.messages.isNotEmpty()) {
                            listState.animateScrollToItem(state.messages.lastIndex + 1)
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        state.matchDetail?.let { RequestPreviewCard(it) }
                        Spacer(Modifier.height(1.dp))

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item { Spacer(Modifier.height(4.dp)) }
                            item { DateDivider(label = "오늘") }

                            // 정렬: 내 메시지는 우측 갈색, 상대는 좌측 회색
                            items(state.messages) { msg ->
                                // 👈 [변경] 뷰모델에서 인증 완료한 내 진짜 유저 ID와 메시지 발송자 ID 대조
                                val isMe = msg.senderId == viewModel.myUserId
                                if (isMe) {
                                    SentMessageItem(msg)
                                } else {
                                    ReceivedMessageItem(msg, state.opponentNickname)
                                }
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(title: String, showAcceptBtn: Boolean, onBack: () -> Unit, onAcceptClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(BgC)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(BgC)
                .clickable { onBack() },
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로", tint = TextBlackC, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(CircleShape).background(MintLightC)) {
            Text(text = title.firstOrNull()?.toString() ?: "", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green500C)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlackC)
            Text(text = "⭐ 4.9 · 동네 매칭 회원", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700C)
        }
        if (showAcceptBtn) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Pink500C)
                    .clickable { onAcceptClick() }
                    .padding(horizontal = 17.dp, vertical = 7.dp),
            ) {
                Text(text = "수락", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}

@Composable
private fun RequestPreviewCard(detail: MatchDetailResponse) {
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
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(PinkSurfaceC)) {
            Icon(painter = painterResource(R.drawable.ic_handshake), contentDescription = null, tint = Pink500C, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = detail.title ?: "요청 정보", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextBlackC, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "${detail.desiredDate?.take(10)} · ${detail.address}", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700C)
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(PinkSurfaceC).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(text = detail.status ?: "검토 중", fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Pink500C)
        }
    }
}

@Composable
private fun DateDivider(label: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300C, thickness = 1.dp)
        Text(text = label, fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700C)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300C, thickness = 1.dp)
    }
}

@Composable
private fun ReceivedMessageItem(msg: ChatMessageItem, name: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Bottom) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).clip(CircleShape).background(MintLightC)) {
            Text(text = name.firstOrNull()?.toString() ?: "", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Green500C)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(BgC)
                    .border(1.dp, Gray300C, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(text = msg.content, fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackC)
            }
            Text(text = msg.createdAt.take(16).replace("T", " "), fontFamily = PretendardFamily, fontSize = 10.sp, color = Brown700C)
        }
    }
}

@Composable
private fun SentMessageItem(msg: ChatMessageItem) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                    .background(Brown700C)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(text = msg.content, fontFamily = PretendardFamily, fontSize = 14.sp, color = Color.White)
            }
            Text(text = msg.createdAt.take(16).replace("T", " "), fontFamily = PretendardFamily, fontSize = 10.sp, color = Brown700C)
        }
    }
}

@Composable
private fun ChatInputBar(inputText: String, onTextChange: (String) -> Unit, onSendClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgC)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50.dp))
                .background(InputBgC)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackC),
            decorationBox = { innerTextField ->
                if (inputText.isEmpty()) {
                    Text(text = "메시지를 입력하세요...", fontFamily = PretendardFamily, fontSize = 14.sp, color = PlaceholderC)
                }
                innerTextField()
            },
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brown900C)
                .clickable { onSendClick() },
        ) {
            Icon(painter = painterResource(R.drawable.ic_send), contentDescription = "전송", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}