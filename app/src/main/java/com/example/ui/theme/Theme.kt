package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NilDarkColorScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = Slate950,
    primaryContainer = EmeraldDeep,
    onPrimaryContainer = EmeraldGlow,
    secondary = IndigoAccent,
    onSecondary = Slate100,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate300,
    tertiary = CyberGold,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    outlineVariant = Slate800,
    error = CyberRed,
    onError = Slate100
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NilDarkColorScheme,
        typography = Typography,
        content = content
    )
}

