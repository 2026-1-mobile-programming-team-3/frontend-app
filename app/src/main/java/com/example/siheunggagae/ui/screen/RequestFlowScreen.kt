package com.example.siheunggagae.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.activity.compose.BackHandler
import com.example.siheunggagae.ui.component.AppAsyncImage
import com.example.siheunggagae.ui.component.SiheungAlertDialog
import com.example.siheunggagae.ui.component.SiheungSnackbarHost
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.RequestUiState
import com.example.siheunggagae.ui.viewmodel.RequestViewModel
import com.example.siheunggagae.data.location.LocationProvider
import com.example.siheunggagae.data.model.GeoSearchResult
import com.example.siheunggagae.data.network.RetrofitClient
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId // 👈 대한민국 시간대 고정을 위한 패키지 임포트
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

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(1) }

    var selectedPetIds by remember { mutableStateOf<Set<Int>>(viewModel.selectedPetIds.toSet()) }
    var selectedCategory by remember { mutableStateOf<MatchCategory?>(viewModel.selectedCategory) }

    // 🕒 대한민국 기준 타임존 가드 상수 정의
    val kstZone = remember { ZoneId.of("Asia/Seoul") }

    var currentMonth by remember { mutableStateOf(YearMonth.now(kstZone)) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    var hourInput by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    var titleInput by remember { mutableStateOf(viewModel.title) }
    var destination by remember { mutableStateOf(viewModel.address) }
    var memo by remember { mutableStateOf(viewModel.content) }
    var selectedImageUris by remember { mutableStateOf(viewModel.selectedImageUris) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris ->
        if (uris.isNotEmpty()) {
            // 최대 5장. 기존 + 새로 선택분 누적 후 dedup, 5장 초과는 trim.
            val merged = (selectedImageUris + uris.map { it.toString() }).distinct().take(5)
            selectedImageUris = merged
            viewModel.selectedImageUris = merged
        }
    }

    var showLocationSearch by remember { mutableStateOf(false) }
    var latInput by remember { mutableStateOf(viewModel.latitude) }
    var lngInput by remember { mutableStateOf(viewModel.longitude) }
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isSubmitting by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(matchId) {
        if (matchId != 0) {
            viewModel.loadMatchDetail(matchId)
        }
    }

    LaunchedEffect(viewModel.title, viewModel.address, viewModel.content, viewModel.selectedPetIds, viewModel.desiredTime, viewModel.latitude, viewModel.longitude) {
        if (matchId != 0) {
            titleInput = viewModel.title
            destination = viewModel.address
            memo = viewModel.content
            selectedPetIds = viewModel.selectedPetIds.toSet()

            val timeParts = viewModel.desiredTime?.split(":")
            if (timeParts != null && timeParts.size >= 2) {
                hourInput = timeParts[0]
                minuteInput = timeParts[1]
            }
            selectedTime = viewModel.desiredTime?.take(5)
            latInput = viewModel.latitude
            lngInput = viewModel.longitude
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is RequestUiState.Success) {
            coroutineScope.launch { snackbarHostState.showSnackbar("요청 처리가 완료되었습니다.") }
            viewModel.resetState()
            isSubmitting = false
            onComplete()
        } else if (uiState is RequestUiState.Error) {
            coroutineScope.launch { snackbarHostState.showSnackbar((uiState as RequestUiState.Error).message) }
            viewModel.resetState()
            isSubmitting = false
        }
    }

    val isDirty = matchId == 0 && (
        selectedPetIds.isNotEmpty() || selectedCategory != null ||
        selectedDay != null || hourInput.isNotBlank() || minuteInput.isNotBlank() ||
        titleInput.isNotBlank() || destination.isNotBlank() || memo.isNotBlank()
    )

    val handleBack: () -> Unit = {
        when {
            currentStep > 1 -> currentStep--
            isDirty -> showExitConfirm = true
            else -> onBack()
        }
    }

    BackHandler(enabled = true) { handleBack() }

    val buttonText = when (currentStep) {
        1    -> "→ 일정 선택"
        2    -> "→ 요청 내용 작성"
        else -> if (matchId != 0) "수정 완료하기" else "요청 등록하기"
    }

    // ─── 🕒 [KST 대한민국 기준 과거 시간 실시간 연산 필터] ───
    val isToday = selectedDay != null && currentMonth.atDay(selectedDay!!) == LocalDate.now(kstZone)
    val now = LocalTime.now(kstZone)
    val currentHour = hourInput.toIntOrNull()
    val currentMinute = minuteInput.toIntOrNull()
    val isPastTimeSelected = isToday && currentHour != null && currentMinute != null &&
            (currentHour < now.hour || (currentHour == now.hour && currentMinute < now.minute))

    val isNextEnabled = when (currentStep) {
        1 -> selectedPetIds.isNotEmpty() && selectedCategory != null
        2 -> selectedDay != null && hourInput.isNotBlank() && minuteInput.isNotBlank() && !isPastTimeSelected
        else -> titleInput.isNotBlank() && destination.isNotBlank() && memo.isNotBlank()
    }

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SiheungSnackbarHost(snackbarHostState) },
        topBar = {
            RequestFlowTopBar(
                step = currentStep,
                onBack = handleBack
            )
        },
        bottomBar = {
            FlowBottomButton(
                text = buttonText,
                enabled = isNextEnabled && !isSubmitting,
                isLoading = (uiState is RequestUiState.Loading && currentStep == 3) || isSubmitting
            ) {
                if (currentStep < 3) {
                    viewModel.selectedPetIds = selectedPetIds.toList()
                    if (currentStep == 2) {
                        val monthStr = currentMonth.monthValue.toString().padStart(2, '0')
                        val dayStr = selectedDay.toString().padStart(2, '0')
                        viewModel.desiredDate = "${currentMonth.year}-$monthStr-$dayStr"

                        val hStr = hourInput.padStart(2, '0')
                        val mStr = minuteInput.padStart(2, '0')
                        viewModel.desiredTime = "$hStr:$mStr:00"
                    }
                    currentStep++
                } else {
                    if (isSubmitting) return@FlowBottomButton
                    isSubmitting = true

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
                    selectedPetIds = selectedPetIds,
                    onTogglePet = { petId ->
                        selectedPetIds = if (selectedPetIds.contains(petId)) {
                            selectedPetIds - petId
                        } else {
                            selectedPetIds + petId
                        }
                    },
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
                    hourInput = hourInput,
                    onHourChange = { hourInput = it; selectedTime = if(it.length == 2 && minuteInput.length == 2) "$it:$minuteInput" else null },
                    minuteInput = minuteInput,
                    onMinuteChange = { minuteInput = it; selectedTime = if(hourInput.length == 2 && it.length == 2) "$hourInput:$it" else null },
                    selectedTime = selectedTime,
                    onSelectTime = { selectedTime = it },
                    isPastTimeSelected = isPastTimeSelected,
                    kstZone = kstZone
                )
                3 -> Step3Content(
                    title = titleInput, onTitleChange = { titleInput = it },
                    destination = destination, onDestinationChange = { destination = it },
                    memo = memo, onMemoChange = { if (it.length <= 500) memo = it },
                    onSearchClick = { showLocationSearch = true },
                    imageUris = selectedImageUris,
                    onPickImages = {
                        imagePickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    onRemoveImage = { uri ->
                        val next = selectedImageUris.filterNot { it == uri }
                        selectedImageUris = next
                        viewModel.selectedImageUris = next
                    },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLocationSearch) {
        LocationSearchBottomSheet(
            sheetState = searchSheetState,
            initialQuery = destination,
            onDismiss = { showLocationSearch = false },
            onPlaceSelected = { place ->
                destination = place.placeName.ifBlank { place.roadAddress ?: place.address ?: "" }
                latInput = place.lat.toFloat()
                lngInput = place.lng.toFloat()
            }
        )
    }

    if (showExitConfirm) {
        SiheungAlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = "작성을 그만두시겠어요?",
            text = "지금까지 입력한 내용이 사라져요.",
            confirmText = "나가기",
            confirmColor = Color(0xFFEE6A46),
            onConfirm = {
                showExitConfirm = false
                onBack()
            },
            dismissText = "계속 작성",
            onDismiss = { showExitConfirm = false }
        )
    }
}

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

@Composable
private fun Step1Content(
    uiState: RequestUiState,
    selectedPetIds: Set<Int>,
    onTogglePet: (Int) -> Unit,
    onAddPet: () -> Unit = {},
    selectedCategory: MatchCategory?,
    isVolunteer: Boolean,
    onSelectCategory: (MatchCategory) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        QuestionText("어떤 반려동물과\n함께 이동하나요?")
        Spacer(Modifier.height(8.dp))
        SubText("도움이 필요한 반려동물을 선택해 주세요. (중복 선택 가능)")
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
                                            isSelected = selectedPetIds.contains(pet.id),
                                            onClick = { pet.id?.let { onTogglePet(it) } }
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = GrayText
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "탭하여 등록하기",
            fontFamily = PretendardFamily,
            fontSize = 13.sp,
            color = GrayText.copy(alpha = 0.6f)
        )
    }
}

