package com.rimaro.musify.domain.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class UserObject (
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val type: String,
    val uri: String,
    val display_name: String? = null,
)