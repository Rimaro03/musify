package com.rimaro.musify.data.remote.retrofit

import com.rimaro.musify.domain.model.SimplifiedPlaylistObject
import com.rimaro.musify.domain.model.GenericPlaylistsResponse
import com.rimaro.musify.domain.model.PlaylistResponse
import com.rimaro.musify.domain.model.UserProfileResponse
import com.rimaro.musify.domain.model.UserTopTrackResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface SpotifyApiService {
    @GET("me")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String,
    ): UserProfileResponse

    @GET("me/top/tracks")
    suspend fun getUserTopTrack(
        @Header("Authorization") authorization: String,
    ): UserTopTrackResponse

    @GET("users/{user_id}/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") authorization: String,
        @Path("user_id") userId: String
    ): GenericPlaylistsResponse<SimplifiedPlaylistObject>

    @GET("playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Header("Authorization") authorization: String,
        @Path("playlist_id") playlistId: String
    ): PlaylistResponse
}