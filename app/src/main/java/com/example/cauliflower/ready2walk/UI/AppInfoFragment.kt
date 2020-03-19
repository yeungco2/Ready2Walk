package com.example.cauliflower.ready2walk.UI


    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.EditText
    import android.widget.TextView
    import androidx.appcompat.app.AppCompatActivity
    import com.example.cauliflower.ready2walk.R
    import kotlinx.android.synthetic.main.activity_main.*
    import kotlinx.android.synthetic.main.fragment_app_info.*
    import kotlinx.android.synthetic.main.fragment_user_info.*


    class AppInfoFragment : BaseFragment()  {

        override fun onCreateView(
                inflater: LayoutInflater, container: ViewGroup?,
                savedInstanceState: Bundle?
    ): View? {
        // Create option Menu
            setHasOptionsMenu(true)

            //Setting the Title of the Fragment Page
            (context as AppCompatActivity).supportActionBar!!.title = activity!!.resources.getString(R.string.Instruction)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val disclaimer = "This application is meant to serve as an aid for rehabilitation, and not " +
                "as a substitute for the instruction from a trained medical professional. " +
                "Please consult the advice of a tranined medical professional, " +
                "before and while you use this program. ";

        val introduction = "Thank you for downloading Ready2Walk! We hope that you are able to use" +
                "this application to improve your rehabilitation proccess as you become accustomed to" +
                " your prosthetic device. The purpose of this application is to provide your health " +
                "care team with information about your mobility while wearing the prosthetic.";

        Disclaimer.setText(disclaimer);
        Introduction.setText(introduction);
    }
}