package com.yovinchen.bookkeeping

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.yovinchen.bookkeeping.utils.FilePickerUtil

/**
 * 全局文件选择器启动器
 * 用于在整个应用程序中共享同一个文件选择器实例
 */
private var filePickerLauncher: ActivityResultLauncher<Array<String>>? = null

/**
 * 获取预先注册的文件选择器启动器的扩展函数
 * 
 * @return 预先注册的文件选择器启动器
 * @throws IllegalStateException 如果文件选择器未初始化
 */
fun ComponentActivity.getPreregisteredFilePickerLauncher(): ActivityResultLauncher<Array<String>> {
    return filePickerLauncher ?: throw IllegalStateException("FilePickerLauncher not initialized")
}

/**
 * 应用程序的主活动
 * 负责初始化应用界面和必要的系统组件
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置系统窗口装饰，确保内容能够扩展到系统栏区域
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 预注册文件选择器，用于处理文件选择操作
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            // 当用户选择文件后，调用工具类处理文件选择结果
            FilePickerUtil.handleFileSelection(this, uri)
        }

        // 设置应用的主Compose内容
        setContent {
            BookkeepingApp()
        }
    }
}

/**
 * 系统状态栏和导航栏颜色设置
 * 根据当前主题模式设置系统UI元素的颜色和外观
 * 
 * @param isDarkTheme 是否为暗色主题
 */
@Composable
private fun SystemBarColor(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        // 获取当前主题的表面颜色用于系统栏
        val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
        val currentWindow = (view.context as? Activity)?.window
        SideEffect {
            currentWindow?.let { window ->
                // 设置状态栏和导航栏颜色
                window.statusBarColor = surfaceColor
                window.navigationBarColor = surfaceColor
                // 设置系统栏图标的亮暗模式，以确保在不同背景下的可见性
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDarkTheme
                    isAppearanceLightNavigationBars = !isDarkTheme
                }
            }
        }
    }
}

/**
 * 记账应用的主Compose函数
 * 处理主题设置并启动主导航组件
 */
@Composable
fun BookkeepingApp() {
    // 跟踪当前应用的主题模式状态
    var themeMode by remember { mutableStateOf<ThemeMode>(ThemeMode.FOLLOW_SYSTEM) }
    
    // 根据主题模式确定是否使用暗色主题
    val isDarkTheme = when (themeMode) {
        is ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()  // 跟随系统设置
        is ThemeMode.LIGHT -> false                          // 强制使用亮色主题
        is ThemeMode.DARK -> true                            // 强制使用暗色主题
        is ThemeMode.CUSTOM -> isSystemInDarkTheme()         // 自定义主题下的基础亮暗模式仍跟随系统
    }

    // 处理自定义主题颜色方案
    val customColorScheme = when (themeMode) {
        is ThemeMode.CUSTOM -> {
            // 从主题模式中提取自定义主色
            val primaryColor = (themeMode as ThemeMode.CUSTOM).primaryColor
            if (isDarkTheme) {
                // 暗色模式下的自定义颜色方案
                MaterialTheme.colorScheme.copy(
                    primary = primaryColor,
                    secondary = primaryColor.copy(alpha = 0.7f),
                    tertiary = primaryColor.copy(alpha = 0.5f)
                )
            } else {
                // 亮色模式下的自定义颜色方案
                MaterialTheme.colorScheme.copy(
                    primary = primaryColor,
                    secondary = primaryColor.copy(alpha = 0.7f),
                    tertiary = primaryColor.copy(alpha = 0.5f)
                )
            }
        }
        else -> null  // 非自定义主题模式使用默认颜色方案
    }

    // 应用主题到整个应用内容
    BookkeepingTheme(
        darkTheme = isDarkTheme,
        customColorScheme = customColorScheme
    ) {
        // 设置系统状态栏和导航栏颜色
        SystemBarColor(isDarkTheme)
        
        // 创建填充整个屏幕的基础Surface
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            // 启动主导航组件，并传递主题相关参数
            MainNavigation(
                currentTheme = themeMode,
                onThemeChange = { themeMode = it }  // 允许导航组件中的屏幕更改主题
            )
        }
    }
}

/**
 * 示例问候函数
 * 仅用于开发预览和测试目的
 * 
 * @param name 要显示的名称
 * @param modifier 应用于Text组件的修饰符
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello 你好 $name!",
        modifier = modifier
    )
}

/**
 * Greeting组件的预览函数
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BookkeepingTheme {
        Greeting("Android")
    }
}

/**
 * 整个应用的预览函数
 */
@Preview(showBackground = true)
@Composable
fun BookkeepingAppPreview() {
    BookkeepingApp()
}