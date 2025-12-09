package com.example.playlistmaker.data.repository

import android.util.Log
import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.network.NetworkClient
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class TrackRepositoryImpl(private val networkClient: NetworkClient) : TrackRepository {

    private var lastSearchQuery: String = ""
    private var lastSearchResults: List<Track> = emptyList()

    override suspend fun searchTracks(query: String): List<Track> {
        if (query.isBlank()) {
            return emptyList()
        }

        lastSearchQuery = query

        return try {
            val response: SearchResponse = networkClient.searchTracksSuspend(query)
            val tracks = TrackMapper.mapList(response.results)
            lastSearchResults = tracks
            tracks
        } catch (e: Exception) {
            Log.e("TrackRepositoryImpl", "Search error: ${e.message}", e)
            emptyList()
        }
    }

    fun getLastSearchQuery(): String = lastSearchQuery
    fun getLastSearchResults(): List<Track> = lastSearchResults
}
