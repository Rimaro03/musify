package com.rimaro.musify.utils

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.rimaro.musify.BuildConfig
import com.rimaro.musify.domain.repository.SpotifyRepository
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyTokenManager @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val prefs: SharedPreferences
) {
    private var authCode: String?
        get() = prefs.getString("auth_code", null)
        set(value) = prefs.edit { putString("auth_code", value) }

    private var accessToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) = prefs.edit { putString("auth_token", value) }

    private var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(value) = prefs.edit { putString("refresh_token", value) }

    private var expiresAt: String?
        get() = prefs.getString("expires_at", null)
        set(value) = prefs.edit { putString("expires_at", value) }

    // the viewmodel will handle the async code, making the ui state = loading
    // until access token available and api response ready
    suspend fun retrieveAccessToken(): String {
        val currentDateTime = LocalDateTime.now()

        // if one of the tokens is null or the token is expired, request a new one
        Log.d("SpotifyTokenManager", "accessToken: $accessToken, refreshToken: $refreshToken, expiresAt: $expiresAt")
        if(accessToken == null || refreshToken == null || expiresAt == null) {
            requestNewToken()
        }
        if(currentDateTime.isAfter(LocalDateTime.parse(expiresAt))) {
            Log.d("SpotifyTokenManager", "Token expired, refreshing")
            refreshToken()
        }

        return accessToken!!
    }

    // TODO: handle refresh token the right way
    private suspend fun requestNewToken(){
        // viewmodel will catch this exception and redirect to auth fragment
        if(authCode == null)
            throw Exception("No auth code found")

        val clientId = BuildConfig.CLIENT_ID
        val clientSecret = BuildConfig.CLIENT_SECRET
        val base64auth = android.util.Base64.encodeToString("$clientId:$clientSecret".toByteArray(), android.util.Base64.NO_WRAP)

        val spotifyToken = spotifyRepository.getSpotifyToken(authCode!!, "musify://auth", "Basic $base64auth")
        accessToken = spotifyToken.access_token
        refreshToken = spotifyToken.refresh_token
        expiresAt = LocalDateTime.now().plusSeconds(spotifyToken.expires_in.toLong()).toString()
    }

    private suspend fun refreshToken() {
        if(refreshToken == null)
            throw Exception("No refresh token found")

        val clientId = BuildConfig.CLIENT_ID
        val clientSecret = BuildConfig.CLIENT_SECRET
        val base64auth = android.util.Base64.encodeToString("$clientId:$clientSecret".toByteArray(), android.util.Base64.NO_WRAP)

        val newSpotifyToken = spotifyRepository.refreshToken(refreshToken!!, clientId, "Basic $base64auth")
        accessToken = newSpotifyToken.access_token
        expiresAt = LocalDateTime.now().plusSeconds(newSpotifyToken.expires_in.toLong()).toString()
    }

}