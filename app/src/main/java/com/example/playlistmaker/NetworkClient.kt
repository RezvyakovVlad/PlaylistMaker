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
        val call = itunesApi.search(query)

        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse != null && searchResponse.results.isNotEmpty()) {
                        onSuccess(searchResponse.results)
                    } else {
                        onSuccess(emptyList())
                    }
                } else {
                    onError("Ошибка сервера: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                onError("Ошибка сети: ${t.message}")
            }
        })
    }
}