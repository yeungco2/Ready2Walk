package com.example.cauliflower.ready2walk.UI


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.Contacts
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi

import com.example.cauliflower.ready2walk.Database.Sessions
import com.example.cauliflower.ready2walk.Database.SessionsDatabase
import com.example.cauliflower.ready2walk.UI.toast
import java.time.LocalDateTime

import com.example.cauliflower.ready2walk.R
import kotlinx.android.synthetic.main.fragment_sampling.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class SamplingFragment : BaseFragment(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    var phoneAccelerometer: Sensor? = null
    var running  = false
    var accelerometerData: MutableList<Float> = mutableListOf()
    var sessionDate: String = String()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sampling, container, false)
    }


    //create functionality of sampling fragment
    @RequiresApi(Build.VERSION_CODES.O) // for local time option
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //intialize sensor service (dont forget the activity instance)
        sensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        phoneAccelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //sensorManager!!.unregisterListener(this)

        //functionality start button
        startButton.setOnClickListener {
            sensorManager!!.registerListener(this, phoneAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            activity!!.toast("Session Started") //send verification message

        }
        //functionality stop button
        stopButton.setOnClickListener {
            //stopSampling
            sensorManager!!.unregisterListener(this)
            //save data
            CoroutineScope(Dispatchers.Main + job1).launch {
                context?.let {
                    //Create session entry
                    var sessionAccelerometer = accelerometerData.toList()
                    // LocalData time bug to be fixed
                    //val dateSession = LocalDateTime.now()
                    //val sensorData =

                    //push into database
                    val session = Sessions(sessionDate, sessionAccelerometer)
                    SessionsDatabase(it).getSessionsDao().addSession(session)
                    it.toast("Session Saved")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerData.add(event.values[0])
                sessionDate = Calendar.getInstance().toString()
            }
        }
    }

    //saving without coroutines
    /*private fun saveSession(sessions: Sessions) {
        class SaveSession : AsyncTask<Void, Void, Void>() {
            //run in background
            override fun doInBackground(vararg parameters: Void?): Void? {
                SessionsDatabase(activity!!).getSessionsDao().addSession(sessions)
                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)

                Toast.makeText(activity, "Session Saved", Toast.LENGTH_LONG).show()
            }

        }
        SaveSession().execute()
    }*/
}


