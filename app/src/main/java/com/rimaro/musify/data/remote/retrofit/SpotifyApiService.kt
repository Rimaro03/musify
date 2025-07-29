package com.rimaro.musify.data.remote.retrofit

import com.rimaro.musify.data.remote.model.*
import retrofit2.http.*

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
    ): GenericListResponse<SavedTrackObject>

    @GET("me/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") authorization: String,
    ): GenericListResponse<SimplifiedPlaylistObject>

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

    @GET("me/albums")
    suspend fun getUserSavedAlbums(
        @Header("Authorization") authorization: String,
    ): GenericListResponse<SavedAlbumObject>

    @GET("me/following")
    suspend fun getUserFollowingArtists(
        @Header("Authorization") authorization: String,
        @Query("type") type: String = "artist"
    ): FollowedArtistsResponse

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

    @POST("users/{user_id}/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") authorization: String,
        @Path("user_id") userId: String,
        @Body request: CreatePlaylistRequestBody
    ): PlaylistResponse
}