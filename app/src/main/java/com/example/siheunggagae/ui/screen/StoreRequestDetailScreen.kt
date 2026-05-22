package com.example.siheunggagae.ui.screen

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.StoreRequestItem
import com.example.siheunggagae.data.model.StoreRequestPayload
import com.example.siheunggagae.data.model.StoreRequestStatus
import com.example.siheunggagae.data.model.StoreRequestType
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.viewmodel.CancelState
import com.example.siheunggagae.ui.viewmodel.StoreRequestDetailUi
import com.example.siheunggagae.ui.viewmodel.StoreRequestDetailViewModel

// ─── 색상 토큰 ────────────────────────────────────────────────────────────────

private val BackgroundD = Color(0xFFFEFEFE)
private val TextBlackD = Color(0xFF1E120A)
private val Brown700D = Color(0xFF8A6E58)
private val Brown900D = Color(0xFF614B3A)
private val PendingBgStartD = Color(0xFFFEF3C7)
private val PendingBgEndD = Color(0xFFFFEDD4)
private val PendingBorderD = Color(0xFFF7A35B)
private val PendingTextD = Color(0xFFCA8A04)
private val ApprovedBgStartD = Color(0xFFF0FDF4)
private val ApprovedBgEndD = Color(0xFFD0FEE1)
private val ApprovedBorderD = Color(0xFF00A63E)
private val ApprovedTextD = Color(0xFF16A34A)
private val RejectedBgStartD = Color(0xFFFEE7EC)
private val RejectedBgEndD = Color(0xFFFEFEFE)
private val RejectedBorderD = Color(0xFFF04268)
private val RejectedTextD = Color(0xFFE84B6A)
private val PinkSurfaceD = Color(0xFFFEE7EC)
private val TipBgD = Color(0xFFFEF3C7)
private val Pink500D = Color(0xFFF04268)
private val OrangeSandD = Color(0xFFFFEDD4)
private val Orange500D = Color(0xFFF7A35B)
private val MintLightD = Color(0xFFD0FEE1)
private val DividerD = Color(0xFFF2F2F2)

// ─── 화면 ─────────────────────────────────────────────────────────────────────

@Composable
fun StoreRequestDetailScreen(
    viewModel: StoreRequestDetailViewModel? = null,
    onBack: () -> Unit = {},
    onSeeStore: (storeId: Int) -> Unit = {},
    onRetryRequest: (sourceRequestId: Int, type: StoreRequestType) -> Unit = { _, _ -> },
) {
    val state by (viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(StoreRequestDetailUi.Loading) })
    val cancel by (viewModel?.cancel?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(CancelState.Idle) })

    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cancel) {
        if (cancel is CancelState.Cancelled) onBack()
    }

    Scaffold(
        topBar = { DetailTopBar(onBack = onBack) },
        containerColor = BackgroundD,
        bottomBar = {
            when (val s = state) {
                is StoreRequestDetailUi.Success -> StateCta(
                    item = s.item,
                    isCancelling = cancel is CancelState.Cancelling,
                    onCancelClick = { showCancelDialog = true },
                    onSeeStore = { id -> onSeeStore(id) },
                    onRetryRequest = { id, type -> onRetryRequest(id, type) },
                )
                else -> {}
            }
        },
    ) { innerPadding ->
        when (val s = state) {
            StoreRequestDetailUi.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Orange500D) }

            is StoreRequestDetailUi.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = s.message,
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        color = Brown700D,
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brown900D)
                            .clickable { viewModel?.refresh() }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        Text(
                            "다시 시도",
                            fontFamily = PretendardFamily,
                            color = Color.White,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            is StoreRequestDetailUi.Success -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                StateHeaderCard(item = s.item)
                Spacer(Modifier.height(12.dp))

                if (s.item.status == StoreRequestStatus.APPROVED && s.item.targetStoreId != null) {
                    RegisteredStoreCard(
                        storeId = s.item.targetStoreId,
                        storeName = s.item.payload.name,
                        category = s.item.payload.category,
                        onSeeStore = { onSeeStore(s.item.targetStoreId) },
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (s.item.status == StoreRequestStatus.REJECTED) {
                    TipBox()
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(4.dp))
                SectionLabel("제출 내용")
                Spacer(Modifier.height(8.dp))
                SubmittedPayloadCard(
                    payload = s.item.payload,
                    type = s.item.type,
                    proofUrlsCount = s.item.proofUrls.size,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    "요청 취소",
                    fontFamily = PretendardFamily,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    "정말 이 요청을 취소할까요? 이 작업은 되돌릴 수 없어요.",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel?.cancelRequest()
                }) {
                    Text("취소하기", color = Pink500D, fontFamily = PretendardFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("닫기", fontFamily = PretendardFamily)
                }
            },
        )
    }
}

