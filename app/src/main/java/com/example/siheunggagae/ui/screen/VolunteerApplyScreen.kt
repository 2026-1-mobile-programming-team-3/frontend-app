package com.example.siheunggagae.ui.screen

import android.net.Uri
import android.provider.OpenableColumns
import com.example.siheunggagae.ui.viewmodel.VolunteerApplyUiEvent
import com.example.siheunggagae.ui.viewmodel.VolunteerApplyViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// 스펙 컬러
private val Brown700VA    = Color(0xFF8A6E58)
private val Brown400VA    = Color(0xFFC4A882)
private val BorderBeigeVA = Color(0xFFE8D3C2)
private val Green600VA    = Color(0xFF00A63E)
private val PlaceholderVA = Color(0xFFC1AEA0)
private val BackgroundVA  = Color(0xFFFEFEFE)
private val TextBlackVA   = Color(0xFF1E120A)
private val Pink500VA     = Color(0xFFF04268)
private val Gray300VA     = Color(0xFFE8E8E8)
private val Gray100VA     = Color(0xFFF4F4F4)
private val Orange500VA   = Color(0xFFF7A35B)

private const val MAX_ATTACHMENTS = 5

private data class AttachmentItem(
    val uri: Uri,
    val name: String,
    val isImage: Boolean,
)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VolunteerApplyScreen(
    viewModel: VolunteerApplyViewModel? = null,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    var titleInput by remember { mutableStateOf("") }
    var applyInput by remember { mutableStateOf("") }
    var showEmptyError by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf(listOf<AttachmentItem>()) }
    var showAttachPickerSheet by remember { mutableStateOf(false) }

    val event by remember(viewModel) {
        viewModel?.event ?: MutableStateFlow(VolunteerApplyUiEvent.Idle)
    }.collectAsStateWithLifecycle()

    val isSubmitting = event is VolunteerApplyUiEvent.Submitting

    val alreadyPendingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val successSheetState        = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val attachSheetState         = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // 이미지 선택 런처
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        if (attachments.size < MAX_ATTACHMENTS) {
            val name = resolveFileName(context, uri) ?: "이미지"
            attachments = attachments + AttachmentItem(uri, name, isImage = true)
        }
    }

    // 파일 선택 런처
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        if (attachments.size < MAX_ATTACHMENTS) {
            val name = resolveFileName(context, uri) ?: "파일"
            attachments = attachments + AttachmentItem(uri, name, isImage = false)
        }
    }

    Scaffold(
        containerColor = BackgroundVA,
        topBar = { VolunteerApplyTopBar(onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (showEmptyError) {
                    Text(
                        text = "신청서 내용을 입력해주세요",
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        color = Pink500VA,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSubmitting) Gray300VA else Green600VA)
                        .clickable(enabled = !isSubmitting) {
                            if (applyInput.isBlank()) {
                                showEmptyError = true
                            } else {
                                showEmptyError = false
                                viewModel?.submit(applyInput)
                            }
                        },
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
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

            // 제목: UI 전용, 백엔드 미전송
            InputSection(
                label = "제목",
                content = {
                    ApplyTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = "예: 봉사자 자격 요청드립니다.",
                        singleLine = true,
                    )
                    Text(
                        text = "본인 확인용 메모입니다 (서버에 저장되지 않아요)",
                        fontFamily = PretendardFamily,
                        fontSize = 11.sp,
                        color = Brown700VA,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                    )
                },
            )

            InputSection(
                label = "신청서 *",
                content = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ApplyTextField(
                            value = applyInput,
                            onValueChange = {
                                if (it.length <= 500) {
                                    applyInput = it
                                    if (showEmptyError && it.isNotBlank()) showEmptyError = false
                                }
                            },
                            placeholder = "시흥가개에 전달할 내용을 입력하세요.\n예: 반려동물 이동 봉사에 관심이 많아 신청합니다.",
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

            // ─── 첨부파일 섹션 ──────────────────────────────────────────────
            InputSection(
                label = "증빙서류 첨부 (선택)",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // 안내 텍스트
                        Text(
                            text = "자원봉사활동 실적 확인서 또는 우수자원봉사자증(바로패스)을 첨부하면 검토에 도움이 돼요",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 18.sp,
                            color = Brown400VA,
                        )

                        // 첨부 버튼 (최대 5개 미만일 때만 표시)
                        if (attachments.size < MAX_ATTACHMENTS) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderBeigeVA, RoundedCornerShape(12.dp))
                                    .background(Gray100VA)
                                    .clickable { showAttachPickerSheet = true }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = Brown700VA,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = if (attachments.isEmpty()) "파일 또는 사진 첨부하기"
                                               else "파일 추가 (${attachments.size}/$MAX_ATTACHMENTS)",
                                        fontFamily = PretendardFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Brown700VA,
                                    )
                                }
                            }
                        }

                        // 첨부된 파일 목록
                        if (attachments.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                attachments.forEach { item ->
                                    AttachmentChip(
                                        item = item,
                                        onRemove = {
                                            attachments = attachments.filter { it.uri != item.uri }
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
            )

            Spacer(Modifier.height(8.dp))
        }
    }

    // ─── 첨부파일 선택 시트 ─────────────────────────────────────────────────────
    if (showAttachPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachPickerSheet = false },
            sheetState = attachSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            AttachPickerSheetContent(
                onImageClick = {
                    scope.launch { attachSheetState.hide() }.invokeOnCompletion {
                        showAttachPickerSheet = false
                        imageLauncher.launch("image/*")
                    }
                },
                onFileClick = {
                    scope.launch { attachSheetState.hide() }.invokeOnCompletion {
                        showAttachPickerSheet = false
                        // PDF, 문서 등 일반 파일
                        fileLauncher.launch("*/*")
                    }
                },
            )
        }
    }

    // ─── 409 — 이미 검토중인 신청 있음 ─────────────────────────────────────────
    if (event is VolunteerApplyUiEvent.AlreadyPending) {
        ModalBottomSheet(
            onDismissRequest = { viewModel?.resetEvent() },
            sheetState = alreadyPendingSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            ResultSheetContent(
                icon = {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = Brown400VA,
                        modifier = Modifier.size(48.dp),
                    )
                },
                title = "이미 검토중인 신청이 있어요",
                body = "현재 신청이 검토 중이에요.\n결과는 앱 내 알림으로 안내드릴게요.",
                buttonText = "확인",
                buttonColor = Brown700VA,
                onButtonClick = {
                    scope.launch { alreadyPendingSheetState.hide() }.invokeOnCompletion {
                        viewModel?.resetEvent()
                        onBack()
                    }
                },
            )
        }
    }

    // ─── 201 성공 ───────────────────────────────────────────────────────────────
    if (event is VolunteerApplyUiEvent.Success) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel?.resetEvent()
                onBack()
            },
            sheetState = successSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            ResultSheetContent(
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Green600VA,
                        modifier = Modifier.size(48.dp),
                    )
                },
                title = "신청이 완료되었어요",
                body = "검토 중입니다.\n운영자 승인 후 봉사자 기능을 사용할 수 있어요.",
                buttonText = "확인",
                buttonColor = Green600VA,
                onButtonClick = {
                    scope.launch { successSheetState.hide() }.invokeOnCompletion {
                        viewModel?.resetEvent()
                        onBack()
                    }
                },
            )
        }
    }

    // ─── 기타 오류 ──────────────────────────────────────────────────────────────
    if (event is VolunteerApplyUiEvent.Error) {
        val errorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel?.resetEvent() },
            sheetState = errorSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            ResultSheetContent(
                icon = {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = Pink500VA,
                        modifier = Modifier.size(48.dp),
                    )
                },
                title = "오류가 발생했어요",
                body = (event as VolunteerApplyUiEvent.Error).message,
                buttonText = "닫기",
                buttonColor = Pink500VA,
                onButtonClick = { viewModel?.resetEvent() },
            )
        }
    }
}

