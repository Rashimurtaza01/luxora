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

private val LuxoraDarkColorScheme = darkColorScheme(
    primary = LuxoraChampagneGold,
    onPrimary = LuxoraMidnightNavy,
    primaryContainer = LuxoraDeepEmerald,
    onPrimaryContainer = LuxoraChampagneLight,
    secondary = LuxoraChampagneLight,
    onSecondary = LuxoraMidnightNavy,
    secondaryContainer = LuxoraDarkCard,
    onSecondaryContainer = LuxoraChampagneLight,
    tertiary = LuxoraDeepEmerald,
    onTertiary = LuxoraPureWhite,
    background = LuxoraMidnightNavy,
    onBackground = LuxoraWarmIvory,
    surface = LuxoraDarkSurface,
    onSurface = LuxoraWarmIvory,
    surfaceVariant = LuxoraDarkCard,
    onSurfaceVariant = LuxoraSlateGray,
    outline = LuxoraDarkBorder,
    error = LuxoraErrorBurgundy,
    onError = LuxoraPureWhite
)

private val LuxoraLightColorScheme = lightColorScheme(
    primary = LuxoraDeepEmerald,
    onPrimary = LuxoraPureWhite,
    primaryContainer = LuxoraMidnightNavy,
    onPrimaryContainer = LuxoraChampagneGold,
    secondary = LuxoraChampagneGold,
    onSecondary = LuxoraMidnightNavy,
    secondaryContainer = LuxoraChampagneLight,
    onSecondaryContainer = LuxoraMidnightNavy,
    tertiary = LuxoraMidnightNavy,
    onTertiary = LuxoraPureWhite,
    background = LuxoraWarmIvory,
    onBackground = LuxoraMidnightNavy,
    surface = LuxoraPureWhite,
    onSurface = LuxoraMidnightNavy,
    surfaceVariant = LuxoraSurfaceIvory,
    onSurfaceVariant = LuxoraSlateGray,
    outline = LuxoraBorderLight,
    error = LuxoraErrorBurgundy,
    onError = LuxoraPureWhite
)

@Composable
fun LuxoraTheme(
    darkTheme: Boolean = false, // Default to elegant warm ivory & navy luxury aesthetic
    dynamicColor: Boolean = false, // Maintain bespoke Luxora branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LuxoraDarkColorScheme else LuxoraLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
