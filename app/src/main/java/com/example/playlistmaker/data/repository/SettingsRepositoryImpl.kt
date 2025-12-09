package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    companion object {
        private const val THEME_MODE_KEY = "NightMode"
    }

    override fun getThemeMode(): Int {
        return sharedPreferences.getInt(THEME_MODE_KEY, 0) // 0 = MODE_NIGHT_FOLLOW_SYSTEM
    }

    override fun saveThemeMode(mode: Int) {
        sharedPreferences.edit().putInt(THEME_MODE_KEY, mode).apply()
    }
}