// ─── 첨부파일 선택 시트 내용 ────────────────────────────────────────────────────

@Composable
private fun AttachPickerSheetContent(
    onImageClick: () -> Unit,
    onFileClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "첨부 방식 선택",
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp,
            color = TextBlackVA,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        HorizontalDivider(color = Color(0xFFE8E8E8))

        AttachOptionRow(
            icon = Icons.Default.Image,
            iconBg = Color(0xFFFFF3E0),
            iconTint = Orange500VA,
            title = "사진 / 이미지",
            subtitle = "갤러리에서 이미지 파일을 선택해요",
            onClick = onImageClick,
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = Color(0xFFF4F4F4),
        )
        AttachOptionRow(
            icon = Icons.Default.Description,
            iconBg = Color(0xFFE8F5E9),
            iconTint = Green600VA,
            title = "파일 (PDF · 문서)",
            subtitle = "실적 확인서, 우수자원봉사자증 등",
            onClick = onFileClick,
        )
    }
}

@Composable
private fun AttachOptionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
        Column {
            Text(
                text = title,
                fontFamily = PretendardFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp,
                color = TextBlackVA,
            )
            Text(
                text = subtitle,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown400VA,
            )
        }
    }
}

// ─── 첨부파일 칩 ──────────────────────────────────────────────────────────────

@Composable
private fun AttachmentChip(item: AttachmentItem, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Gray100VA)
            .border(1.dp, BorderBeigeVA, RoundedCornerShape(8.dp))
            .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = if (item.isImage) Icons.Default.Image else Icons.Default.Description,
            contentDescription = null,
            tint = if (item.isImage) Orange500VA else Green600VA,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = item.name,
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Brown700VA,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 140.dp),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(50.dp))
                .clickable { onRemove() },
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                tint = Brown400VA,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

// ─── 바텀시트 공통 레이아웃 ─────────────────────────────────────────────────────

@Composable
private fun ResultSheetContent(
    icon: @Composable () -> Unit,
    title: String,
    body: String,
    buttonText: String,
    buttonColor: Color,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Text(
            text = title,
            fontFamily = PretendardFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 27.sp,
            color = TextBlackVA,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
            color = Brown700VA,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(buttonColor)
                .clickable { onButtonClick() },
        ) {
            Text(
                text = buttonText,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
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

// ─── 유틸: ContentResolver로 파일명 조회 ───────────────────────────────────────

private fun resolveFileName(context: android.content.Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (_: Exception) {
        uri.lastPathSegment
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VolunteerApplyScreenPreview() {
    SiheungGagaeTheme { VolunteerApplyScreen() }
}
