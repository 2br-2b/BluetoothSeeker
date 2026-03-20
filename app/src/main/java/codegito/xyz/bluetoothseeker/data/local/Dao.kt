package codegito.xyz.bluetoothseeker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothDao {
    @Query("SELECT * FROM tracked_devices ORDER BY name COLLATE NOCASE")
    fun observeDevices(): Flow<List<TrackedBluetoothDeviceEntity>>

    @Query("SELECT * FROM tracked_devices WHERE address = :address LIMIT 1")
    fun observeDevice(address: String): Flow<TrackedBluetoothDeviceEntity?>

    @Query("SELECT * FROM tracked_devices WHERE address = :address LIMIT 1")
    suspend fun getDevice(address: String): TrackedBluetoothDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevice(device: TrackedBluetoothDeviceEntity)

    @Query("UPDATE tracked_devices SET isIgnored = :ignored WHERE address = :address")
    suspend fun setIgnored(address: String, ignored: Boolean)

    @Query("SELECT * FROM device_event_logs WHERE deviceAddress = :address ORDER BY happenedAt DESC")
    fun observeEventsForDevice(address: String): Flow<List<DeviceEventLogEntity>>

    @Query("SELECT * FROM device_event_logs ORDER BY happenedAt DESC")
    suspend fun getAllEvents(): List<DeviceEventLogEntity>

    @Insert
    suspend fun insertEvent(event: DeviceEventLogEntity): Long

    @Query("DELETE FROM device_event_logs WHERE happenedAt < :cutoff")
    suspend fun deleteLogsOlderThan(cutoff: Long)

    @Query("DELETE FROM tracked_devices")
    suspend fun clearDevices()

    @Query("DELETE FROM device_event_logs")
    suspend fun clearEvents()

    @Transaction
    suspend fun replaceAll(
        devices: List<TrackedBluetoothDeviceEntity>,
        events: List<DeviceEventLogEntity>,
    ) {
        clearEvents()
        clearDevices()
        devices.forEach { upsertDevice(it) }
        events.forEach { insertEvent(it.copy(id = 0)) }
    }
}
