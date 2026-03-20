package codegito.xyz.bluetoothseeker.data.model

import kotlinx.serialization.Serializable

enum class DeviceEventType {
    CONNECTED,
    DISCONNECTED,
}

enum class LogMode {
    CONNECT_AND_DISCONNECT,
    DISCONNECT_ONLY,
}

enum class ThemePreference {
    SYSTEM,
    AMOLED,
}

enum class SortMode {
    MOST_RECENT,
    NAME,
    CONNECTED_FIRST,
    CLOSEST,
}

enum class LocationQuality {
    PRECISE,
    APPROXIMATE,
    LAST_KNOWN,
}

@Serializable
data class ExportPayload(
    val settings: SettingsSnapshot,
    val devices: List<ExportDevice>,
    val events: List<ExportEvent>,
)

@Serializable
data class SettingsSnapshot(
    val logMode: String,
    val retentionDays: Int,
    val amoledMode: Boolean,
    val disconnectNotifications: Boolean,
    val sortMode: String,
    val ignoredAddresses: List<String>,
)

@Serializable
data class ExportDevice(
    val address: String,
    val name: String,
    val type: Int,
    val isConnected: Boolean,
    val lastSeenAt: Long?,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastPlaceLabel: String?,
    val lastLocationQuality: String?,
)

@Serializable
data class ExportEvent(
    val id: Long,
    val address: String,
    val name: String,
    val eventType: String,
    val happenedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val placeLabel: String?,
    val locationQuality: String?,
)
