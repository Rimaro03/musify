package com.rimaro.musify.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SimplifiedArtistObject(
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)
