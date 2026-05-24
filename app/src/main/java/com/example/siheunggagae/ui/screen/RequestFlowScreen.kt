package com.example.siheunggagae.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.platform.LocalContext
import com.example.siheunggagae.R
import com.example.siheunggagae.data.local.CurrentUserStore
import com.example.siheunggagae.data.model.MatchCategory
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.ui.component.SiheungSnackbarHost
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.RequestUiState
import com.example.siheunggagae.ui.viewmodel.RequestViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val Brown900F    = Color(0xFF614B3A)
private val Brown700F    = Color(0xFF8A6E58)
private val Brown400F    = Color(0xFFC4A882)
private val BrownBorderF = Color(0xFFE8D3C2)
private val Orange500F   = Color(0xFFF7A35B)
private val Orange100F   = Color(0xFFE8D3C2)
private val Pink500F     = Color(0xFFF04268)
private val Blue400F     = Color(0xFF388AF5)
private val GrayBg       = Color(0xFFF4F4F4)
private val GrayText     = Color(0xFF6B7280)
private val TextBlack    = Color(0xFF1E120A)
private val OrangeSand   = Color(0xFFFFEDD4)

private val quickTimes = listOf(
    "09:00", "10:00", "11:00", "12:00",
    "13:00", "14:00", "15:00", "16:00",
    "17:00", "18:00", "19:00", "20:00",
)

private val dayHeaders = listOf("일", "월", "화", "수", "목", "금", "토")

