package org.fossify.phone.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.phone.activities.SimpleActivity

@Composable
fun PhoneTheme(
    activity: SimpleActivity? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val primaryColor = activity?.getProperPrimaryColor()?.let { Color(it) } ?: PrimaryBlue
    val backgroundColor = activity?.getProperBackgroundColor()?.let { Color(it) }
        ?: if (darkTheme) DarkBackground else LightBackground
    val textColor = activity?.getProperTextColor()?.let { Color(it) }
        ?: if (darkTheme) TextWhite else TextDark

    val isDark = if (activity != null) {
        backgroundColor.luminance() < 0.5f
    } else {
        darkTheme
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            background = backgroundColor,
            surface = backgroundColor,
            surfaceContainer = Color(0xFF1E1E22),
            surfaceContainerHigh = Color(0xFF28282D),
            surfaceContainerHighest = Color(0xFF333338),
            surfaceVariant = Color(0xFF2A2A2E),
            onPrimary = TextWhite,
            onBackground = textColor,
            onSurface = textColor,
            onSurfaceVariant = Color(0xFFC7C7CF),
            outline = Color(0xFF55555C),
            outlineVariant = Color(0xFF35353A)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            background = backgroundColor,
            surface = backgroundColor,
            surfaceContainer = Color(0xFFF2F2F7),
            surfaceContainerHigh = Color(0xFFEBEBF0),
            surfaceContainerHighest = Color(0xFFE0E0E6),
            surfaceVariant = Color(0xFFE5E5EA),
            onPrimary = TextWhite,
            onBackground = textColor,
            onSurface = textColor,
            onSurfaceVariant = Color(0xFF505058),
            outline = Color(0xFFB0B0B8),
            outlineVariant = Color(0xFFDCDCE2)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
