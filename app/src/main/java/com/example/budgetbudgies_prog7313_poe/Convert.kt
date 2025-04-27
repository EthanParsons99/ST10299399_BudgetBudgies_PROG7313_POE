package com.example.budgetbudgies_prog7313_poe

import androidx.room.TypeConverter
import java.util.Date

class Convert {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time

    }
}