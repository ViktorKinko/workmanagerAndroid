package com.example.test.myapplication

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "MAIN_THREAD"

/**
 * Workers are used on heavy tasks which absolutely need to be done, like saving something in background or data sync
 * Workers aren't run right away, so don't use them instead of threads or rxjava
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = applicationContext.getSharedPreferences("prefs", 0)
        findViewById<TextView>(R.id.text).text = Date(prefs.getLong(Pref.PREF_LATEST_UPDATE, 0)).toString()

        //Data setup
        val data = Data.Builder()
        data.putString("key", "value")
        //Data can contain just numbers and strings, so choose carefully.
        //It might be better to pass a database id list rather than whole array wrapped in json

        //Work constraints setup
        val myConstraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        //Allows you to enable works considering power management, phone state, network state
        //If constraints not met then work returns retry()

        //Worker setup
        //We can make OneTime work or Periodic workRequest
        //Smallest repeat interval is 900000ms which equals 15 minutes. Anything less will be set to 15m anyway
        /**        PeriodicWorkRequest.Builder(WorkerExample::class.java, 15, TimeUnit.MINUTES)*/
        val workTest = OneTimeWorkRequest.Builder(WorkerExample::class.java)
            .addTag("myWork")
            .setConstraints(myConstraints)
            .setInputData(data.build())
            //Backoff policy is optional, it defines retry behavior. Exponential by default
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 300L, TimeUnit.SECONDS)
            .build()

        //starting work
        WorkManager.getInstance()
            .enqueue(workTest) //work started!

        //Chaining example:
        /**WorkManager.getInstance()
         * .beginWith(setOf(workTest, workTest).toList())
         * .then(workTest)
         * .enqueue()
         */

        //Work observer setup. We can get current worker by id or by tag, so we don't have to manage the worker object
        WorkManager.getInstance().getWorkInfoByIdLiveData(workTest.id).observe(this, Observer { workInfo ->
            workInfo ?: return@Observer
            when (workInfo.state) {
                WorkInfo.State.ENQUEUED -> Log.d(TAG, "work enqueued")
                WorkInfo.State.RUNNING -> Log.d(TAG, "work is running")
                WorkInfo.State.BLOCKED -> Log.d(TAG, "work blocked")
                WorkInfo.State.FAILED -> Log.d(TAG, "work failed")
                WorkInfo.State.SUCCEEDED -> Log.d(TAG, "work done " + Date(workInfo.outputData.getLong("key2", 0)))
                WorkInfo.State.CANCELLED -> Log.d(TAG, "work cancelled")
            }
        })
    }

    //Log example for this worker. Note a slight delay between actual event and observer reaction
    /**
     * 10:38:02.718 MAIN_THREAD: work enqueued
     * 10:38:02.718 WORKER: doWork: start with param = value
     * 10:38:02.748 MAIN_THREAD: work is running
     * 10:38:32.718 WORKER: doWork: end
     * 10:38:32.768 MAIN_THREAD: work done Tue Apr 02 10:38:32 GMT+06:00 2019
     */
}
