package codegito.xyz.bluetoothseeker

import android.app.Application
import codegito.xyz.bluetoothseeker.data.AppContainer
import org.osmdroid.config.Configuration

class BluetoothSeekerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
        container = AppContainer(this)
    }
}
