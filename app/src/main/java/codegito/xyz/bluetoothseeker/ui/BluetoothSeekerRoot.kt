package codegito.xyz.bluetoothseeker.ui

import android.bluetooth.BluetoothClass
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import codegito.xyz.bluetoothseeker.BluetoothSeekerApp
import codegito.xyz.bluetoothseeker.data.local.DeviceEventLogEntity
import codegito.xyz.bluetoothseeker.data.local.TrackedBluetoothDeviceEntity
import codegito.xyz.bluetoothseeker.data.local.UserSettings
import codegito.xyz.bluetoothseeker.data.location.LocationSnapshot
import codegito.xyz.bluetoothseeker.data.model.DeviceEventType
import codegito.xyz.bluetoothseeker.data.model.LogMode
import codegito.xyz.bluetoothseeker.data.model.SortMode
import codegito.xyz.bluetoothseeker.data.repo.DeviceSummary
import java.util.Locale
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.currentBackStackEntryAsState

private object Routes {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Settings = "settings"
    const val IgnoredDevices = "ignored_devices"
    const val Device = "device/{address}"
    fun device(address: String) = "device/$address"
}

@Composable
fun BluetoothSeekerRoot(
    viewModel: RootViewModel,
    onPermissionsResult: () -> Unit,
    permissionRequest: Array<String>,
) {
    val context = LocalContext.current
    val rootState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val container = (context.applicationContext as BluetoothSeekerApp).container
    val appViewModel: AppViewModel = viewModel(factory = AppViewModelFactory(container.bluetoothRepository))
    val themePreference by appViewModel.themePreference.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        onPermissionsResult()
    }

    if (rootState.isLoading) return

    LaunchedEffect(rootState.showOnboarding) {
        navController.navigate(if (rootState.showOnboarding) Routes.Onboarding else Routes.Home) {
            popUpTo(0)
        }
    }

    NavHost(navController = navController, startDestination = if (rootState.showOnboarding) Routes.Onboarding else Routes.Home) {
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onContinue = { launcher.launch(permissionRequest) },
                onOpenApp = {
                    onPermissionsResult()
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Home) {
            HomeScreen(
                appViewModel = appViewModel,
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenDevice = { navController.navigate(Routes.device(it)) },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
                onOpenIgnoredDevices = { navController.navigate(Routes.IgnoredDevices) },
            )
        }
        composable(Routes.IgnoredDevices) {
            IgnoredDevicesScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.Device,
            arguments = listOf(navArgument("address") { type = NavType.StringType }),
        ) { entry ->
            DeviceDetailsScreen(
                address = entry.arguments?.getString("address").orEmpty(),
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun OnboardingScreen(
    onContinue: () -> Unit,
    onOpenApp: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("BluetoothSeeker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Track paired Bluetooth device connections with places and timestamps on an OpenStreetMap view.")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Grant permissions")
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpenApp, modifier = Modifier.fillMaxWidth()) {
                Text("I already granted them")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    appViewModel: AppViewModel,
    onOpenSettings: () -> Unit,
    onOpenDevice: (String) -> Unit,
) {
    val devices by appViewModel.devices.collectAsState()
    val settings by appViewModel.settings.collectAsState()
    val userLocation by appViewModel.currentUserLocation().collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val filteredDevices = remember(devices, query) {
        devices.filter {
            query.isBlank() || it.name.contains(query, ignoreCase = true) || it.address.contains(query, ignoreCase = true)
        }
    }
    val scaffoldState = rememberBottomSheetScaffoldState()

    // State to trigger map re-centering on user location
    var centerOnUserLocation by remember { mutableStateOf(0) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 280.dp,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = { Text("BluetoothSeeker") },
                actions = {
                    IconButton(onClick = {
                        appViewModel.refresh()
                        centerOnUserLocation++
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Center on my location")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Search paired devices") },
                )
                Spacer(Modifier.height(12.dp))
                SortModeRow(settings = settings, onSortSelected = appViewModel::updateSortMode)
                Spacer(Modifier.height(12.dp))
                if (filteredDevices.isEmpty()) {
                    Text("No paired devices match this view yet.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredDevices, key = { it.address }) { device ->
                            DeviceRow(device = device, onClick = { onOpenDevice(device.address) })
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            DeviceMap(
                devices = filteredDevices,
                userLocation = userLocation,
                centerOnUserTrigger = centerOnUserLocation,
                onOpenDevice = onOpenDevice,
            )
        }
    }
}

@Composable
private fun SortModeRow(settings: UserSettings, onSortSelected: (SortMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SortMode.entries.forEach { sortMode ->
            FilterChip(
                selected = settings.sortMode == sortMode,
                onClick = { onSortSelected(sortMode) },
                label = { Text(sortMode.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
            )
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceSummary, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(deviceIcon(device.type), contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, fontWeight = FontWeight.SemiBold)
                val status = if (device.isConnected) {
                    "Currently connected"
                } else {
                    Formatting.formatSeenAt(device.lastSeenAt)
                }
                Text(status, color = statusColor(device.isConnected, device.lastSeenAt))
                Text(device.lastPlaceLabel ?: formatCoordinates(device.lastLatitude, device.lastLongitude))
            }
            if (device.isConnected) {
                AssistChip(onClick = {}, label = { Text("Connected") })
            }
        }
    }
}

@Composable
private fun DeviceMap(
    devices: List<DeviceSummary>,
    userLocation: LocationSnapshot?,
    centerOnUserTrigger: Int,
    onOpenDevice: (String) -> Unit,
) {
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Center on user location when trigger increments, or on first load
    LaunchedEffect(centerOnUserTrigger, userLocation) {
        val mapView = mapViewRef.value ?: return@LaunchedEffect
        if (userLocation != null) {
            mapView.controller.animateTo(GeoPoint(userLocation.latitude, userLocation.longitude))
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                setMultiTouchControls(true)
                controller.setZoom(13.0)
                mapViewRef.value = this
            }
        },
        update = { mapView ->
            mapViewRef.value = mapView
            mapView.overlays.clear()
            val points = devices.mapNotNull { device ->
                val lat = device.lastLatitude ?: return@mapNotNull null
                val lon = device.lastLongitude ?: return@mapNotNull null
                device to GeoPoint(lat, lon)
            }
            // Initial center: prefer user location, fall back to first device
            if (centerOnUserTrigger == 0) {
                when {
                    userLocation != null -> mapView.controller.setCenter(GeoPoint(userLocation.latitude, userLocation.longitude))
                    points.isNotEmpty() -> mapView.controller.setCenter(points.first().second)
                }
            }
            if (userLocation != null) {
                val marker = Marker(mapView).apply {
                    position = GeoPoint(userLocation.latitude, userLocation.longitude)
                    title = "You are here"
                    snippet = "Current location"
                }
                mapView.overlays.add(marker)
            }
            clusterDevices(points).forEach { cluster ->
                val first = cluster.first()
                val marker = Marker(mapView).apply {
                    position = first.second
                    title = if (cluster.size == 1) first.first.name else "${cluster.size} devices nearby"
                    snippet = first.first.lastPlaceLabel ?: formatCoordinates(first.first.lastLatitude, first.first.lastLongitude)
                    setOnMarkerClickListener { _, _ ->
                        onOpenDevice(first.first.address)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        },
    )
}

private fun clusterDevices(points: List<Pair<DeviceSummary, GeoPoint>>): List<List<Pair<DeviceSummary, GeoPoint>>> =
    points.groupBy { (_, point) ->
        "${(point.latitude * 500).toInt()}:${(point.longitude * 500).toInt()}"
    }.values.toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceDetailsScreen(
    address: String,
    appViewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val device by appViewModel.device(address).collectAsState()
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    val filterState = remember(filter) { kotlinx.coroutines.flow.MutableStateFlow(filter) }
    val events by appViewModel.deviceEvents(address, filterState).collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = { Text(device?.name ?: "Device") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                device?.let {
                    DeviceHeaderCard(
                        device = it,
                        onOpenMaps = { launchMap(context, it) },
                        onCopyCoordinates = { copyCoordinates(context, it) },
                        onShare = { shareLocation(context, it) },
                        onToggleIgnored = { appViewModel.toggleIgnored(it.address, !it.isIgnored) },
                    )
                } ?: Text("Device not found.")
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HistoryFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(option.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase(Locale.getDefault()) }) },
                        )
                    }
                }
            }
            items(events, key = { it.id }) { event ->
                EventRow(event = event)
            }
        }
    }
}

