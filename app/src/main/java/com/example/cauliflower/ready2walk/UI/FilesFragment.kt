package com.example.cauliflower.ready2walk.UI


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.cauliflower.ready2walk.R
import com.example.cauliflower.ready2walk.Database.SessionsDatabase
import kotlinx.coroutines.launch


import kotlinx.android.synthetic.main.fragment_files.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A simple [Fragment] subclass.
 */
class FilesFragment : BaseFragment() {// dont forget the base fragmet !!!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_files, container, false) //connect to the xml
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_view_files.setHasFixedSize(true)
        recycler_view_files.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        CoroutineScope(Dispatchers.Main + job1).launch {
            context?.let {
                val allSessions = SessionsDatabase(it).getSessionsDao().getAllSessions()
                recycler_view_files.adapter = SessionAdapter(allSessions)
            }
        }
        CoroutineScope(Dispatchers.Main + job2).launch {  }
    }
}
