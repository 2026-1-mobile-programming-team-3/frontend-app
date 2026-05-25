# Phase 6 — Lists & Interactions Implementation Plan

> Use superpowers:subagent-driven-development.

**Goal:** 리스트 표시·상호작용 폴리시 (시간 포맷, 아바타 이미지, Undo, 카테고리 그라디언트, 수락 피드백, 저장 즉시 반영).

**Roadmap:** §Phase 6

---

## Bundle A — Chat & PetList 아바타·시간 (3 items, ~3h)

### Item A.1 — #24 Chat 시간 포맷 컨텍스트 분기

File: `app/src/main/java/com/example/siheunggagae/ui/screen/ChatScreen.kt`

`formatChatTime()` 가 현재 `"yyyy-MM-dd HH:mm"` 고정.

변경: 오늘 = `"오후 H:mm"`, 어제 = `"어제 H:mm"`, 그 외 같은 해 = `"M.d HH:mm"`, 작년 이전 = `"yyyy.M.d"`.

```kotlin
private fun formatChatTime(createdAt: String): String {
    val parsed = runCatching { java.time.ZonedDateTime.parse(createdAt) }.getOrNull() ?: return createdAt
    val kst = parsed.withZoneSameInstant(java.time.ZoneId.of("Asia/Seoul"))
    val now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul"))
    val today = now.toLocalDate()
    val msgDate = kst.toLocalDate()

    return when {
        msgDate == today -> {
            val hour12 = if (kst.hour % 12 == 0) 12 else kst.hour % 12
            val ampm = if (kst.hour < 12) "오전" else "오후"
            "$ampm $hour12:${"%02d".format(kst.minute)}"
        }
        msgDate == today.minusDays(1) -> "어제 ${"%02d".format(kst.hour)}:${"%02d".format(kst.minute)}"
        msgDate.year == today.year -> kst.format(java.time.format.DateTimeFormatter.ofPattern("M.d HH:mm"))
        else -> kst.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.M.d"))
    }
}
```

기존 `formatChatTime` 의 입력 형식이 ISO 8601 (UTC) 가정. 만약 다른 형식 (`yyyy-MM-dd HH:mm:ss` 등) 이면 parse 로직 조정.

### Item A.2 — #26 Chat 상대방 아바타 실 이미지

같은 파일. `ReceivedMessageItem` 의 이니셜 아바타를 `AppAsyncImage` 또는 fallback 이니셜로.

**필요 조건**: `ChatUiState.Success` 에 `opponentProfileUrl: String?` 필드 추가.

Pre-check:
```bash
grep -rn "data class ChatUiState\|opponentProfileUrl\|profileImageUrl" app/src/main/java/com/example/siheunggagae/ui/viewmodel/ChatViewModel.kt app/src/main/java/com/example/siheunggagae/data/model/ | head -10
```

ChatUiState 에 필드 추가:
```kotlin
data class ChatUiState(
    // ... existing fields
    val opponentProfileUrl: String? = null,
)
```

ChatViewModel 에서 `loadMatchDetail` 또는 유사 위치에서 `opponentProfileUrl` 설정 — 가능하면 author profile URL. 정확한 model 필드 모르면 fallback (현재 데이터로 채울 수 있는지 확인 후 결정).

화면 사용 변경 (`ReceivedMessageItem`):
```kotlin
import com.example.siheunggagae.ui.component.AppAsyncImage

if (!opponentProfileUrl.isNullOrBlank()) {
    AppAsyncImage(
        model = opponentProfileUrl,
        contentDescription = "${opponentName} 프로필",
        modifier = Modifier.size(36.dp).clip(CircleShape),
    )
} else {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).background(MintLightC),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = opponentName.take(1),
            fontFamily = PretendardFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
```

데이터 모델에서 opponent profile URL 을 가져올 수 없으면 Item A.2 는 skip (commit 메시지 노트).

### Item A.3 — #31 PetList 사진 표시

File: `app/src/main/java/com/example/siheunggagae/ui/screen/PetListScreen.kt`

`PetRow` 가 현재 `ic_pets` 아이콘만. `PetResponse.photoUrl` 있으면 AsyncImage.

Pre-check:
```bash
grep -n "photoUrl\|imageUrl" app/src/main/java/com/example/siheunggagae/data/model/UserModels.kt app/src/main/java/com/example/siheunggagae/data/model/PetModels.kt 2>/dev/null
```

변경:
```kotlin
import com.example.siheunggagae.ui.component.AppAsyncImage

if (!pet.photoUrl.isNullOrBlank()) {
    AppAsyncImage(
        model = pet.photoUrl,
        contentDescription = "${pet.name} 사진",
        modifier = Modifier.size(48.dp).clip(CircleShape),
    )
} else {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFEDD4)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pets),
            contentDescription = null,
            tint = Color(0xFFF7A35B),
            modifier = Modifier.size(24.dp),
        )
    }
}
```

PetResponse 에 photoUrl 필드 없으면 skip + note.

### 컴파일 + 커밋

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/screen/ChatScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/PetListScreen.kt \
        [ChatViewModel.kt if modified]
git commit -m "feat(ui): Phase 6 bundle A — Chat 시간 포맷·아바타 + PetList 사진 3건

#24 Chat 시간 포맷 — 오늘 '오후 H:mm' / 어제 / 같은 해 'M.d HH:mm' / 그 외 'yyyy.M.d'
#26 Chat 상대 아바타 — opponentProfileUrl 있으면 AsyncImage, 없으면 이니셜 fallback
#31 PetList 반려동물 사진 — photoUrl 있으면 AsyncImage, 없으면 아이콘 fallback"
```

---

## Bundle B — Favorite & Match Detail 상호작용 (3 items, ~4h)

### Item B.1 — #36 Favorite 즐겨찾기 해제 Undo Snackbar

File: `app/src/main/java/com/example/siheunggagae/ui/screen/FavoriteStoresScreen.kt`

`onHeartClick` 가 즉시 `removeFavorite` 호출 → Undo 없음.

추가:
```kotlin
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

// Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, ...) {  // 기존 Scaffold 가 있으면 그 안에 추가, 없으면 감싸기

// onHeartClick 변경:
onHeartClick = { item ->
    item.storeId?.let { storeId ->
        viewModel?.removeFavorite(storeId)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "즐겨찾기에서 제거했어요",
                actionLabel = "실행취소",
                duration = androidx.compose.material3.SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel?.addFavorite(storeId)
            }
        }
    }
}
```

`addFavorite` 메서드가 ViewModel 에 없으면 추가 필요. grep 으로 확인:
```bash
grep -n "fun addFavorite\|fun removeFavorite" app/src/main/java/com/example/siheunggagae/ui/viewmodel/FavoriteStoresViewModel.kt 2>/dev/null
```

없으면 view model 에 추가하거나 그냥 노트하고 Undo 만 표시 (실제 복구 안 함). 데모 wow 목적이면 노트 후 placeholder action 으로도 OK.

### Item B.2 — #37 Favorite 썸네일 카테고리별 그라디언트

같은 파일. 현재 모든 매장 썸네일이 `Brush.linearGradient(listOf(Color(0xFFD0FEE1), Color(0xFFE0F7FA)))`.

변경: `CategoryVisual.forCategory(item.category).gradient` 사용:

```kotlin
import com.example.siheunggagae.ui.util.CategoryVisual

val visual = CategoryVisual.forCategory(item.category)
Box(
    modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(visual.gradient)),
    contentAlignment = Alignment.Center,
) {
    Icon(
        painter = painterResource(visual.drawableRes),
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(22.dp),
    )
}
```

`item.category` 필드명이 다르면 조정.

### Item B.3 — #39 MatchDetail 수락 버튼 피드백

File: `app/src/main/java/com/example/siheunggagae/ui/screen/MatchingDetailScreen.kt`

`viewModel.acceptApplication(requestId, appId) {}` 의 빈 콜백. → 로딩 상태 + 성공 Snackbar.

ViewModel side: `acceptApplication` 콜백 시그니처 확인. 가능하면 `(success: Boolean) -> Unit` 패턴.

화면 측:
```kotlin
import androidx.compose.material3.CircularProgressIndicator

var isAccepting by remember { mutableStateOf(false) }
val snackbarHostState = remember { SnackbarHostState() }  // Scaffold 안에서 사용
val scope = rememberCoroutineScope()

// 수락 버튼:
Box(
    modifier = Modifier
        .clickable(enabled = !isAccepting) {
            isAccepting = true
            viewModel.acceptApplication(requestId, appId) { success ->
                isAccepting = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (success) "수락했어요. 채팅을 시작해 보세요." else "수락에 실패했어요. 잠시 후 다시 시도해 주세요."
                    )
                }
            }
        }
        ...,
) {
    if (isAccepting) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.dp,
            modifier = Modifier.size(18.dp),
        )
    } else {
        Text("수락", ...)
    }
}
```

`viewModel.acceptApplication` 콜백 시그니처가 다르면 grep 으로 확인 후 조정. 콜백이 없으면 viewmodel 측에 success boolean 콜백 추가 또는 LiveData/StateFlow 로 결과 받는 방식.

### 컴파일 + 커밋

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/screen/FavoriteStoresScreen.kt \
        app/src/main/java/com/example/siheunggagae/ui/screen/MatchingDetailScreen.kt \
        [필요 시 viewmodel]
git commit -m "feat(ui): Phase 6 bundle B — Favorite Undo·카테고리 그라디언트 + Match 수락 피드백 3건

#36 Favorite 즐겨찾기 해제 Undo Snackbar (실행취소 액션)
#37 Favorite 썸네일 CategoryVisual 그라디언트로 카테고리별 차별화
#39 MatchDetail 수락 버튼 로딩 인디케이터 + 성공/실패 Snackbar"
```

---

## Bundle C — ProfileEdit 저장 즉각 반영 (1 item, ~1h)

### Item C.1 — #40 ProfileEdit 저장 후 즉시 onBack

File: `app/src/main/java/com/example/siheunggagae/ui/screen/ProfileEditScreen.kt`

저장 성공 → Snackbar 1초 → onBack().

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

// 저장 버튼 onClick:
onSave = {
    viewModel.save { success ->
        if (success) {
            scope.launch {
                snackbarHostState.showSnackbar("프로필이 저장되었어요 ✓")
                kotlinx.coroutines.delay(800L)  // sb 보이는 시간 짧게
                onBack()
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("저장에 실패했어요")
            }
        }
    }
}
```

ViewModel `save` 콜백 시그니처 확인:
```bash
grep -n "fun save\b" app/src/main/java/com/example/siheunggagae/ui/viewmodel/ProfileEditViewModel.kt
```

저장 즉시 onBack 호출 패턴이 현재라면 onBack 직전에 Snackbar 표시 + delay 추가. 너무 복잡하면 단순히 토스트 (`Toast.makeText(context, "프로필이 저장되었어요", Toast.LENGTH_SHORT).show()`) 도 OK.

### 컴파일 + 커밋

```bash
./gradlew :app:compileDebugKotlin
git add app/src/main/java/com/example/siheunggagae/ui/screen/ProfileEditScreen.kt
git commit -m "feat(ui): Phase 6 bundle C — ProfileEdit 저장 성공 Snackbar + 짧은 delay 후 뒤로가기 (#40)"
```

---

## Verification

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```
