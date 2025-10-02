package com.example.playlistmaker

class TrackRepository(private val networkClient: NetworkClient) {

    private var lastSearchQuery: String = ""
    private var lastSearchResults: List<Track> = emptyList()

    fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (query.isBlank()) {
            onSuccess(emptyList())
            return
        }

        lastSearchQuery = query
        networkClient.searchTracks(query,
            onSuccess = { tracks ->
                lastSearchResults = tracks
                onSuccess(tracks)
            },
            onError = onError
        )
    }

    fun getLastSearchQuery(): String = lastSearchQuery

    fun getLastSearchResults(): List<Track> = lastSearchResults
}