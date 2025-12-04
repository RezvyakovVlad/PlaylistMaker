package com.example.playlistmaker

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<Track>
)

data class Track(
    @SerializedName("trackId") val trackId: Long?,
    @SerializedName("trackName") val trackName: String?,
    @SerializedName("artistName") val artistName: String?,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long?,
    @SerializedName("artworkUrl100") val artworkUrl100: String?,
    @SerializedName("collectionName") val collectionName: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("primaryGenreName") val primaryGenreName: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("previewUrl") val previewUrl: String? // Уже есть - отлично!
) : Serializable {

    fun getFormattedTrackTime(): String {
        return if (trackTimeMillis != null) {
            val minutes = (trackTimeMillis / 1000) / 60
            val seconds = (trackTimeMillis / 1000) % 60
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "0:00"
        }
    }

    fun getCoverArtwork(): String {
        return if (!artworkUrl100.isNullOrEmpty()) {
            artworkUrl100.replace("100x100bb", "512x512bb")
        } else {
            ""
        }
    }

    fun getReleaseYear(): String? {
        return releaseDate?.take(4)
    }

    fun getSafeTrackName(): String {
        return trackName ?: "Неизвестный трек"
    }

    fun getSafeArtistName(): String {
        return artistName ?: "Неизвестный исполнитель"
    }

    fun getSafeCollectionName(): String {
        return collectionName ?: "Не указано"
    }

    fun getSafePrimaryGenreName(): String {
        return primaryGenreName ?: "Не указано"
    }

    fun getSafeCountry(): String {
        return country ?: "Не указано"
    }

    // ДОБАВЬТЕ ЭТОТ МЕТОД для задачи аудиоплеера
    fun getSafePreviewUrl(): String {
        return previewUrl ?: ""
    }

    // Дополнительный метод для форматирования времени воспроизведения
    fun getFormattedPlaybackTime(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
