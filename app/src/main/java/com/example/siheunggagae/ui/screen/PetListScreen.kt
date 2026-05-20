package com.example.siheunggagae.ui.screen

import com.example.siheunggagae.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import com.example.siheunggagae.data.model.PetResponse
import com.example.siheunggagae.data.model.PetSpecies
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.viewmodel.RequestUiState
import com.example.siheunggagae.ui.viewmodel.RequestViewModel

private val Brown900PL = Color(0xFF614B3A)
private val Brown700PL = Color(0xFF8A6E58)
private val Orange500PL = Color(0xFFF7A35B)
private val Pink500PL = Color(0xFFF04268)
private val Gray300PL = Color(0xFFE8E8E8)
private val OrangeSandPL = Color(0xFFFFEDD4)
private val PinkSurfacePL = Color(0xFFFEE7EC)
private val BackgroundPL = Color(0xFFFEFEFE)
private val TextBlackPL = Color(0xFF1E120A)

private val PetSpecies.iconBg get() = if (this == PetSpecies.DOG) OrangeSandPL else PinkSurfacePL
private val PetSpecies.iconTint get() = if (this == PetSpecies.DOG) Orange500PL else Pink500PL
private val PetSpecies.label get() = when(this) { PetSpecies.DOG -> "강아지"
    PetSpecies.CAT -> "고양이" else -> "기타" }

@Composable
fun PetListScreen(viewModel: RequestViewModel, onBack: () -> Unit = {}, onAddPet: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.fetchMyPets() }

    Scaffold(
        containerColor = BackgroundPL,
        topBar = { PetListTopBar(onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPet, shape = RoundedCornerShape(16.dp), containerColor = Brown900PL, contentColor = Color.White, modifier = Modifier.size(56.dp)) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = "추가", modifier = Modifier.size(24.dp))
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            when (val state = uiState) {
                is RequestUiState.Loading -> CircularProgressIndicator(color = Orange500PL, modifier = Modifier.align(Alignment.Center))
                is RequestUiState.Error -> Text(state.message, color = Pink500PL, fontFamily = PretendardFamily, modifier = Modifier.align(Alignment.Center))
                is RequestUiState.PetsLoaded -> {
                    val myPets = state.pets
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(Modifier.height(16.dp))
                        Text("내 반려동물 ${myPets.size}마리", fontFamily = PretendardFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Brown700PL, modifier = Modifier.padding(horizontal = 4.dp))
                        Spacer(Modifier.height(10.dp))
                        if (myPets.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                                Text("등록된 반려동물이 없습니다.\n우측 하단 버튼을 눌러 추가해주세요.", fontFamily = PretendardFamily, fontSize = 14.sp, color = Brown700PL, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        } else {
                            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(Color.White), modifier = Modifier.fillMaxWidth()) {
                                myPets.forEachIndexed { index, pet ->
                                    PetRow(pet = pet)
                                    if (index < myPets.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Gray300PL)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PetListTopBar(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.CenterStart).size(40.dp).shadow(2.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable { onBack() }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로", tint = TextBlackPL, modifier = Modifier.size(22.dp))
        }
        Text("내 반려동물", fontFamily = PretendardFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextBlackPL, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun PetRow(pet: PetResponse) {
    Row(modifier = Modifier.fillMaxWidth().clickable { }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(pet.species.iconBg)) {
            Icon(painterResource(R.drawable.ic_pets), contentDescription = null, tint = pet.species.iconTint, modifier = Modifier.size(28.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(pet.name, fontFamily = PretendardFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlackPL)
            Spacer(Modifier.height(2.dp))
            Text("${pet.species.label} · ${pet.breed?.ifBlank { null } ?: "품종 모름"} · ${pet.age?.let { "${it}살" } ?: "나이 모름"} · ${pet.weightKg?.let { "${it}kg" } ?: "몸무게 비공개"}", fontFamily = PretendardFamily, fontSize = 12.sp, color = Brown700PL)
        }
    }
}