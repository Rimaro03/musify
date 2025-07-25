package com.rimaro.musify.data.remote.model

import android.annotation.SuppressLint
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class UserProfileResponse(
    val country: String,
    val display_name: String,
    val email: String? = null,
    val external_url: ExternalUrls? = null,
    val href: String,
    val id: String,
    val images: List<ImageObject>,
    val product: String,
    val type: String,
    val uri: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class UserTopTrackResponse (
    val href: String,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String? = null,
    val total: Int,
    val items: List<TrackObject>,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GenericListResponse<T> (
    val href: String,
    val limit: Int,
    val next: String? = null,
    val offset: Int? = null,
    val previous: String? = null,
    val cursors: Cursors? = null,
    val total: Int,
    val items: List<T>,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PlaylistResponse (
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
    @Contextual
    val tracks: GenericListResponse<PlaylistTrackObject>,
    val type: String,
    val uri: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FollowedArtistsResponse (
    val artists: GenericListResponse<ArtistObject>,
)
