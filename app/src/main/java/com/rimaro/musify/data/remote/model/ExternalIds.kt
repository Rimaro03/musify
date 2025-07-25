package com.rimaro.musify.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ExternalIds(
    val isrc: String? = null,
    val ean: String? = null,
    val upc: String? = null
)
