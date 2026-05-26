package com.example.siheunggagae.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.StoreRequestType
import com.example.siheunggagae.data.repository.StoreRequestRepository
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.PlanInput
import com.example.siheunggagae.ui.viewmodel.StoreRequestFormState
import com.example.siheunggagae.ui.viewmodel.StoreRequestFormViewModel
import com.example.siheunggagae.ui.viewmodel.SubmitState

// ─── 색상 토큰 ─────────────────────────────────────────────────────────────────

private val BackgroundF   = Color(0xFFFEFEFE)
private val TextBlackF    = Color(0xFF1E120A)
private val Brown700F     = Color(0xFF8A6E58)
private val Brown900F     = Color(0xFF614B3A)
private val BorderBeigeF  = Color(0xFFE8D3C2)
private val OrangeSandF   = Color(0xFFFFEDD4)
private val Orange500F    = Color(0xFFF7A35B)
private val MintLightF    = Color(0xFFD0FEE1)
private val Gray300F      = Color(0xFFE8E8E8)
private val AddGrayF      = Color(0xFFF4F4F4)
private val PlaceholderF  = Color(0xFFC1AEA0)
private val PinkSurfaceF  = Color(0xFFFEE7EC)
private val Pink500F      = Color(0xFFF04268)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun StoreRequestFormScreen(
    viewModel: StoreRequestFormViewModel,
    onBack: () -> Unit,
    onPickLocation: (lat: Double?, lng: Double?) -> Unit,
    onSubmitted: (requestId: Int) -> Unit,
) {
    val state by viewModel.form.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()
    val submitState by viewModel.submit.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 제출 결과 처리
    LaunchedEffect(submitState) {
        when (val s = submitState) {
            is SubmitState.Submitted -> onSubmitted(s.requestId)
            is SubmitState.Failed    -> snackbarHostState.showSnackbar(s.message)
            else                     -> {}
        }
    }

    // 파일 선택 런처
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.addPhotos(uris) }

    val proofLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.addProofs(uris) }

    val title = if (viewModel.mode == StoreRequestType.UPDATE) "매장 정보 수정" else "매장 추가 요청"

    val isSubmitEnabled = submitState !is SubmitState.Submitting &&
        state.proofUrls.isNotEmpty() &&
        state.name.isNotBlank() &&
        state.category != null &&
        state.latitude != null &&
        state.longitude != null &&
        (state.category != "PET_HOTEL" || state.plans.isNotEmpty())

    Scaffold(
        containerColor = BackgroundF,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FormTopBar(title = title, onBack = onBack)
        },
        bottomBar = {
            SubmitBar(
                isSubmitting = submitState is SubmitState.Submitting,
                enabled = isSubmitEnabled,
                onSubmit = { viewModel.submit() },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // 헤더
            FormHeader()

            // 매장명
            NameSection(
                name = state.name,
                error = errors["name"],
                onNameChange = viewModel::setName,
            )

            // 카테고리
            CategorySection(
                selected = state.category,
                error = errors["category"],
                onSelect = viewModel::setCategory,
            )

            // 위치
            LocationSection(
                state = state,
                error = errors["location"],
                onPickLocation = onPickLocation,
            )

            // 반려동물 동반
            PetAllowedSection(
                isPetAllowed = state.isPetAllowed,
                onToggle = viewModel::setIsPetAllowed,
            )

            // 가격 플랜 (PET_HOTEL only)
            if (state.category == "PET_HOTEL") {
                PlansSection(
                    plans = state.plans,
                    error = errors["plans"],
                    onAddPlan = viewModel::addPlan,
                    onUpdateName = viewModel::updatePlanName,
                    onUpdatePrice = viewModel::updatePlanPrice,
                    onRemove = viewModel::removePlan,
                )
            }

            // 선택 정보
            OptionalInfoSection(
                phone = state.phone,
                operatingHours = state.operatingHours,
                onPhoneChange = viewModel::setPhone,
                onHoursChange = viewModel::setHours,
            )

            // 매장 사진
            PhotosSection(
                photoStubUrls = state.photoStubUrls,
                photoUrisByUrl = state.photoUrisByUrl,
                onAddPhotos = { photoLauncher.launch("image/*") },
                onRemovePhoto = viewModel::removePhoto,
            )

            // 증빙 자료
            ProofsSection(
                proofUrls = state.proofUrls,
                proofUrisByUrl = state.proofUrisByUrl,
                error = errors["proof"],
                onAddProofs = { proofLauncher.launch("*/*") },
                onRemoveProof = viewModel::removeProof,
            )

            // 관리자 메시지
            MessageSection(
                message = state.message,
                onMessageChange = viewModel::setMessage,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun FormTopBar(title: String, onBack: () -> Unit) {
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
                tint = TextBlackF,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = title,
            fontFamily = PretendardFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp,
            color = TextBlackF,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── Submit Bar ────────────────────────────────────────────────────────────────

@Composable
private fun SubmitBar(
    isSubmitting: Boolean,
    enabled: Boolean,
    onSubmit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Button(
            onClick = onSubmit,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Brown900F,
                contentColor = Color.White,
                disabledContainerColor = Brown900F.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.6f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = "요청 제출하기",
                    fontFamily = PretendardFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

// ─── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun FormHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "매장 정보를\n알려주세요",
            fontFamily = PretendardFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 30.sp,
            color = TextBlackF,
        )
        Text(
            text = "관리자 검수 후 지도에 추가됩니다",
            fontFamily = PretendardFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 18.sp,
            color = Brown700F,
        )
    }
}

// ─── 매장명 섹션 ────────────────────────────────────────────────────────────────

@Composable
private fun NameSection(
    name: String,
    error: String?,
    onNameChange: (String) -> Unit,
) {
    FormSection(label = "매장명", required = true) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AddGrayF)
                    .border(
                        width = if (error != null) 1.dp else 0.dp,
                        color = if (error != null) Pink500F else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                val nameFocusManager = LocalFocusManager.current
                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 24.sp,
                        color = TextBlackF,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { nameFocusManager.clearFocus() }),
                    cursorBrush = SolidColor(Orange500F),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (name.isEmpty()) {
                            Text(
                                text = "예: 배곧 펫호텔",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = PlaceholderF,
                            )
                        }
                        inner()
                    },
                )
            }
            if (error != null) {
                Text(
                    text = error,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Pink500F,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

// ─── 카테고리 섹션 ──────────────────────────────────────────────────────────────

private data class CategoryEntry(val key: String, val label: String, val iconRes: Int)

private val categoryEntries = listOf(
    CategoryEntry("CAFE",        "카페",    R.drawable.ic_coffee),
    CategoryEntry("RESTAURANT",  "식당",    R.drawable.ic_utensils),
    CategoryEntry("PARK",        "공원",    R.drawable.ic_trees),
    CategoryEntry("PET_HOTEL",   "펫호텔",  R.drawable.ic_hotel),
)

@Composable
private fun CategorySection(
    selected: String?,
    error: String?,
    onSelect: (String) -> Unit,
) {
    FormSection(label = "카테고리", required = true) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // 2x2 그리드
            val rows = categoryEntries.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { rowEntries ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowEntries.forEach { entry ->
                            val isSelected = selected == entry.key
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) OrangeSandF else Color.White)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Orange500F else BorderBeigeF,
                                        shape = RoundedCornerShape(14.dp),
                                    )
                                    .clickable { onSelect(entry.key) }
                                    .padding(vertical = 18.dp),
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(entry.iconRes),
                                        contentDescription = entry.label,
                                        tint = if (isSelected) Brown900F else Brown700F,
                                        modifier = Modifier.size(28.dp),
                                    )
                                    Text(
                                        text = entry.label,
                                        fontFamily = PretendardFamily,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        lineHeight = 20.sp,
                                        color = if (isSelected) Brown900F else Brown700F,
                                    )
                                }
                            }
                        }
                        // 홀수일 경우 빈 칸 채우기 (현재 4개라 불필요하지만 방어적)
                        if (rowEntries.size < 2) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            if (error != null) {
                Text(
                    text = error,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Pink500F,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

// ─── 위치 섹션 ─────────────────────────────────────────────────────────────────

@Composable
private fun LocationSection(
    state: StoreRequestFormState,
    error: String?,
    onPickLocation: (lat: Double?, lng: Double?) -> Unit,
) {
    FormSection(label = "위치", required = true) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MintLightF)
                    .border(
                        width = if (error != null) 1.dp else 0.dp,
                        color = if (error != null) Pink500F else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .clickable { onPickLocation(state.latitude, state.longitude) },
            ) {
                // 지도 핀 아이콘 (중앙)
                Icon(
                    painter = painterResource(R.drawable.ic_map_pin),
                    contentDescription = "위치 선택",
                    tint = Orange500F,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp),
                )

                // 주소 오버레이 카드 (하단 좌측)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (state.address.isNotBlank()) state.address
                            else "지도에서 위치를 선택해주세요",
                            fontFamily = PretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = if (state.address.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                            lineHeight = 16.sp,
                            color = if (state.address.isNotBlank()) TextBlackF else PlaceholderF,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (state.latitude != null && state.longitude != null) {
                            Text(
                                text = "%.4f, %.4f".format(state.latitude, state.longitude),
                                fontFamily = PretendardFamily,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = Brown700F,
                            )
                        }
                    }
                }
            }
            if (error != null) {
                Text(
                    text = error,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Pink500F,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

// ─── 반려동물 동반 섹션 ─────────────────────────────────────────────────────────

@Composable
private fun PetAllowedSection(
    isPetAllowed: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    FormSection(label = "반려동물 동반") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AddGrayF)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "반려동물 동반 가능",
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                color = TextBlackF,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = isPetAllowed,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Brown900F,
                    checkedBorderColor = Brown900F,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Gray300F,
                    uncheckedBorderColor = Gray300F,
                ),
            )
        }
    }
}

