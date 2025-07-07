package com.rimaro.musify.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Restrictions(
    val reason: String
)
