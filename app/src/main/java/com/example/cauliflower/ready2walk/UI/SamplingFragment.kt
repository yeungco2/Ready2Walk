package com.example.cauliflower.ready2walk.UI

import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.cauliflower.ready2walk.Database.Sessions
import com.example.cauliflower.ready2walk.Database.SessionsDatabase
import java.time.LocalDateTime

import com.example.cauliflower.ready2walk.R
import kotlinx.android.synthetic.main.fragment_sampling.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SamplingFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sampling, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    //create functionality of sampling fragment
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        startButton.setOnClickListener {
            //Create session entry
            val dateSession = LocalDateTime.now()
            val session = Sessions(dateSession.toString())

            //Push session to database
            saveSession(session)
        }
    }


    private fun saveSession(sessions: Sessions) {
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
    }
}