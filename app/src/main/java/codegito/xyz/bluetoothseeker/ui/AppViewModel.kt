package codegito.xyz.bluetoothseeker.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import codegito.xyz.bluetoothseeker.data.local.DeviceEventLogEntity
import codegito.xyz.bluetoothseeker.data.local.TrackedBluetoothDeviceEntity
import codegito.xyz.bluetoothseeker.data.local.UserSettings
import codegito.xyz.bluetoothseeker.data.location.LocationSnapshot
import codegito.xyz.bluetoothseeker.data.model.DeviceEventType
import codegito.xyz.bluetoothseeker.data.model.LogMode
import codegito.xyz.bluetoothseeker.data.model.SortMode
import codegito.xyz.bluetoothseeker.data.model.ThemePreference
import codegito.xyz.bluetoothseeker.data.repo.BluetoothRepository
import codegito.xyz.bluetoothseeker.data.repo.ConnectionEvent
import codegito.xyz.bluetoothseeker.data.repo.DeviceSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class HistoryFilter {
    ALL,
    CONNECTS,
    DISCONNECTS,
    LAST_7_DAYS,
}

class AppViewModel(
    private val repository: BluetoothRepository,
) : ViewModel() {
    private val userLocation = MutableStateFlow<LocationSnapshot?>(null)

    val devices = repository.devices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())
    val themePreference: StateFlow<ThemePreference> = settings
        .map { if (it.amoledMode) ThemePreference.AMOLED else ThemePreference.SYSTEM }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemePreference.SYSTEM)

    val recentConnectionEvent: SharedFlow<ConnectionEvent> = repository.recentConnectionEvent

    init {
        refresh()
        startLiveLocationUpdates()
    }

    private fun startLiveLocationUpdates() {
        viewModelScope.launch {
            while (isActive) {
                userLocation.value = repository.getCurrentUserLocation()
                delay(10_000L)
            }
        }
    }

    fun device(address: String): StateFlow<TrackedBluetoothDeviceEntity?> =
        repository.observeDevice(address)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun deviceEvents(address: String, filter: StateFlow<HistoryFilter>): StateFlow<List<DeviceEventLogEntity>> =
        combine(repository.observeDeviceEvents(address), filter) { events, activeFilter ->
            val cutoff = System.currentTimeMillis() - 7 * 86_400_000L
            events.filter {
                when (activeFilter) {
                    HistoryFilter.ALL -> true
                    HistoryFilter.CONNECTS -> it.eventType == DeviceEventType.CONNECTED
                    HistoryFilter.DISCONNECTS -> it.eventType == DeviceEventType.DISCONNECTED
                    HistoryFilter.LAST_7_DAYS -> it.happenedAt >= cutoff
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun currentUserLocation(): StateFlow<LocationSnapshot?> = userLocation

    fun refresh() {
        viewModelScope.launch {
            repository.refreshPairedDevices()
            userLocation.value = repository.getCurrentUserLocation()
        }
    }

    fun toggleIgnored(address: String, ignored: Boolean) {
        viewModelScope.launch {
            repository.setIgnored(address, ignored)
        }
    }

    fun setCustomIcon(address: String, icon: String?) {
        viewModelScope.launch {
            repository.setCustomIcon(address, icon)
        }
    }

    fun updateRetention(days: Int) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(retentionDays = days.coerceIn(1, 365)) }
            repository.pruneOldLogs(days)
        }
    }

    fun updateSortMode(sortMode: SortMode) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(sortMode = sortMode) }
        }
    }

    fun updateLogMode(logMode: LogMode) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(logMode = logMode) }
        }
    }

    fun updateAmoled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(amoledMode = enabled) }
        }
    }

    fun updateDisconnectNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(disconnectNotifications = enabled) }
        }
    }

    fun exportData(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                repository.exportTo(outputStream)
            }
        }
    }

    fun importData(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                repository.importFrom(inputStream)
            }
            refresh()
        }
    }
}

class AppViewModelFactory(
    private val repository: BluetoothRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AppViewModel(repository) as T
    }
}
