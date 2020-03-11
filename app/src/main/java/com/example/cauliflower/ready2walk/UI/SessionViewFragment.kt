package com.example.cauliflower.ready2walk.UI

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.cauliflower.ready2walk.Database.Sessions
import com.example.cauliflower.ready2walk.Database.SessionsDatabase

import com.example.cauliflower.ready2walk.R
import com.example.cauliflower.ready2walk.UI.SessionViewArgs
import com.example.cauliflower.ready2walk.UI.SessionViewDirections
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
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
        }

        //graph the session data
        graphSession()
    }

    // take session data and graph
    private fun graphSession(){
        sessionGraphSeries = LineGraphSeries<DataPoint>()
        sessionAutocorrSeries = LineGraphSeries<DataPoint>()
        val dataSize = session!!.accelerometerData.toList().size
        var timeX = 0.0
        var accY = 0.0
        var autocorrY = 0.0

        //save data
        for((index, value) in session!!.accelerometerData.toList().withIndex()){
            System.out.println("index: " + index + ", value: " + value)
            timeX = index.toDouble()*session!!.samplePeriodUs/MICROSECONDSOVERSECONDS //to get time
            accY = value.toDouble()
            // obtain autocorrelation series
            for ((jindex, jvalue) in session!!.accelerometerData.toList().withIndex()) {
                autocorrY += jvalue * session!!.accelerometerData.toList().get((jindex+index) % dataSize)
            }

            sessionGraphSeries.appendData(DataPoint(timeX, accY), true, dataSize)
            sessionAutocorrSeries.appendData(DataPoint(timeX , autocorrY/(dataSize - index)), true, dataSize)
        }
        plotGraph(sessionGraph, sessionGraphSeries, "Real Time Graph")
        plotGraph(sessionAutocorrGraph, sessionAutocorrSeries, "Auto Correlation Graph")
    }

    // Plot graph at graphID in XML, given LineGraphSeries and title
    private fun plotGraph(graph:GraphView, series:LineGraphSeries<DataPoint>, title:String) {
        series.title = title
        graph.addSeries(series)
        graph.viewport.isScalable = true
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)
        graph.viewport.borderColor = Color.BLACK
        graph.legendRenderer.apply {
            isVisible = true
            align = LegendRenderer.LegendAlign.BOTTOM
        }
        graph.gridLabelRenderer.apply {
            gridColor = Color.BLACK
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
        when(item.itemId){// in node is not null delete
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
