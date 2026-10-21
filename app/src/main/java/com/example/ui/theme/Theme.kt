package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleBlue,
    onPrimary = AppleWhite,
    primaryContainer = Color(0xFF003870),
    onPrimaryContainer = AppleWhite,
    secondary = AppleGreen,
    onSecondary = Color(0xFF00210B),
    secondaryContainer = Color(0xFF113D1E),
    onSecondaryContainer = AppleWhite,
    background = OledBlack,
    onBackground = AppleWhite,
    surface = AppleCardSurface,
    onSurface = AppleWhite,
    surfaceVariant = AppleCardElevated,
    onSurfaceVariant = AppleMutedGray,
    outline = AppleCardBorder,
    error = AppleRed,
    onError = AppleWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppleDarkColorScheme,
        typography = Typography,
        content = content
    )
}
