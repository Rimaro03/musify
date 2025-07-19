package com.rimaro.musify.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rimaro.musify.data.local.dao.AudioStreamUrlDAO
import com.rimaro.musify.data.local.entity.AudioStreamUrl
import com.rimaro.musify.utils.Converters

@Database(
    entities = [AudioStreamUrl::class ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioStreamUrlDao(): AudioStreamUrlDAO
}