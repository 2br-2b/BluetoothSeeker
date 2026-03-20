package codegito.xyz.bluetoothseeker.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import codegito.xyz.bluetoothseeker.data.model.LocationQuality
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val quality: LocationQuality,
)

class AndroidLocationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    suspend fun getBestAvailableLocation(): LocationSnapshot? {
        if (!hasLocationPermission()) return null
        val current = getCurrentLocation()
        if (current != null) return current
        return getLastKnownLocation()
    }

    fun getLastKnownLocation(): LocationSnapshot? {
        if (!hasLocationPermission()) return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        val location = providers
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
            ?: return null
        return location.toSnapshot(if (location.accuracy <= 50f) LocationQuality.APPROXIMATE else LocationQuality.LAST_KNOWN)
    }

    private suspend fun getCurrentLocation(): LocationSnapshot? {
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        } ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            suspendCancellableCoroutine { continuation ->
                val cancellationSignal = CancellationSignal()
                locationManager.getCurrentLocation(provider, cancellationSignal, appContext.mainExecutor) { location ->
                    continuation.resume(location?.toSnapshot(LocationQuality.PRECISE))
                }
                continuation.invokeOnCancellation { cancellationSignal.cancel() }
            }
        } else {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        continuation.resume(location.toSnapshot(LocationQuality.PRECISE))
                    }
                }
                @Suppress("DEPRECATION")
                locationManager.requestSingleUpdate(provider, listener, null)
                continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
            }
        }
    }

    private fun Location.toSnapshot(quality: LocationQuality): LocationSnapshot =
        LocationSnapshot(latitude = latitude, longitude = longitude, quality = quality)
}
