package com.example.cauliflower.ready2walk.UI


import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
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
import kotlin.math.absoluteValue


/**
 * A simple [Fragment] subclass.
 */
class SamplingFragment : BaseFragment(), SensorEventListener {

    // Create sensors to be used
    private var sensorManager: SensorManager? = null
    var phoneAccelerometer: Sensor? = null
    var phoneStepSensor: Sensor? = null
    var magenticSensor: Sensor? = null
    var gameRotationSensor: Sensor? = null

    //Added Step Counter
    var phoneStepCounter: Sensor? = null

    var accelerometerData: MutableList<Float> = mutableListOf()
    var autocorrelationData: MutableList<Float> = mutableListOf()
    var autocorrelationRawData: MutableList<Float> = mutableListOf()
    var stepData: MutableList<Float> = mutableListOf()
    var peaksData: MutableList<Float> = mutableListOf()

    var rotationData: MutableList<Float> = mutableListOf()
    var sessionDate: String = String()
    var sessionSamplingPeriodUs: Int = 1000

    //
    var gravity: FloatArray = FloatArray(3)
    var linear_acceleration: FloatArray = FloatArray(3)

    // Current data from accelerometer & magnetometer.  The arrays hold values
    // for X, Y, and Z.
    private var mAccelerometerData = FloatArray(3)
    private var mMagnetometerData = FloatArray(3)
    private var mRotationData = FloatArray(3)
    private var mEulerAngles = FloatArray(3)
    private var rotationMatrix = FloatArray(9)

    // System display. Need this for determining rotation.
    private var mDisplay: Display? = null

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
        magenticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gameRotationSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

        //sensorManager!!.unregisterListener(this)

