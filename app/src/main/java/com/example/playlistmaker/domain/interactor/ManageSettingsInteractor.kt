package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.SettingsRepository

class ManageSettingsInteractor(private val repository: SettingsRepository) {
    fun getThemeMode(): Int = repository.getThemeMode()
    fun saveThemeMode(mode: Int) = repository.saveThemeMode(mode)
    fun toggleTheme(isDarkTheme: Boolean): Int {
        val newMode = if (isDarkTheme) {
            2 // MODE_NIGHT_YES
        } else {
            1 // MODE_NIGHT_NO
        }
        saveThemeMode(newMode)
        return newMode
    }
}