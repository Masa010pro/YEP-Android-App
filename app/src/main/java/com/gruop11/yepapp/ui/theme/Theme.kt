package com.gruop11.yepapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val YepLightColorScheme = lightColorScheme(
    primary = YepEmerald,
    onPrimary = YepWhite,
    primaryContainer = YepEmeraldSoft,
    onPrimaryContainer = YepEmeraldDark,
    secondary = YepSlateDark,
    onSecondary = YepWhite,
    secondaryContainer = YepSlate100,
    onSecondaryContainer = YepSlate800,
    tertiary = YepIndigo,
    onTertiary = YepWhite,
    tertiaryContainer = YepIndigoSoft,
    onTertiaryContainer = YepIndigo,
    background = YepSlateBg,
    onBackground = YepSlateDark,
    surface = YepWhite,
    onSurface = YepSlateDark,
    surfaceVariant = YepSlate100,
    onSurfaceVariant = YepSlate600,
    outline = YepSlate200,
    outlineVariant = YepSlate200,
    error = YepRose,
    onError = YepWhite,
    errorContainer = YepRoseSoft,
    onErrorContainer = YepRose,
)

val YepShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun YepPlatformTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = YepLightColorScheme,
        typography = YepTypography,
        shapes = YepShapes,
        content = content,
    )
}