// ─── TopBar ──────────────────────────────────────────────────────────────────

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = "뒤로가기",
                tint = TextBlackD,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            "요청 상세",
            fontFamily = PretendardFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextBlackD,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(40.dp))
    }
}

// ─── StateHeaderCard ─────────────────────────────────────────────────────────

@Composable
private fun StateHeaderCard(item: StoreRequestItem) {
    val (bgStart, bgEnd, borderColor, textColor, iconRes, statusLabel) = when (item.status) {
        StoreRequestStatus.PENDING -> HeaderStyle(
            bgStart = PendingBgStartD,
            bgEnd = PendingBgEndD,
            border = PendingBorderD,
            text = PendingTextD,
            icon = R.drawable.ic_clock,
            label = "검토 중",
        )
        StoreRequestStatus.APPROVED -> HeaderStyle(
            bgStart = ApprovedBgStartD,
            bgEnd = ApprovedBgEndD,
            border = ApprovedBorderD,
            text = ApprovedTextD,
            icon = R.drawable.ic_check_circle,
            label = "승인되었어요",
        )
        StoreRequestStatus.REJECTED -> HeaderStyle(
            bgStart = RejectedBgStartD,
            bgEnd = RejectedBgEndD,
            border = RejectedBorderD,
            text = RejectedTextD,
            icon = R.drawable.ic_alert_circle,
            label = "반려되었어요",
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(listOf(bgStart, bgEnd)),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = statusLabel,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }
            Spacer(Modifier.height(12.dp))

            when (item.status) {
                StoreRequestStatus.PENDING -> {
                    Text(
                        text = "관리자가 제출하신 내용을 확인하고 있어요. 평일 기준 1~2일 소요됩니다.",
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        color = Brown700D,
                        lineHeight = 19.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "신청 ${item.createdAt.take(10)}",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        color = Brown700D,
                    )
                }
                StoreRequestStatus.APPROVED -> {
                    Text(
                        text = "매장이 지도에 추가되었습니다. 이제 다른 사용자도 검색할 수 있어요.",
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        color = Brown700D,
                        lineHeight = 19.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "처리 ${item.processedAt?.take(10) ?: "—"}",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        color = Brown700D,
                    )
                    if (!item.reviewNote.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "관리자 메모: ${item.reviewNote}",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            color = ApprovedTextD,
                        )
                    }
                }
                StoreRequestStatus.REJECTED -> {
                    if (!item.reviewNote.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(12.dp),
                        ) {
                            Text(
                                text = item.reviewNote,
                                fontFamily = PretendardFamily,
                                fontSize = 13.sp,
                                color = TextBlackD,
                                lineHeight = 19.sp,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        text = "처리 ${item.processedAt?.take(10) ?: "—"}",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        color = Brown700D,
                    )
                }
            }
        }
    }
}

private data class HeaderStyle(
    val bgStart: Color,
    val bgEnd: Color,
    val border: Color,
    val text: Color,
    val icon: Int,
    val label: String,
)

// destructuring via operator functions
private operator fun HeaderStyle.component1() = bgStart
private operator fun HeaderStyle.component2() = bgEnd
private operator fun HeaderStyle.component3() = border
private operator fun HeaderStyle.component4() = text
private operator fun HeaderStyle.component5() = icon
private operator fun HeaderStyle.component6() = label

// ─── RegisteredStoreCard ─────────────────────────────────────────────────────

