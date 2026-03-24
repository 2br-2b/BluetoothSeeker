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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Car
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Earbuds
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView as MapLibreView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import codegito.xyz.bluetoothseeker.data.model.LocationQuality
import codegito.xyz.bluetoothseeker.data.model.MapStyle as AppMapStyle
import codegito.xyz.bluetoothseeker.BuildConfig

private object Routes {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Settings = "settings"
    const val IgnoredDevices = "ignored_devices"
    const val MapTheme = "map_theme"
    const val Device = "device/{address}"
    fun device(address: String) = "device/$address"
}

private data class IconOption(val key: String, val icon: ImageVector, val label: String)

// Curated list of icons useful for labeling Bluetooth devices.
private val ALL_ICON_OPTIONS: List<IconOption> by lazy {
    listOf(
        IconOption("Bluetooth", Icons.Filled.Bluetooth, "Bluetooth"),
        IconOption("BluetoothConnected", Icons.Filled.BluetoothConnected, "Connected"),
        IconOption("Headphones", Icons.Filled.Headphones, "Headphones"),
        IconOption("Earbuds", Icons.Filled.Earbuds, "Earbuds"),
        IconOption("Speaker", Icons.Filled.Speaker, "Speaker"),
        IconOption("Smartphone", Icons.Filled.Smartphone, "Smartphone"),
        IconOption("Phone", Icons.Filled.Phone, "Phone"),
        IconOption("Tablet", Icons.Filled.Tablet, "Tablet"),
        IconOption("Watch", Icons.Filled.Watch, "Watch"),
        IconOption("Laptop", Icons.Filled.Laptop, "Laptop"),
        IconOption("Computer", Icons.Filled.Computer, "Computer"),
        IconOption("Tv", Icons.Filled.Tv, "TV"),
        IconOption("Keyboard", Icons.Filled.Keyboard, "Keyboard"),
        IconOption("Mouse", Icons.Filled.Mouse, "Mouse"),
        IconOption("Gamepad", Icons.Filled.Gamepad, "Gamepad"),
        IconOption("Print", Icons.Filled.Print, "Printer"),
        IconOption("Car", Icons.Filled.Car, "Car"),
        IconOption("CameraAlt", Icons.Filled.CameraAlt, "Camera"),
        IconOption("MusicNote", Icons.Filled.MusicNote, "Music"),
        IconOption("Wifi", Icons.Filled.Wifi, "Wifi"),
        IconOption("Home", Icons.Filled.Home, "Home"),
        IconOption("Person", Icons.Filled.Person, "Person"),
        IconOption("Settings", Icons.Filled.Settings, "Settings"),
    ).sortedBy { it.label }
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
    val bgLocationPerm = Permissions.backgroundLocationPermission()
    val bgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        onPermissionsResult()
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        // After core permissions, request background location separately (required on API 29+)
        if (bgLocationPerm != null && !Permissions.hasBackgroundLocation(context)) {
            bgLauncher.launch(bgLocationPerm)
        } else {
            onPermissionsResult()
        }
    }

    if (rootState.isLoading) return

    LaunchedEffect(rootState.showOnboarding) {
        navController.navigate(if (rootState.showOnboarding) Routes.Onboarding else Routes.Home) {
            popUpTo(0)
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (rootState.showOnboarding) Routes.Onboarding else Routes.Home,
    ) {
        composable(Routes.Onboarding) {
            OnboardingScreen(
                missingPermissions = Permissions.missingPermissionLabels(context),
                onContinue = { launcher.launch(permissionRequest) },
                onOpenApp = {
                    onPermissionsResult()
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.Home,
            // Home stays still; device screen slides up over it
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
        ) {
            HomeScreen(
                appViewModel = appViewModel,
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenDevice = { navController.navigate(Routes.device(it)) },
                onOpenMapTheme = { navController.navigate(Routes.MapTheme) },
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
                onOpenIgnoredDevices = { navController.navigate(Routes.IgnoredDevices) },
            )
        }
        composable(Routes.MapTheme) {
            MapThemeScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
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
            // Slide up from bottom (like a modal on top of Home)
            enterTransition = { slideInVertically(animationSpec = tween(320)) { it } },
            exitTransition = { slideOutVertically(animationSpec = tween(280)) { it } },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { slideOutVertically(animationSpec = tween(280)) { it } },
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
    missingPermissions: List<String>,
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
            if (missingPermissions.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text("The following permissions are required:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                missingPermissions.forEach { label ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
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

private enum class SheetSnap { FULL, PARTIAL, HANDLE }

/**
 * Reusable draggable bottom sheet with three snap points (HANDLE / PARTIAL / FULL).
 *
 * Must be placed inside a Box so it can overlay sibling content (e.g. a map).
 *
 * @param partialFraction Fraction of available height for the PARTIAL snap (e.g. 0.35f for home,
 *   0.60f for device detail).
 * @param defaultSnap Initial snap position.
 * @param header Non-scrollable composable shown above the scrollable body.
 * @param scrollableContent Body content; receives the LazyListState for nested-scroll integration.
 */
@Composable
private fun DraggableBottomSheet(
    partialFraction: Float = 0.35f,
    defaultSnap: SheetSnap = SheetSnap.PARTIAL,
    header: @Composable () -> Unit = {},
    scrollableContent: @Composable (androidx.compose.foundation.lazy.LazyListState) -> Unit,
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val fullPx = with(density) { maxHeight.toPx() }
        val handlePx = with(density) { 28.dp.toPx() }
        val partialPx = fullPx * partialFraction

        val sheetHeightPx = remember { Animatable(0f) }

        fun anchorFor(snap: SheetSnap) = when (snap) {
            SheetSnap.FULL    -> fullPx
            SheetSnap.PARTIAL -> partialPx
            SheetSnap.HANDLE  -> handlePx
        }

        suspend fun snapTo(snap: SheetSnap, initialVelocity: Float = 0f) =
            sheetHeightPx.animateTo(
                anchorFor(snap),
                spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.72f),
                initialVelocity = initialVelocity,
            )

        fun nearestSnap(px: Float): SheetSnap =
            SheetSnap.entries.minByOrNull { kotlin.math.abs(anchorFor(it) - px) } ?: SheetSnap.FULL

        var sheetSnap by remember { mutableStateOf(defaultSnap) }
        val listState = rememberLazyListState()

        LaunchedEffect(fullPx) {
            if (sheetHeightPx.value == 0f) sheetHeightPx.snapTo(anchorFor(defaultSnap))
        }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y > 0f && sheetHeightPx.value >= fullPx - 1f && !listState.canScrollBackward) {
                        scope.launch {
                            sheetHeightPx.snapTo((sheetHeightPx.value + available.y).coerceIn(handlePx, fullPx))
                        }
                        return available
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    val threshold = 400f
                    val cur = nearestSnap(sheetHeightPx.value)
                    if (available.y > threshold && sheetHeightPx.value > handlePx + 1f && !listState.canScrollBackward) {
                        val snap = when (cur) {
                            SheetSnap.FULL    -> SheetSnap.PARTIAL
                            SheetSnap.PARTIAL -> SheetSnap.HANDLE
                            SheetSnap.HANDLE  -> SheetSnap.HANDLE
                        }
                        sheetSnap = snap; snapTo(snap, -available.y); return available
                    }
                    if (available.y < -threshold && sheetHeightPx.value < fullPx - 1f) {
                        val snap = when (cur) {
                            SheetSnap.HANDLE  -> SheetSnap.PARTIAL
                            SheetSnap.PARTIAL -> SheetSnap.FULL
                            SheetSnap.FULL    -> SheetSnap.FULL
                        }
                        sheetSnap = snap; snapTo(snap, -available.y); return available
                    }
                    return Velocity.Zero
                }
            }
        }

        val cornerDp = if (sheetHeightPx.value >= fullPx - 1f) 0.dp else 16.dp

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(with(density) { sheetHeightPx.value.toDp() })
                .clip(RoundedCornerShape(topStart = cornerDp, topEnd = cornerDp))
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    val velocityTracker = VelocityTracker()
                    detectVerticalDragGestures(
                        onDragStart = { velocityTracker.resetTracking() },
                        onVerticalDrag = { change, delta ->
                            velocityTracker.addPointerInputChange(change)
                            scope.launch {
                                sheetHeightPx.snapTo((sheetHeightPx.value - delta).coerceIn(handlePx, fullPx))
                            }
                        },
                        onDragEnd = {
                            val velocity = velocityTracker.calculateVelocity()
                            val threshold = 400f
                            val cur = nearestSnap(sheetHeightPx.value)
                            val snap = when {
                                velocity.y < -threshold -> when (cur) {
                                    SheetSnap.HANDLE  -> SheetSnap.PARTIAL
                                    SheetSnap.PARTIAL -> SheetSnap.FULL
                                    SheetSnap.FULL    -> SheetSnap.FULL
                                }
                                velocity.y > threshold -> when (cur) {
                                    SheetSnap.FULL    -> SheetSnap.PARTIAL
                                    SheetSnap.PARTIAL -> SheetSnap.HANDLE
                                    SheetSnap.HANDLE  -> SheetSnap.HANDLE
                                }
                                else -> cur
                            }
                            sheetSnap = snap
                            scope.launch { snapTo(snap, -velocity.y) }
                        },
                    )
                },
        ) {
            // Drag handle pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val next = when (sheetSnap) {
                            SheetSnap.FULL    -> SheetSnap.PARTIAL
                            SheetSnap.PARTIAL -> SheetSnap.HANDLE
                            SheetSnap.HANDLE  -> SheetSnap.FULL
                        }
                        sheetSnap = next
                        scope.launch { snapTo(next) }
                    }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
            if (sheetHeightPx.value > handlePx + with(density) { 8.dp.toPx() }) {
                header()
                Box(modifier = Modifier.weight(1f).nestedScroll(nestedScrollConnection)) {
                    scrollableContent(listState)
                }
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
    onOpenMapTheme: () -> Unit,
) {
    val context = LocalContext.current
    val devices by appViewModel.devices.collectAsState()
    val settings by appViewModel.settings.collectAsState()
    val userLocation by appViewModel.currentUserLocation().collectAsState()
    val isDark = isSystemInDarkTheme()
    val developerMode by appViewModel.developerMode.collectAsState()
    var versionTapCount by remember { mutableStateOf(0) }
    var devLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(developerMode) {
        if (developerMode) {
            devLogs = try {
                Runtime.getRuntime()
                    .exec(arrayOf("logcat", "-d", "-t", "200", "-s", "BT_Receiver:D", "BT_Repository:D"))
                    .inputStream.bufferedReader().readLines()
            } catch (e: Exception) {
                listOf("Failed to read logs: ${e.message}")
            }
        }
    }
    val activeMapStyle = if (settings.mapStyleFollowsDark && isDark) settings.mapStyleDark else settings.mapStyle

    // Request background location if not yet granted (must be separate from core permissions on API 29+)
    val bgLocationPerm = Permissions.backgroundLocationPermission()
    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (bgLocationPerm != null && !Permissions.hasBackgroundLocation(context)) {
            bgLauncher.launch(bgLocationPerm)
        }
    }
    var query by rememberSaveable { mutableStateOf("") }
    val filteredDevices = remember(devices, query) {
        devices.filter {
            query.isBlank() || it.name.contains(query, ignoreCase = true) || it.address.contains(query, ignoreCase = true)
        }
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Anchor heights in px — computed once layout is known
    val handlePx  = with(density) { 28.dp.toPx() }
    // partial: handle + search field + sort chips + 1 device card + spacing
    val partialPx = with(density) { (28 + 56 + 48 + 88 + 12 + 12 + 16 + 16).dp.toPx() }

    // Animatable sheet height in px; starts at 0 until we know the available height
    val sheetHeightPx = remember { Animatable(0f) }
    var fullPx by remember { mutableStateOf(0f) }

    fun anchorFor(snap: SheetSnap) = when (snap) {
        SheetSnap.FULL    -> fullPx
        SheetSnap.PARTIAL -> partialPx
        SheetSnap.HANDLE  -> handlePx
    }

    suspend fun snapTo(snap: SheetSnap, initialVelocity: Float = 0f) =
        sheetHeightPx.animateTo(
            anchorFor(snap),
            spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.72f),
            initialVelocity = initialVelocity,
        )

    var sheetSnap by remember { mutableStateOf(SheetSnap.PARTIAL) }

    // Nearest snap given current px height
    fun nearestSnap(px: Float): SheetSnap {
        val snaps = SheetSnap.entries
        return snaps.minByOrNull { kotlin.math.abs(anchorFor(it) - px) } ?: SheetSnap.FULL
    }
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

    Scaffold(
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
                        Icon(Icons.Default.MyLocation, contentDescription = "Center on my location")
                    }
                    IconButton(onClick = onOpenMapTheme) {
                        Icon(Icons.Default.Palette, contentDescription = "Themes")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onSizeChanged { size ->
                    if (fullPx == 0f) {
                        fullPx = size.height.toFloat()
                        scope.launch { sheetHeightPx.snapTo(partialPx) }
                    }
                },
        ) {
            DeviceMap(
                devices = filteredDevices,
                userLocation = userLocation,
                centerOnUserTrigger = centerOnUserLocation,
                mapStyle = activeMapStyle,
                onOpenDevice = onOpenDevice,
            )

            // Connection/disconnection banner overlaid at top
            ConnectionBanner(event = bannerEvent)

            // Corner radius: 0 when fully expanded, 16dp otherwise
            val cornerDp = if (fullPx > 0f && sheetHeightPx.value >= fullPx - 1f) 0.dp else 16.dp

            val listState = rememberLazyListState()

            // NestedScrollConnection: intercept downward scroll when list is at top
            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        // Only consume downward drag when sheet is fully expanded
                        if (available.y > 0f && sheetHeightPx.value >= fullPx - 1f) {
                            val atTop = !listState.canScrollBackward
                            if (atTop) {
                                scope.launch {
                                    val newH = (sheetHeightPx.value + available.y).coerceIn(handlePx, fullPx)
                                    sheetHeightPx.snapTo(newH)
                                }
                                return available
                            }
                        }
                        return Offset.Zero
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        val velocityThreshold = 400f
                        val currentSnap = nearestSnap(sheetHeightPx.value)
                        // Downward fling: collapse sheet (only when list is at top)
                        if (available.y > velocityThreshold && sheetHeightPx.value > handlePx + 1f && !listState.canScrollBackward) {
                            val snap = when (currentSnap) {
                                SheetSnap.FULL    -> SheetSnap.PARTIAL
                                SheetSnap.PARTIAL -> SheetSnap.HANDLE
                                SheetSnap.HANDLE  -> SheetSnap.HANDLE
                            }
                            sheetSnap = snap
                            snapTo(snap, initialVelocity = -available.y)
                            return available
                        }
                        // Upward fling: expand sheet when not fully open
                        if (available.y < -velocityThreshold && sheetHeightPx.value < fullPx - 1f) {
                            val snap = when (currentSnap) {
                                SheetSnap.HANDLE  -> SheetSnap.PARTIAL
                                SheetSnap.PARTIAL -> SheetSnap.FULL
                                SheetSnap.FULL    -> SheetSnap.FULL
                            }
                            sheetSnap = snap
                            snapTo(snap, initialVelocity = -available.y)
                            return available
                        }
                        return Velocity.Zero
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(with(density) { sheetHeightPx.value.toDp() })
                    .clip(RoundedCornerShape(topStart = cornerDp, topEnd = cornerDp))
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        val velocityTracker = VelocityTracker()
                        detectVerticalDragGestures(
                            onDragStart = { velocityTracker.resetTracking() },
                            onVerticalDrag = { change, delta ->
                                velocityTracker.addPointerInputChange(change)
                                // delta > 0 = finger moving down = sheet shrinks
                                scope.launch {
                                    val newH = (sheetHeightPx.value - delta).coerceIn(handlePx, fullPx)
                                    sheetHeightPx.snapTo(newH)
                                }
                            },
                            onDragEnd = {
                                val velocity = velocityTracker.calculateVelocity()
                                // velocity.y < 0 → swiping up → expand; velocity.y > 0 → swiping down → collapse
                                val velocityThreshold = 400f
                                val currentSnap = nearestSnap(sheetHeightPx.value)
                                val snap = when {
                                    velocity.y < -velocityThreshold -> when (currentSnap) {
                                        SheetSnap.HANDLE  -> SheetSnap.PARTIAL
                                        SheetSnap.PARTIAL -> SheetSnap.FULL
                                        SheetSnap.FULL    -> SheetSnap.FULL
                                    }
                                    velocity.y > velocityThreshold -> when (currentSnap) {
                                        SheetSnap.FULL    -> SheetSnap.PARTIAL
                                        SheetSnap.PARTIAL -> SheetSnap.HANDLE
                                        SheetSnap.HANDLE  -> SheetSnap.HANDLE
                                    }
                                    else -> currentSnap
                                }
                                sheetSnap = snap
                                // pass height velocity (negated because height is inverse of y)
                                scope.launch { snapTo(snap, initialVelocity = -velocity.y) }
                            },
                        )
                    },
            ) {
                // Drag handle pill — tapping cycles through states
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = when (sheetSnap) {
                                SheetSnap.FULL    -> SheetSnap.PARTIAL
                                SheetSnap.PARTIAL -> SheetSnap.HANDLE
                                SheetSnap.HANDLE  -> SheetSnap.FULL
                            }
                            sheetSnap = next
                            scope.launch { snapTo(next) }
                        }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                }

                if (sheetHeightPx.value > handlePx + with(density) { 8.dp.toPx() }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                    ) {
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
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.nestedScroll(nestedScrollConnection),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(filteredDevices, key = { it.address }) { device ->
                                    DeviceRow(device = device, onClick = { onOpenDevice(device.address) })
                                }
                            }
                        }
                    }
                    if (developerMode) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "Developer logs",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, devLogs.joinToString("\n"))
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share logs"))
                                    },
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "Share logs", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share logs", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp),
                            ) {
                                val scrollState = rememberScrollState(Int.MAX_VALUE)
                                Text(
                                    text = if (devLogs.isEmpty()) "No BT logs captured yet." else devLogs.joinToString("\n"),
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.verticalScroll(scrollState),
                                )
                            }
                        }
                    }
                    // Version row
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}" + if (developerMode) " (dev)" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (developerMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                versionTapCount++
                                if (!developerMode && versionTapCount >= 7) {
                                    appViewModel.developerMode.value = true
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                    )
                }
            }
        }
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

