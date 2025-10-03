package com.example.playlistmaker

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkClient {

    companion object {
        private const val TAG = "NetworkClient"
        private const val BASE_URL = "https://itunes.apple.com"
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesApi: ItunesApi = retrofit.create(ItunesApi::class.java)

    fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "Searching for: '$query'")

        itunesApi.search(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    Log.d(TAG, "Found ${tracks.size} tracks")
                    onSuccess(tracks)
                } else {
                    val errorMsg = "Server error: ${response.code()}"
                    Log.e(TAG, errorMsg)
                    onError(errorMsg)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                val errorMsg = "Network error: ${t.message}"
                Log.e(TAG, errorMsg, t)
                onError(errorMsg)
            }
        })
    }
}