package com.rimaro.musify.utils

import androidx.room.TypeConverter
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromString(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun localDateTimeToString(localDateTime: LocalDateTime?): String? {
        return localDateTime?.toString()
    }
}