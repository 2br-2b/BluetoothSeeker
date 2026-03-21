package codegito.xyz.bluetoothseeker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import codegito.xyz.bluetoothseeker.data.model.DeviceEventType
import codegito.xyz.bluetoothseeker.data.model.LocationQuality

@Entity(tableName = "tracked_devices")
data class TrackedBluetoothDeviceEntity(
    @PrimaryKey val address: String,
    val name: String,
    val deviceType: Int,
    val isConnected: Boolean,
    val isIgnored: Boolean,
    val lastSeenAt: Long?,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastPlaceLabel: String?,
    val lastLocationQuality: LocationQuality?,
    val customIcon: String? = null,
)

@Entity(
    tableName = "device_event_logs",
    foreignKeys = [
        ForeignKey(
            entity = TrackedBluetoothDeviceEntity::class,
            parentColumns = ["address"],
            childColumns = ["deviceAddress"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("deviceAddress"), Index("happenedAt")],
)
data class DeviceEventLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceAddress: String,
    val deviceName: String,
    val eventType: DeviceEventType,
    val happenedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val placeLabel: String?,
    val locationQuality: LocationQuality?,
)