// ─── 주소 검색 결과 매핑 데이터 모델 ──────────────────────────────────────────
data class SearchPlaceItem(
    val placeName: String,
    val addressName: String,
    val latitude: Float,
    val longitude: Float
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RequestFlowScreen(
    viewModel: RequestViewModel,
    matchId: Int = 0,
    onBack: () -> Unit = {},
    onComplete: () -> Unit = {},
    onAddPet: () -> Unit = {}
) {
    val context = LocalContext.current
    val isVolunteer = remember { CurrentUserStore(context).isVolunteer() }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(1) }
    var selectedPetId by remember { mutableStateOf<Int?>(viewModel.selectedPetId) }
    var selectedCategory by remember { mutableStateOf<MatchCategory?>(viewModel.selectedCategory) }

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var timeInput by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    var titleInput by remember { mutableStateOf(viewModel.title) }
    var destination by remember { mutableStateOf(viewModel.address) }
    var memo by remember { mutableStateOf(viewModel.content) }

    // ─── 주소 검색 및 좌표 데이터 상태 ───
    var showLocationSearch by remember { mutableStateOf(false) }
    var latInput by remember { mutableStateOf(viewModel.latitude) }
    var lngInput by remember { mutableStateOf(viewModel.longitude) }
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(matchId) {
        if (matchId != 0) {
            viewModel.loadMatchDetail(matchId)
        }
    }

    LaunchedEffect(viewModel.title, viewModel.address, viewModel.content, viewModel.selectedPetId, viewModel.desiredTime, viewModel.latitude, viewModel.longitude) {
        if (matchId != 0) {
            titleInput = viewModel.title
            destination = viewModel.address
            memo = viewModel.content
            selectedPetId = viewModel.selectedPetId
            timeInput = viewModel.desiredTime?.take(5) ?: ""
            selectedTime = viewModel.desiredTime?.take(5)
            latInput = viewModel.latitude
            lngInput = viewModel.longitude
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is RequestUiState.Success) {
            coroutineScope.launch { snackbarHostState.showSnackbar("요청 처리가 완료되었습니다.") }
            viewModel.resetState()
            onComplete()
        } else if (uiState is RequestUiState.Error) {
            coroutineScope.launch { snackbarHostState.showSnackbar((uiState as RequestUiState.Error).message) }
            viewModel.resetState()
        }
    }

    val buttonText = when (currentStep) {
        1    -> "→ 일정 선택"
        2    -> "→ 요청 내용 작성"
        else -> if (matchId != 0) "수정 완료하기" else "요청 등록하기"
    }

    val isNextEnabled = when (currentStep) {
        1 -> selectedPetId != null && selectedCategory != null
        2 -> selectedDay != null && (timeInput.isNotBlank() || selectedTime != null)
        else -> titleInput.isNotBlank() && destination.isNotBlank() && memo.isNotBlank()
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SiheungSnackbarHost(snackbarHostState) },
        topBar = {
            RequestFlowTopBar(
                step = currentStep,
                onBack = { if (currentStep > 1) currentStep-- else onBack() }
            )
        },
        bottomBar = {
            FlowBottomButton(
                text = buttonText,
                enabled = isNextEnabled,
                isLoading = uiState is RequestUiState.Loading && currentStep == 3
            ) {
                if (currentStep < 3) {
                    viewModel.selectedPetId = selectedPetId
                    if (currentStep == 2) {
                        val monthStr = currentMonth.monthValue.toString().padStart(2, '0')
                        val dayStr = selectedDay.toString().padStart(2, '0')
                        viewModel.desiredDate = "${currentMonth.year}-$monthStr-$dayStr"

                        val rawTime = if (timeInput.isNotBlank()) timeInput else selectedTime ?: "12:00"
                        val formattedTime = when {
                            rawTime.matches(Regex("^\\d{4}$")) -> "${rawTime.take(2)}:${rawTime.drop(2)}:00"
                            rawTime.matches(Regex("^\\d{2}:\\d{2}$")) -> "$rawTime:00"
                            rawTime.matches(Regex("^\\d{1}:\\d{2}$")) -> "0$rawTime:00"
                            rawTime.contains("오전") -> {
                                val clean = rawTime.replace("오전", "").trim()
                                val parts = clean.split(":")
                                val h = parts.getOrNull(0)?.toIntOrNull()?.toString()?.padStart(2, '0') ?: "00"
                                val m = parts.getOrNull(1)?.toIntOrNull()?.toString()?.padStart(2, '0') ?: "00"
                                "$h:$m:00"
                            }
                            rawTime.contains("오후") -> {
                                val clean = rawTime.replace("오후", "").trim()
                                val parts = clean.split(":")
                                var h = parts.getOrNull(0)?.toIntOrNull() ?: 12
                                if (h < 12) h += 12
                                val m = parts.getOrNull(1)?.toIntOrNull()?.toString()?.padStart(2, '0') ?: "00"
                                "$h:$m:00"
                            }
                            else -> if (rawTime.contains(":")) "$rawTime:00" else "12:00:00"
                        }
                        viewModel.desiredTime = formattedTime
                    }
                    currentStep++
                } else {
                    viewModel.title = titleInput
                    viewModel.address = destination
                    viewModel.content = memo
                    viewModel.latitude = latInput
                    viewModel.longitude = lngInput

                    if (matchId != 0) {
                        viewModel.updateRequest(matchId)
                    } else {
                        viewModel.submitRequest()
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            StepIndicator(currentStep = currentStep)

            when (currentStep) {
                1 -> Step1Content(
                    uiState = uiState,
                    selectedPetId = selectedPetId,
                    onSelectPet = { selectedPetId = it },
                    onAddPet = onAddPet,
                    selectedCategory = selectedCategory,
                    isVolunteer = isVolunteer,
                    onSelectCategory = {
                        selectedCategory = it
                        viewModel.setCategory(it)
                    }
                )
                2 -> Step2Content(
                    currentMonth = currentMonth,
                    onMonthChange = { currentMonth = it },
                    selectedDay = selectedDay,
                    onSelectDay = { selectedDay = it },
                    timeInput = timeInput,
                    onTimeChange = { input ->
                        timeInput = input.filter { it.isDigit() }.take(4)
                        selectedTime = null
                    },
                    selectedTime = selectedTime,
                    onSelectTime = { selectedTime = it; timeInput = it }
                )
                3 -> Step3Content(
                    title = titleInput, onTitleChange = { titleInput = it },
                    destination = destination, onDestinationChange = { destination = it },
                    memo = memo, onMemoChange = { if (it.length <= 500) memo = it },
                    onSearchClick = { showLocationSearch = true }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLocationSearch) {
        LocationSearchBottomSheet(
            sheetState = searchSheetState,
            onDismiss = { showLocationSearch = false },
            onPlaceSelected = { place ->
                destination = place.addressName
                latInput = place.latitude
                lngInput = place.longitude
            }
        )
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun RequestFlowTopBar(step: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { onBack() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로",
                tint = TextBlack,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = "도움 요청하기",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlack,
            modifier = Modifier.align(Alignment.Center)
        )
        Text(
            text = "$step / 3",
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp,
            color = Brown700F,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        )
    }
}

// ─── 스텝 인디케이터 ─────────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { idx ->
                val segStep = idx + 1
                val color = if (segStep <= currentStep) Orange500F
                else Brown900F.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            stepLabels.forEachIndexed { idx, label ->
                val segStep = idx + 1
                Text(
                    text = label,
                    fontFamily = PretendardFamily,
                    fontSize = 12.sp,
                    fontWeight = if (segStep == currentStep) FontWeight.Bold else FontWeight.Normal,
                    lineHeight = 16.sp,
                    color = if (segStep == currentStep) Orange500F else Brown700F,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── Step 1: 반려동물 선택 ────────────────────────────────────────

@Composable
private fun Step1Content(
    uiState: RequestUiState,
    selectedPetId: Int?,
    onSelectPet: (Int) -> Unit,
    onAddPet: () -> Unit = {},
    selectedCategory: MatchCategory?,
    isVolunteer: Boolean,
    onSelectCategory: (MatchCategory) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        QuestionText("어떤 반려동물과\n함께 이동하나요?")
        Spacer(Modifier.height(8.dp))
        SubText("도움이 필요한 반려동물을 선택해 주세요.")
        Spacer(Modifier.height(24.dp))

        when (uiState) {
            is RequestUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange500F)
                }
            }
            is RequestUiState.PetsLoaded -> {
                val myPets = uiState.pets
                val totalItems = myPets.size + 1
                val rowCount = (totalItems + 1) / 2

                repeat(rowCount) { rowIdx ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(2) { colIdx ->
                            val itemIdx = rowIdx * 2 + colIdx
                            Box(modifier = Modifier.weight(1f)) {
                                when {
                                    itemIdx < myPets.size -> {
                                        val pet = myPets[itemIdx]
                                        PetCard(
                                            pet = pet,
                                            isSelected = pet.id == selectedPetId,
                                            onClick = { pet.id?.let { onSelectPet(it) } }
                                        )
                                    }
                                    itemIdx == myPets.size -> AddPetCard(onAddPet)
                                    else -> Spacer(Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                    if (rowIdx < rowCount - 1) Spacer(Modifier.height(12.dp))
                }
            }
            is RequestUiState.Error -> {
                Text(uiState.message, color = Pink500F, fontFamily = PretendardFamily)
            }
            else -> {}
        }

        Spacer(Modifier.height(28.dp))
        CategorySelector(
            selected = selectedCategory,
            isVolunteer = isVolunteer,
            onSelect = onSelectCategory,
        )
    }
}

// ─── 카테고리 칩 selector ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    selected: MatchCategory?,
    isVolunteer: Boolean,
    onSelect: (MatchCategory) -> Unit,
) {
    Column {
        Text(
            text = "어떤 도움이 필요하신가요?",
            fontFamily = PretendardFamily,
            color = TextBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryFormChip(MatchCategory.WALK, "산책동행", R.drawable.ic_paw, selected == MatchCategory.WALK, enabled = true) { onSelect(MatchCategory.WALK) }
            CategoryFormChip(MatchCategory.VET, "병원동행", R.drawable.ic_stethoscope, selected == MatchCategory.VET, enabled = true) { onSelect(MatchCategory.VET) }
            CategoryFormChip(MatchCategory.SHOPPING, "장보기", R.drawable.ic_shopping_cart, selected == MatchCategory.SHOPPING, enabled = true) { onSelect(MatchCategory.SHOPPING) }
            CategoryFormChip(MatchCategory.MOVE, "이동", R.drawable.ic_car, selected == MatchCategory.MOVE, enabled = true) { onSelect(MatchCategory.MOVE) }
            CategoryFormChip(MatchCategory.OTHER, "기타", R.drawable.ic_users, selected == MatchCategory.OTHER, enabled = true) { onSelect(MatchCategory.OTHER) }
            CategoryFormChip(
                cat = MatchCategory.VOLUNTEER,
                label = "봉사 (자격 필요)",
                iconRes = R.drawable.ic_award,
                selected = selected == MatchCategory.VOLUNTEER,
                enabled = isVolunteer,
            ) { if (isVolunteer) onSelect(MatchCategory.VOLUNTEER) }
        }
        if (!isVolunteer) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "봉사 카테고리는 봉사자 자격 보유자만 사용할 수 있어요",
                fontFamily = PretendardFamily,
                color = Brown700F,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun CategoryFormChip(
    cat: MatchCategory,
    label: String,
    iconRes: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bg = when {
        selected -> Brown900F
        !enabled -> Color(0xFFF4F4F4)
        else     -> Color.White
    }
    val fg = when {
        selected -> Color.White
        !enabled -> Color(0xFFC1AEA0)
        else     -> Brown900F
    }
    val border = when {
        selected -> Brown900F
        !enabled -> Color(0xFFE0E0E0)
        else     -> BrownBorderF
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontFamily = PretendardFamily,
            color = fg,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun PetCard(pet: PetResponse, isSelected: Boolean, onClick: () -> Unit) {
    val speciesName = pet.species?.name ?: "알수없음"
    val isDog = speciesName == "DOG"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Orange500F else BrownBorderF,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(18.dp)).background(if (isDog) OrangeSand else Color(0xFFFFE4E6))
            ) {
                Icon(painter = painterResource(R.drawable.ic_pets), contentDescription = null, tint = if (isDog) Orange500F else Pink500F, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(pet.name, fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextBlack)
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${if (isDog) "강아지" else "고양이"} · ${pet.age ?: "?"}세",
                fontFamily = PretendardFamily, fontSize = 13.sp, color = Brown700F
            )
        }
        if (isSelected) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.align(Alignment.TopEnd).size(24.dp).clip(CircleShape).background(Orange500F)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "선택됨",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPetCard(onAddPet: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GrayBg)
            .dashedBorder(color = GrayText.copy(alpha = 0.4f), cornerRadius = 16.dp)
            .clickable { onAddPet() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GrayText.copy(alpha = 0.08f))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = GrayText,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "반려동물 추가",
            fontFamily = PretendardFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = GrayText
        )
    }
}

// ─── Step 2: 일정 선택 ─────────────────────────────────────────────────────────

@Composable
private fun Step2Content(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
    timeInput: String,
    onTimeChange: (String) -> Unit,
    selectedTime: String?,
    onSelectTime: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        QuestionText("언제 도움이\n필요한가요?")
        Spacer(Modifier.height(24.dp))

        CalendarSection(
            yearMonth = currentMonth,
            onMonthChange = onMonthChange,
            selectedDay = selectedDay,
            onSelectDay = onSelectDay
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "희망 시간",
            fontFamily = PretendardFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 27.sp,
            color = TextBlack
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = timeInput,
            onValueChange = onTimeChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "예: 1430 (오후 2시 30분)",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = Brown400F
                )
            },
            leadingIcon = {
                Icon(painter = painterResource(R.drawable.ic_schedule), contentDescription = null, tint = Orange500F)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange500F,
                unfocusedBorderColor = BrownBorderF,
                focusedLeadingIconColor = Orange500F,
                unfocusedLeadingIconColor = Orange500F,
                cursorColor = Orange500F,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(14.dp))
        Text(
            text = "빠른 선택",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            color = Brown700F
        )
        Spacer(Modifier.height(8.dp))

        quickTimes.chunked(4).forEach { rowTimes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTimes.forEach { time ->
                    val isSel = time == selectedTime
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Orange100F else Color.White)
                            .border(
                                width = if (isSel) 1.5.dp else 1.dp,
                                color = if (isSel) Orange500F else BrownBorderF,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectTime(time) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            color = if (isSel) Orange500F else Brown700F,
                        )
                    }
                }
                repeat(4 - rowTimes.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CalendarSection(
    yearMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
) {
    val firstDay = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.let { if (it == DayOfWeek.SUNDAY) 0 else it.value }
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalRows = (startOffset + daysInMonth + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(yearMonth.minusMonths(1)) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 달", tint = Brown700F)
            }
            Text(
                text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp,
                color = TextBlack
            )
            IconButton(onClick = { onMonthChange(yearMonth.plusMonths(1)) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 달", tint = Brown700F)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEachIndexed { idx, day ->
                val dayColor = when (idx) {
                    0    -> Pink500F
                    6    -> Blue400F
                    else -> Brown900F
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        color = dayColor
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        repeat(totalRows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val day = if (cellIndex < startOffset || cellIndex >= startOffset + daysInMonth) null
                    else cellIndex - startOffset + 1
                    Box(
                        modifier = Modifier.weight(1f).height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val isSelected = day == selectedDay
                            val cellDate = yearMonth.atDay(day)
                            val today = LocalDate.now()
                            val isPast = cellDate.isBefore(today)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Orange100F else Color.Transparent)
                                    .alpha(if (isPast) 0.3f else 1f)
                                    .clickable(enabled = !isPast) { onSelectDay(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    fontFamily = PretendardFamily,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = when {
                                        isSelected -> Orange500F
                                        col == 0   -> Pink500F
                                        else       -> Brown900F
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Step 3: 요청 내용 ─────────────────────────────────────────────────────────

@Composable
private fun Step3Content(
    title: String, onTitleChange: (String) -> Unit,
    destination: String, onDestinationChange: (String) -> Unit,
    memo: String, onMemoChange: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        QuestionText("요청 내용이\n무엇인가요?")
        Spacer(Modifier.height(28.dp))

        RequiredFieldLabel("제목")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "예: 정왕동 실외견 이동 부탁드립니다.", fontFamily = PretendardFamily, fontSize = 16.sp, color = Brown400F)
            },
            singleLine = true,
            colors = flowFieldColors(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(20.dp))

        RequiredFieldLabel("목적지")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = destination,
            onValueChange = onDestinationChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "예: 정왕 동물병원 (우측 검색 버튼 이용)", fontFamily = PretendardFamily, fontSize = 16.sp, color = Brown400F)
            },
            leadingIcon = {
                Icon(painter = painterResource(R.drawable.ic_location_on), contentDescription = null, tint = Orange500F)
            },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangeSand)
                        .clickable { onSearchClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "주소 검색",
                        fontFamily = PretendardFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Orange500F
                    )
                }
            },
            singleLine = true,
            colors = flowFieldColors(leadingAlwaysOrange = true),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(20.dp))

        RequiredFieldLabel("메모")
        Spacer(Modifier.height(6.dp))
        Box {
            OutlinedTextField(
                value = memo,
                onValueChange = onMemoChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(text = "봉사자에게 전달할 내용을 입력하세요.", fontFamily = PretendardFamily, fontSize = 16.sp, color = Brown400F)
                },
                maxLines = Int.MAX_VALUE,
                colors = flowFieldColors(),
                shape = RoundedCornerShape(16.dp)
            )
            Text(
                text = "${memo.length} / 500",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
                color = Brown400F,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun RequiredFieldLabel(text: String) {
    Text(
        text = buildAnnotatedString {
            append(text)
            withStyle(SpanStyle(color = Orange500F, fontFamily = PretendardFamily)) { append(" *") }
        },
        fontFamily = PretendardFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
        color = Brown700F
    )
}

@Composable
private fun flowFieldColors(leadingAlwaysOrange: Boolean = false) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Orange500F,
    unfocusedBorderColor = BrownBorderF,
    focusedLeadingIconColor = Orange500F,
    unfocusedLeadingIconColor = if (leadingAlwaysOrange) Orange500F else BrownBorderF,
    cursorColor = Orange500F,
)

// ─── 공통 컴포넌트 ─────────────────────────────────────────────────────────────

@Composable
private fun QuestionText(text: String) {
    Text(
        text = text,
        fontFamily = PretendardFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 33.sp,
        color = TextBlack,
    )
}

@Composable
private fun SubText(text: String) {
    Text(
        text = text,
        fontFamily = PretendardFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        color = Brown700F
    )
}

@Composable
private fun FlowBottomButton(text: String, enabled: Boolean, isLoading: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) Brown700F else Color(0xFFE5E7EB))
                .clickable(enabled = enabled && !isLoading) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = text,
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    color = if (enabled) Color.White else Brown400F
                )
            }
        }
    }
}

// ─── 주소 검색용 실시간 바텀시트 레이아웃 ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSearchBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onPlaceSelected: (SearchPlaceItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val siheungPlaces = listOf(
        SearchPlaceItem("정왕역 (4호선)", "경기 시흥시 정왕역로 170", 37.3517f, 126.7427f),
        SearchPlaceItem("시흥시청", "경기 시흥시 시청로 20", 37.3801f, 126.8029f),
        SearchPlaceItem("배grouped 생명공원", "경기 시흥시 배고5로 55", 37.3685f, 126.7171f),
        SearchPlaceItem("시흥 스마트허브 병원", "경기 시흥시 공단대로 247", 37.3384f, 126.7291f),
        SearchPlaceItem("옥구공원 반려견 놀이터", "경기 시흥시 서해안로 277", 37.3592f, 126.7022f)
    )
    val filteredPlaces = siheungPlaces.filter {
        it.placeName.contains(searchQuery) || it.addressName.contains(searchQuery)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(text = "목적지 검색", fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("장소명 또는 시흥시 도로명 주소 입력") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500F,
                    unfocusedBorderColor = BrownBorderF,
                    cursorColor = Orange500F
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                if (searchQuery.isNotBlank() && filteredPlaces.isEmpty()) {
                    item {
                        Text(text = "검색 결과가 없습니다.", color = GrayText, modifier = Modifier.padding(vertical = 16.dp), fontFamily = PretendardFamily)
                    }
                } else {
                    items(if (searchQuery.isBlank()) siheungPlaces else filteredPlaces) { place ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPlaceSelected(place)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(text = place.placeName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextBlack, fontFamily = PretendardFamily)
                            Spacer(Modifier.height(2.dp))
                            Text(text = place.addressName, fontSize = 13.sp, color = GrayText, fontFamily = PretendardFamily)
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF4F4F4))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp = 12.dp, strokeWidth: Dp = 1.5.dp): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
        )
    }

private val stepLabels = listOf("반려동물", "일정", "요청 내용")