// ─── 가격 플랜 섹션 (PET_HOTEL) ─────────────────────────────────────────────────

@Composable
private fun PlansSection(
    plans: List<PlanInput>,
    error: String?,
    onAddPlan: () -> Unit,
    onUpdateName: (Int, String) -> Unit,
    onUpdatePrice: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    FormSection(
        label = "가격 플랜",
        labelSuffix = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(PinkSurfaceF)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "호텔 전용",
                    fontFamily = PretendardFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 14.sp,
                    color = Pink500F,
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // 플랜 목록 카드
            if (plans.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderBeigeF, RoundedCornerShape(12.dp)),
                ) {
                    plans.forEachIndexed { index, plan ->
                        PlanRow(
                            plan = plan,
                            onUpdateName = { onUpdateName(index, it) },
                            onUpdatePrice = { onUpdatePrice(index, it) },
                            onRemove = { onRemove(index) },
                        )
                        if (index < plans.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(BorderBeigeF),
                            )
                        }
                    }
                }
            }

            // 플랜 추가 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAddPlan() }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = null,
                    tint = Brown700F,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "플랜 추가",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    color = Brown700F,
                )
            }

            if (error != null) {
                Text(
                    text = error,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Pink500F,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun PlanRow(
    plan: PlanInput,
    onUpdateName: (String) -> Unit,
    onUpdatePrice: (Int) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val planFocusManager = LocalFocusManager.current
        // 플랜 이름
        BasicTextField(
            value = plan.planName,
            onValueChange = onUpdateName,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = TextBlackF,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { planFocusManager.moveFocus(FocusDirection.Next) }),
            cursorBrush = SolidColor(Orange500F),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (plan.planName.isEmpty()) {
                    Text(
                        text = "플랜명 예: 1박 소형견",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = PlaceholderF,
                    )
                }
                inner()
            },
        )

        // 가격 입력
        BasicTextField(
            value = if (plan.priceKrw == 0) "" else plan.priceKrw.toString(),
            onValueChange = { raw ->
                val num = raw.filter { it.isDigit() }.toIntOrNull() ?: 0
                onUpdatePrice(num)
            },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                color = Brown900F,
                textAlign = TextAlign.End,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { planFocusManager.clearFocus() }),
            cursorBrush = SolidColor(Orange500F),
            modifier = Modifier.width(90.dp),
            decorationBox = { inner ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        if (plan.priceKrw == 0) {
                            Text(
                                text = "0",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 20.sp,
                                color = PlaceholderF,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        inner()
                    }
                    Text(
                        text = " 원",
                        fontFamily = PretendardFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = Brown700F,
                    )
                }
            },
        )

        // 삭제 버튼
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable { onRemove() },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_x),
                contentDescription = "삭제",
                tint = Brown700F,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ─── 선택 정보 섹션 ─────────────────────────────────────────────────────────────

