package com.rimaro.musify.network

import com.rimaro.musify.model.UserTopTrackResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface SpotifyApiService {
    @GET("me/top/tracks")
    suspend fun getUserTopTrack(
        @Header("Authorization") authorization: String,
    ): UserTopTrackResponse
}