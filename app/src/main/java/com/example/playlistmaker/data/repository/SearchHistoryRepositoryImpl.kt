package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SearchHistoryRepository {

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    private val gson = Gson()

    override fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }

        saveHistory(history)
    }

    override fun getHistory(): List<Track> {
        val historyJson = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (historyJson != null) {
            try {
                val type = object : TypeToken<List<TrackDto>>() {}.type
                val dtoList: List<TrackDto> = gson.fromJson(historyJson, type) ?: emptyList()
                TrackMapper.mapList(dtoList)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    override fun hasHistory(): Boolean {
        return getHistory().isNotEmpty()
    }

    private fun saveHistory(history: List<Track>) {
        try {
            val dtoList = history.map { TrackMapper.mapToDto(it) }
            val historyJson = gson.toJson(dtoList)
            sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, historyJson).apply()
        } catch (e: Exception) {
            // Логируем ошибку, но не падаем
            e.printStackTrace()
        }
    }
}