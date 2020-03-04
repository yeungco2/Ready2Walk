package com.example.cauliflower.ready2walk.UI


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.cauliflower.ready2walk.R
import kotlinx.android.synthetic.main.fragment_home.*
import com.example.cauliflower.ready2walk.UI.HomeFragmentDirections as HomeFragmentDirections1


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Setting the Title of the Fragment Page
        (context as AppCompatActivity).supportActionBar!!.title = activity!!.resources.getString(R.string.Home)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        trainingButton.setOnClickListener {
            // make training button go to the sampling fragment
            val action = HomeFragmentDirections1.actionStartSampling()
            Navigation.findNavController(it).navigate(action)
        }
        filesButton.setOnClickListener {
            // make files button go to the files fragment
            val action = HomeFragmentDirections1.actionHomeFragmentToFilesFragment()
            Navigation.findNavController(it).navigate(action)
        }

        userInfoButton.setOnClickListener{
            val action = HomeFragmentDirections1.actionHomeFragmentToUserInfoFragment()
            Navigation.findNavController(it).navigate(action)
        }
        appInfoButton.setOnClickListener{
            val action = HomeFragmentDirections1.actionHomeFragmentToAppInfoFragment()
            Navigation.findNavController(it).navigate(action)
        }

    }

}
