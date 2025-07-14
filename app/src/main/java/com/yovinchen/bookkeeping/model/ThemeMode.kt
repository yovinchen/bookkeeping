package com.yovinchen.bookkeeping.model

import androidx.compose.ui.graphics.Color

/**
 * 主题模式密封类
 * 用于表示应用程序的不同主题设置选项
 * 通过密封类实现，限制可能的主题模式类型
 */
sealed class ThemeMode {
    /**
     * 跟随系统主题模式
     * 应用将根据设备系统的暗色/亮色主题设置自动调整
     */
    object FOLLOW_SYSTEM : ThemeMode()
    
    /**
     * 固定亮色主题模式
     * 无论设备系统设置如何，应用将始终使用亮色主题
     */
    object LIGHT : ThemeMode()
    
    /**
     * 固定暗色主题模式
     * 无论设备系统设置如何，应用将始终使用暗色主题
     */
    object DARK : ThemeMode()
    
    /**
     * 自定义主题模式
     * 允许用户选择自定义的主题颜色
     * 
     * @property primaryColor 用户选择的主要颜色，将影响应用的主色调
     */
    data class CUSTOM(val primaryColor: Color) : ThemeMode()
}
