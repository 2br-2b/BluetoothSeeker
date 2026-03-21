package codegito.xyz.bluetoothseeker.data.repo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import codegito.xyz.bluetoothseeker.data.local.AppDatabase
import codegito.xyz.bluetoothseeker.data.local.DeviceEventLogEntity
import codegito.xyz.bluetoothseeker.data.local.SettingsRepository
import codegito.xyz.bluetoothseeker.data.local.TrackedBluetoothDeviceEntity
import codegito.xyz.bluetoothseeker.data.local.UserSettings
import codegito.xyz.bluetoothseeker.data.location.AndroidLocationRepository
import codegito.xyz.bluetoothseeker.data.location.PlaceLabelRepository
import codegito.xyz.bluetoothseeker.data.model.DeviceEventType
import codegito.xyz.bluetoothseeker.data.model.ExportDevice
import codegito.xyz.bluetoothseeker.data.model.ExportEvent
import codegito.xyz.bluetoothseeker.data.model.ExportPayload
import codegito.xyz.bluetoothseeker.data.model.LocationQuality
import codegito.xyz.bluetoothseeker.data.model.LogMode
import codegito.xyz.bluetoothseeker.data.model.SettingsSnapshot
import codegito.xyz.bluetoothseeker.data.model.SortMode
import codegito.xyz.bluetoothseeker.data.notifications.BluetoothNotificationManager
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class DeviceSummary(
    val address: String,
    val name: String,
    val type: Int,
    val isConnected: Boolean,
    val lastSeenAt: Long?,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastPlaceLabel: String?,
    val lastLocationQuality: LocationQuality?,
    val distanceMeters: Float?,
    val isIgnored: Boolean,
    val customIcon: String? = null,
)

data class ConnectionEvent(
    val deviceName: String,
    val eventType: DeviceEventType,
)

class BluetoothRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val settingsRepository: SettingsRepository,
    private val locationRepository: AndroidLocationRepository,
    private val placeLabelRepository: PlaceLabelRepository,
    private val notificationManager: BluetoothNotificationManager,
) {
    private val dao = database.bluetoothDao()
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val _recentConnectionEvent = MutableSharedFlow<ConnectionEvent>(extraBufferCapacity = 8)
    val recentConnectionEvent: SharedFlow<ConnectionEvent> = _recentConnectionEvent.asSharedFlow()

    val settings: Flow<UserSettings> = settingsRepository.settings

    val devices: Flow<List<DeviceSummary>> = combine(
        dao.observeDevices(),
        settingsRepository.settings,
    ) { devices, settings ->
        val userLocation = locationRepository.getLastKnownLocation()
        devices
            .filterNot { it.address in settings.ignoredAddresses || it.isIgnored }
            .map { entity ->
                DeviceSummary(
                    address = entity.address,
                    name = entity.name,
                    type = entity.deviceType,
                    isConnected = entity.isConnected,
                    lastSeenAt = entity.lastSeenAt,
                    lastLatitude = entity.lastLatitude,
                    lastLongitude = entity.lastLongitude,
                    lastPlaceLabel = entity.lastPlaceLabel,
                    lastLocationQuality = entity.lastLocationQuality,
                    distanceMeters = distanceMeters(
                        userLocation?.latitude,
                        userLocation?.longitude,
                        entity.lastLatitude,
                        entity.lastLongitude,
                    ),
                    isIgnored = entity.isIgnored,
                    customIcon = entity.customIcon,
                )
            }
            .sortedWith(deviceComparator(settings))
    }

    fun observeDevice(address: String): Flow<TrackedBluetoothDeviceEntity?> = dao.observeDevice(address)

    fun observeDeviceEvents(address: String): Flow<List<DeviceEventLogEntity>> = dao.observeEventsForDevice(address)

    suspend fun getCurrentUserLocation() = locationRepository.getLastKnownLocation()

    suspend fun refreshPairedDevices() {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return
        if (!hasBluetoothConnectPermission()) return
        adapter.bondedDevices.orEmpty().forEach { device ->
            val existing = dao.getDevice(device.address)
            dao.upsertDevice(
                TrackedBluetoothDeviceEntity(
                    address = device.address,
                    name = device.displayName(),
                    deviceType = device.type,
                    isConnected = existing?.isConnected ?: false,
                    isIgnored = existing?.isIgnored ?: false,
                    lastSeenAt = existing?.lastSeenAt,
                    lastLatitude = existing?.lastLatitude,
                    lastLongitude = existing?.lastLongitude,
                    lastPlaceLabel = existing?.lastPlaceLabel,
                    lastLocationQuality = existing?.lastLocationQuality,
                ),
            )
        }
    }

    suspend fun handleBluetoothIntent(intent: Intent) {
        val device = intent.parcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
        val eventType = when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> DeviceEventType.CONNECTED
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> DeviceEventType.DISCONNECTED
            else -> return
        }

        if (!hasBluetoothConnectPermission()) return
        val settings = settingsRepository.settings.first()
        if (settings.logMode == LogMode.DISCONNECT_ONLY && eventType == DeviceEventType.CONNECTED) return
        if (device.bondState != BluetoothDevice.BOND_BONDED) return
        if (device.address in settings.ignoredAddresses) return

        val location = if (locationRepository.hasLocationPermission()) {
            locationRepository.getBestAvailableLocation()
        } else null
        val placeLabel = if (location != null) {
            placeLabelRepository.getPlaceLabel(location.latitude, location.longitude)
        } else null
        val existing = dao.getDevice(device.address)
        val timestamp = System.currentTimeMillis()
        dao.upsertDevice(
            TrackedBluetoothDeviceEntity(
                address = device.address,
                name = device.displayName(),
                deviceType = device.type.takeIf { it != BluetoothDevice.DEVICE_TYPE_UNKNOWN }
                    ?: existing?.deviceType
                    ?: BluetoothDevice.DEVICE_TYPE_UNKNOWN,
                isConnected = eventType == DeviceEventType.CONNECTED,
                isIgnored = existing?.isIgnored ?: false,
                lastSeenAt = timestamp,
                lastLatitude = location?.latitude ?: existing?.lastLatitude,
                lastLongitude = location?.longitude ?: existing?.lastLongitude,
                lastPlaceLabel = placeLabel ?: existing?.lastPlaceLabel,
                lastLocationQuality = location?.quality ?: existing?.lastLocationQuality,
            ),
        )
        dao.insertEvent(
            DeviceEventLogEntity(
                deviceAddress = device.address,
                deviceName = device.displayName(),
                eventType = eventType,
                happenedAt = timestamp,
                latitude = location?.latitude,
                longitude = location?.longitude,
                placeLabel = placeLabel,
                locationQuality = location?.quality,
            ),
        )
        _recentConnectionEvent.tryEmit(ConnectionEvent(device.displayName(), eventType))
        pruneOldLogs(settings.retentionDays)
        if (eventType == DeviceEventType.DISCONNECTED && settings.disconnectNotifications) {
            notificationManager.showDisconnectNotification(device.displayName())
        }
    }

    suspend fun setCustomIcon(address: String, icon: String?) {
        dao.setCustomIcon(address, icon)
    }

    suspend fun setIgnored(address: String, ignored: Boolean) {
        dao.setIgnored(address, ignored)
        settingsRepository.update { current ->
            current.copy(
                ignoredAddresses = current.ignoredAddresses.toMutableSet().apply {
                    if (ignored) add(address) else remove(address)
                },
            )
        }
    }

    suspend fun updateSettings(transform: (UserSettings) -> UserSettings) {
        settingsRepository.update(transform)
    }

    suspend fun pruneOldLogs(retentionDays: Int) {
        val cutoff = System.currentTimeMillis() - retentionDays * MILLIS_PER_DAY
        dao.deleteLogsOlderThan(cutoff)
    }

    suspend fun exportTo(outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val settings = settings.first()
        val devices = dao.observeDevices().first()
        val events = dao.getAllEvents()
        outputStream.writer().use { writer ->
            writer.write(
                json.encodeToString(
                    ExportPayload(
                        settings = SettingsSnapshot(
                            logMode = settings.logMode.name,
                            retentionDays = settings.retentionDays,
                            amoledMode = settings.amoledMode,
                            disconnectNotifications = settings.disconnectNotifications,
                            sortMode = settings.sortMode.name,
                            ignoredAddresses = settings.ignoredAddresses.toList(),
                        ),
                        devices = devices.map {
                            ExportDevice(
                                address = it.address,
                                name = it.name,
                                type = it.deviceType,
                                isConnected = it.isConnected,
                                lastSeenAt = it.lastSeenAt,
                                lastLatitude = it.lastLatitude,
                                lastLongitude = it.lastLongitude,
                                lastPlaceLabel = it.lastPlaceLabel,
                                lastLocationQuality = it.lastLocationQuality?.name,
                            )
                        },
                        events = events.map {
                            ExportEvent(
                                id = it.id,
                                address = it.deviceAddress,
                                name = it.deviceName,
                                eventType = it.eventType.name,
                                happenedAt = it.happenedAt,
                                latitude = it.latitude,
                                longitude = it.longitude,
                                placeLabel = it.placeLabel,
                                locationQuality = it.locationQuality?.name,
                            )
                        },
                    ),
                ),
            )
        }
    }

    suspend fun importFrom(inputStream: InputStream) = withContext(Dispatchers.IO) {
        val payload = inputStream.reader().use { reader ->
            json.decodeFromString<ExportPayload>(reader.readText())
        }
        settingsRepository.import(payload.settings)
        dao.replaceAll(
            devices = payload.devices.map {
                TrackedBluetoothDeviceEntity(
                    address = it.address,
                    name = it.name,
                    deviceType = it.type,
                    isConnected = it.isConnected,
                    isIgnored = it.address in payload.settings.ignoredAddresses,
                    lastSeenAt = it.lastSeenAt,
                    lastLatitude = it.lastLatitude,
                    lastLongitude = it.lastLongitude,
                    lastPlaceLabel = it.lastPlaceLabel,
                    lastLocationQuality = it.lastLocationQuality?.let(LocationQuality::valueOf),
                )
            },
            events = payload.events.map {
                DeviceEventLogEntity(
                    deviceAddress = it.address,
                    deviceName = it.name,
                    eventType = DeviceEventType.valueOf(it.eventType),
                    happenedAt = it.happenedAt,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    placeLabel = it.placeLabel,
                    locationQuality = it.locationQuality?.let(LocationQuality::valueOf),
                )
            },
        )
    }

    private fun hasBluetoothConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED

    private fun deviceComparator(settings: UserSettings): Comparator<DeviceSummary> =
        when (settings.sortMode) {
            SortMode.NAME -> compareBy<DeviceSummary> { it.name.lowercase() }
            SortMode.CONNECTED_FIRST -> compareByDescending<DeviceSummary> { it.isConnected }
                .thenByDescending { it.lastSeenAt ?: 0L }
            SortMode.CLOSEST -> compareBy<DeviceSummary> { it.distanceMeters ?: Float.MAX_VALUE }
            SortMode.MOST_RECENT -> compareByDescending<DeviceSummary> { it.lastSeenAt ?: 0L }
        }

    private fun distanceMeters(
        fromLat: Double?,
        fromLon: Double?,
        toLat: Double?,
        toLon: Double?,
    ): Float? {
        if (fromLat == null || fromLon == null || toLat == null || toLon == null) return null
        val results = FloatArray(1)
        Location.distanceBetween(fromLat, fromLon, toLat, toLon, results)
        return results[0]
    }

    private inline fun <reified T : Any> Intent.parcelableExtra(name: String): T? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(name)
        }

    private fun BluetoothDevice.displayName(): String = name?.takeIf { it.isNotBlank() } ?: address

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
