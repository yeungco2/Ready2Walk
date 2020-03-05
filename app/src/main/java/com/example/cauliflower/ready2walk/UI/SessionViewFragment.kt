package com.example.cauliflower.ready2walk.UI

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.cauliflower.ready2walk.Database.Sessions
import com.example.cauliflower.ready2walk.Database.SessionsDatabase

import com.example.cauliflower.ready2walk.R
import com.example.cauliflower.ready2walk.UI.SessionViewArgs
import com.example.cauliflower.ready2walk.UI.SessionViewDirections
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

    // vars
    private lateinit var sessionGraphSeries:LineGraphSeries<DataPoint>

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
        val dataSize = session!!.accelerometerData.toList().size
        var timeX:Double = 0.0
        var accY:Double = 0.0

        for((index, value) in session!!.accelerometerData.toList().withIndex()){
            System.out.println("index: " + index + ", value: " + value)
            timeX = index.toDouble()
            accY = value.toDouble()


            sessionGraphSeries.appendData(DataPoint(timeX, accY), true, dataSize)
        }
        sessionGraph.addSeries(sessionGraphSeries)
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
