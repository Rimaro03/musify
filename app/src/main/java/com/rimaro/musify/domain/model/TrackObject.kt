package com.rimaro.musify.domain.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class TrackObject(
    val album: Album,
    val artists: List<SimplifiedArtistObject>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_ids: ExternalIds,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val is_playable: Boolean? = null,
    //val linked_from: ,
    val restrictions: Restrictions? = null,
    val name: String,
    val popularity: Int,
    val preview_url: String? = null,
    val track_number: Int,
    val type: String,
    val uri: String,
    val is_local: Boolean,
)
