package com.rimaro.musify.model

import kotlinx.serialization.Serializable

@Serializable
data class Restrictions(
    val reason: String
)
