package com.example.cauliflower.ready2walk.Database

import android.hardware.SensorEvent
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "sessions_table")
data class Sessions (
    val sessionDate: String
    //val event: SensorEvent
    //val accelerometerData:

    //val time: Vector<Int>
    //val gyroscopeData: vector
): Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
