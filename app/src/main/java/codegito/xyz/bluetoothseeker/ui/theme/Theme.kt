package codegito.xyz.bluetoothseeker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import codegito.xyz.bluetoothseeker.data.model.ThemePreference

private val DarkColorScheme = darkColorScheme(
    primary = FreshMint,
    secondary = SignalBlue,
    tertiary = Sand,
    background = Slate,
    surface = DeepOcean,
)

private val LightColorScheme = lightColorScheme(
    primary = SignalBlue,
    secondary = DeepOcean,
    tertiary = FreshMint,
    background = Sand,
    surface = Color.White,
)

@Composable
fun BluetoothSeekerTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themePreference) {
        ThemePreference.SYSTEM -> LightColorScheme
        ThemePreference.AMOLED -> DarkColorScheme.copy(background = Night, surface = Night)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
