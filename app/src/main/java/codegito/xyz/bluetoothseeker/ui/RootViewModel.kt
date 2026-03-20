package codegito.xyz.bluetoothseeker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codegito.xyz.bluetoothseeker.data.local.SettingsRepository
import codegito.xyz.bluetoothseeker.data.model.ThemePreference
import codegito.xyz.bluetoothseeker.data.repo.BluetoothRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RootUiState(
    val isLoading: Boolean = true,
    val showOnboarding: Boolean = true,
)

class RootViewModel(
    private val repository: BluetoothRepository,
    settingsRepository: SettingsRepository,
    requiredPermissionsGranted: Boolean,
) : ViewModel() {
    private val permissionsGranted = MutableStateFlow(requiredPermissionsGranted)

    val uiState = combine(
        permissionsGranted,
        repository.settings,
    ) { hasPermissions, _ ->
        RootUiState(
            isLoading = false,
            showOnboarding = !hasPermissions,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RootUiState())

    val themePreference: StateFlow<ThemePreference> = settingsRepository.settings
        .map { if (it.amoledMode) ThemePreference.AMOLED else ThemePreference.SYSTEM }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.SYSTEM)

    init {
        viewModelScope.launch {
            repository.refreshPairedDevices()
        }
    }

    fun onPermissionsUpdated(granted: Boolean) {
        permissionsGranted.value = granted
    }
}

class RootViewModelFactory(
    private val repository: BluetoothRepository,
    private val settingsRepository: SettingsRepository,
    private val requiredPermissionsGranted: Boolean,
) : ViewModelProvider.Factory {
    var initialized: Boolean = false
        private set

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        initialized = true
        @Suppress("UNCHECKED_CAST")
        return RootViewModel(repository, settingsRepository, requiredPermissionsGranted) as T
    }
}
