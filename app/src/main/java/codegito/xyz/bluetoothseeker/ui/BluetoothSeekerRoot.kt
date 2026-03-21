package codegito.xyz.bluetoothseeker.ui

import android.bluetooth.BluetoothClass
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import codegito.xyz.bluetoothseeker.data.repo.ConnectionEvent
import codegito.xyz.bluetoothseeker.data.repo.DeviceSummary
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.viewinterop.AndroidView

private object Routes {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Settings = "settings"
    const val IgnoredDevices = "ignored_devices"
    const val Device = "device/{address}"
    fun device(address: String) = "device/$address"
}

private data class IconOption(val key: String, val icon: ImageVector, val label: String)

// Enumerate every icon in Icons.Filled via reflection, lazily on first use.
private val ALL_ICON_OPTIONS: List<IconOption> by lazy {
    Icons.Filled::class.java.declaredFields
        .filter { it.type == ImageVector::class.java }
        .mapNotNull { field ->
            runCatching {
                field.isAccessible = true
                val icon = field.get(Icons.Filled) as? ImageVector ?: return@mapNotNull null
                IconOption(key = field.name, icon = icon, label = field.name)
            }.getOrNull()
        }
        .sortedBy { it.label }
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
            Text("Bluetooth Seeker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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

    // Bottom sheet: peek=280 when expanded, 20dp (just handle) when hidden
    val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val scope = rememberCoroutineScope()

    var centerOnUserLocation by remember { mutableStateOf(0) }

    // Connection event banner
    var bannerEvent by remember { mutableStateOf<ConnectionEvent?>(null) }
    LaunchedEffect(Unit) {
        appViewModel.recentConnectionEvent.collect { event ->
            bannerEvent = event
            delay(3500)
            bannerEvent = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 20.dp,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    title = { Text("Bluetooth Seeker") },
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
                    // Drag handle row with hide/show toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                .clickable {
                                    scope.launch {
                                        if (sheetState.currentValue == SheetValue.Expanded ||
                                            sheetState.currentValue == SheetValue.PartiallyExpanded) {
                                            sheetState.hide()
                                        } else {
                                            sheetState.partialExpand()
                                        }
                                    }
                                }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
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

        // Connection/disconnection banner overlaid at top
        ConnectionBanner(event = bannerEvent)
    }
}

@Composable
private fun ConnectionBanner(event: ConnectionEvent?) {
    AnimatedVisibility(
        visible = event != null,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300, easing = FastOutSlowInEasing)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(300, easing = FastOutSlowInEasing)),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (event != null) {
            val isConnected = event.eventType == DeviceEventType.CONNECTED
            val bgColor = if (isConnected) Color(0xFF1B5E20) else Color(0xFFB71C1C)
            val icon = if (isConnected) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled
            val label = if (isConnected) "${event.deviceName} connected!" else "${event.deviceName} disconnected!"

            // Pulse animation for the icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
                label = "iconAlpha",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    label,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }
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
            Icon(resolveIcon(device.customIcon, device.type), contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, fontWeight = FontWeight.SemiBold)
                val status = if (device.isConnected) "Currently connected" else Formatting.formatSeenAt(device.lastSeenAt)
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

    LaunchedEffect(centerOnUserTrigger, userLocation) {
        val mapView = mapViewRef.value ?: return@LaunchedEffect
        if (userLocation != null) {
            mapView.controller.animateTo(GeoPoint(userLocation.latitude, userLocation.longitude), mapView.zoomLevelDouble, 800L)
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
            if (centerOnUserTrigger == 0) {
                when {
                    userLocation != null -> mapView.controller.setCenter(GeoPoint(userLocation.latitude, userLocation.longitude))
                    points.isNotEmpty() -> mapView.controller.setCenter(points.first().second)
                }
            }
            // "I'm here" circle overlay (like Maps apps)
            if (userLocation != null) {
                mapView.overlays.add(UserLocationOverlay(GeoPoint(userLocation.latitude, userLocation.longitude)))
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

/** Draws the familiar blue dot + accuracy halo used in mapping apps. */
private class UserLocationOverlay(private val location: GeoPoint) : Overlay() {
    private val haloPaint = AndroidPaint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(50, 33, 150, 243)
        style = AndroidPaint.Style.FILL
    }
    private val dotPaint = AndroidPaint().apply {
        isAntiAlias = true
        color = android.graphics.Color.rgb(33, 150, 243)
        style = AndroidPaint.Style.FILL
    }
    private val strokePaint = AndroidPaint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = AndroidPaint.Style.STROKE
        strokeWidth = 4f
    }

    override fun draw(canvas: AndroidCanvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val point = mapView.projection.toPixels(location, null)
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 32f, haloPaint)
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 14f, dotPaint)
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 14f, strokePaint)
    }
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
    var showIconPicker by remember { mutableStateOf(false) }

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
                device?.let { dev ->
                    DeviceHeaderCard(
                        device = dev,
                        onOpenMaps = { launchMap(context, dev) },
                        onCopyCoordinates = { copyCoordinates(context, dev) },
                        onShare = { shareLocation(context, dev) },
                        onPickIcon = { showIconPicker = true },
                    )
                } ?: Text("Device not found.")
            }
            if (showIconPicker) {
                item {
                    IconPickerCard(
                        currentIcon = device?.customIcon,
                        onPick = { key ->
                            device?.let { appViewModel.setCustomIcon(it.address, key) }
                            showIconPicker = false
                        },
                        onDismiss = { showIconPicker = false },
                    )
                }
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
    onPickIcon: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    resolveIconFromKey(device.customIcon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
                Column {
                    Text(if (device.isConnected) "Currently connected" else Formatting.formatSeenAt(device.lastSeenAt), color = statusColor(device.isConnected, device.lastSeenAt))
                    Text(device.lastPlaceLabel ?: formatCoordinates(device.lastLatitude, device.lastLongitude))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenMaps, enabled = device.lastLatitude != null && device.lastLongitude != null) {
                    Icon(Icons.Default.Directions, contentDescription = null)
                    Text("Open in maps")
                }
                Button(onClick = onCopyCoordinates) {
                    Text("Copy coords")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text("Share")
                }
                Button(onClick = onPickIcon) {
                    Text("Change icon")
                }
            }
        }
    }
}

@Composable
private fun IconPickerCard(
    currentIcon: String?,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var search by remember { mutableStateOf("") }
    val icons = remember(search) {
        if (search.isBlank()) ALL_ICON_OPTIONS
        else ALL_ICON_OPTIONS.filter { it.label.contains(search, ignoreCase = true) }
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Choose icon", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onDismiss).padding(4.dp),
                )
            }
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                placeholder = { Text("Search icons…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(icons, key = { it.key }) { option ->
                    val selected = option.key == currentIcon
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { onPick(option.key) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(option.icon, contentDescription = option.label, modifier = Modifier.size(28.dp))
                        Text(
                            option.label,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    }
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
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
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

private fun resolveIcon(customIcon: String?, deviceType: Int): ImageVector =
    ALL_ICON_OPTIONS.find { it.key == customIcon }?.icon ?: defaultDeviceIcon(deviceType)

private fun resolveIconFromKey(customIcon: String?): ImageVector =
    ALL_ICON_OPTIONS.find { it.key == customIcon }?.icon ?: Icons.Default.Bluetooth

private fun defaultDeviceIcon(type: Int): ImageVector = when (type) {
    BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> Icons.Default.Headphones
    BluetoothClass.Device.PHONE_SMART -> Icons.Default.Smartphone
    BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> Icons.Default.Speaker
    else -> Icons.Default.Bluetooth
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
