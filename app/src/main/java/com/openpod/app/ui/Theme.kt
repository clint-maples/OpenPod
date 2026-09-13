package com.openpod.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = darkColorScheme(
    primary = Color(0xFF7FB7C9),
    onPrimary = Color(0xFF00344A),
    secondary = Color(0xFFB5C9D3),
    background = Color(0xFF121418),
    surface = Color(0xFF121418),
    surfaceVariant = Color(0xFF1C2026),
    onBackground = Color(0xFFE8F1F5),
    onSurface = Color(0xFFE8F1F5)
)

@Composable
fun OpenPodTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
