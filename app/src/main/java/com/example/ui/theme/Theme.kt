package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    primaryContainer = IosBlueLight,
    onPrimaryContainer = IosBlueDark,
    secondary = IosGreen,
    onSecondary = Color.White,
    secondaryContainer = IosGreenLight,
    onSecondaryContainer = IosGreenDark,
    tertiary = IosOrange,
    onTertiary = Color.White,
    tertiaryContainer = IosOrangeLight,
    onTertiaryContainer = Color(0xFF8A4D00),
    background = IosBackground,
    onBackground = IosTextPrimary,
    surface = IosCardSurface,
    onSurface = IosTextPrimary,
    surfaceVariant = IosCardSurfaceVariant,
    onSurfaceVariant = IosTextSecondary,
    outline = IosBorder,
    error = IosRed,
    onError = Color.White,
    errorContainer = IosRedLight,
    onErrorContainer = IosRedDark
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color(0xFF002244),
    primaryContainer = Color(0xFF004080),
    onPrimaryContainer = Color(0xFFD0E6FF),
    secondary = Color(0xFF30D158),
    onSecondary = Color(0xFF003814),
    secondaryContainer = Color(0xFF0A5524),
    onSecondaryContainer = Color(0xFFC7F8D4),
    tertiary = Color(0xFFFF9F0A),
    onTertiary = Color(0xFF4A2800),
    background = IosDarkBackground,
    onBackground = IosDarkTextPrimary,
    surface = IosDarkCard,
    onSurface = IosDarkTextPrimary,
    surfaceVariant = IosDarkCardVariant,
    onSurfaceVariant = IosDarkTextSecondary,
    outline = IosDarkBorder,
    error = Color(0xFFFF453A),
    onError = Color(0xFF490005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our refined iOS colors by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

