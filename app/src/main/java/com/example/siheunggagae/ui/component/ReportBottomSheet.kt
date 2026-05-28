package com.example.siheunggagae.ui.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siheunggagae.data.model.ReportCreateRequest
import com.example.siheunggagae.data.network.RetrofitClient
import com.example.siheunggagae.ui.theme.PretendardFamily
import kotlinx.coroutines.launch

private val presetReasons = listOf("스팸/도배", "욕설/혐오 표현", "허위 정보", "기타")

private val TextBlackR  = Color(0xFF1E120A)
private val Brown700R   = Color(0xFF8A6E58)
private val Brown400R   = Color(0xFFC4A882)
private val BrownBorderR = Color(0xFFE8D3C2)
private val Pink500R    = Color(0xFFF04268)
private val Gray100R    = Color(0xFFF3F3F3)
private val Orange500R  = Color(0xFFF7A35B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    targetUserId: Int,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val isOther = selectedReason == "기타"
    val canSubmit = selectedReason != null && (!isOther || detail.isNotBlank()) && !isSubmitting

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "신고 사유를 선택해주세요",
                fontFamily = PretendardFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlackR,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presetReasons.forEach { reason ->
                    val isSelected = selectedReason == reason
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) Pink500R else BrownBorderR,
                                RoundedCornerShape(12.dp),
                            )
                            .background(if (isSelected) Color(0xFFFEF2F4) else Color.White)
                            .clickable { selectedReason = reason }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = reason,
                            fontFamily = PretendardFamily,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Pink500R else TextBlackR,
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Pink500R,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            if (isOther) {
                OutlinedTextField(
                    value = detail,
                    onValueChange = { if (it.length <= 200) detail = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = {
                        Text(
                            "신고 내용을 입력해주세요.",
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = Color(0xFFC1AEA0),
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BrownBorderR,
                        focusedBorderColor = Orange500R,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                )
            } else if (selectedReason != null) {
                OutlinedTextField(
                    value = detail,
                    onValueChange = { if (it.length <= 200) detail = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = {
                        Text(
                            "추가 설명(선택)",
                            fontFamily = PretendardFamily,
                            fontSize = 14.sp,
                            color = Color(0xFFC1AEA0),
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BrownBorderR,
                        focusedBorderColor = Orange500R,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                )
            }

            Spacer(Modifier.height(4.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (canSubmit) Pink500R else Gray100R)
                    .clickable(enabled = canSubmit) {
                        val reason = buildString {
                            append(selectedReason ?: "")
                            if (detail.isNotBlank()) append(": $detail")
                        }
                        isSubmitting = true
                        scope.launch {
                            val result = runCatching {
                                RetrofitClient.api.createReport(
                                    ReportCreateRequest(
                                        targetUserId = targetUserId,
                                        reason = reason,
                                    )
                                )
                            }
                            val ok = result.getOrNull()?.isSuccessful == true
                            if (ok) {
                                onSuccess()
                                onDismiss()
                                Toast.makeText(context, "신고가 접수되었어요", Toast.LENGTH_SHORT).show()
                            } else {
                                isSubmitting = false
                                val code = result.getOrNull()?.code()
                                val msg = when (code) {
                                    409 -> "이미 신고한 대상이에요"
                                    401, 403 -> "로그인이 필요합니다"
                                    null -> "네트워크 오류가 발생했어요"
                                    else -> "신고에 실패했어요 (HTTP $code)"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "신고하기",
                        fontFamily = PretendardFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canSubmit) Color.White else Brown400R,
                    )
                }
            }
        }
    }
}
