package com.rimaro.musify.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class AudioStreamUrl (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val trackId: String,
    val streamUrl: String?,
    val expiresAt: LocalDateTime
)