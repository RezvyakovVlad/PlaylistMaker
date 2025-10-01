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

    fun searchTracksOld(query: String): List<Track> {
        val mockTracks = getMockTracks()
        val lowerCaseQuery = query.lowercase()

        return mockTracks.filter { track ->
            (track.trackName?.lowercase()?.contains(lowerCaseQuery) == true) ||
                    (track.artistName?.lowercase()?.contains(lowerCaseQuery) == true)
        }
    }

    fun getMockTracks(): List<Track> {
        return listOf(
            Track(
                trackName = "Yesterday",
                artistName = "The Beatles",
                trackTimeMillis = 125000,
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            )
        )
    }
}