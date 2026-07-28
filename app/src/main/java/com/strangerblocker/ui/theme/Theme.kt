package com.strangerblocker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightOnSurfaceMuted,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = LightError,
    secondary = LightPrimary,
    onSecondary = LightOnPrimary,
    tertiary = LightPrimary,
    onTertiary = LightOnPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkOnSurfaceMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = DarkError,
    secondary = DarkPrimary,
    onSecondary = DarkOnPrimary,
    tertiary = DarkPrimary,
    onTertiary = DarkOnPrimary,
)

@Composable
fun StrangerBlockerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
