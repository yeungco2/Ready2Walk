package com.example.cauliflower.ready2walk.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job

abstract class BaseFragment : Fragment(){

    lateinit var job1: CompletableJob //background task in coroutines
    lateinit var job2: CompletableJob //background task in coroutines

    /**override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main //using main scope this coroutine job*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job1 = Job() //create job intance
        job2 = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job1.cancel() //stop job
        job2.cancel() //stop job

    }
}