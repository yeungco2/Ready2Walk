package com.example.cauliflower.ready2walk.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.cauliflower.ready2walk.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //find id of naviagtion host in this case main activity xml homeFragment
        val navController = Navigation.findNavController(this, R.id.homeFragment)
        NavigationUI.setupActionBarWithNavController(this,navController)
        }
    //find navigation host
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            Navigation.findNavController(this, R.id.homeFragment),
            null
        )
    }

}
