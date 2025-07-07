package com.rimaro.musify.domain.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PlaylistTrackObject(
    val added_at: String,
    val added_by: UserObject,
    val is_local: Boolean,
    val track: TrackObject
)
