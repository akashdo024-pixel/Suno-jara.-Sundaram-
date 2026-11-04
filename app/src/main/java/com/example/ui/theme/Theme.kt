package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MelodyXColorScheme = darkColorScheme(
  primary = MelodyXGreen,
  onPrimary = MelodyXDarkBg,
  primaryContainer = MelodyXSurfaceHighlight,
  onPrimaryContainer = MelodyXGreenBright,
  secondary = MelodyXPurple,
  onSecondary = MelodyXDarkBg,
  secondaryContainer = MelodyXPurpleDeep,
  onSecondaryContainer = MelodyXTextPrimary,
  tertiary = MelodyXNeonPink,
  onTertiary = MelodyXDarkBg,
  background = MelodyXDarkBg,
  onBackground = MelodyXTextPrimary,
  surface = MelodyXSurface,
  onSurface = MelodyXTextPrimary,
  surfaceVariant = MelodyXSurfaceVariant,
  onSurfaceVariant = MelodyXTextSecondary,
  outline = MelodyXBorder,
  error = MelodyXError,
  onError = MelodyXTextPrimary
)

@Composable
fun MelodyXTheme(
  darkTheme: Boolean = true, // MelodyX is dark-first music streaming experience
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = MelodyXColorScheme,
    typography = Typography,
    content = content
  )
}
