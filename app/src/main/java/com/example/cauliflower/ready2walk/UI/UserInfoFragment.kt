package com.example.cauliflower.ready2walk.UI


    import android.content.Context
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.appcompat.app.AppCompatActivity
    import com.example.cauliflower.ready2walk.R
    import kotlinx.android.synthetic.main.fragment_user_info.*
    import java.io.BufferedReader
    import java.io.File
    import java.io.FileInputStream
    import java.io.InputStreamReader


class UserInfoFragment : BaseFragment()  {

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val filename = "userInfo"


        val file = File(context?.filesDir, filename);

        if(file.exists()) {
            val `in`: FileInputStream? = context?.openFileInput(filename);
            val inputStreamReader = InputStreamReader(`in`);
            val bufferedReader = BufferedReader(inputStreamReader);
            val sb = StringBuilder();
            var line = bufferedReader.readLine();
            UserName.setText(line);
            inputStreamReader.close()
        }else{
            UserName.setText("Bobby");
        }

        //Save the data in the User Info
        ButtonSave.setOnClickListener {
            val message = editText.text.toString();
            //TODO: Find a way to save the data so it can accessed on load
            val fileContents = message;

            context?.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it?.write(fileContents.toByteArray())
            }
            UserName.setText(message);
            activity!!.toast("Info Saved") //send verification message
        }
    }
}