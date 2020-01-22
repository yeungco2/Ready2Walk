package com.example.cauliflower.ready2walk.UI

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation

import com.example.cauliflower.ready2walk.R

//Imports the activity_main.xml and fragment_home.xml from the layout folder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        trainingButton.setOnClickListener {
            // make training button go to the sampling fragment
            val action = HomeFragmentDirections.actionStartSampling()
            Navigation.findNavController(it).navigate(action)
        }
    }

}
