package com.rimaro.musify.data.remote.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SimplifiedTrackObject(
    val artists: List<SimplifiedArtistObject>,
    val available_markets: List<String>,
    val disc_number: Int,
    val duration_ms: Int,
    val explicit: Boolean,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val is_playable: Boolean? = null,
    val linked_form: LinkedForm? = null,
    val restrictions: Restrictions? = null,
    val name: String,
    val preview_url: String? = null,
    val track_number: Int,
    val type: String,
    val uri: String,
    val is_local: Boolean
)
