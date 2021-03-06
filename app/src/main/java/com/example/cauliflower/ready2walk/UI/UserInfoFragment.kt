package com.example.cauliflower.ready2walk.UI


    import android.content.Context
    import android.content.Intent
    import android.graphics.Color
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.EditText
    import android.widget.TextView
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.app.AppCompatDelegate
    import com.example.cauliflower.ready2walk.Database.SessionsDatabase
    import com.example.cauliflower.ready2walk.R
    import com.jjoe64.graphview.GraphView
    import com.jjoe64.graphview.LegendRenderer
    import com.jjoe64.graphview.series.DataPoint
    import com.jjoe64.graphview.series.LineGraphSeries
    import kotlinx.android.synthetic.main.fragment_session_view.*
    import kotlinx.android.synthetic.main.fragment_user_info.*
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import java.io.BufferedReader
    import java.io.File
    import java.io.FileInputStream
    import java.io.InputStreamReader


class UserInfoFragment : BaseFragment()  {
    private lateinit var sessionGraphSeries:LineGraphSeries<DataPoint>
    private lateinit var sessionGraphAngleSeries:LineGraphSeries<DataPoint>


    override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?,
                savedInstanceState: Bundle?
    ): View? {
        // Create option Menu
        setHasOptionsMenu(true)
            //Setting the Title of the Fragment Page
            (context as AppCompatActivity).supportActionBar!!.title = activity!!.resources.getString(R.string.UserInfo)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_info, container, false)
    }

    //Saves User Input Data into System Memory
    fun saveData(message: String, box: EditText, filename: String) {
        var fileContents = message;
        if(!message.isEmpty()) {
            context?.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it?.write(fileContents.toByteArray())
            }
            box.setText(fileContents);
        }
    }

    fun loadDefault(filename:String, box: EditText, default_string: String){
        val file = File(context?.filesDir, filename);

        if(file.exists()) {
            val `in`: FileInputStream? = context?.openFileInput(filename);
            val inputStreamReader = InputStreamReader(`in`);
            val bufferedReader = BufferedReader(inputStreamReader);
            val sb = StringBuilder();
            var line = bufferedReader.readLine();
            box.setText(line);
            inputStreamReader.close();
        }else{
            box.setText(default_string);
        }
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        sessionGraphSeries = LineGraphSeries<DataPoint>()
        sessionGraphAngleSeries = LineGraphSeries<DataPoint>()

        super.onActivityCreated(savedInstanceState)
        val user_name = "userName"
        val user_height = "userHeight"
        val user_weight = "userWeight"
        val user_age = "userAge"
        val user_gender = "userGender"

        loadDefault(user_name, UserName, "Bobby");
        loadDefault(user_weight, UserWeight, "155");
        loadDefault(user_height, UserHeight, "150");
        loadDefault(user_age, UserAge, "20");
        loadDefault(user_gender, UserGender, "Male");

        ButtonCancel.setOnClickListener(){
            //Basically disregards the data in the text box and loads what was saved in memory previously
            loadDefault(user_name, UserName, "Bobby");
            loadDefault(user_weight, UserWeight, "155");
            loadDefault(user_height, UserHeight, "150");
            loadDefault(user_age, UserAge, "20");
            loadDefault(user_gender, UserGender, "Male");
        }
        //Save the data in the User Info
        ButtonSave.setOnClickListener {
            //Load through all user data and save them locally

            saveData(UserName.text.toString(), UserName, user_name);
            saveData(UserHeight.text.toString(), UserHeight, user_height);
            saveData(UserWeight.text.toString(), UserWeight, user_weight);
            saveData(UserAge.text.toString(), UserAge, user_age);
            saveData(UserGender.text.toString(),UserGender, user_gender);

            activity!!.toast("Info Saved") //send verification message
        }

        tvChangeTheme.setOnClickListener {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        fun plotGraph(graph: GraphView, series: LineGraphSeries<DataPoint>, title:String, horizontalTitle:String, verticalTitle:String, plotColor:Int) {
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
                verticalLabelsColor = Color.BLUE //R.attr.textColor
                horizontalLabelsColor = Color.BLUE
                horizontalAxisTitleColor = Color.BLUE
                verticalAxisTitleColor = Color.BLUE
                horizontalAxisTitle = horizontalTitle
                verticalAxisTitle = verticalTitle
                labelVerticalWidth = (verticalAxisTitleWidth+20)
            }
        }

        CoroutineScope(Dispatchers.Main + job1).launch {
            context?.let {
                val allSessions = SessionsDatabase(it).getSessionsDao().getAllSessions()
                if (!allSessions.isEmpty()) {

                    var savedValuesAverage: MutableList<Double> = mutableListOf();


                    for ((index, value) in allSessions!!.toList().withIndex()) {
                        var session = allSessions[index];
                        var AccelerometerAverage = session.autocorrelationData.average();
                        savedValuesAverage.add(AccelerometerAverage);
                    }

                    for((index, value) in savedValuesAverage.toList().withIndex()){
                        // Fill graph series
                        sessionGraphSeries.appendData(DataPoint(index.toDouble() + 1, value), true, savedValuesAverage.size)
                    }
                    plotGraph(averageGraph, sessionGraphSeries, "Average Step Symmetry", "Session Number", "Step Symmetry", Color.BLUE)
                }
            }
        }

        CoroutineScope(Dispatchers.Main + job1).launch {
            context?.let {
                val allSessions = SessionsDatabase(it).getSessionsDao().getAllSessions()
                if (!allSessions.isEmpty()) {

                    var savedValuesAverage: MutableList<Double> = mutableListOf();


                    for ((index, value) in allSessions!!.toList().withIndex()) {
                        var session = allSessions[index];
                        var AccelerometerAverage = session.gyroscopeData.average();
                        savedValuesAverage.add(AccelerometerAverage);
                    }

                    for((index, value) in savedValuesAverage.toList().withIndex()){
                        // Fill graph series
                        sessionGraphAngleSeries.appendData(DataPoint(index.toDouble() + 1, value), true, savedValuesAverage.size)
                    }
                    plotGraph(angleGraph, sessionGraphAngleSeries, "Average Angle", "Session Number", "Angle", Color.BLUE)
                }
            }
        }
    }
}