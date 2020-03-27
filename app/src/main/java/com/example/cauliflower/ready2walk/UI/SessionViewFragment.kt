package com.example.cauliflower.ready2walk.UI

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.cauliflower.ready2walk.Database.Sessions
import com.example.cauliflower.ready2walk.Database.SessionsDatabase
import com.example.cauliflower.ready2walk.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_session_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 */

class SessionView : BaseFragment()  {

    //received note
    private var session: Sessions? = null
    private val MICROSECONDSOVERSECONDS = 1000000

    // vars
    private lateinit var sessionGraphSeries:LineGraphSeries<DataPoint>
    private lateinit var sessionAutocorrSeries:LineGraphSeries<DataPoint>
    private lateinit var sessionGyroscopeSeries:LineGraphSeries<DataPoint>
    private lateinit var sessionStepsSeries:LineGraphSeries<DataPoint>

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Create option Menu
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_session_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //receive arguments
        arguments?.let {
            session = SessionViewArgs.fromBundle(it).sessions
            // do stuff with the session passed DEBUG show acc data
            //sessionDataList.text = session?.accelerometerData.toString()
            //graph the session data
            graphSession()
        }

        sendEmail.setOnClickListener {
            Log.i("Send email", "")
            val TO = arrayOf("")
            val CC = arrayOf("")
            val emailIntent = Intent(Intent.ACTION_SEND)

            emailIntent.data = Uri.parse("mailto:")
            emailIntent.type = "text/plain"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO)
            emailIntent.putExtra(Intent.EXTRA_CC, CC)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Session Data")
            val accelerationData = session!!.accelerometerData.toList().toString()
            val autocorrelationData = session!!.autocorrelationData.toList().toString()
            val gyroscopeData = session!!.gyroscopeData.toList().toString()
            val peaksData = session!!.peaksData.toList().toString()
            //Add extra data when extra data gets added to the session storage

            val message = "Acceleration Data:\n$accelerationData\nAutocorrelation Data:\n$autocorrelationData\n" +
                    "Gyroscope Data:\n$gyroscopeData\nPeaks Data:\n$peaksData"
            emailIntent.putExtra(Intent.EXTRA_TEXT, message)

