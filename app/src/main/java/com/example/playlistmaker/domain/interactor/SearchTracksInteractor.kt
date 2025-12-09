package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchTracksInteractor(private val repository: TrackRepository) {
    suspend fun execute(query: String): List<Track> {
        val tracks = repository.searchTracks(query)
        // Можно добавить фильтрацию или другую бизнес-логику здесь
        return tracks.filter { track ->
            // Пример бизнес-правила: фильтруем треки без названия или исполнителя
            track.trackName.isNotEmpty() && track.artistName.isNotEmpty()
        }
    }
}