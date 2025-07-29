package com.rimaro.musify.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePlaylistRequestBody(
    val name: String,
    val description: String,
    val public: Boolean
)
