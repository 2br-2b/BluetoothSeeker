package codegito.xyz.bluetoothseeker.data

import android.content.Context
import codegito.xyz.bluetoothseeker.data.local.AppDatabase
import codegito.xyz.bluetoothseeker.data.local.SettingsRepository
import codegito.xyz.bluetoothseeker.data.location.AndroidLocationRepository
import codegito.xyz.bluetoothseeker.data.location.PlaceLabelRepository
import codegito.xyz.bluetoothseeker.data.notifications.BluetoothNotificationManager
import codegito.xyz.bluetoothseeker.data.repo.BluetoothRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.build(appContext)
    val settingsRepository = SettingsRepository(appContext)
    private val locationRepository = AndroidLocationRepository(appContext)
    private val placeLabelRepository = PlaceLabelRepository(appContext)
    private val notificationManager = BluetoothNotificationManager(appContext)

    val bluetoothRepository = BluetoothRepository(
        context = appContext,
        database = database,
        settingsRepository = settingsRepository,
        locationRepository = locationRepository,
        placeLabelRepository = placeLabelRepository,
        notificationManager = notificationManager,
    )
}
