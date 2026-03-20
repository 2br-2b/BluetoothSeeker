package codegito.xyz.bluetoothseeker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import codegito.xyz.bluetoothseeker.ui.BluetoothSeekerRoot
import codegito.xyz.bluetoothseeker.ui.Permissions
import codegito.xyz.bluetoothseeker.ui.RootViewModel
import codegito.xyz.bluetoothseeker.ui.RootViewModelFactory
import codegito.xyz.bluetoothseeker.ui.theme.BluetoothSeekerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as BluetoothSeekerApp).container
        val viewModelFactory = RootViewModelFactory(
            repository = container.bluetoothRepository,
            settingsRepository = container.settingsRepository,
            requiredPermissionsGranted = Permissions.hasCorePermissions(this),
        )

        splashScreen.setKeepOnScreenCondition {
            !viewModelFactory.initialized
        }

        setContent {
            val viewModel: RootViewModel = viewModel(factory = viewModelFactory)
            val themePreference by viewModel.themePreference.collectAsState()
            BluetoothSeekerTheme(themePreference = themePreference) {
                BluetoothSeekerRoot(
                    viewModel = viewModel,
                    onPermissionsResult = {
                        viewModel.onPermissionsUpdated(Permissions.hasCorePermissions(this))
                    },
                    permissionRequest = Permissions.requiredPermissionsForPrompt(),
                )
            }
        }
    }
}

object BluetoothPermissions {
    val bluetoothConnectPermission: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            null
        }
}
