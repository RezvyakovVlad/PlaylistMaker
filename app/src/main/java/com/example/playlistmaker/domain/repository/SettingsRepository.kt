package com.example.playlistmaker.domain.repository

interface SettingsRepository {
    fun getThemeMode(): Int
    fun saveThemeMode(mode: Int)
}