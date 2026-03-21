package codegito.xyz.bluetoothseeker.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import codegito.xyz.bluetoothseeker.data.model.ThemePreference

private val FallbackDarkColorScheme = darkColorScheme(
    primary = FreshMint,
    secondary = SignalBlue,
    tertiary = Sand,
    background = Slate,
    surface = DeepOcean,
)

private val FallbackLightColorScheme = lightColorScheme(
    primary = SignalBlue,
    secondary = DeepOcean,
    tertiary = FreshMint,
    background = Sand,
    surface = Color.White,
)

private val AmoledColorScheme = darkColorScheme(
    primary = FreshMint,
    secondary = SignalBlue,
    tertiary = Sand,
    background = Night,
    surface = Night,
    surfaceVariant = Color(0xFF0D0D0D),
    surfaceContainer = Color(0xFF0D0D0D),
    surfaceContainerHigh = Color(0xFF111111),
    surfaceContainerHighest = Color(0xFF151515),
    surfaceContainerLow = Color(0xFF090909),
    surfaceContainerLowest = Color(0xFF050505),
)

@Composable
fun BluetoothSeekerTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (themePreference) {
        ThemePreference.AMOLED -> AmoledColorScheme
        ThemePreference.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(context)
            } else {
                FallbackLightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
