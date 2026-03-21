package codegito.xyz.bluetoothseeker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import codegito.xyz.bluetoothseeker.BluetoothSeekerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: action=${intent.action}")
        val pendingResult = goAsync()
        val repository = (context.applicationContext as BluetoothSeekerApp).container.bluetoothRepository
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.refreshPairedDevices()
                repository.handleBluetoothIntent(intent)
            }.onFailure { e ->
                Log.e(TAG, "Error handling bluetooth intent", e)
            }
            pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "BT_Receiver"
    }
}