@Composable
private fun OptionalInfoSection(
    phone: String,
    operatingHours: String,
    onPhoneChange: (String) -> Unit,
    onHoursChange: (String) -> Unit,
) {
    val contactFocusManager = LocalFocusManager.current
    FormSection(label = "선택 정보") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderBeigeF, RoundedCornerShape(12.dp))
                .background(Color.White),
        ) {
            // 전화번호
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_call),
                    contentDescription = null,
                    tint = Brown700F,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = TextBlackF,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { contactFocusManager.moveFocus(FocusDirection.Next) }),
                    cursorBrush = SolidColor(Orange500F),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (phone.isEmpty()) {
                            Text(
                                text = "전화번호 (선택)",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = PlaceholderF,
                            )
                        }
                        inner()
                    },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
                    .background(BorderBeigeF),
            )

            // 영업시간
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = null,
                    tint = Brown700F,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = operatingHours,
                    onValueChange = onHoursChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        color = TextBlackF,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { contactFocusManager.clearFocus() }),
                    cursorBrush = SolidColor(Orange500F),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (operatingHours.isEmpty()) {
                            Text(
                                text = "영업시간 예: 10:00 ~ 20:00 (선택)",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = PlaceholderF,
                            )
                        }
                        inner()
                    },
                )
            }
        }
    }
}

