package com.rimaro.musify.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageObject(
    val height: Int,
    val url: String,
    val width: Int
)
