package com.mindnote.data.db

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun stringListToString(list: List<String>?): String =
        list?.joinToString(SEP).orEmpty()

    @TypeConverter
    fun stringToStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split(SEP)

    private companion object {
        const val SEP = ""
    }
}