// ─── 매장 사진 섹션 ─────────────────────────────────────────────────────────────

@Composable
private fun PhotosSection(
    photoStubUrls: List<String>,
    photoUrisByUrl: Map<String, Uri>,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
) {
    FormSection(
        label = "매장 사진 (${photoStubUrls.size}/5)",
        sublabel = "매장 외관·내부를 보여주는 사진을 추가하세요",
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 사진 목록: photoStubUrls가 source of truth
            // 신규 추가 항목은 photoUrisByUrl[url]에 로컬 Uri가 있음
            // 프리필된 항목은 Uri 없이 서버 URL로 Coil 로드
            photoStubUrls.forEachIndexed { index, url ->
                val localUri = photoUrisByUrl[url]
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BorderBeigeF, RoundedCornerShape(14.dp)),
                ) {
                    AsyncImage(
                        model = localUri ?: url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_image),
                        error = painterResource(R.drawable.ic_image),
                        modifier = Modifier.fillMaxSize(),
                    )
                    // 삭제 버튼 (상단 오른쪽)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onRemovePhoto(index) },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_x),
                            contentDescription = "삭제",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }

            // 추가 버튼 (5장 미만일 때)
            if (photoStubUrls.size < 5) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(OrangeSandF)
                        .border(1.dp, BorderBeigeF, RoundedCornerShape(14.dp))
                        .clickable { onAddPhotos() },
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_image),
                            contentDescription = null,
                            tint = Brown700F,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "사진 추가",
                            fontFamily = PretendardFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp,
                            color = Brown700F,
                        )
                    }
                }
            }
        }
    }
}

// ─── 증빙 자료 섹션 ─────────────────────────────────────────────────────────────

