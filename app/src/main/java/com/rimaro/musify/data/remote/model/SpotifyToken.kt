package com.rimaro.musify.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SpotifyToken (
    val access_token: String,
    val token_type: String,
    val scope : String,
    val expires_in: Int,
    val refresh_token: String? = null
)