        //Step Counter to use if Step Detector doesn't register due to power issue
        phoneStepCounter = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        //functionality start button
        startButton.setOnClickListener {

            activity!!.toast("Session Started") //send verification message

            //Register Sensors
            sensorManager!!.registerListener(this, phoneAccelerometer, sessionSamplingPeriodUs)
            sensorManager!!.registerListener(this, phoneStepSensor, sessionSamplingPeriodUs, 0)
            sensorManager!!.registerListener(this, gameRotationSensor, sessionSamplingPeriodUs, 0)
            sensorManager!!.registerListener(this, magenticSensor, sessionSamplingPeriodUs)


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
            if (sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
                activity!!.toast("you have a step sensor") //send verification message
            } else {
                activity!!.toast("Step sensor not found") //send verification message
            }
            System.out.println("Sampling Process Initiated")


            // Get the display from the window manager (for rotation).
            val wm = activity!!.getSystemService(WINDOW_SERVICE) as WindowManager?
            mDisplay = wm!!.defaultDisplay

        }
        //functionality stop button
        stopButton.setOnClickListener {
            //stopSampling
            sensorManager!!.unregisterListener(this)

            //Create coroutine for auto correlation and saving session
            CoroutineScope(Dispatchers.Main + job1).launch {
                context?.let { it ->
                    if (accelerometerData.isEmpty() == false) {

                        var filteredAcc = movingAverage(entries = accelerometerData, window = 300,
                                averageCalc = { mean() }).toMutableList()
                        var filteredRot = movingAverage(entries = rotationData, window = 100,
                                averageCalc = { mean() }).toMutableList()

                        peaksData = findPeaks(filteredAcc)

                        // Drop a few peaks that may be initial noise or initial steps
                        autocorrelationRawData = ((peaksData.filter { it != 0.0F }).drop(5)).toMutableList()

                        var autocorrK = 0.0
                        var autocorr0 = 0.0
                        val dataSize = autocorrelationRawData.toList().size
                        var meanRaw = (autocorrelationRawData.sum()) / dataSize


                        /*// substract mean
                        for ((m, mvalue) in autocorrelationRawData.withIndex()) {
                            autocorrelationRawData[m] = mvalue - meanRaw
                        }
                        // normalize
                        val highestPeak = autocorrelationRawData.max()
                        val lowestPeak = autocorrelationRawData.min()
                        val normalizatonFactor = maxOf(highestPeak!!.absoluteValue, lowestPeak!!.absoluteValue)
                        for ((n, nvalue) in autocorrelationRawData.withIndex()) {
                            autocorrelationRawData[n] = nvalue / normalizatonFactor
                        }*/


                        //System.out.println("total" + autocorrelationRawData.size)

                        if (dataSize > 1) {
                            // Perform autocorrelation
                            for ((k, value) in autocorrelationRawData.withIndex()) {
                                System.out.println("index: " + k + ", value: " + value + " Data Size: " + dataSize)
                                autocorrK = 0.0
                                // obtain autocorrelation series
                                for ((i, ivalue) in autocorrelationRawData.withIndex()) {
                                    if (i<(dataSize - k)) {
                                        autocorrK += (ivalue.absoluteValue) *
                                                (autocorrelationRawData.toList().get((k + i))).absoluteValue
                                        System.out.println(autocorrK)
                                    }
                                }
                                autocorrK /= (dataSize - k)
                                if (k == 0) {
                                    autocorr0 = autocorrK
                                }

                                //autocorr0 += (autocorrK.absoluteValue - autocorr0.absoluteValue) / (k + 1)
                                //System.out.println("Autocorr0: " + autocorr0 + autocorrK)
                                autocorrelationData.add((autocorrK / (autocorr0)).toFloat())
                                //autocorrelationData.add((autocorrK).toFloat())
                            }

                            it.toast("Autocorrelation Finished")
                        }
                        // Save session
                        if (autocorrelationData.isEmpty() == false && peaksData.isEmpty() == false) {

                            // Ensure steps vector is not empty in case phones dont detect steps
                            stepData.add(0f)

                            //Create session entry
                            var sessionAccelerometer = filteredAcc.toList()
                            var sessionAutocorrelation = autocorrelationData.toList()
                            var sessionGyroscope = filteredRot.toList()
                            var sessionSteps = stepData.toList()
                            var sessionPeaks = peaksData.toList()
                            sessionDate = Calendar.getInstance().time.toString()
                            //push into database
                            val session = Sessions(sessionDate, sessionAccelerometer, sessionAutocorrelation,
                                    sessionSamplingPeriodUs, sessionGyroscope, sessionSteps, sessionPeaks)
                            /*
                            System.out.println("fuck" + sessionAccelerometer.size + "fuck" + sessionAutocorrelation.size
                                    + "fuck" + sessionGyroscope.size + "fuck" + sessionSteps.size + "fuck" + sessionPeaks.size)
                            */
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
            /*if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerData.add(event.values[2]) // Extract acceleration in Medilateral direction (phone z axis)
                stepData.add((0).toFloat())
                //System.out.println("step1")
                return;
            }

            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                rotationData.add(event.values[2])  // get value about z-axis (value[2]), to get left and right sway
                //System.out.println("Gyroscope: " + event.values[2])
                return;
            }

            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                //autocorrelationRawData.add(accelerometerData.last()) // get last accelerometer value
                stepData[stepData.lastIndex] = (10).toFloat()
                //System.out.println("step2")
                //context?.toast("you have step sensor")
                return;
            }*/

            // The sensorEvent object is reused across calls to onSensorChanged().
            // clone() gets a copy so the data doesn't change out from under us
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accelerometerData.add(event.values[0] * 10) // Extract acceleration in Medilateral direction (phone z axis)
                    stepData.add((0).toFloat())
                    mAccelerometerData = event.values.clone();
                    //System.out.println("step1")
                }
                /*Sensor.TYPE_MAGNETIC_FIELD -> {
                    mMagnetometerData = event.values.clone()
                }*/


                Sensor.TYPE_GAME_ROTATION_VECTOR -> {

                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values.clone())
                    SensorManager.getOrientation(rotationMatrix, mEulerAngles)
                    rotationData.add(mEulerAngles[0] * 57.2957795f) // get value of azimuth angle, to get left and right sway
                    //mMagnetometerData = event.values.clone()


                    //System.out.println("is emtoy fuck" + rotationData.lastIndex)
                }

                Sensor.TYPE_STEP_DETECTOR -> {
                    //autocorrelationRawData.add(accelerometerData.last()) // get last accelerometer value
                    stepData[stepData.lastIndex] = (10).toFloat()
                }
                else -> return
            }

            /*if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Compute the rotation matrix: merges and translates the data
                // from the accelerometer and magnetometer, in the device coordinate
                // system, into a matrix in the world's coordinate system.
                //
                // The second argument is an inclination matrix, which isn't
                // used in this example.

                // Compute the rotation matrix: merges and translates the data
                // from the accelerometer and magnetometer, in the device coordinate
                // system, into a matrix in the world's coordinate system.
                //
                // The second argument is an inclination matrix, which isn't
                // used in this example.
                val rotationMatrix = FloatArray(9)
                val rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                        null, mAccelerometerData, mMagnetometerData)


                // Remap the matrix based on current device/activity rotation.
                var rotationMatrixAdjusted = FloatArray(9)
                when (mDisplay!!.getRotation()) {
                    Surface.ROTATION_0 -> rotationMatrixAdjusted = rotationMatrix.clone()
                    Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
                            rotationMatrixAdjusted)
                    Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y,
                            rotationMatrixAdjusted)
                    Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(rotationMatrix,
                            SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X,
                            rotationMatrixAdjusted)
                }

                // Get the orientation of the device (azimuth, pitch, roll) based
                // on the rotation matrix. Output units are radians.
                val orientationValues = FloatArray(3)
                if (rotationOK) {
                    SensorManager.getOrientation(rotationMatrixAdjusted,
                            orientationValues)

                    // Pull out the individual values from the array.
                    /*val azimuth = orientationValues[0]
                    val pitch = orientationValues[1]
                    val roll = orientationValues[2]*/
                }
                rotationData.add(orientationValues[0] * 57.2957795f)
            }*/
        }
    }

    private fun findPeaks(accData: MutableList<Float>): MutableList<Float> {
        var peakData: MutableList<Float> = mutableListOf()
        val windowSize = 150
        var indexWindow = 0
        var tempPeak = 0f
        var tempIndex = 0
        var highFlag = true
        var lowFlag = false
        var restartWindow = true


        for ((i, value) in accData.withIndex()) {
            if (i == 0)
                System.out.println("hello")
            // look for highest peak
            if ((value > tempPeak) && highFlag) {
                tempPeak = value
                tempIndex = i
                restartWindow = true
                //System.out.println("up"+i)
            }

            // look for lowest peak
            else if ((value < tempPeak) && lowFlag) {
                tempPeak = value
                tempIndex = i
                restartWindow = true
            }
            else
                restartWindow = false
            //restart window
            if (restartWindow) {
                indexWindow = 0
                //System.out.println(indexWindow)
            } else // start window
                indexWindow++
            //System.out.println(indexWindow)
            //save found peak high and change to low
            if (!restartWindow && indexWindow == windowSize && highFlag) {
                peakData.add(tempIndex, tempPeak)
                System.out.println("up" + value + "index" + tempIndex)
                highFlag = false
                lowFlag = true
                //tempPeak = 0f
            }

            //save found peak low and change to high
            else if (!restartWindow && indexWindow == windowSize && lowFlag) {
                peakData.add(tempIndex, tempPeak)
                System.out.println("low" + value + "index" + tempIndex)
                highFlag = true
                lowFlag = false
            }
            else {
                peakData.add(0.0F)
            }
        }
        return peakData
    }

    private fun smoothingData(rawData: MutableList<Float>): MutableList<Float> {
        var smoothData: MutableList<Float> = mutableListOf()

        // Moving Average Algorithm
        for ((i, value) in rawData.withIndex()) {


        }

        return smoothData
    }


    // Moving average filter source: https://peterscully.name/2017/08/03/moving-average/
    fun <T> List<T>.slidingWindow(size: Int): List<List<T>> {
        if (size < 1) {
            throw IllegalArgumentException("Size must be > 0, but is $size.")
        }
        return this.mapIndexed { index, _ ->
            this.subList(maxOf(index - size + 1, 0), index + 1)
        }
    }

    fun Iterable<Float>.mean(): Float {
        val sum: Float = this.sum()
        return sum / this.count()
    }

    fun sumTo(n: Int): Int = n * (n + 1) / 2

    fun Iterable<Float>.weightedMean(): Float {
        val sum: Float = this
                .mapIndexed { index, t -> t * (index + 1) }
                .sum()
        return sum / sumTo(this.count())
    }

    fun movingAverage(entries: List<Float>, window: Int,
                      averageCalc: Iterable<Float>.() -> Float): List<Float> {
        val result = entries.slidingWindow(size = window)
                .filter { it.isNotEmpty() }
                .map { it -> it.averageCalc() }
                .toList()
        return result
    }


}