@Composable
private fun DeviceHeaderCard(
    device: TrackedBluetoothDeviceEntity,
    onOpenMaps: () -> Unit,
    onCopyCoordinates: () -> Unit,
    onShare: () -> Unit,
    onToggleIgnored: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (device.isConnected) "Currently connected" else Formatting.formatSeenAt(device.lastSeenAt), color = statusColor(device.isConnected, device.lastSeenAt))
            Text(device.lastPlaceLabel ?: formatCoordinates(device.lastLatitude, device.lastLongitude))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenMaps, enabled = device.lastLatitude != null && device.lastLongitude != null) {
                    Icon(Icons.Default.Directions, contentDescription = null)
                    Text("Open in maps")
                }
                Button(onClick = onCopyCoordinates) {
                    Text("Copy coordinates")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text("Share")
                }
                Button(onClick = onToggleIgnored) {
                    Text(if (device.isIgnored) "Unignore" else "Ignore")
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: DeviceEventLogEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (event.eventType == DeviceEventType.CONNECTED) "Connected" else "Disconnected",
                fontWeight = FontWeight.SemiBold,
            )
            Text(Formatting.formatSeenAt(event.happenedAt))
            Text(event.placeLabel ?: formatCoordinates(event.latitude, event.longitude))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenIgnoredDevices: () -> Unit,
) {
    val context = LocalContext.current
    val settings by appViewModel.settings.collectAsState()
    var retentionText by rememberSaveable(settings.retentionDays) { mutableStateOf(settings.retentionDays.toString()) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) appViewModel.exportData(context, uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) appViewModel.importData(context, uri)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Event logging", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Column {
                    LogMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { appViewModel.updateLogMode(mode) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RadioButton(
                                selected = settings.logMode == mode,
                                onClick = { appViewModel.updateLogMode(mode) },
                            )
                            Text(
                                if (mode == LogMode.CONNECT_AND_DISCONNECT) "Connect + disconnect" else "Disconnect only",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = retentionText,
                    onValueChange = {
                        retentionText = it
                        it.toIntOrNull()?.let(appViewModel::updateRetention)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Log retention days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            item {
                SettingToggleRow("AMOLED mode", settings.amoledMode, appViewModel::updateAmoled)
            }
            item {
                SettingToggleRow("Disconnect notifications", settings.disconnectNotifications, appViewModel::updateDisconnectNotifications)
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenIgnoredDevices)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Ignored devices", style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { exportLauncher.launch("bluetoothseeker-export.json") }) {
                        Text("Export")
                    }
                    Button(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                        Text("Import")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IgnoredDevicesScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val devices by appViewModel.devices.collectAsState()
    val settings by appViewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = { Text("Ignored devices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (devices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No paired devices found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(devices, key = { it.address }) { device ->
                    val isIgnored = settings.ignoredAddresses.contains(device.address)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { appViewModel.toggleIgnored(device.address, !isIgnored) }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Checkbox(
                            checked = isIgnored,
                            onCheckedChange = { appViewModel.toggleIgnored(device.address, it) },
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(device.name, style = MaterialTheme.typography.bodyLarge)
                            Text(device.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun statusColor(isConnected: Boolean, timestamp: Long?): Color {
    if (isConnected) return Color(0xFF2E7D32)
    val diff = System.currentTimeMillis() - (timestamp ?: 0L)
    return when {
        diff < 86_400_000L -> Color(0xFF00897B)
        diff < 3 * 86_400_000L -> Color(0xFFF9A825)
        else -> Color(0xFFC62828)
    }
}

private fun formatCoordinates(latitude: Double?, longitude: Double?): String =
    if (latitude != null && longitude != null) {
        String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude)
    } else {
        "Location unavailable"
    }

private fun deviceIcon(type: Int) = when (type) {
    BluetoothDeviceType.AUDIO -> Icons.Default.Headphones
    BluetoothDeviceType.PHONE -> Icons.Default.Smartphone
    BluetoothDeviceType.SPEAKER -> Icons.Default.Speaker
    else -> Icons.Default.Bluetooth
}

private object BluetoothDeviceType {
    const val AUDIO = BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES
    const val PHONE = BluetoothClass.Device.PHONE_SMART
    const val SPEAKER = BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER
}

private fun launchMap(context: Context, device: TrackedBluetoothDeviceEntity) {
    val lat = device.lastLatitude ?: return
    val lon = device.lastLongitude ?: return
    context.startActivity(Formatting.mapsIntent(lat, lon, device.name))
}

private fun copyCoordinates(context: Context, device: TrackedBluetoothDeviceEntity) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(
        ClipData.newPlainText("coordinates", formatCoordinates(device.lastLatitude, device.lastLongitude)),
    )
}

private fun shareLocation(context: Context, device: TrackedBluetoothDeviceEntity) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "${device.name}: ${device.lastPlaceLabel ?: formatCoordinates(device.lastLatitude, device.lastLongitude)}",
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share last seen"))
}
