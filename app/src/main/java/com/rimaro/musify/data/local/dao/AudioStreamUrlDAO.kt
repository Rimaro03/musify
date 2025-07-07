package com.rimaro.musify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rimaro.musify.data.local.entity.AudioStreamUrl

@Dao
interface AudioStreamUrlDAO {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(audioStreamUrl: AudioStreamUrl)

    @Query("SELECT * FROM AudioStreamUrl WHERE trackId = :trackId")
    suspend fun getAudioStreamUrl(trackId: String): AudioStreamUrl?
}