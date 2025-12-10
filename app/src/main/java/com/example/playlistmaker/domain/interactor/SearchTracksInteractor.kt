package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchTracksInteractor(private val repository: TrackRepository) {
    suspend fun execute(query: String): List<Track> {
        val tracks = repository.searchTracks(query)
        return tracks.filter { track ->
            track.trackName.isNotEmpty() && track.artistName.isNotEmpty()
        }
    }
}