// ─── 🕒 대한민국 시간 기준 동기화 가드 통합 섹션 ───
@Composable
private fun Step2Content(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
    hourInput: String,
    onHourChange: (String) -> Unit,
    minuteInput: String,
    onMinuteChange: (String) -> Unit,
    selectedTime: String?,
    onSelectTime: (String) -> Unit,
    isPastTimeSelected: Boolean,
    kstZone: ZoneId
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        QuestionText("언제 도움이\n필요한가요?")
        Spacer(Modifier.height(24.dp))

        CalendarSection(
            yearMonth = currentMonth,
            onMonthChange = onMonthChange,
            selectedDay = selectedDay,
            onSelectDay = onSelectDay,
            kstZone = kstZone
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
        Spacer(Modifier.height(16.dp))

        // 🛠️ 1번, 2번 개선 완료: 직접 설정 칩을 상단으로 올리고 콤팩트 키패드 입력창 교체 (짤림 완벽 해결)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hourInput,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(2)
                    val num = filtered.toIntOrNull()
                    if (filtered.isEmpty() || (num != null && num in 0..23)) {
                        onHourChange(filtered)
                    }
                },
                modifier = Modifier.width(72.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = PretendardFamily),
                placeholder = { Text("00", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500F, unfocusedBorderColor = BrownBorderF, cursorColor = Orange500F),
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                text = "시",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, end = 16.dp),
                color = TextBlack
            )

            OutlinedTextField(
                value = minuteInput,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(2)
                    val num = filtered.toIntOrNull()
                    if (filtered.isEmpty() || (num != null && num in 0..59)) {
                        onMinuteChange(filtered)
                    }
                },
                modifier = Modifier.width(72.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = PretendardFamily),
                placeholder = { Text("00", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Orange500F, unfocusedBorderColor = BrownBorderF, cursorColor = Orange500F),
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                text = "분",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp),
                color = TextBlack
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "빠른 선택",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            color = Brown700F
        )
        Spacer(Modifier.height(12.dp))

        // 🛠️ 3번 개선 완료: 네이버 예매 시스템 표준화 (대한민국 KST 기준 실시간 과거 버튼 회색 비활성화 동결)
        val now = LocalTime.now(kstZone)
        val isToday = (selectedDay != null && currentMonth.atDay(selectedDay) == LocalDate.now(kstZone))

        quickTimes.chunked(4).forEach { rowTimes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTimes.forEach { time ->
                    val tabHour = time.take(2).toIntOrNull() ?: 0
                    val tabMinute = time.drop(3).toIntOrNull() ?: 0
                    val isPast = isToday && (tabHour < now.hour || (tabHour == now.hour && tabMinute < now.minute))
                    val isSel = time == selectedTime

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPast) Color(0xFFF4F4F4) else if (isSel) Orange100F else Color.White)
                            .border(
                                width = if (isSel && !isPast) 1.5.dp else 1.dp,
                                color = if (isPast) Color.Transparent else if (isSel) Orange500F else BrownBorderF,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = !isPast) {
                                onSelectTime(time)
                                onHourChange(time.take(2))
                                onMinuteChange(time.drop(3))
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            color = if (isPast) GrayText else if (isSel) Orange500F else Brown700F,
                        )
                    }
                }
                repeat(4 - rowTimes.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (isPastTimeSelected) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "현재 시간 이후의 시간을 선택해 주세요.",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Pink500F,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CalendarSection(
    yearMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDay: Int?,
    onSelectDay: (Int) -> Unit,
    kstZone: ZoneId
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
                            val today = LocalDate.now(kstZone)
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

@Composable
private fun Step3Content(
    title: String, onTitleChange: (String) -> Unit,
    destination: String, onDestinationChange: (String) -> Unit,
    memo: String, onMemoChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    imageUris: List<String> = emptyList(),
    onPickImages: () -> Unit = {},
    onRemoveImage: (String) -> Unit = {},
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

        Spacer(Modifier.height(20.dp))

        Text(
            text = "사진 (선택, 최대 5장)",
            fontFamily = PretendardFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            color = Brown700F,
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (imageUris.size < 5) {
                item(key = "picker") {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BrownBorderF, RoundedCornerShape(12.dp))
                            .background(OrangeSand.copy(alpha = 0.3f))
                            .clickable(onClickLabel = "사진 추가") { onPickImages() },
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null,
                                tint = Orange500F,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${imageUris.size}/5",
                                fontFamily = PretendardFamily,
                                fontSize = 11.sp,
                                color = Brown700F,
                            )
                        }
                    }
                }
            }
            items(imageUris, key = { it }) { uri ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BrownBorderF, RoundedCornerShape(12.dp)),
                ) {
                    AppAsyncImage(
                        model = uri,
                        contentDescription = "첨부 사진",
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                            .clickable(onClickLabel = "사진 제거") { onRemoveImage(uri) },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_x),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
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

@Composable
fun QuestionText(text: String) {
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
fun SubText(text: String) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSearchBottomSheet(
    sheetState: androidx.compose.material3.SheetState,
    initialQuery: String = "",
    onDismiss: () -> Unit,
    onPlaceSelected: (GeoSearchResult) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var searchResults by remember { mutableStateOf<List<GeoSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    var userCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    LaunchedEffect(Unit) {
        val loc = runCatching { LocationProvider(context).getLocationOrNull() }.getOrNull()
        if (loc != null) userCoords = loc.longitude to loc.latitude
        delay(180L)
        runCatching { focusRequester.requestFocus() }
    }

    LaunchedEffect(searchQuery, userCoords) {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            searchError = null
            return@LaunchedEffect
        }
        delay(300L)
        isSearching = true
        searchError = null
        val (cx, cy) = userCoords ?: (126.8030 to 37.3799)
        val response = runCatching {
            RetrofitClient.api.searchGeo(
                query = searchQuery,
                x = cx,
                y = cy,
                radius = 20000,
                size = 15,
            )
        }
        isSearching = false
        val raw = response.getOrNull()
        val body = raw?.body()
        if (response.isSuccess && raw?.isSuccessful == true && body != null) {
            searchResults = body.results
        } else {
            searchError = "검색 중 오류가 발생했어요"
            searchResults = emptyList()
        }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("장소명 또는 도로명 주소 입력") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange500F,
                    unfocusedBorderColor = BrownBorderF,
                    cursorColor = Orange500F
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.heightIn(max = 480.dp)) {
                when {
                    searchQuery.isBlank() -> {
                        item {
                            Text(
                                text = "장소나 매장명을 입력해 주세요",
                                fontFamily = PretendardFamily,
                                fontSize = 13.sp,
                                color = Brown700F,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    }
                    isSearching -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Orange500F,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    searchError != null -> {
                        item {
                            Text(
                                text = searchError!!,
                                fontFamily = PretendardFamily,
                                fontSize = 13.sp,
                                color = Color(0xFFE84B6A),
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    }
                    searchResults.isEmpty() -> {
                        item {
                            Text(
                                text = "검색 결과가 없어요",
                                fontFamily = PretendardFamily,
                                fontSize = 13.sp,
                                color = Brown700F,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    }
                    else -> {
                        items(searchResults) { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPlaceSelected(place)
                                        onDismiss()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_location_on),
                                    contentDescription = null,
                                    tint = Orange500F,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = place.placeName,
                                        fontFamily = PretendardFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    val addr = place.roadAddress ?: place.address
                                    if (!addr.isNullOrBlank()) {
                                        Text(
                                            text = addr,
                                            fontFamily = PretendardFamily,
                                            fontSize = 11.sp,
                                            color = Brown700F,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                val distance = place.distanceMeters
                                if (distance != null && distance > 0) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = formatDistance(distance),
                                        fontFamily = PretendardFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Brown400F,
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF4F4F4))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun formatDistance(meters: Double): String =
    if (meters < 1000.0) "${meters.toInt()}m"
    else "%.1fkm".format(meters / 1000.0)

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp = 12.dp, strokeWidth: Dp = 1.5.dp): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
        )
    }

private val stepLabels = listOf("반려동물", "일정", "요청 내용")