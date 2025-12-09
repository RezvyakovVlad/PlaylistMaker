package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("/search")
    suspend fun search(
        @Query("term") query: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50
    ): SearchResponse

    @GET("/search")
    fun searchCall(
        @Query("term") query: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 50
    ): Call<SearchResponse>
}