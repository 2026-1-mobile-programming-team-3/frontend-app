package com.example.siheunggagae.ui.screen


import android.content.Intent
import android.graphics.ImageDecoder
import androidx.compose.runtime.collectAsState
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.PetGender
import com.example.siheunggagae.ui.component.SiheungAlertDialog
import com.example.siheunggagae.data.model.PetSpecies
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import com.example.siheunggagae.ui.viewmodel.PetAddUiState
import com.example.siheunggagae.ui.viewmodel.PetAddViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.YearMonth

private val Brown900PA    = Color(0xFF614B3A)
private val Brown700PA    = Color(0xFF8A6E58)
private val Brown400PA    = Color(0xFFC4A882)
private val BrownBorderPA = Color(0xFFE8D3C2)
private val Orange500PA   = Color(0xFFF7A35B)
private val Gray300PA     = Color(0xFFE8E8E8)
private val OrangeSandPA  = Color(0xFFFFEDD4)
private val BackgroundPA  = Color(0xFFFEFEFE)
private val TextBlackPA   = Color(0xFF1E120A)

private val speciesOptions = listOf("강아지", "고양이", "기타")
private val genderOptions  = listOf("수컷", "암컷")
private val ageUnitOptions = listOf("살", "개월")

private fun String.toPetSpecies() = when (this) {
    "강아지" -> PetSpecies.DOG
    "고양이" -> PetSpecies.CAT
    else    -> PetSpecies.OTHER
}
private fun PetSpecies.toLabel() = when (this) {
    PetSpecies.DOG   -> "강아지"
    PetSpecies.CAT   -> "고양이"
    PetSpecies.OTHER -> "기타"
}
private fun String.toPetGender() = when (this) {
    "수컷" -> PetGender.MALE
    "암컷" -> PetGender.FEMALE
    else  -> null
}
private fun PetGender?.toLabel() = when (this) {
    PetGender.MALE   -> "수컷"
    PetGender.FEMALE -> "암컷"
    else             -> "수컷"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetAddScreen(
    viewModel: PetAddViewModel? = null,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: kotlinx.coroutines.flow.MutableStateFlow(PetAddUiState.Idle)
    }.collectAsState()
    val initialPet by remember(viewModel) {
        viewModel?.initialPet ?: kotlinx.coroutines.flow.MutableStateFlow(null)
    }.collectAsState()
    val localPhotoUri by remember(viewModel) {
        viewModel?.localPhotoUri ?: kotlinx.coroutines.flow.MutableStateFlow(null)
    }.collectAsState()

    var photoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(localPhotoUri) {
        photoBitmap = if (localPhotoUri != null) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(localPhotoUri)
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source).asImageBitmap()
                }.getOrNull()
            }
        } else null
    }

    val imagePicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel?.setLocalPhotoUri(uri.toString())
            }.onFailure {
                android.widget.Toast.makeText(
                    context,
                    "사진을 불러올 수 없어요. 다시 시도해 주세요.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val isEditMode = viewModel?.isEditMode == true
    var formInitialized by rememberSaveable { mutableStateOf(!isEditMode) }

    var nameInput       by rememberSaveable { mutableStateOf("") }
    var selectedSpecies by rememberSaveable { mutableStateOf("강아지") }
    var breedInput      by rememberSaveable { mutableStateOf("") }
    var age             by rememberSaveable { mutableIntStateOf(1) }
    var ageUnit         by rememberSaveable { mutableStateOf("살") }
    var gender          by rememberSaveable { mutableStateOf("수컷") }
    var isNeutered      by rememberSaveable { mutableStateOf(false) }
    var noteInput       by rememberSaveable { mutableStateOf("") }

    var hasBirthDate      by rememberSaveable { mutableStateOf(false) }
    var selectedBirthDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker    by remember { mutableStateOf(false) }

    LaunchedEffect(selectedBirthDate, hasBirthDate) {
        if (hasBirthDate && selectedBirthDate != null) {
            val now = LocalDate.now(ZoneId.of("Asia/Seoul"))
            val period = Period.between(selectedBirthDate, now)
            if (period.years > 0) {
                age = period.years
                ageUnit = "살"
            } else {
                age = period.months.coerceAtLeast(1)
                ageUnit = "개월"
            }
        }
    }

    LaunchedEffect(initialPet) {
        if (!formInitialized && initialPet != null) {
            val p = initialPet!!
            nameInput       = p.name
            selectedSpecies = p.species.toLabel()
            breedInput      = p.breed ?: ""
            age             = p.age ?: 1
            gender          = p.gender.toLabel()
            isNeutered      = p.isNeutered
            noteInput       = p.note ?: ""
            formInitialized = true
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is PetAddUiState.SaveSuccess -> {
                Toast.makeText(context, "성공적으로 저장되었습니다!", Toast.LENGTH_SHORT).show()
                viewModel?.clearError()
                onBack()
            }
            is PetAddUiState.Error -> {
                Toast.makeText(context, (uiState as PetAddUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel?.clearError()
            }
            else -> {}
        }
    }

    val isSaving = uiState is PetAddUiState.Saving
    val isLoading = uiState is PetAddUiState.Loading
    val canSave = nameInput.isNotBlank() && !isSaving && !isLoading

    val isDirty = if (isEditMode) {
        initialPet?.let { p ->
            nameInput != p.name ||
            selectedSpecies != p.species.toLabel() ||
            breedInput != (p.breed ?: "") ||
            age != (p.age ?: 1) ||
            gender != p.gender.toLabel() ||
            isNeutered != p.isNeutered ||
            noteInput != (p.note ?: "") ||
            localPhotoUri != null
        } ?: false
    } else {
        nameInput.isNotBlank() || breedInput.isNotBlank() ||
            noteInput.isNotBlank() || localPhotoUri != null ||
            hasBirthDate
    }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val handleBack: () -> Unit = { if (isDirty) showDiscardDialog = true else onBack() }
    BackHandler(enabled = isDirty) { showDiscardDialog = true }

    Scaffold(
        containerColor = BackgroundPA,
        topBar = {
            PetAddTopBar(
                title = if (isEditMode) "반려동물 수정" else "반려동물 추가",
                onBack = handleBack,
            )
        },
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
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (canSave) Brown900PA else Gray300PA)
                        .then(if (canSave) Modifier.clickable {
                            viewModel?.save(
                                name = nameInput,
                                species = selectedSpecies.toPetSpecies(),
                                breed = breedInput.takeIf { it.isNotBlank() },
                                age = age,
                                gender = gender.toPetGender(),
                                isNeutered = isNeutered,
                                note = noteInput.takeIf { it.isNotBlank() },
                                birthDate = if (hasBirthDate) selectedBirthDate?.toString() else null
                            )
                        } else Modifier),
                ) {
                    if (isSaving || isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = "저장하기", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = if (canSave) Color.White else Brown700PA)
                    }
                }
            }
        },
    ) { innerPadding ->
        if (isLoading && !isSaving) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange500PA)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                PetPreviewCard(
                    name = nameInput,
                    species = selectedSpecies,
                    age = age,
                    ageUnit = ageUnit,
                    gender = gender,
                    photoBitmap = photoBitmap,
                    onPickPhoto = { imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                )

                PetSectionLabelPA("기본 정보")
                BasicInfoCard(
                    nameInput = nameInput,
                    onNameChange = { nameInput = it },
                    selectedSpecies = selectedSpecies,
                    onSpeciesSelect = { selectedSpecies = it },
                    speciesEnabled = !isEditMode,
                    breedInput = breedInput,
                    onBreedChange = { breedInput = it },
                )

                PetSectionLabelPA("상세 정보")
                DetailInfoCard(
                    age = age,
                    ageUnit = ageUnit,
                    onDecrement = { if (age > 1) age-- },
                    onIncrement = { val max = if (ageUnit == "개월") 36 else 30; if (age < max) age++ },
                    onAgeUnitSelect = { ageUnit = it },
                    gender = gender,
                    onGenderSelect = { gender = it },
                    isNeutered = isNeutered,
                    onNeuteredChange = { isNeutered = it },
                    hasBirthDate = hasBirthDate,
                    onHasBirthDateChange = {
                        hasBirthDate = it
                        if(!it) selectedBirthDate = null
                    },
                    selectedBirthDate = selectedBirthDate,
                    onPickDateClick = { showDatePicker = true }
                )

                NoteSection(note = noteInput, onNoteChange = { noteInput = it.take(300) })
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedBirthDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("확인", color = Orange500PA, fontWeight = FontWeight.Bold, fontFamily = PretendardFamily) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소", color = Brown700PA, fontFamily = PretendardFamily) }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Orange500PA,
                    todayContentColor = Orange500PA,
                    selectedDayContentColor = Color.White
                )
            )
        }
    }

    if (showDiscardDialog) {
        SiheungAlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = "작성을 그만두시겠어요?",
            text = "입력한 내용이 사라져요.",
            confirmText = "나가기",
            confirmColor = Color(0xFFEE6A46),
            onConfirm = {
                showDiscardDialog = false
                onBack()
            },
            dismissText = "계속 작성",
            onDismiss = { showDiscardDialog = false },
        )
    }
}

