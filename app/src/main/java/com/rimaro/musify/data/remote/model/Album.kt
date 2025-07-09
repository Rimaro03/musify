package com.rimaro.musify.data.remote.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Album(
    val album_type: String,
    val total_tracks: Int,
    val available_markets: List<String>,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<ImageObject>,
    val is_playable: Boolean? = null,
    val name: String,
    val release_date: String,
    val release_date_precision: String,
    val restrictions: Restrictions? = null,
    val type: String,
    val uri: String,
    val artists: List<SimplifiedArtistObject>
)
