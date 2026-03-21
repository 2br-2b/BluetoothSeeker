package codegito.xyz.bluetoothseeker

import android.app.Application
import codegito.xyz.bluetoothseeker.data.AppContainer

class BluetoothSeekerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
