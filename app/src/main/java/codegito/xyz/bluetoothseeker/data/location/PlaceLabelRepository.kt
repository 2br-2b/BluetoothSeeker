package codegito.xyz.bluetoothseeker.data.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PlaceLabelRepository(context: Context) {
    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun getPlaceLabel(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    continuation.resume(addresses.firstOrNull()?.getAddressLine(0))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.getAddressLine(0)
        }
    }
}
