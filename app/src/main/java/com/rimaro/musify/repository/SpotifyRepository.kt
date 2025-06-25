package com.rimaro.musify.repository

import com.rimaro.musify.network.SpotifyApiService
import com.rimaro.musify.network.SpotifyAuthService
import javax.inject.Inject

class SpotifyRepository @Inject constructor(
    private val spotifyAuthService: SpotifyAuthService,
    private val spotifyApiService: SpotifyApiService
) {
    suspend fun getSpotifyToken(code: String, redirectUri: String, authorization: String) =
        spotifyAuthService.getToken(code = code, redirectUri = redirectUri, authorization = authorization)

    suspend fun refreshToken(refreshToken: String, clientId: String, authorization: String) =
        spotifyAuthService.refreshToken(refreshToken = refreshToken, clientId = clientId, authorization = authorization)

    suspend fun getUserTopTracks(authorization: String) =
        spotifyApiService.getUserTopTrack(authorization = authorization)
}