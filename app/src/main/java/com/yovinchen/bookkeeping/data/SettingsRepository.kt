package com.yovinchen.bookkeeping.data

import com.yovinchen.bookkeeping.model.Settings
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    
    fun getSettings(): Flow<Settings?> = settingsDao.getSettings()
    
    suspend fun getSettingsOnce(): Settings {
        return settingsDao.getSettingsOnce() ?: Settings()
    }
    
    suspend fun updateSettings(settings: Settings) {
        settingsDao.updateSettings(settings)
    }
    
    suspend fun updateMonthStartDay(day: Int) {
        // 确保日期在有效范围内 (1-28)
        val validDay = day.coerceIn(1, 28)
        settingsDao.updateMonthStartDay(validDay)
    }
    
    suspend fun updateThemeMode(mode: String) {
        settingsDao.updateThemeMode(mode)
    }
    
    suspend fun updateAutoBackupEnabled(enabled: Boolean) {
        settingsDao.updateAutoBackupEnabled(enabled)
    }
    
    suspend fun updateAutoBackupInterval(interval: Int) {
        settingsDao.updateAutoBackupInterval(interval)
    }
    
    suspend fun updateLastBackupTime(time: Long) {
        settingsDao.updateLastBackupTime(time)
    }
    
    suspend fun ensureSettingsExist() {
        if (settingsDao.getSettingsOnce() == null) {
            settingsDao.updateSettings(Settings())
        }
    }
}