package codegito.xyz.bluetoothseeker.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object Permissions {
    data class PermissionInfo(val permission: String, val label: String)

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

    /** Returns human-readable labels for all missing required permissions (excluding notifications). */
    fun missingPermissionLabels(context: Context): List<String> = buildList {
        fun missing(perm: String) =
            ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && missing(Manifest.permission.BLUETOOTH_CONNECT)) {
            add("Nearby devices (Bluetooth)")
        }
        if (missing(Manifest.permission.ACCESS_FINE_LOCATION) || missing(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            add("Location")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && missing(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            add("Background location (\"Allow all the time\")")
        }
    }
}
