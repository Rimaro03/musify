package com.rimaro.musify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rimaro.musify.data.local.entity.AudioStreamUrl
import java.time.LocalDateTime

@Dao
interface AudioStreamUrlDAO {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(audioStreamUrl: AudioStreamUrl)

    @Query("SELECT * FROM AudioStreamUrl WHERE trackId = :trackId")
    suspend fun getAudioStreamUrl(trackId: String): AudioStreamUrl?

    @Query("DELETE FROM AudioStreamUrl WHERE trackId = :trackId")
    suspend fun delete(trackId: String)

    @Query("DELETE FROM AudioStreamUrl WHERE expiresAt < :dateTime")
    suspend fun deleteByDateTime(dateTime: LocalDateTime)

    @Query("UPDATE AudioStreamUrl SET streamUrl = :streamUrl, expiresAt = :expiresAt WHERE trackId = :trackId")
    suspend fun update(trackId: String, streamUrl: String?, expiresAt: String)
}