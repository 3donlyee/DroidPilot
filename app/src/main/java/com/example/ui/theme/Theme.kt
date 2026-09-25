package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PilotPrimary,
    onPrimary = PilotOnPrimary,
    primaryContainer = PilotPrimaryContainer,
    onPrimaryContainer = PilotOnPrimaryContainer,
    secondary = PilotSecondary,
    onSecondary = PilotOnSecondary,
    secondaryContainer = PilotSecondaryContainer,
    onSecondaryContainer = PilotOnSecondaryContainer,
    tertiary = PilotTertiary,
    onTertiary = PilotOnTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PilotPrimary,
    onPrimary = PilotOnPrimary,
    primaryContainer = PilotPrimaryContainer,
    onPrimaryContainer = PilotOnPrimaryContainer,
    secondary = PilotSecondary,
    onSecondary = PilotOnSecondary,
    tertiary = PilotTertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent theme brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // DroidPilot looks best in high-tech dark theme by default
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
