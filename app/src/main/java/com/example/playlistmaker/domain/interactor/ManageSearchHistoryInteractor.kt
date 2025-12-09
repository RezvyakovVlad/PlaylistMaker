package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository

class ManageSearchHistoryInteractor(private val repository: SearchHistoryRepository) {
    fun addTrack(track: Track) = repository.addTrack(track)
    fun getHistory(): List<Track> = repository.getHistory()
    fun clearHistory() = repository.clearHistory()
    fun hasHistory(): Boolean = repository.hasHistory()
}
