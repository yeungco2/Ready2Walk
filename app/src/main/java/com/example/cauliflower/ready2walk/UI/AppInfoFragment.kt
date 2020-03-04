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
/*
        fun sendMessage() {
            val message = editText.text.toString()
            UserName.text = message
        }*/
    }
}