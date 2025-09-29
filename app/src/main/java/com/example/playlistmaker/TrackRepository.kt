package com.example.playlistmaker

class TrackRepository {

    fun getMockTracks(): List<Track> {
        return listOf(
            Track(
                trackName = "Yesterday (Remastered 2009)",
                artistName = "The Beatles",
                trackTime = "2:05",
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Here Comes The Sun (Remastered 2009)",
                artistName = "The Beatles",
                trackTime = "3:05",
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "No Reply",
                artistName = "The Beatles",
                trackTime = "2:15",
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Let It Be",
                artistName = "The Beatles",
                trackTime = "4:03",
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Girl",
                artistName = "The Beatles",
                trackTime = "2:30",
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Michelle",
                artistName = "The Beatles",
                trackTime = "2:42",
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Eleanor Rigby",
                artistName = "The Beatles",
                trackTime = "2:06",
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Come Together",
                artistName = "The Beatles",
                trackTime = "4:20",
                artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/58/8a/13/588a1326-2c51-2ea5-78ba-3c7ef9bcec35/00602547096771.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Smells Like Teen Spirit",
                artistName = "Nirvana",
                trackTime = "5:01",
                artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Billie Jean",
                artistName = "Michael Jackson",
                trackTime = "4:35",
                artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Stayin' Alive",
                artistName = "Bee Gees",
                trackTime = "4:10",
                artworkUrl100 = "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Whole Lotta Love",
                artistName = "Led Zeppelin",
                trackTime = "5:33",
                artworkUrl100 = "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Sweet Child O'Mine",
                artistName = "Guns N' Roses",
                trackTime = "5:03",
                artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
            )

        )
    }

    fun searchTracks(query: String): List<Track> {
        val allTracks = getMockTracks()
        val lowerCaseQuery = query.lowercase()

        return allTracks.filter { track ->
            track.trackName.lowercase().contains(lowerCaseQuery) ||
                    track.artistName.lowercase().contains(lowerCaseQuery)
        }
    }
}