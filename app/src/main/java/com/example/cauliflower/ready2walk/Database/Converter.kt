package com.example.cauliflower.ready2walk.Database

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromListToString(listString: String): List<Float> {
        return listString.split(",").map { it.toFloat() }
    }

    @TypeConverter
    fun fromStringToList(stringList: List<Float>): String {
        return stringList.joinToString(separator = ",")
    }

}