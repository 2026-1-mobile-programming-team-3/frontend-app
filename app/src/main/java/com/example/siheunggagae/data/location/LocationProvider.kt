package com.example.siheunggagae.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationProvider(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * 현재 위치를 반환. 권한 없음 or 실패 시 null.
     * 마지막 캐시 위치를 먼저 시도하고, null이면 신선한 좌표를 요청.
     */
    suspend fun getLocationOrNull(): Location? {
        if (!hasPermission()) return null
        return try {
            @Suppress("MissingPermission")
            val last = getLastLocation()
            if (last != null) last else getFreshLocation()
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("MissingPermission")
    private suspend fun getLastLocation(): Location? = suspendCancellableCoroutine { cont ->
        fusedClient.lastLocation.addOnCompleteListener { task ->
            if (cont.isActive) cont.resume(if (task.isSuccessful) task.result else null)
        }
    }

    @Suppress("MissingPermission")
    private suspend fun getFreshLocation(): Location? = suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()
        cont.invokeOnCancellation { cts.cancel() }
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnCompleteListener { task ->
                if (cont.isActive) cont.resume(if (task.isSuccessful) task.result else null)
            }
    }
}