private fun createUserLocationBitmap(context: Context): android.graphics.Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (28 * density).toInt().coerceAtLeast(1)
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    val r = size / 2f
    // White outer ring
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(r, r, r, paint)
    // Teal fill
    paint.color = android.graphics.Color.parseColor("#00BCD4")
    canvas.drawCircle(r, r, r * 0.72f, paint)
    // White center dot
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(r, r, r * 0.28f, paint)
    return bitmap
}

private fun createDeviceMarkerBitmap(
    context: Context,
    deviceName: String,
    customIconKey: String?,
    deviceType: Int,
): android.graphics.Bitmap {
    val density = context.resources.displayMetrics.density
    val circleSize = (34 * density).toInt().coerceAtLeast(1)
    val tailHeight = (10 * density).toInt().coerceAtLeast(1)
    val totalHeight = circleSize + tailHeight
    val bitmap = android.graphics.Bitmap.createBitmap(circleSize, totalHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    val r = circleSize / 2f
    val fillColor = when {
        customIconKey != null -> iconKeyToColor(customIconKey)
        deviceType == android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> android.graphics.Color.parseColor("#7B1FA2")
        deviceType == android.bluetooth.BluetoothClass.Device.PHONE_SMART -> android.graphics.Color.parseColor("#1565C0")
        deviceType == android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> android.graphics.Color.parseColor("#2E7D32")
        else -> android.graphics.Color.parseColor("#37474F")
    }
    // White outline circle
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(r, r, r, paint)
    // Colored fill
    paint.color = fillColor
    canvas.drawCircle(r, r, r * 0.84f, paint)
    // First letter of device name
    paint.color = android.graphics.Color.WHITE
    paint.textSize = r * 0.92f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    val letter = deviceName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val textY = r - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(letter, r, textY, paint)
    // Tail triangle pointing down
    val path = android.graphics.Path()
    path.moveTo(r - r * 0.38f, circleSize.toFloat())
    path.lineTo(r + r * 0.38f, circleSize.toFloat())
    path.lineTo(r, totalHeight.toFloat())
    path.close()
    paint.color = fillColor
    canvas.drawPath(path, paint)
    return bitmap
}

private fun iconKeyToColor(key: String): Int {
    val hue = ((key.hashCode() and 0x7FFFFFFF) % 360).toFloat()
    return android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.65f, 0.50f))
}

