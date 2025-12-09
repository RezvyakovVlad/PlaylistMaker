package com.example.playlistmaker.domain.model

import java.io.Serializable

data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
) : Serializable {

    fun getFormattedTrackTime(): String {
        val minutes = (trackTimeMillis / 1000) / 60
        val seconds = (trackTimeMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getCoverArtwork(): String {
        return if (!artworkUrl100.isNullOrEmpty()) {
            artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        } else {
            ""
        }
    }

    fun getReleaseYear(): String? {
        return releaseDate?.take(4)
    }

    fun getSafeTrackName(): String {
        return trackName.ifEmpty { "Неизвестный трек" }
    }

    fun getSafeArtistName(): String {
        return artistName.ifEmpty { "Неизвестный исполнитель" }
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

    fun getSafePreviewUrl(): String {
        return previewUrl ?: ""
    }
}