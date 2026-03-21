package codegito.xyz.bluetoothseeker.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import codegito.xyz.bluetoothseeker.data.model.LogMode
import codegito.xyz.bluetoothseeker.data.model.MapStyle
import codegito.xyz.bluetoothseeker.data.model.SettingsSnapshot
import codegito.xyz.bluetoothseeker.data.model.SortMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("bluetooth_seeker_settings")

data class UserSettings(
    val logMode: LogMode = LogMode.CONNECT_AND_DISCONNECT,
    val retentionDays: Int = 30,
    val amoledMode: Boolean = false,
    val disconnectNotifications: Boolean = false,
    val sortMode: SortMode = SortMode.MOST_RECENT,
    val ignoredAddresses: Set<String> = emptySet(),
    val mapStyle: MapStyle = MapStyle.LIBERTY,
    val mapStyleDark: MapStyle = MapStyle.DARK,
    val mapStyleFollowsDark: Boolean = false,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val logMode = stringPreferencesKey("log_mode")
        val retentionDays = intPreferencesKey("retention_days")
        val amoledMode = booleanPreferencesKey("amoled_mode")
        val disconnectNotifications = booleanPreferencesKey("disconnect_notifications")
        val sortMode = stringPreferencesKey("sort_mode")
        val ignoredAddresses = stringPreferencesKey("ignored_addresses")
        val mapStyle = stringPreferencesKey("map_style")
        val mapStyleDark = stringPreferencesKey("map_style_dark")
        val mapStyleFollowsDark = booleanPreferencesKey("map_style_follows_dark")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map(::readSettings)

    suspend fun update(transform: (UserSettings) -> UserSettings) {
        context.dataStore.edit { prefs ->
            writeSettings(prefs, transform(readSettings(prefs)))
        }
    }

    suspend fun import(snapshot: SettingsSnapshot) {
        context.dataStore.edit { prefs ->
            writeSettings(
                prefs,
                UserSettings(
                    logMode = LogMode.valueOf(snapshot.logMode),
                    retentionDays = snapshot.retentionDays,
                    amoledMode = snapshot.amoledMode,
                    disconnectNotifications = snapshot.disconnectNotifications,
                    sortMode = SortMode.valueOf(snapshot.sortMode),
                    ignoredAddresses = snapshot.ignoredAddresses.toSet(),
                    mapStyle = snapshot.mapStyle?.let { runCatching { MapStyle.valueOf(it) }.getOrNull() } ?: MapStyle.LIBERTY,
                ),
            )
        }
    }

    private fun readSettings(prefs: Preferences): UserSettings =
        UserSettings(
            logMode = prefs[Keys.logMode]?.let(LogMode::valueOf)
                ?: LogMode.CONNECT_AND_DISCONNECT,
            retentionDays = prefs[Keys.retentionDays] ?: 30,
            amoledMode = prefs[Keys.amoledMode] ?: false,
            disconnectNotifications = prefs[Keys.disconnectNotifications] ?: false,
            sortMode = prefs[Keys.sortMode]?.let(SortMode::valueOf) ?: SortMode.MOST_RECENT,
            ignoredAddresses = prefs[Keys.ignoredAddresses]
                ?.split("|")
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet(),
            mapStyle = prefs[Keys.mapStyle]?.let { runCatching { MapStyle.valueOf(it) }.getOrNull() } ?: MapStyle.LIBERTY,
            mapStyleDark = prefs[Keys.mapStyleDark]?.let { runCatching { MapStyle.valueOf(it) }.getOrNull() } ?: MapStyle.DARK,
            mapStyleFollowsDark = prefs[Keys.mapStyleFollowsDark] ?: false,
        )

    private fun writeSettings(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        settings: UserSettings,
    ) {
        prefs[Keys.logMode] = settings.logMode.name
        prefs[Keys.retentionDays] = settings.retentionDays
        prefs[Keys.amoledMode] = settings.amoledMode
        prefs[Keys.disconnectNotifications] = settings.disconnectNotifications
        prefs[Keys.sortMode] = settings.sortMode.name
        prefs[Keys.ignoredAddresses] = settings.ignoredAddresses.sorted().joinToString("|")
        prefs[Keys.mapStyle] = settings.mapStyle.name
        prefs[Keys.mapStyleDark] = settings.mapStyleDark.name
        prefs[Keys.mapStyleFollowsDark] = settings.mapStyleFollowsDark
    }
}
