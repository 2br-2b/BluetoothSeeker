package codegito.xyz.bluetoothseeker.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object Permissions {
    fun requiredPermissionsForPrompt(): Array<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    // Background location must be requested separately after fine location is granted (API 29+).
    fun backgroundLocationPermission(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACCESS_BACKGROUND_LOCATION
        else null

    fun hasBackgroundLocation(context: Context): Boolean {
        val perm = backgroundLocationPermission() ?: return true // not needed pre-Q
        return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCorePermissions(context: Context): Boolean =
        requiredPermissionsForPrompt()
            .filterNot { it == Manifest.permission.POST_NOTIFICATIONS }
            .all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
}
