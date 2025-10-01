package com.example.playlistmaker

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class NetworkClient {

    companion object {
        private const val TAG = "NetworkClient"
        private const val BASE_URL = "https://itunes.apple.com"
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val itunesApi: ItunesApi by lazy {
        retrofit.create(ItunesApi::class.java)
    }

    fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "Starting search for: '$query'")

        if (query.isBlank()) {
            Log.w(TAG, "Empty search query")
            onSuccess(emptyList())
            return
        }

        // Логируем полный URL вручную
        val fullUrl = "$BASE_URL/search?entity=song&term=${query.replace(" ", "+")}"
        Log.d(TAG, "Request URL: $fullUrl")

        val call = itunesApi.search(query)

        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                Log.d(TAG, "Response received. Code: ${response.code()}, Successful: ${response.isSuccessful}")

                when {
                    response.isSuccessful -> {
                        val tracks = response.body()?.results ?: emptyList()
                        Log.d(TAG, "Search successful. Found ${tracks.size} tracks")
                        onSuccess(tracks)
                    }
                    response.code() == 404 -> {
                        Log.w(TAG, "No results found (404)")
                        onSuccess(emptyList())
                    }
                    else -> {
                        val errorMsg = "Server error: ${response.code()} - ${response.message()}"
                        Log.e(TAG, errorMsg)
                        onError(errorMsg)
                    }
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                val errorMsg = when (t) {
                    is UnknownHostException -> {
                        Log.e(TAG, "No internet connection", t)
                        "Проверьте подключение к интернету"
                    }
                    is SocketTimeoutException -> {
                        Log.e(TAG, "Request timeout", t)
                        "Превышено время ожидания ответа"
                    }
                    is SSLHandshakeException -> {
                        Log.e(TAG, "SSL handshake failed", t)
                        "Ошибка безопасного соединения"
                    }
                    else -> {
                        Log.e(TAG, "Network request failed: ${t.message}", t)
                        "Ошибка сети: ${t.message ?: "неизвестная ошибка"}"
                    }
                }
                onError(errorMsg)
            }
        })
    }
}
