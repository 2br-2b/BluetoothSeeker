package codegito.xyz.bluetoothseeker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import codegito.xyz.bluetoothseeker.data.model.DeviceEventType
import codegito.xyz.bluetoothseeker.data.model.LocationQuality

@Database(
    entities = [TrackedBluetoothDeviceEntity::class, DeviceEventLogEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bluetoothDao(): BluetoothDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tracked_devices ADD COLUMN customIcon TEXT")
            }
        }

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "bluetooth-seeker.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

class DatabaseConverters {
    @TypeConverter
    fun fromDeviceEventType(value: DeviceEventType?): String? = value?.name

    @TypeConverter
    fun toDeviceEventType(value: String?): DeviceEventType? = value?.let(DeviceEventType::valueOf)

    @TypeConverter
    fun fromLocationQuality(value: LocationQuality?): String? = value?.name

    @TypeConverter
    fun toLocationQuality(value: String?): LocationQuality? = value?.let(LocationQuality::valueOf)
}
