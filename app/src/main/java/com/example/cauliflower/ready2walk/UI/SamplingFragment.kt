package com.example.cauliflower.ready2walk.UI


import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.cauliflower.ready2walk.Database.Sessions
import com.example.cauliflower.ready2walk.Database.SessionsDatabase
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
    var phoneGyroscope: Sensor? = null

    //Added Step Counter
    var phoneStepCounter: Sensor? = null

    var accelerometerData: MutableList<Float> = mutableListOf()
    var autocorrelationData: MutableList<Float> = mutableListOf()
    var autocorrelationRawData: MutableList<Float> = mutableListOf()
    var stepData: MutableList<Float> = mutableListOf()
    var peaksData: MutableList<Float> = mutableListOf()

    var gyroscopeData: MutableList<Float> = mutableListOf()
    var sessionDate: String = String()
    var sessionSamplingPeriodUs: Int = 1000

    //
    var gravity: FloatArray = FloatArray(3)
    var linear_acceleration: FloatArray = FloatArray(3)

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
        phoneGyroscope = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        //sensorManager!!.unregisterListener(this)

        //Step Counter to use if Step Detector doesn't register due to power issue
        phoneStepCounter = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        //functionality start button
        startButton.setOnClickListener {
            //Register Sensors
            sensorManager!!.registerListener(this, phoneAccelerometer, 1000)
            var sensor = sensorManager!!.registerListener(this, phoneStepSensor,
                    0,0)
            sensorManager!!.registerListener(this, phoneGyroscope, sessionSamplingPeriodUs)
            activity!!.toast("Session Started") //send verification message

            //System.out.println("Step Detector is: " + sensor)
            val pm: PackageManager = context!!.getPackageManager()
            if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
                // the awesome stuff here
                sensorManager!!.registerListener(this, phoneStepCounter, SensorManager.SENSOR_DELAY_UI)
                System.out.println("You have a step counter")
            } else {
                System.out.println("No step counter")
            }

            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                activity!!.toast("you have a step sensor") //send verification message
            }

            // check if step sensor is available
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                activity!!.toast("you have a step sensor") //send verification message
            } else {
                activity!!.toast("Step sensor not found") //send verification message
            }
            System.out.println("Sampling Process Initiated")

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

                        if (dataSize > 1) {
                            // Perform autocorrelation
                            for ((k, value) in autocorrelationRawData.withIndex()) {
                                System.out.println("index: " + k + ", value: " + value + " Data Size: " + dataSize)
                                autocorrK = 0.0
                                // obtain autocorrelation series
                                for ((i, ivalue) in autocorrelationRawData.withIndex()) {
                                    autocorrK += ((ivalue - meanRaw) *
                                            (autocorrelationRawData.toList().get((k + i) % (dataSize - k)) - meanRaw))
                                }
                                autocorr0 += (autocorrK - autocorr0)/(k+1)
                                System.out.println("Autocorr0: " + autocorr0 + autocorrK)
                                autocorrelationData.add((autocorrK / (autocorr0)).toFloat())
                            }
                            it.toast("Autocorrelation Finished")
                        }
                        // Save session
                        if (autocorrelationData.isEmpty() == false) {
                            //Create session entry
                            var sessionAccelerometer = accelerometerData.toList()
                            var sessionAutocorrelation = autocorrelationData.toList()
                            var sessionGyroscope = gyroscopeData.toList()
                            var sessionSteps = stepData.toList()
                            sessionDate = Calendar.getInstance().time.toString()
                            //push into database
                            val session = Sessions(sessionDate, sessionAccelerometer, sessionAutocorrelation,
                                    sessionSamplingPeriodUs, sessionGyroscope,sessionSteps )
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
            // Go back to home after sampling is done
            val action = SamplingFragmentDirections.actionSaveSampling()
            Navigation.findNavController(it).navigate(action)
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerData.add(event.values[0]) // Extract acceleration in Medilateral direction
                stepData.add((0).toFloat())
                //System.out.println("step1")
                return;
            }

            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                gyroscopeData.add(event.values[2])  // get value about z-axis (value[2]), to get left and right sway

                //System.out.println("Gyroscope: " + event.values[2])
                return;
            }

            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                autocorrelationRawData.add(accelerometerData.last()) // get last accelerometer value
                stepData[stepData.lastIndex] = (10).toFloat()
                //System.out.println("step2")
                //context?.toast("you have step sensor")
                return;
            }

            /*//System.out.println("Event is:" + event.sensor.type)
            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                autocorrelationRawData.add(accelerometerData.last()) // get last accelerometer value
                System.out.println("Step Counter Registered")
                return;
            }*/
        }
    }

    private fun findPeaks(accData:MutableList<Float>){
        var peakData: MutableList<Float> = mutableListOf()
        val indexRange = 500
        val indexDivision = accData.size
        var tempBig: Float = 0.0F
        val highFlag = false
        val lowFlag = false
        //var templow = 0
        for ((i, value) in peakData.withIndex()) {
            while (!highFlag){
                if (value > tempBig) {
                    tempBig = value
                }
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