@Composable
private fun PetAddTopBar(title: String, onBack: () -> Unit) {
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
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로", tint = TextBlackPA, modifier = Modifier.size(22.dp))
        }
        Text(text = title, fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp, color = TextBlackPA, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun PetPreviewCard(
    name: String,
    species: String,
    age: Int,
    ageUnit: String,
    gender: String,
    photoBitmap: ImageBitmap? = null,
    onPickPhoto: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OrangeSandPA),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).clickable { onPickPhoto() },
                ) {
                    if (photoBitmap != null) {
                        Image(bitmap = photoBitmap, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(painter = painterResource(R.drawable.ic_pets), contentDescription = null, tint = Orange500PA, modifier = Modifier.size(32.dp))
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.BottomEnd).size(20.dp).clip(CircleShape).background(Brown900PA),
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "사진 변경", tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "미리보기", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp, color = Brown700PA)
                Text(text = name.ifBlank { "이름을 입력하세요" }, fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = if (name.isBlank()) Brown400PA else TextBlackPA)
                Text(text = "$species · ${age}$ageUnit · $gender", fontFamily = PretendardFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Brown700PA)
            }
        }
    }
}

@Composable
private fun PetSectionLabelPA(label: String, isBlack: Boolean = false) {
    Text(
        text = label,
        fontFamily = PretendardFamily,
        fontSize = if (isBlack) 13.sp else 12.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = if (isBlack) 20.sp else 16.sp,
        color = if (isBlack) TextBlackPA else Brown700PA,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun BasicInfoCard(
    nameInput: String,
    onNameChange: (String) -> Unit,
    selectedSpecies: String,
    onSpeciesSelect: (String) -> Unit,
    speciesEnabled: Boolean = true,
    breedInput: String,
    onBreedChange: (String) -> Unit,
) {
    PetInfoCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "이름", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackPA)
            Spacer(Modifier.weight(1f))
            BasicTextField(
                value = nameInput,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackPA, textAlign = TextAlign.End),
                cursorBrush = SolidColor(Orange500PA),
                modifier = Modifier.width(160.dp),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
                        if (nameInput.isEmpty()) {
                            Text(text = "예: 파댕이", fontFamily = PretendardFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Brown400PA, textAlign = TextAlign.End)
                        }
                        inner()
                    }
                },
            )
        }

        PetCardDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "종류", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackPA)
            Spacer(Modifier.weight(1f))
            speciesOptions.forEach { option ->
                PetSelectChip(label = option, selected = option == selectedSpecies, enabled = speciesEnabled, onClick = { if (speciesEnabled) onSpeciesSelect(option) })
            }
        }

        PetCardDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "품종(선택)", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackPA)
            Spacer(Modifier.weight(1f))
            BasicTextField(
                value = breedInput,
                onValueChange = onBreedChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = PretendardFamily, fontSize = 14.sp, color = TextBlackPA, textAlign = TextAlign.End),
                cursorBrush = SolidColor(Orange500PA),
                modifier = Modifier.width(160.dp),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
                        if (breedInput.isEmpty()) {
                            Text(text = "예: 말티즈", fontFamily = PretendardFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Brown400PA, textAlign = TextAlign.End)
                        }
                        inner()
                    }
                },
            )
        }
    }
}

