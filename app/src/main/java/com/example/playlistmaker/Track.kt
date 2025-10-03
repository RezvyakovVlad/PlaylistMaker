package com.example.playlistmaker

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<Track>
)

data class Track(
    @SerializedName("trackId") val trackId: Long?,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?
) {
    fun getFormattedTrackTime(): String {
        return if (trackTimeMillis != null) {
            val minutes = (trackTimeMillis / 1000) / 60
            val seconds = (trackTimeMillis / 1000) % 60
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "0:00"
        }
    }
}