package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.data.model.BlockListItem
import com.example.siheunggagae.ui.viewmodel.BlockManageUiState
import com.example.siheunggagae.ui.viewmodel.BlockManageViewModel

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Block
import com.example.siheunggagae.ui.component.SiheungAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.siheunggagae.ui.component.SiheungSnackbarHost
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.flow.MutableStateFlow

// 컬러
private val Brown900B    = Color(0xFF614B3A)
private val Brown700B    = Color(0xFF8A6E58)
private val Brown400B    = Color(0xFFC4A882)
private val Pink500B     = Color(0xFFF04268)
private val PinkSurfB    = Color(0xFFFEE7EC)
private val Gray300B     = Color(0xFFE8E8E8)
private val BackgroundB  = Color(0xFFFEFEFE)
private val TextBlackB   = Color(0xFF1E120A)

private val avatarColors = listOf(
    Color(0xFFF04268), Color(0xFFF7A35B), Color(0xFF00A63E),
    Color(0xFF388AF5), Color(0xFF9C27B0), Color(0xFF614B3A),
)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun BlockManageScreen(
    viewModel: BlockManageViewModel? = null,
    onBack: () -> Unit = {},
) {
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: MutableStateFlow(BlockManageUiState.Loading)
    }.collectAsState()

    // 해제 확인 다이얼로그 대상
    var pendingUnblock by remember { mutableStateOf<BlockListItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    Scaffold(
        containerColor = BackgroundB,
        topBar = { BlockManageTopBar(onBack = onBack) },
        snackbarHost = { SiheungSnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is BlockManageUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Brown900B)
                }
            }

            is BlockManageUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = state.message,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = Brown700B,
                            textAlign = TextAlign.Center,
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Brown900B)
                                .clickable { viewModel?.fetchBlocks() }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text("다시 시도", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }

            is BlockManageUiState.Success -> {
                if (state.items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Gray300B,
                                modifier = Modifier.size(48.dp),
                            )
                            Text(
                                text = "차단한 사용자가 없어요",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Brown700B,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "총 ${state.items.size}명 차단됨",
                                fontFamily = PretendardFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp,
                                color = Brown700B,
                            )
                        }
                        items(state.items, key = { it.blockId ?: 0 }) { item ->
                            BlockedUserCard(
                                item = item,
                                onUnblockClick = { pendingUnblock = item },
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    // 차단 해제 확인 다이얼로그
    val target = pendingUnblock
    if (target != null) {
        SiheungAlertDialog(
            onDismissRequest = { pendingUnblock = null },
            title = "차단 해제",
            text = "${target.targetNickname ?: "이 사용자"}님의 차단을 해제하시겠어요?",
            confirmText = "해제",
            onConfirm = {
                val snapshot = target
                snapshot.blockId?.let { viewModel?.unblock(it) }
                pendingUnblock = null
                val nickname = snapshot.targetNickname ?: "사용자"
                snackbarScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "${nickname}님의 차단을 해제했어요",
                        actionLabel = "실행취소",
                        withDismissAction = false,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel?.reblock(snapshot)
                    }
                }
            },
            dismissText = "취소",
            onDismiss = { pendingUnblock = null },
            confirmColor = Pink500B,
            dismissColor = Brown700B,
        )
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun BlockManageTopBar(onBack: () -> Unit) {
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
            text = "차단 관리",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackB,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 차단된 사용자 카드 ─────────────────────────────────────────────────────────

@Composable
private fun BlockedUserCard(
    item: BlockListItem,
    onUnblockClick: () -> Unit,
) {
    val avatarColor = avatarColors[(item.targetUserId ?: 0) % avatarColors.size]
    val initial = item.targetNickname?.take(1) ?: "?"
    val dateLabel = item.createdAt?.take(10) ?: ""

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 아바타
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
            ) {
                Text(
                    text = initial,
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            // 닉네임 + 차단일
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.targetNickname ?: "(알 수 없음)",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = TextBlackB,
                )
                if (dateLabel.isNotEmpty()) {
                    Text(
                        text = "차단일: $dateLabel",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 16.sp,
                        color = Brown400B,
                    )
                }
            }

            // 해제 버튼
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(PinkSurfB)
                    .clickable { onUnblockClick() }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "해제",
                    fontFamily = PretendardFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp,
                    color = Pink500B,
                )
            }
        }
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BlockManageScreenPreview() {
    SiheungGagaeTheme {
        val flow = MutableStateFlow<BlockManageUiState>(
            BlockManageUiState.Success(
                listOf(
                    BlockListItem(1, 101, "댕댕이주인", "2026-04-10T12:00:00"),
                    BlockListItem(2, 102, "고양이집사", "2026-05-01T09:00:00"),
                )
            )
        )
        BlockManageScreen()
    }
}