@Composable
private fun DetailInfoCard(
    age: Int,
    ageUnit: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onAgeUnitSelect: (String) -> Unit,
    gender: String,
    onGenderSelect: (String) -> Unit,
    isNeutered: Boolean,
    onNeuteredChange: (Boolean) -> Unit,
    hasBirthDate: Boolean,
    onHasBirthDateChange: (Boolean) -> Unit,
    selectedBirthDate: LocalDate?,
    onPickDateClick: () -> Unit
) {
    PetInfoCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "나이", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackPA)
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AgeControlButton(enabled = !hasBirthDate, icon = { Text("−", color = Brown700PA, fontSize = 16.sp, fontFamily = PretendardFamily, fontWeight = FontWeight.Bold) }, onClick = onDecrement)
                Text(
                    text = "$age",
                    fontFamily = PretendardFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp,
                    color = TextBlackPA,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center,
                )
                AgeControlButton(enabled = !hasBirthDate, icon = { Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, tint = Brown700PA, modifier = Modifier.size(16.dp)) }, onClick = onIncrement)

                Spacer(Modifier.width(4.dp))

                ageUnitOptions.forEach { unit ->
                    PetSelectChip(
                        label = unit,
                        selected = unit == ageUnit,
                        enabled = !hasBirthDate,
                        onClick = { onAgeUnitSelect(unit) },
                    )
                }
            }
        }

        PetCardDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = hasBirthDate,
                onCheckedChange = onHasBirthDateChange,
                colors = CheckboxDefaults.colors(checkedColor = Brown900PA, checkmarkColor = Color.White)
            )
            Text(
                text = "정확한 생년월일을 알고 있어요",
                fontFamily = PretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextBlackPA,
                modifier = Modifier.clickable { onHasBirthDateChange(!hasBirthDate) }
            )
        }

        AnimatedVisibility(visible = hasBirthDate) {
            // ─── 🛠️ [패딩 규격 오버로드 불일치 오류 수정 구역] ───
            Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                OutlinedButton(
                    onClick = onPickDateClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Brown900PA),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrownBorderPA)
                ) {
                    Text(
                        text = selectedBirthDate?.toString() ?: "달력에서 생년월일 선택하기",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (hasBirthDate) PetCardDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "성별", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackPA)
            Spacer(Modifier.weight(1f))
            genderOptions.forEach { option ->
                PetSelectChip(label = option, selected = option == gender, onClick = { onGenderSelect(option) })
            }
        }

        PetCardDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "중성화", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp, color = TextBlackPA)
            Spacer(Modifier.weight(1f))
            Switch(
                checked = isNeutered,
                onCheckedChange = onNeuteredChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, checkedTrackColor = Brown900PA, checkedBorderColor = Brown900PA,
                    uncheckedThumbColor = Color.White, uncheckedTrackColor = Gray300PA, uncheckedBorderColor = Gray300PA,
                ),
            )
        }
    }
}

