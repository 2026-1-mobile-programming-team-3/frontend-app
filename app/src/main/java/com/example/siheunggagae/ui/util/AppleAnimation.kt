package com.example.siheunggagae.ui.util

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState

/**
 * Apple HIG 표준 ease 곡선 — NavGraph 의 `AppleEaseOut`/`AppleEaseInOut` 을 화면 내부에서도 재사용할 수 있도록 공용화.
 *
 * 화면 전환은 NavGraph 가 처리. 화면 내부 morph (칩 색·버튼 tap·시트 토글) 는 여기서.
 */
val AppleEaseOut = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
val AppleEaseInOut = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

const val AppleMorphMs = 280
const val AppleTapMs = 120

/** 칩·버튼 색 보간용 — `animateColorAsState(animationSpec = appleColorSpec())`. */
fun <T> appleSpec(durationMillis: Int = AppleMorphMs) =
    tween<T>(durationMillis = durationMillis, easing = AppleEaseOut)

/**
 * iOS 탭 피드백: 누를 때 0.97 로 살짝 줄었다가 떼면 원래 크기로 spring-out.
 * `interactionSource` 를 외부에서 받아 `clickable` 과 공유해야 ripple/tap 이 정확히 매칭됨.
 */
fun Modifier.appleTapScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = AppleTapMs, easing = AppleEaseOut),
        label = "tapScale",
    )
    this.scale(scale)
}

/** interactionSource 를 화면이 별도로 가지지 않아도 되게 하는 편의 — `clickable(remember{...})` 와 함께 쓸 것. */
@Composable
fun rememberAppleInteractionSource(): MutableInteractionSource =
    remember { MutableInteractionSource() }
