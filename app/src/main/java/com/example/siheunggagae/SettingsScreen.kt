package com.example.siheunggagae

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.ui.theme.PretendardFamily
import com.example.siheunggagae.ui.theme.SiheungGagaeTheme

// 스펙 컬러
private val Brown900St    = Color(0xFF614B3A)
private val Brown700St    = Color(0xFF8A6E58)
private val Orange500St   = Color(0xFFF7A35B)
private val Pink500St     = Color(0xFFF04268)
private val Gray300St     = Color(0xFFE8E8E8)
private val OrangeSandSt  = Color(0xFFFFEDD4)
private val PinkSurfaceSt = Color(0xFFFEE7EC)
private val BackgroundSt  = Color(0xFFFEFEFE)
private val TextBlackSt   = Color(0xFF1E120A)

// ─── 메인 화면 ─────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onPetListClick: () -> Unit = {},
) {
    var matchingNotif      by remember { mutableStateOf(true) }
    var announcementNotif  by remember { mutableStateOf(true) }
    var reviewNotif        by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundSt,
        topBar = { SettingsTopBar(onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingsSectionLabel("계정")
            SettingsSectionCard {
                SettingsNavItem(icon = Icons.Default.AccountCircle, label = "프로필 편집", onClick = {})
                SettingsDivider()
                SettingsNavItem(icon = Icons.Default.Pets, label = "반려동물 정보", onClick = onPetListClick)
                SettingsDivider()
                SettingsNavItem(
                    icon = Icons.Default.Leaderboard,
                    label = "봉사 이력",
                    rightContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "3건",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 20.sp,
                                color = Orange500St
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Gray300St, modifier = Modifier.size(20.dp))
                        }
                    },
                    onClick = {},
                )
            }

            Spacer(Modifier.height(4.dp))

            SettingsSectionLabel("알림")
            SettingsSectionCard {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    label = "매칭 알림",
                    checked = matchingNotif,
                    onCheckedChange = { matchingNotif = it },
                )
                SettingsDivider()
                SettingsSwitchItem(
                    icon = Icons.Default.Campaign,
                    label = "공지 알림",
                    checked = announcementNotif,
                    onCheckedChange = { announcementNotif = it },
                )
                SettingsDivider()
                SettingsSwitchItem(
                    icon = Icons.Default.ChatBubble,
                    label = "리뷰 알림",
                    checked = reviewNotif,
                    onCheckedChange = { reviewNotif = it },
                )
            }

            Spacer(Modifier.height(4.dp))

            SettingsSectionLabel("앱")
            SettingsSectionCard {
                SettingsNavItem(
                    icon = Icons.Default.LocationOn,
                    label = "위치 설정",
                    rightContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "정왕동",
                                fontFamily = PretendardFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 20.sp,
                                color = Orange500St
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Gray300St, modifier = Modifier.size(20.dp))
                        }
                    },
                    onClick = {},
                )
                SettingsDivider()
                SettingsNavItem(
                    icon = Icons.Default.PhoneAndroid,
                    label = "버전",
                    rightContent = {
                        Text(
                            text = "1.0.0",
                            fontFamily = PretendardFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 20.sp,
                            color = Orange500St
                        )
                    },
                    onClick = null,
                )
            }

            Spacer(Modifier.height(4.dp))

            SettingsSectionLabel("기타")
            SettingsSectionCard {
                SettingsNavItem(icon = Icons.AutoMirrored.Filled.Help, label = "도움말", onClick = {})
                SettingsDivider()
                SettingsNavItem(icon = Icons.Default.Lock, label = "개인정보 처리 방침", onClick = {})
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PinkSurfaceSt),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Pink500St, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "로그아웃",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        color = Pink500St
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
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
                tint = TextBlackSt,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = "설정",
            fontFamily = PretendardFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            color = TextBlackSt,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

// ─── 공통 컴포넌트 ──────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        text = label,
        fontFamily = PretendardFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 16.sp,
        color = Brown700St,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

@Composable
private fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFF3F4F6),
    )
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    label: String,
    rightContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OrangeSandSt),
        ) {
            Icon(icon, null, tint = Orange500St, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp,
            color = TextBlackSt,
            modifier = Modifier.weight(1f),
        )
        if (rightContent != null) {
            rightContent()
        } else {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Gray300St, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OrangeSandSt),
        ) {
            Icon(icon, null, tint = Orange500St, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp,
            color = TextBlackSt,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Brown900St,
                checkedBorderColor = Brown900St,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Gray300St,
                uncheckedBorderColor = Gray300St,
            ),
        )
    }
}

// ─── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SiheungGagaeTheme { SettingsScreen() }
}
