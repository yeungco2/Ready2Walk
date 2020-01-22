package com.example.cauliflower.ready2walk.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "sessions_table")
data class Sessions (
        val sessionDate: String
        //val time: Vector<Int>
        //val gyroscopeData: vector
)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}