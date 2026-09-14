package com.kidsg.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KidsGLightColorScheme = lightColorScheme(
    primary = KidsGColors.OrangePrimary,
    onPrimary = KidsGColors.White,
    primaryContainer = KidsGColors.OrangeLight,
    onPrimaryContainer = KidsGColors.OrangeDark,
    secondary = KidsGColors.AccentYellow,
    onSecondary = KidsGColors.BlackText,
    tertiary = KidsGColors.AccentSkyBlue,
    background = KidsGColors.Background,
    onBackground = KidsGColors.BlackText,
    surface = KidsGColors.White,
    onSurface = KidsGColors.TextPrimary,
    surfaceVariant = KidsGColors.SurfaceDesk,
    onSurfaceVariant = KidsGColors.TextSecondary,
    outline = KidsGColors.BorderSubtle,
    error = KidsGColors.Error,
    onError = KidsGColors.White
)

@Composable
fun KidsGTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // KidsG enforces its signature crisp stationery white & orange brand aesthetic
    MaterialTheme(
        colorScheme = KidsGLightColorScheme,
        content = content
    )
}
