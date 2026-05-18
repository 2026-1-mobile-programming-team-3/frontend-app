package com.example.siheunggagae.ui.util

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class LocationPermissionState(
    val hasPermission: Boolean,
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>,
) {
    fun request() = launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
}

/**
 * 위치 권한 상태를 관리하는 Composable 헬퍼.
 *
 * 사용 예:
 * ```
 * val locationPermission = rememberLocationPermissionState { granted ->
 *     if (granted) viewModel.loadLocation()
 * }
 * if (!locationPermission.hasPermission) {
 *     Button(onClick = { locationPermission.request() }) { Text("위치 권한 허용") }
 * }
 * ```
 */
@Composable
fun rememberLocationPermissionState(
    onResult: (granted: Boolean) -> Unit = {},
): LocationPermissionState {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        onResult(granted)
    }
    return remember(hasPermission) { LocationPermissionState(hasPermission, launcher) }
}