            try {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."))
                //finish()
                System.out.println("Finished sending email")
            } catch (ex: ActivityNotFoundException) {
                System.out.println("Error. No email sent")
            }
        }

    }

    // take session data and graph
    private fun graphSession(){
        sessionGraphSeries = LineGraphSeries<DataPoint>()
        sessionAutocorrSeries = LineGraphSeries<DataPoint>()
        sessionGyroscopeSeries = LineGraphSeries<DataPoint>()
        sessionStepsSeries = LineGraphSeries<DataPoint>()

       // find size of data
        val dataSizeAccelerometer = session!!.accelerometerData.toList().size
        val dataSizeAutocorrelation = session!!.autocorrelationData.toList().size
        val dataSizeGyroscope = session!!.gyroscopeData.toList().size
        val dataSizeStep = session!!.peaksData.toList().size

        var timeX = 0.0
        var accY = 0.0
        var gyroY = 0.0
        var stepY = 0.0


        //Load Data
        for((index, value) in session!!.accelerometerData.toList().withIndex()){
            //System.out.println("index: " + index + ", value: " + value)
            timeX = index.toDouble()*session!!.samplePeriodUs/MICROSECONDSOVERSECONDS //to get time
            accY = value.toDouble()
            // Fill graph series
            sessionGraphSeries.appendData(DataPoint(timeX, accY), true, dataSizeAccelerometer)
            //sessionAutocorrSeries.appendData(DataPoint(timeX , autocorrY/(dataSize - index)), true, dataSize)
        }

        timeX = 0.0
        for((index, value) in session!!.peaksData.toList().withIndex()){
            //System.out.println("index: " + index + ", value: " + value)
            timeX = index.toDouble()*session!!.samplePeriodUs/MICROSECONDSOVERSECONDS //to get time
            stepY = value.toDouble()
            // Fill graph series
            sessionStepsSeries.appendData(DataPoint(timeX, stepY), true, dataSizeStep)
            //sessionAutocorrSeries.appendData(DataPoint(timeX , autocorrY/(dataSize - index)), true, dataSize)
        }

        for((index, value) in session!!.autocorrelationData.toList().withIndex()) {
            // Fill graph series
            sessionAutocorrSeries.appendData(DataPoint(index.toDouble(), value.toDouble()), true, dataSizeAutocorrelation)
        }
        // Plot lines
        //save
        timeX = 0.0
        for((index, value) in session!!.gyroscopeData.toList().withIndex()) {
            timeX = index.toDouble()*session!!.samplePeriodUs/MICROSECONDSOVERSECONDS //to get time
            gyroY = value.toDouble()
            sessionGyroscopeSeries.appendData(DataPoint(timeX, gyroY), true, dataSizeGyroscope)
        }
        plotGraph(sessionGraph, sessionGraphSeries, "Real Time Graph", "Time(ms)", "Trunk Sway acceleration(m/s^2)", Color.BLUE)
        plotGraph(sessionAutocorrGraph, sessionAutocorrSeries, "Auto Correlation Graph", "Step Lag (number of steps)", "Autocorrelation Coefficient", Color.BLUE)
        plotGraph(sessionGyroGraph, sessionGyroscopeSeries, "Gyroscope Graph", "Time(ms)", "Trunk Angle(degrees)", Color.BLUE)
        plotGraph(sessionGraph, sessionStepsSeries, "Steps Graph", "Time(ms)", "Trunk Sway acceleration(m/s^2)", Color.RED)

        // Update averages
        val angleAverageValue  = session!!.gyroscopeData.sum() / dataSizeGyroscope
        angleAverage.setText("Angle Average: $angleAverageValue")
        val autocorrelationAverageValue  = session!!.autocorrelationData.sum() / dataSizeAutocorrelation
        autocorrelationAverage.setText("Autocorrelation Average: $autocorrelationAverageValue")

    }

    // Plot graph at graphID in XML, given LineGraphSeries and title
    private fun plotGraph(graph:GraphView, series:LineGraphSeries<DataPoint>, title:String, horizontalTitle:String, verticalTitle:String, plotColor:Int) {
        series.title = title
        series.color = plotColor
        graph.addSeries(series)
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)
        graph.viewport.borderColor = Color.RED
        graph.legendRenderer.textColor = Color.CYAN
        graph.setPadding(0,0,0,0)

        graph.legendRenderer.apply {
            isVisible = true
            align = LegendRenderer.LegendAlign.BOTTOM
        }
        graph.gridLabelRenderer.apply {
            gridColor = Color.RED //R.attr.textColor
            verticalLabelsColor = Color.BLACK //R.attr.textColor
            horizontalLabelsColor = Color.BLACK
            horizontalAxisTitle = horizontalTitle
            verticalAxisTitle = verticalTitle
            labelVerticalWidth = (verticalAxisTitleWidth+20)
        }
    }

    private fun deleteSession(){
        AlertDialog.Builder(context).apply {
            setTitle("Are you sure?")
            setMessage("You cannot undo this operation")
            setPositiveButton("Yes"){_,_->
                CoroutineScope(Dispatchers.Main + job1).launch {
                    SessionsDatabase(context).getSessionsDao().deleteSession(session!!)
                    //go back to files fragment
                    val action = SessionViewDirections.actionSessionViewToFilesFragment()
                    Navigation.findNavController(view!!).navigate(action)
                }
            }
            setNegativeButton("No"){_,_->}// Do nothing
        }.create().show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){// if node is not null delete
            R.id.DeleteSession ->
                if(session != null)
                    deleteSession()
                else context?.toast("Cannot Delete")}
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }


}
