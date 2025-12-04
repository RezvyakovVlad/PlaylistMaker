package com.example.playlistmaker

class TrackRepository(private val networkClient: NetworkClient) {

    // Существующий метод для обратной совместимости
    fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onError: (String) -> Unit
    ) {
        // ваша текущая реализация
    }

    // Новый метод для ViewModel (пока заглушка)
    fun searchTracksSync(query: String): List<Track> {
        // TODO: Реализовать позже
        return emptyList()
    }
}
