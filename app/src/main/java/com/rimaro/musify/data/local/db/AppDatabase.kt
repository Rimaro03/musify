package com.rimaro.musify.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rimaro.musify.data.local.dao.AudioStreamUrlDAO
import com.rimaro.musify.data.local.entity.AudioStreamUrl

@Database(
    entities = [AudioStreamUrl::class ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioStreamUrlDao(): AudioStreamUrlDAO
}