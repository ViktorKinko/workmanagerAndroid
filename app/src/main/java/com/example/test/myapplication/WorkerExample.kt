package com.example.test.myapplication

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

private const val TAG = "WORKER"

class WorkerExample(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    //heavy maintenance in this function
    override fun doWork(): ListenableWorker.Result {
        Log.d(TAG, "doWork: start with param = " + inputData.getString("key")!!)
        try {
            TimeUnit.SECONDS.sleep(30)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val prefs = applicationContext.getSharedPreferences("prefs", 0)
        prefs.edit().putLong(Pref.PREF_LATEST_UPDATE, System.currentTimeMillis()).apply()
        Log.d(TAG, "doWork: end")

        //Setup work output data
        val outputData = Data.Builder()
            .putString("key1", "Hello From WorkerExample")
            .putLong("key2", System.currentTimeMillis())
            .build()

        //pass result
        return ListenableWorker.Result.success(outputData)
        //we can pass failure or retry if our work wasn't finished properly
//        return ListenableWorker.Result.failure(outputData)
//        return ListenableWorker.Result.retry()
    }
}