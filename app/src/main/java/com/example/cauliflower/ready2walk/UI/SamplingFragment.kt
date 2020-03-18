package com.example.cauliflower.ready2walk.UI


import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity

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

    // Create sensors to be used
    private var sensorManager: SensorManager? = null
    var phoneAccelerometer: Sensor? = null
    var phoneStepSensor: Sensor? = null


    var accelerometerData: MutableList<Float> = mutableListOf()
    var autocorrelationData: MutableList<Float> = mutableListOf()
    var autocorrelationRawData: MutableList<Float> = mutableListOf()
    var sessionDate: String = String()
    var sessionSamplingPeriodUs: Int = 1000

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //Setting the Title of the Fragment Page
        (context as AppCompatActivity).supportActionBar!!.title = activity!!.resources.getString(R.string.Sampling)

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
        phoneStepSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        //sensorManager!!.unregisterListener(this)

        //functionality start button
        startButton.setOnClickListener {
            //Register Sensors
            sensorManager!!.registerListener(this, phoneAccelerometer, 1000)
            sensorManager!!.registerListener(this, phoneStepSensor,
                    SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
            activity!!.toast("Session Started") //send verification message

            // check if step sensor is available
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                activity!!.toast("you have a step sensor") //send verification message
            }
            else {
                activity!!.toast("Step sensor not found") //send verification message
            }

        }
        //functionality stop button
        stopButton.setOnClickListener {
            //stopSampling
            sensorManager!!.unregisterListener(this)

            //Create coroutine for auto correlation and saving session
            CoroutineScope(Dispatchers.Main + job1).launch {
                context?.let {
                    if ((accelerometerData.isEmpty() == false) || (accelerometerData.isEmpty() == false)) {
                        var autocorrK = 0.0
                        var autocorr0 = 0.0
                        val dataSize = autocorrelationRawData.toList().size
                        var meanRaw = (autocorrelationRawData.sum()) / dataSize

                        // Perform autocorrelation
                        for ((k, value) in autocorrelationRawData.withIndex()) {
                            System.out.println("index: " + k + ", value: " + value)
                            // obtain autocorrelation series
                            for ((i, ivalue) in autocorrelationRawData.withIndex()) {
                                autocorrK += ((ivalue - meanRaw) *
                                        (autocorrelationRawData.toList().get((k + i) % (dataSize - 1)) - meanRaw))
                            }
                            if (k == 0) {
                                autocorr0 = autocorrK
                            }
                            autocorrelationData.add((autocorrK / autocorr0).toFloat())
                        }
                        it.toast("Autocorrelation Finished")
                        if (autocorrelationData.isEmpty() == false) {
                            //Create session entry
                            var sessionAccelerometer = accelerometerData.toList()
                            var sessionAutocorrelation = autocorrelationData.toList()
                            sessionDate = Calendar.getInstance().time.toString()
                            //push into database
                            val session = Sessions(sessionDate, sessionAccelerometer, sessionAutocorrelation, sessionSamplingPeriodUs)
                            SessionsDatabase(it).getSessionsDao().addSession(session)
                            it.toast("Session Saved")
                        } else {
                            it.toast("Steps not found, Try Again")
                        }
                    } else {
                        it.toast("Nothing is Saved")
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerData.add(event.values[0]) // Extract acceleration in Medilateral direction
                //System.out.println("step1")
            }

            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                autocorrelationRawData.add(accelerometerData.last()) // get last accelerometer value
                //System.out.println("step2")
                //context?.toast("you have step sensor")
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