private fun addMarkersToMap(
    map: MapLibreMap,
    devices: List<DeviceSummary>,
    userLocation: LocationSnapshot?,
    context: Context,
    onOpenDevice: (String) -> Unit,
) {
    val iconFactory = IconFactory.getInstance(context)
    map.clear()
    userLocation?.let { loc ->
        val userIcon = iconFactory.fromBitmap(createUserLocationBitmap(context))
        map.addMarker(
            MarkerOptions()
                .position(LatLng(loc.latitude, loc.longitude))
                .title("You are here")
                .icon(userIcon)
        )
    }
    clusterDevices(devices).forEach { cluster ->
        val first = cluster.first()
        val lat = first.lastLatitude ?: return@forEach
        val lon = first.lastLongitude ?: return@forEach
        val deviceIcon = iconFactory.fromBitmap(
            createDeviceMarkerBitmap(context, first.name, first.customIcon, first.type)
        )
        map.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lon))
                .title(if (cluster.size == 1) first.name else "${cluster.size} devices nearby")
                .snippet(first.lastPlaceLabel ?: formatCoordinates(first.lastLatitude, first.lastLongitude))
                .icon(deviceIcon)
        )
    }
    map.setOnMarkerClickListener { marker ->
        val device = devices.find { d ->
            d.lastLatitude == marker.position.latitude && d.lastLongitude == marker.position.longitude
        }
        if (device != null) onOpenDevice(device.address)
        true
    }
}

