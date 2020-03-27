package com.example.cauliflower.ready2walk.Database

import android.hardware.SensorEvent
import androidx.room.*
import java.io.Serializable

@Entity(tableName = "sessions_table")
data class Sessions (
    val sessionDate: String,
    var accelerometerData: List<Float>,
    var autocorrelationData: List<Float>,
    val samplePeriodUs: Int,
    var gyroscopeData: List<Float>,
    var stepData: List<Float>,
    var peaksData: List<Float>



    //val time: Vector<Int>
    //val gyroscopeData: vector
): Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
