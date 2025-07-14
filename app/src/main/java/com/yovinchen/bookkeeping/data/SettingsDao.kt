package com.yovinchen.bookkeeping.data

import androidx.room.*
import com.yovinchen.bookkeeping.model.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings?>
    
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): Settings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: Settings)
    
    @Query("UPDATE settings SET monthStartDay = :day WHERE id = 1")
    suspend fun updateMonthStartDay(day: Int)
    
    @Query("UPDATE settings SET themeMode = :mode WHERE id = 1")
    suspend fun updateThemeMode(mode: String)
    
    @Query("UPDATE settings SET autoBackupEnabled = :enabled WHERE id = 1")
    suspend fun updateAutoBackupEnabled(enabled: Boolean)
    
    @Query("UPDATE settings SET autoBackupInterval = :interval WHERE id = 1")
    suspend fun updateAutoBackupInterval(interval: Int)
    
    @Query("UPDATE settings SET lastBackupTime = :time WHERE id = 1")
    suspend fun updateLastBackupTime(time: Long)
}