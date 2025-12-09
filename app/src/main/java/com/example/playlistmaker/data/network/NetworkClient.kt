package com.example.playlistmaker.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.playlistmaker.data.dto.SearchResponse
import com.example.playlistmaker.data.mapper.TrackMapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkClient(private val context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesApi = retrofit.create(ItunesApi::class.java)

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun searchTracks(
        query: String,
        onSuccess: (List<com.example.playlistmaker.domain.model.Track>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("No internet connection")
            return
        }

        val call = itunesApi.searchCall(query)
        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val searchResponse = response.body()
                    if (searchResponse != null && searchResponse.results.isNotEmpty()) {
                        val tracks = TrackMapper.mapList(searchResponse.results)
                        onSuccess(tracks)
                    } else {
                        onSuccess(emptyList())
                    }
                } else {
                    onError("Ошибка сервера: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("NetworkClient", "Network error", t)
                onError("Ошибка сети: ${t.message}")
            }
        })
    }

    suspend fun searchTracksSuspend(query: String): SearchResponse {
        return itunesApi.search(query)
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}
