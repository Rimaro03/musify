package com.rimaro.musify.domain.model

data class PlaylistLocal(
    val name: String,
    val imageUrl: String?,
    val owner: UserLocal?,
    val description: String?
)
