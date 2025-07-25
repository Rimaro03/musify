package com.rimaro.musify.data.remote.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CopyrightObject(
    val text: String,
    val type: String
)
