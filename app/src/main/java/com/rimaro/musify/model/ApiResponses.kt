package com.rimaro.musify.model

import kotlinx.serialization.Serializable

@Serializable
data class UserTopTrackResponse(
    val href: String,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: String? = null,
    val total: Int,
    val items: List<TrackObject>,
)