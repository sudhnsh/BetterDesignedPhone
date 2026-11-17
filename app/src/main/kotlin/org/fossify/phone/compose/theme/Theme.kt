package org.fossify.phone.compose.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.phone.activities.SimpleActivity
import org.fossify.phone.extensions.config

@Composable
fun rememberIsPowerSaveMode(): State<Boolean> {
    val context = LocalContext.current
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    val isPowerSave = remember { mutableStateOf(powerManager?.isPowerSaveMode == true) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                    isPowerSave.value = powerManager?.isPowerSaveMode == true
                }
            }
        }
        val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        try {
            context.registerReceiver(receiver, filter)
        } catch (_: Exception) {}

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }
    return isPowerSave
}

@Composable
fun PhoneTheme(
    activity: SimpleActivity? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    isOledModeEnabled: Boolean? = null,
    isOledScreen: Boolean? = null,
    isSystemTheme: Boolean? = null,
    customBackgroundColor: Int? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val config = remember { context.config }
    val baseConfig = remember { context.baseConfig }
    val isPowerSaveMode by rememberIsPowerSaveMode()

    val oledEnabled = isOledModeEnabled ?: config.oledModeEnabled
    val oledScreen = isOledScreen ?: config.isOledScreen
    val systemTheme = isSystemTheme ?: baseConfig.isSystemThemeEnabled
    val customBg = customBackgroundColor ?: baseConfig.backgroundColor

    val isDark = if (systemTheme) {
        darkTheme
    } else {
        ColorUtils.calculateLuminance(customBg) < 0.5
    }

    // OLED mode is applied when:
    // 1. User has enabled OLED mode in config, OR
    // 2. User's screen is OLED and system Low Power / Battery Saver mode is active
    val isOledMode = isDark && (oledEnabled || (oledScreen && isPowerSaveMode))

    val colorScheme = when {
        isOledMode -> {
            // Pure Black 100% OLED mode (#000000)
            darkColorScheme(
                primary = AccentOffWhite, // Clean off-white / cream
                onPrimary = AccentPureBlack, // Crisp pure black on primary
                primaryContainer = Color(0xFF1E1E22),
                onPrimaryContainer = AccentPureWhite,
                background = OledBackground, // True 0 nits pure black
                surface = OledSurface, // True 0 nits pure black
                surfaceContainer = Color(0xFF000000),
                surfaceContainerHigh = Color(0xFF101010),
                surfaceContainerHighest = Color(0xFF181818),
                surfaceVariant = Color(0xFF121212),
                onBackground = AccentPureWhite,
                onSurface = AccentPureWhite,
                onSurfaceVariant = Color(0xFFA5A5AB),
                outline = Color(0xFF262628),
                outlineVariant = Color(0xFF161618),
                error = Color(0xFFE57373),
                onError = AccentPureBlack
            )
        }
        isDark -> {
            // Standard Monochromatic Dark Mode (Charcoal / Off-White / Grey)
            darkColorScheme(
                primary = AccentOffWhite, // Off-white / cream
                onPrimary = Color(0xFF141416), // Dark charcoal
                primaryContainer = Color(0xFF2C2C30),
                onPrimaryContainer = AccentOffWhite,
                background = DarkBackground,
                surface = DarkSurface,
                surfaceContainer = Color(0xFF1E1E24),
                surfaceContainerHigh = Color(0xFF28282E),
                surfaceContainerHighest = Color(0xFF323238),
                surfaceVariant = Color(0xFF26262C),
                onBackground = AccentOffWhite,
                onSurface = AccentOffWhite,
                onSurfaceVariant = Color(0xFFB0B0B8),
                outline = Color(0xFF45454E),
                outlineVariant = Color(0xFF2B2B30),
                error = Color(0xFFE57373),
                onError = Color(0xFF141416)
            )
        }
        else -> {
            // Standard Monochromatic Light Mode (Cream / Charcoal / Grey)
            lightColorScheme(
                primary = AccentCharcoal, // Charcoal
                onPrimary = AccentPureWhite,
                primaryContainer = Color(0xFFEAEAEA),
                onPrimaryContainer = AccentCharcoal,
                background = LightBackground, // Warm cream / off-white
                surface = LightSurface,
                surfaceContainer = Color(0xFFF3F3F0),
                surfaceContainerHigh = Color(0xFFEBEBE6),
                surfaceContainerHighest = Color(0xFFE0E0DB),
                surfaceVariant = Color(0xFFE8E8E2),
                onBackground = Color(0xFF181818),
                onSurface = Color(0xFF181818),
                onSurfaceVariant = Color(0xFF65656C),
                outline = Color(0xFFC0C0C6),
                outlineVariant = Color(0xFFDCDCE2),
                error = Color(0xFFD32F2F),
                onError = AccentPureWhite
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
