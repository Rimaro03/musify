package com.rimaro.musify.data.remote.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ArtistObject(
    val external_urls: ExternalUrls,
    val followers: Followers,
    val genres: List<String>,
    val href: String,
    val id: String,
    val images: List<ImageObject>,
    val name: String,
    val popularity: Int,
    val type: String,
    val uri: String
)