@Composable
private fun DeviceMap(
    devices: List<DeviceSummary>,
    userLocation: LocationSnapshot?,
    centerOnUserTrigger: Int,
    mapStyle: AppMapStyle,
    onOpenDevice: (String) -> Unit,
    initialCameraTarget: LocationSnapshot? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapRef = remember { mutableStateOf<MapLibreMap?>(null) }
    val mapViewRef = remember { mutableStateOf<MapLibreView?>(null) }

    MapLibre.getInstance(context)

    // Proper lifecycle management: forward Activity lifecycle events to MapView
    // and destroy it when the composable leaves composition.
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) { mapViewRef.value?.onStart() }
            override fun onResume(owner: LifecycleOwner) { mapViewRef.value?.onResume() }
            override fun onPause(owner: LifecycleOwner) { mapViewRef.value?.onPause() }
            override fun onStop(owner: LifecycleOwner) { mapViewRef.value?.onStop() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapRef.value = null
            mapViewRef.value?.onStop()
            mapViewRef.value?.onDestroy()
            mapViewRef.value = null
        }
    }

    // Animate to user location when button tapped
    LaunchedEffect(centerOnUserTrigger) {
        if (centerOnUserTrigger == 0) return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        mapRef.value?.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(loc.latitude, loc.longitude))
                    .zoom(15.0)
                    .build()
            ),
            800,
        )
    }

    // Update markers whenever devices, location, or map readiness changes
    val mapReady = mapRef.value
    LaunchedEffect(devices, userLocation, mapReady) {
        val map = mapRef.value ?: return@LaunchedEffect
        addMarkersToMap(map, devices, userLocation, context, onOpenDevice)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapLibreView(ctx).also { mapView ->
                mapViewRef.value = mapView
                mapView.onCreate(null)
                mapView.onStart()
                mapView.onResume()
                mapView.getMapAsync { map ->
                    mapRef.value = map
                    map.setStyle(mapStyle.url)
                    map.uiSettings.isRotateGesturesEnabled = true
                    map.uiSettings.isDoubleTapGesturesEnabled = true
                    val startPos = initialCameraTarget
                        ?: userLocation
                        ?: devices.firstOrNull { it.lastLatitude != null }
                            ?.let { LocationSnapshot(it.lastLatitude!!, it.lastLongitude!!, LocationQuality.LAST_KNOWN) }
                    startPos?.let {
                        map.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(it.latitude, it.longitude))
                                    .zoom(13.0)
                                    .build()
                            )
                        )
                    }
                }
            }
        },
        update = { _ ->
            mapRef.value?.let { map ->
                if (map.style?.uri != mapStyle.url) {
                    map.setStyle(mapStyle.url) {
                        addMarkersToMap(map, devices, userLocation, context, onOpenDevice)
                    }
                }
            }
        },
    )
}

