package com.rimaro.musify.data.remote.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SavedAlbumObject(
    val added_at: String,
    val album: Album
)