package com.example.easymorsecoding.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Machined steel and cut acrylic: every structural box is sharp-cornered.
private val SharpShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

private val TacticalColorScheme = darkColorScheme(
    primary = TacPrimary,
    onPrimary = TacOnPrimary,
    primaryContainer = TacPrimaryContainer,
    onPrimaryContainer = TacOnPrimaryContainer,
    inversePrimary = TacInversePrimary,
    secondary = TacSecondary,
    onSecondary = TacOnSecondary,
    secondaryContainer = TacSecondaryContainer,
    onSecondaryContainer = TacOnSecondaryContainer,
    tertiary = TacTertiary,
    onTertiary = TacOnTertiary,
    tertiaryContainer = TacTertiaryContainer,
    onTertiaryContainer = TacOnTertiaryContainer,
    background = TacSurface,
    onBackground = TacOnSurface,
    surface = TacSurface,
    onSurface = TacOnSurface,
    surfaceVariant = TacSurfaceVariant,
    onSurfaceVariant = TacOnSurfaceVariant,
    surfaceTint = TacPrimaryContainer,
    inverseSurface = TacInverseSurface,
    inverseOnSurface = TacInverseOnSurface,
    error = TacError,
    onError = TacOnError,
    errorContainer = TacErrorContainer,
    onErrorContainer = TacOnErrorContainer,
    outline = TacOutline,
    outlineVariant = TacOutlineVariant,
    surfaceBright = TacSurfaceBright,
    surfaceDim = TacSurface,
    surfaceContainerLowest = TacSurfaceContainerLowest,
    surfaceContainerLow = TacSurfaceContainerLow,
    surfaceContainer = TacSurfaceContainer,
    surfaceContainerHigh = TacSurfaceContainerHigh,
    surfaceContainerHighest = TacSurfaceContainerHighest
)

@Composable
fun EasyMorseCodingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TacticalColorScheme,
        typography = Typography,
        shapes = SharpShapes,
        content = content
    )
}