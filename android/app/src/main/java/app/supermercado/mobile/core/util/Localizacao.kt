package app.supermercado.mobile.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@SuppressLint("MissingPermission")
suspend fun obterLocalizacaoAtual(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val provider = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> return null
    }

    locationManager.getLastKnownLocation(provider)?.let { return it }

    return suspendCancellableCoroutine { continuation ->
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (continuation.isActive) continuation.resume(location)
            }
        }
        locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
    }
}
