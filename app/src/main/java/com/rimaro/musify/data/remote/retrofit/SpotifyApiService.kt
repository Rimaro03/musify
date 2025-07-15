package com.rimaro.musify.data.remote.retrofit

import com.rimaro.musify.data.remote.model.SimplifiedPlaylistObject
import com.rimaro.musify.data.remote.model.GenericPlaylistsResponse
import com.rimaro.musify.data.remote.model.PlaylistResponse
import com.rimaro.musify.data.remote.model.SavedTrackObject
import com.rimaro.musify.data.remote.model.UserProfileResponse
import com.rimaro.musify.data.remote.model.UserTopTrackResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyApiService {
    @GET("me")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String,
    ): UserProfileResponse

    @GET("me/top/tracks")
    suspend fun getUserTopTrack(
        @Header("Authorization") authorization: String,
    ): UserTopTrackResponse

    @GET("me/tracks")
    suspend fun getUserSavedTracks(
        @Header("Authorization") authorization: String,
    ): GenericPlaylistsResponse<SavedTrackObject>

    @GET("me/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") authorization: String,
    ): GenericPlaylistsResponse<SimplifiedPlaylistObject>

    @GET("me/tracks/contains")
    suspend fun checkUserFollowTracks(
        @Header("Authorization") authorization: String,
        @Query("ids") trackIds: String
    ): List<Boolean>

    @PUT("me/tracks")
    suspend fun followTrack(
        @Header("Authorization") authorization: String,
        @Query("ids") trackIds: String
    )

    @DELETE("me/tracks")
    suspend fun unfollowTrack(
        @Header("Authorization") authorization: String,
        @Query("ids") trackIds: String
    )

    @GET("playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Header("Authorization") authorization: String,
        @Path("playlist_id") playlistId: String
    ): PlaylistResponse

    @GET("playlists/{playlist_id}/followers/contains")
    suspend fun checkUserFollowsPlaylist(
        @Header("Authorization") authorization: String,
        @Path("playlist_id") playlistId: String
    ): List<Boolean>

    @PUT("playlists/{playlist_id}/followers")
    suspend fun followPlaylist(
        @Header("Authorization") authorization: String,
        @Path("playlist_id") playlistId: String
    )

    @DELETE("playlists/{playlist_id}/followers")
    suspend fun unfollowPlaylist(
        @Header("Authorization") authorization: String,
        @Path("playlist_id") playlistId: String
    )
}