@Composable
private fun ProofsSection(
    proofUrls: List<String>,
    proofUrisByUrl: Map<String, Uri>,
    error: String?,
    onAddProofs: () -> Unit,
    onRemoveProof: (Int) -> Unit,
) {
    val resolver = LocalContext.current.contentResolver

    fun fileNameAndSize(uri: Uri): Pair<String?, Long?> {
        var name: String? = null
        var size: Long? = null
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) name = cursor.getString(nameIdx)
                if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
            }
        }
        return name to size
    }

    fun formatSize(bytes: Long?): String {
        if (bytes == null) return ""
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)}KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))}MB"
        }
    }

    fun fileExtension(name: String?): String {
        if (name == null) return ""
        val dot = name.lastIndexOf('.')
        return if (dot >= 0) name.substring(dot + 1).uppercase() else ""
    }

    fun isImageExt(ext: String) = ext in setOf("JPG", "JPEG", "PNG", "WEBP", "GIF", "BMP")

    FormSection(
        label = "증빙 자료",
        labelSuffix = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(PinkSurfaceF)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "필수",
                    fontFamily = PretendardFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 14.sp,
                    color = Pink500F,
                )
            }
        },
        sublabel = "사업자등록증·계약서·메뉴판 등 본인이 운영함을 증명할 자료. 최소 1개 이상 첨부해 주세요.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // 파일 목록: proofUrls가 source of truth
            // 신규 추가 항목은 proofUrisByUrl[url]에 로컬 Uri가 있음
            // 프리필된 항목은 Uri 없이 제네릭 라벨로 표시
            proofUrls.forEachIndexed { index, url ->
                val localUri = proofUrisByUrl[url]
                val (name, size) = if (localUri != null) {
                    remember(localUri) { fileNameAndSize(localUri) }
                } else {
                    null to null
                }
                val ext = fileExtension(name)
                val displayName = when {
                    localUri != null -> name ?: localUri.lastPathSegment ?: "파일 ${index + 1}"
                    else -> "기존 첨부 자료 #${index + 1}"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BorderBeigeF, RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // 파일 타입 배지
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    localUri == null -> Color(0xFFF2F2F2) // 프리필 항목
                                    isImageExt(ext) -> Color(0xFFD0FEE1)
                                    ext == "PDF"    -> Color(0xFFFEE7EC)
                                    ext in setOf("DOC", "DOCX") -> Color(0xFFDBEAFE)
                                    else            -> Color(0xFFF2F2F2)
                                }
                            ),
                    ) {
                        when {
                            localUri != null && isImageExt(ext) -> {
                                AsyncImage(
                                    model = localUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                )
                            }
                            localUri == null -> {
                                // 프리필된 항목: 클립 아이콘으로 표시
                                Icon(
                                    painter = painterResource(R.drawable.ic_paperclip),
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            else -> {
                                Text(
                                    text = ext.take(3).ifEmpty { "FILE" },
                                    fontFamily = PretendardFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 12.sp,
                                    color = when {
                                        ext == "PDF"                -> Color(0xFFE84B6A)
                                        ext in setOf("DOC", "DOCX") -> Color(0xFF2563EB)
                                        else                        -> Color(0xFF6B7280)
                                    },
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    // 파일 정보
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = displayName,
                            fontFamily = PretendardFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp,
                            color = TextBlackF,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (localUri != null && size != null && size > 0) {
                            Text(
                                text = formatSize(size),
                                fontFamily = PretendardFamily,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = Brown700F,
                            )
                        }
                    }

                    // 삭제 버튼
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onRemoveProof(index) },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_x),
                            contentDescription = "삭제",
                            tint = Brown700F,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            // 파일 추가 버튼
            if (proofUrls.size < 10) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeSandF)
                        .border(1.dp, BorderBeigeF, RoundedCornerShape(12.dp))
                        .clickable { onAddProofs() }
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_paperclip),
                                contentDescription = null,
                                tint = Brown700F,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "파일 추가",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp,
                                color = Brown700F,
                            )
                        }
                        Text(
                            text = "사진 · PDF · DOC 등 (최대 10MB / 10개)",
                            fontFamily = PretendardFamily,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = PlaceholderF,
                        )
                    }
                }
            }

            // 에러 메시지
            if (error != null) {
                Text(
                    text = error,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Pink500F,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            // 팁 박스
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lightbulb),
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "증빙 자료가 명확할수록 승인 가능성이 높아져요",
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    color = Color(0xFF92400E),
                )
            }
        }
    }
}

// ─── 메시지 섹션 ────────────────────────────────────────────────────────────────

@Composable
private fun MessageSection(
    message: String,
    onMessageChange: (String) -> Unit,
) {
    FormSection(label = "관리자에게 한마디 (선택)") {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                textStyle = TextStyle(
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    color = TextBlackF,
                ),
                placeholder = {
                    Text(
                        text = "추가로 전달할 내용이 있으면 적어주세요",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = PlaceholderF,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500F,
                    unfocusedBorderColor = BorderBeigeF,
                    focusedTextColor = TextBlackF,
                    unfocusedTextColor = TextBlackF,
                    cursorColor = Orange500F,
                ),
                shape = RoundedCornerShape(12.dp),
                supportingText = {
                    Text(
                        text = "${message.length}/1000",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = Brown700F,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                },
            )
        }
    }
}

// ─── 섹션 공통 래퍼 ─────────────────────────────────────────────────────────────

@Composable
private fun FormSection(
    label: String,
    sublabel: String? = null,
    labelSuffix: (@Composable () -> Unit)? = null,
    required: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                color = TextBlackF,
            )
            if (required) {
                Text(
                    text = "*",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Pink500F,
                )
            }
            labelSuffix?.invoke()
        }
        if (sublabel != null) {
            Text(
                text = sublabel,
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Brown700F,
            )
        }
        content()
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun StoreRequestFormScreenPreview() {
    val repo = StoreRequestRepository()
    val vm = StoreRequestFormViewModel(
        repository = repo,
        mode = StoreRequestType.ADD,
        targetStoreId = null,
        sourceRequestId = null,
    )
    StoreRequestFormScreen(
        viewModel = vm,
        onBack = {},
        onPickLocation = { _, _ -> },
        onSubmitted = {},
    )
}
