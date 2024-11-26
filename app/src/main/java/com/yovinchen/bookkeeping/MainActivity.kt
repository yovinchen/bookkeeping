package com.yovinchen.bookkeeping

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.yovinchen.bookkeeping.model.ThemeMode
import com.yovinchen.bookkeeping.ui.components.predefinedColors
import com.yovinchen.bookkeeping.ui.navigation.MainNavigation
import com.yovinchen.bookkeeping.ui.theme.BookkeepingTheme

@Composable
private fun SystemBarColor(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
        val currentWindow = (view.context as? Activity)?.window
        SideEffect {
            currentWindow?.let { window ->
                window.statusBarColor = surfaceColor
                window.navigationBarColor = surfaceColor
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDarkTheme
                    isAppearanceLightNavigationBars = !isDarkTheme
                }
            }
        }
    }
}

@Composable
fun BookkeepingApp() {
    var themeMode by remember { mutableStateOf<ThemeMode>(ThemeMode.FOLLOW_SYSTEM) }
    
    val isDarkTheme = when (themeMode) {
        is ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        is ThemeMode.LIGHT -> false
        is ThemeMode.DARK -> true
        is ThemeMode.CUSTOM -> isSystemInDarkTheme()
    }

    val customColorScheme = when (themeMode) {
        is ThemeMode.CUSTOM -> {
            val primaryColor = (themeMode as ThemeMode.CUSTOM).primaryColor
            if (isDarkTheme) {
                MaterialTheme.colorScheme.copy(
                    primary = primaryColor,
                    secondary = primaryColor.copy(alpha = 0.7f),
                    tertiary = primaryColor.copy(alpha = 0.5f)
                )
            } else {
                MaterialTheme.colorScheme.copy(
                    primary = primaryColor,
                    secondary = primaryColor.copy(alpha = 0.7f),
                    tertiary = primaryColor.copy(alpha = 0.5f)
                )
            }
        }
        else -> null
    }

    BookkeepingTheme(
        darkTheme = isDarkTheme,
        customColorScheme = customColorScheme
    ) {
        SystemBarColor(isDarkTheme)
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            MainNavigation(
                currentTheme = themeMode,
                onThemeChange = { themeMode = it }
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BookkeepingApp()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello 你好 $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BookkeepingTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun BookkeepingAppPreview() {
    BookkeepingApp()
}