private fun clusterDevices(devices: List<DeviceSummary>): List<List<DeviceSummary>> =
    devices.filter { it.lastLatitude != null && it.lastLongitude != null }
        .groupBy { d ->
            "${((d.lastLatitude ?: 0.0) * 500).toInt()}:${((d.lastLongitude ?: 0.0) * 500).toInt()}"
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
    val settings by appViewModel.settings.collectAsState()
    val userLocation by appViewModel.currentUserLocation().collectAsState()
    val isDark = isSystemInDarkTheme()
    val activeMapStyle = if (settings.mapStyleFollowsDark && isDark) settings.mapStyleDark else settings.mapStyle

    // Stable filter flow so deviceEvents doesn't recreate on every recomposition
    val filterFlow = remember { kotlinx.coroutines.flow.MutableStateFlow(HistoryFilter.ALL) }
    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    LaunchedEffect(filter) { filterFlow.value = filter }
    val events by remember(address) { appViewModel.deviceEvents(address, filterFlow) }.collectAsState()

    var showIconPicker by remember { mutableStateOf(false) }

    // Build single-device list for map; hide old location when currently connected
    val deviceForMap = remember(device) {
        val dev = device ?: return@remember emptyList<DeviceSummary>()
        if (dev.isConnected || dev.lastLatitude == null) emptyList()
        else listOf(
            DeviceSummary(
                address = dev.address,
                name = dev.name,
                type = dev.deviceType,
                isConnected = false,
                lastSeenAt = dev.lastSeenAt,
                lastLatitude = dev.lastLatitude,
                lastLongitude = dev.lastLongitude,
                lastPlaceLabel = dev.lastPlaceLabel,
                lastLocationQuality = dev.lastLocationQuality,
                distanceMeters = null,
                isIgnored = false,
                customIcon = dev.customIcon,
            )
        )
    }

    // Prefer to open camera on the device's last known location
    val deviceCameraTarget = remember(device) {
        val dev = device ?: return@remember null
        if (!dev.isConnected && dev.lastLatitude != null)
            LocationSnapshot(dev.lastLatitude, dev.lastLongitude!!, LocationQuality.LAST_KNOWN)
        else null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background map: user location + device last location (if not currently connected)
        DeviceMap(
            devices = deviceForMap,
            userLocation = userLocation,
            centerOnUserTrigger = 0,
            mapStyle = activeMapStyle,
            onOpenDevice = {},
            initialCameraTarget = deviceCameraTarget,
        )

        // Semi-transparent top bar overlay
        CenterAlignedTopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            title = { Text(device?.name ?: "Device") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

        // Draggable bottom sheet: starts taller than the home sheet (60% of screen)
        DraggableBottomSheet(
            partialFraction = 0.60f,
            defaultSnap = SheetSnap.PARTIAL,
            header = {
                // Status row: icon (tap to change) + status/location text
                val dev = device
                if (dev != null) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showIconPicker = !showIconPicker },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    resolveIconFromKey(dev.customIcon),
                                    contentDescription = "Change icon",
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (dev.isConnected) "Currently connected" else Formatting.formatSeenAt(dev.lastSeenAt),
                                    color = statusColor(dev.isConnected, dev.lastSeenAt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    dev.lastPlaceLabel ?: formatCoordinates(dev.lastLatitude, dev.lastLongitude),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            // Quick actions
                            IconButton(
                                onClick = { launchMap(context, dev) },
                                enabled = dev.lastLatitude != null && !dev.isConnected,
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = "Open in maps")
                            }
                            IconButton(onClick = { shareLocation(context, dev) }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                        }
                        if (showIconPicker) {
                            Spacer(Modifier.height(8.dp))
                            IconPickerCard(
                                currentIcon = dev.customIcon,
                                onPick = { key ->
                                    appViewModel.setCustomIcon(dev.address, key)
                                    showIconPicker = false
                                },
                                onDismiss = { showIconPicker = false },
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        // Filter chips row (non-scrollable, part of header)
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
                                    label = {
                                        Text(
                                            option.name.lowercase().replace('_', ' ')
                                                .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                                        )
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            },
            scrollableContent = { listState ->
                if (events.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text("No events recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
                    ) {
                        items(events, key = { it.id }) { event ->
                            EventRow(event = event)
                        }
                    }
                }
            },
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapThemeScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val settings by appViewModel.settings.collectAsState()
    val isDark = isSystemInDarkTheme()
    val activeMapStyle = if (settings.mapStyleFollowsDark && isDark) settings.mapStyleDark else settings.mapStyle
    val userLocation by appViewModel.currentUserLocation().collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen map preview
        DeviceMap(
            devices = emptyList(),
            userLocation = userLocation,
            centerOnUserTrigger = 0,
            mapStyle = activeMapStyle,
            onOpenDevice = {},
        )

        // Top bar overlay
        CenterAlignedTopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            title = { Text("Map Theme") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Bluetooth, contentDescription = "Back")
                }
            },
        )

        // Bottom overlay: theme cards
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // System dark mode switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (settings.mapStyleFollowsDark && isDark) "System dark mode" else "System light mode",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = settings.mapStyleFollowsDark,
                    onCheckedChange = { appViewModel.updateMapStyleFollowsDark(it) },
                )
            }

            // Light style picker (always shown as "Light theme")
            if (!settings.mapStyleFollowsDark || !isDark) {
                Text("Light theme", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppMapStyle.entries.toList()) { style ->
                        val selected = settings.mapStyle == style
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selected) 2.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { appViewModel.updateMapStyle(style) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(style.label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Dark style picker (shown when follow-dark is on and system is dark)
            if (settings.mapStyleFollowsDark && isDark) {
                Text("Dark theme", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppMapStyle.entries.toList()) { style ->
                        val selected = settings.mapStyleDark == style
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (selected) 2.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { appViewModel.updateMapStyleDark(style) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(style.label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
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
