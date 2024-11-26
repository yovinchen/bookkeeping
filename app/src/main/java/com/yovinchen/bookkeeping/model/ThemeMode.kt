package com.yovinchen.bookkeeping.model

import androidx.compose.ui.graphics.Color

sealed class ThemeMode {
    object FOLLOW_SYSTEM : ThemeMode()
    object LIGHT : ThemeMode()
    object DARK : ThemeMode()
    data class CUSTOM(val primaryColor: Color) : ThemeMode()
}
