package com.example.cauliflower.ready2walk.Database

import android.hardware.SensorEvent
import androidx.room.*
import java.io.Serializable

@Entity(tableName = "sessions_table")
data class Sessions (
    val sessionDate: String,
    var accelerometerData: List<Float>,
    val samplePeriodUs: Int


    //val time: Vector<Int>
    //val gyroscopeData: vector
): Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
