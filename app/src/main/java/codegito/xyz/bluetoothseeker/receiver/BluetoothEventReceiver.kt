package codegito.xyz.bluetoothseeker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import codegito.xyz.bluetoothseeker.BluetoothSeekerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val repository = (context.applicationContext as BluetoothSeekerApp).container.bluetoothRepository
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.refreshPairedDevices()
                repository.handleBluetoothIntent(intent)
            }
            pendingResult.finish()
        }
    }
}
