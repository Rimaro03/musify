package com.rimaro.musify.domain.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SimplifiedPlaylistObject(
    val collaborative: Boolean,
    val description: String,
    val external_url: ExternalUrls? = null,
    val href: String,
    val id: String,
    val images: List<ImageObject>? = null,
    val name: String,
    val owner: UserObject,
    val public: Boolean,
    val snapshot_id: String,
    val tracks: Tracks,
    val type: String,
    val uri: String
)