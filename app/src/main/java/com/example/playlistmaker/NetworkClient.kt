package com.example.playlistmaker

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkClient {

    private val baseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesApi = retrofit.create(ItunesApi::class.java)

    fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onError: (String) -> Unit
    ) {
        itunesApi.search(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    onSuccess(tracks)
                } else {
                    onError("Server error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }
}