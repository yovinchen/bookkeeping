package com.yovinchen.bookkeeping.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.yovinchen.bookkeeping.ui.theme.*

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryDark.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryDark,
    
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryDark.copy(alpha = 0.12f),
    onSecondaryContainer = SecondaryDark,
    
    tertiary = TertiaryDark,
    onTertiary = Color.Black,
    tertiaryContainer = TertiaryDark.copy(alpha = 0.12f),
    onTertiaryContainer = TertiaryDark,
    
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceDark.copy(alpha = 0.7f),
    
    error = ErrorDark,
    onError = Color.Black,
    errorContainer = ErrorDark.copy(alpha = 0.12f),
    onErrorContainer = ErrorDark,
    
    outline = DividerDark,
    outlineVariant = DividerDark.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = PrimaryLight,
    
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = SecondaryLight,
    
    tertiary = TertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = TertiaryLight,
    
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceLight.copy(alpha = 0.7f),
    
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorLight.copy(alpha = 0.12f),
    onErrorContainer = ErrorLight,
    
    outline = DividerLight,
    outlineVariant = DividerLight.copy(alpha = 0.5f)
)

@Composable
fun BookkeepingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    customColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        customColorScheme != null -> customColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}