@Composable
private fun RegisteredStoreCard(
    storeId: Int,
    storeName: String?,
    category: String?,
    onSeeStore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DividerD, RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onSeeStore() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(listOf(OrangeSandD, MintLightD)),
                ),
        ) {
            Icon(
                painter = painterResource(categoryIconDetail(category)),
                contentDescription = null,
                tint = Brown900D,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = storeName ?: "등록된 매장",
                fontFamily = PretendardFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlackD,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${category ?: "—"} · ID $storeId",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                color = Brown700D,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Brown700D,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ─── TipBox ──────────────────────────────────────────────────────────────────

@Composable
private fun TipBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TipBgD)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lightbulb),
            contentDescription = null,
            tint = Color(0xFFCA8A04),
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "다시 작성하면 이전 내용이 초안으로 채워져 빠르게 수정할 수 있어요.",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            color = Color(0xFF92400E),
            lineHeight = 18.sp,
        )
    }
}

// ─── SectionLabel ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = PretendardFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Brown700D,
    )
}

// ─── SubmittedPayloadCard ────────────────────────────────────────────────────

@Composable
private fun SubmittedPayloadCard(
    payload: StoreRequestPayload,
    type: StoreRequestType,
    proofUrlsCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DividerD, RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        PayloadRow("유형", if (type == StoreRequestType.ADD) "추가" else "수정")
        PayloadDivider()
        PayloadRow("매장명", payload.name ?: "—")
        PayloadDivider()
        PayloadRow("카테고리", payload.category ?: "—")
        PayloadDivider()
        PayloadRow(
            label = "주소",
            value = payload.address ?: "—",
            truncate = true,
        )
        PayloadDivider()
        PayloadRow("사진", "${payload.photoUrls.orEmpty().size}장")
        PayloadDivider()
        PayloadRow("증빙", "${proofUrlsCount}개")
        if (type == StoreRequestType.ADD || payload.plans != null) {
            PayloadDivider()
            PayloadRow("가격 플랜", "${payload.plans.orEmpty().size}개")
        }
    }
}

@Composable
private fun PayloadRow(label: String, value: String, truncate: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Brown700D,
            modifier = Modifier.width(72.dp),
        )
        Text(
            text = value,
            fontFamily = PretendardFamily,
            fontSize = 13.sp,
            color = TextBlackD,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = if (truncate) 1 else Int.MAX_VALUE,
            overflow = if (truncate) TextOverflow.Ellipsis else TextOverflow.Clip,
        )
    }
}

@Composable
private fun PayloadDivider() {
    HorizontalDivider(color = DividerD, thickness = 1.dp)
}

// ─── StateCta (bottomBar) ────────────────────────────────────────────────────

@Composable
private fun StateCta(
    item: StoreRequestItem,
    isCancelling: Boolean,
    onCancelClick: () -> Unit,
    onSeeStore: (Int) -> Unit,
    onRetryRequest: (Int, StoreRequestType) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundD)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        when (item.status) {
            StoreRequestStatus.PENDING -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, RejectedBorderD, RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable(enabled = !isCancelling) { onCancelClick() },
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            color = Pink500D,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            "요청 취소",
                            fontFamily = PretendardFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Pink500D,
                        )
                    }
                }
            }

            StoreRequestStatus.APPROVED -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brown900D)
                        .clickable {
                            item.targetStoreId?.let { onSeeStore(it) }
                        },
                ) {
                    Text(
                        "매장 상세 보기",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }

            StoreRequestStatus.REJECTED -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brown900D)
                        .clickable { onRetryRequest(item.requestId, item.type) },
                ) {
                    Text(
                        "다시 작성하기",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// ─── 유틸 ────────────────────────────────────────────────────────────────────

private fun categoryIconDetail(category: String?): Int = when (category) {
    "CAFE" -> R.drawable.ic_coffee
    "RESTAURANT" -> R.drawable.ic_utensils
    "PARK" -> R.drawable.ic_trees
    "PET_HOTEL" -> R.drawable.ic_hotel
    else -> R.drawable.ic_store
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StoreRequestDetailScreenPreview() {
    SiheungGagaeTheme {
        StoreRequestDetailScreen()
    }
}
