package com.example.cooksmart.utils

import androidx.room.TypeConverter
import java.util.UUID

class UUIDTypeConverter {
    @TypeConverter
    fun fromString(value : String?) : UUID?{
        return value?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun toString(uuid : UUID?) : String? {
        return uuid?.toString()
    }
}