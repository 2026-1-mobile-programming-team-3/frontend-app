package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.R
import com.example.siheunggagae.data.model.PetGender
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.model.PetSpecies
import com.example.siheunggagae.ui.viewmodel.PetListUiState
import com.example.siheunggagae.ui.viewmodel.PetListViewModel

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme
import kotlinx.coroutines.flow.MutableStateFlow

private val Brown900PL = Color(0xFF614B3A)
private val Brown700PL = Color(0xFF8A6E58)
private val Orange500PL = Color(0xFFF7A35B)
private val Pink500PL = Color(0xFFF04268)
private val Gray300PL = Color(0xFFE8E8E8)
private val OrangeSandPL = Color(0xFFFFEDD4)
private val PinkSurfacePL = Color(0xFFFEE7EC)
private val BackgroundPL = Color(0xFFFEFEFE)
private val TextBlackPL = Color(0xFF1E120A)

private val PetSpecies.iconBg   get() = if (this == PetSpecies.DOG) OrangeSandPL  else PinkSurfacePL
private val PetSpecies.iconTint get() = if (this == PetSpecies.DOG) Orange500PL   else Pink500PL
private val PetSpecies.label    get() = when (this) {
    PetSpecies.DOG   -> "강아지"
    PetSpecies.CAT   -> "고양이"
    PetSpecies.OTHER -> "기타"
}

private fun PetGender?.label() = when (this) {
    PetGender.MALE   -> "수컷"
    PetGender.FEMALE -> "암컷"
    else             -> null
}

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun PetListScreen(
    viewModel: PetListViewModel? = null,
    onBack: () -> Unit = {},
    onAddPet: () -> Unit = {},
    onEditPet: (petId: Int) -> Unit = {},
) {
    val uiState by remember(viewModel) {
        viewModel?.uiState ?: MutableStateFlow(PetListUiState.Loading)
    }.collectAsState()

    // 삭제 확인 다이얼로그 상태
    var deleteTarget by remember { mutableStateOf<PetResponse?>(null) }

    Scaffold(
        containerColor = BackgroundPL,
        topBar = { PetListTopBar(onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPet,
                shape = RoundedCornerShape(16.dp),
                containerColor = Brown900PL,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "반려동물 추가",
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is PetListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Orange500PL)
                }
            }
            is PetListUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = (uiState as PetListUiState.Error).message,
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = Brown700PL,
                            textAlign = TextAlign.Center,
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Orange500PL)
                                .clickable { viewModel?.fetchPets() }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Text("다시 시도", fontFamily = PretendardFamily, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
            is PetListUiState.Success -> {
                val pets = (uiState as PetListUiState.Success).pets
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "내 반려동물 ${pets.size}마리",
                        fontFamily = PretendardFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp,
                        color = Brown700PL,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )

                    Spacer(Modifier.height(10.dp))

                    if (pets.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "등록된 반려동물이 없어요",
                                fontFamily = PretendardFamily,
                                fontSize = 14.sp,
                                color = Brown700PL,
                            )
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            pets.forEachIndexed { index, pet ->
                                PetRow(
                                    pet = pet,
                                    onEdit = { onEditPet(pet.id) },
                                    onDelete = { deleteTarget = pet },
                                )
                                if (index < pets.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 1.dp,
                                        color = Gray300PL,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
            else -> {}
        }
    }

    // 삭제 확인 다이얼로그
    deleteTarget?.let { pet ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = {
                Text(
                    text = "반려동물 삭제",
                    fontFamily = PretendardFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlackPL,
                )
            },
            text = {
                Text(
                    text = "${pet.name}을(를) 삭제할까요?\n삭제하면 되돌릴 수 없어요.",
                    fontFamily = PretendardFamily,
                    fontSize = 14.sp,
                    color = Brown700PL,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel?.deletePet(pet.id)
                    deleteTarget = null
                }) {
                    Text(
                        text = "삭제",
                        fontFamily = PretendardFamily,
                        fontWeight = FontWeight.Bold,
                        color = Pink500PL,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(
                        text = "취소",
                        fontFamily = PretendardFamily,
                        color = Brown700PL,
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
private fun PetListTopBar(onBack: () -> Unit) {
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
                tint = TextBlackPL,
                modifier = Modifier.size(22.dp),
            )
        }

        Text(
            text = "내 반려동물",
            fontFamily = PretendardFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextBlackPL,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun PetRow(
    pet: PetResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(pet.species.iconBg),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pets),
                contentDescription = null,
                tint = pet.species.iconTint,
                modifier = Modifier.size(28.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pet.name,
                fontFamily = PretendardFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                color = TextBlackPL,
            )
            Spacer(Modifier.height(2.dp))
            val details = buildList {
                add(pet.species.label)
                pet.breed?.let { add(it) }
                pet.age?.let { add("${it}살") }
                pet.weightKg?.let { add("${it}kg") }
            }.joinToString(" · ")
            Text(
                text = details,
                fontFamily = PretendardFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = Brown700PL,
            )
        }

        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "옵션",
                tint = Brown700PL,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showMenu = true },
            )
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "수정",
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = TextBlackPL,
                        )
                    },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "삭제",
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = Pink500PL,
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                )
            }
        }
    }
}