package com.rimaro.musify.data.remote.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ImageObject(
    val url: String,
    val height: Int? = null,
    val width: Int? = null
)