@Composable
private fun NoteSection(note: String, onNoteChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PetSectionLabelPA("특징 및 주의사항", isBlack = true)
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                textStyle = TextStyle(fontFamily = PretendardFamily, fontSize = 16.sp, lineHeight = 24.sp, color = TextBlackPA),
                cursorBrush = SolidColor(Orange500PA),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .border(1.dp, BrownBorderPA, RoundedCornerShape(12.dp))
                    .padding(top = 13.dp, start = 17.dp, end = 17.dp, bottom = 1.dp)
                    .padding(bottom = 24.dp),
                decorationBox = { inner ->
                    if (note.isEmpty()) {
                        Text(text = "알러지, 질환, 성격 등 참고할 만한 내용을 자유롭게 적어주세요.", fontFamily = PretendardFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp, color = Brown400PA)
                    }
                    inner()
                },
            )
            Text(
                text = "${note.length} / 300",
                fontFamily = PretendardFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp,
                color = Brown700PA,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun PetInfoCard(content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { content() }
}

@Composable
private fun PetCardDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
}

@Composable
private fun PetSelectChip(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val bgColor = when {
        !enabled && selected -> Brown400PA
        selected             -> Brown900PA
        else                 -> Color.White
    }
    val textColor = when {
        !enabled -> Brown400PA
        selected -> Color.White
        else     -> Brown700PA
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .then(if (!selected) Modifier.border(1.dp, BrownBorderPA, RoundedCornerShape(50.dp)) else Modifier)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(text = label, fontFamily = PretendardFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, color = textColor)
    }
}

@Composable
private fun AgeControlButton(enabled: Boolean = true, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (enabled) BrownBorderPA else BrownBorderPA.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled) { onClick() },
    ) {
        icon()
    }
}