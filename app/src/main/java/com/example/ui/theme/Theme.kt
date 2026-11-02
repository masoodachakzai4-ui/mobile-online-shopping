package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    onPrimary = OnCoralPrimaryDark,
    primaryContainer = CoralPrimaryContainerDark,
    onPrimaryContainer = OnCoralPrimaryContainerDark,
    secondary = EmeraldSecondaryDark,
    onSecondary = OnEmeraldSecondaryDark,
    secondaryContainer = EmeraldSecondaryContainerDark,
    onSecondaryContainer = OnEmeraldSecondaryContainerDark,
    tertiary = GoldTertiaryDark,
    onTertiary = OnGoldTertiaryDark,
    tertiaryContainer = GoldTertiaryContainerDark,
    onTertiaryContainer = OnGoldTertiaryContainerDark,
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = OnCoralPrimary,
    primaryContainer = CoralPrimaryContainer,
    onPrimaryContainer = OnCoralPrimaryContainer,
    secondary = EmeraldSecondary,
    onSecondary = OnEmeraldSecondary,
    secondaryContainer = EmeraldSecondaryContainer,
    onSecondaryContainer = OnEmeraldSecondaryContainer,
    tertiary = GoldTertiary,
    onTertiary = OnGoldTertiary,
    tertiaryContainer = GoldTertiaryContainer,
    onTertiaryContainer = OnGoldTertiaryContainer,
    background = WarmBackground,
    onBackground = OnWarmBackground,
    surface = WarmSurface,
    onSurface = OnWarmSurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = OnWarmSurfaceVariant,
    outline = WarmOutline
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun BazaarPKTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
