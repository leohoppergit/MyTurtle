package de.leohopper.myturtle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Moss,
    onPrimary = SurfaceWarm,
    primaryContainer = SkyTint,
    onPrimaryContainer = MossDark,
    secondary = Soil,
    onSecondary = SurfaceWarm,
    secondaryContainer = Sand,
    onSecondaryContainer = Soil,
    surface = SurfaceWarm,
    onSurface = MossDark,
    surfaceVariant = Sand,
    onSurfaceVariant = Soil,
    outline = Soil.copy(alpha = 0.45f),
)

private val DarkColors = darkColorScheme(
    primary = SkyTint,
    onPrimary = MossDark,
    primaryContainer = MossDark,
    onPrimaryContainer = SkyTint,
    secondary = Sand,
    onSecondary = Soil,
    secondaryContainer = Soil,
    onSecondaryContainer = Sand,
    surface = ColorTokens.DarkSurface,
    onSurface = Sand,
    surfaceVariant = ColorTokens.DarkSurfaceVariant,
    onSurfaceVariant = SkyTint,
    outline = Sand.copy(alpha = 0.35f),
)

@Composable
fun MyTurtleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

private object ColorTokens {
    val DarkSurface = MossDark
    val DarkSurfaceVariant = Soil
}
