package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchTracksInteractor(private val repository: TrackRepository) {
    fun execute(query: String): List<Track> {
        // Используем query для чего-то
        println("Searching for: $query")  // Добавили использование query
        return emptyList()
    }
}