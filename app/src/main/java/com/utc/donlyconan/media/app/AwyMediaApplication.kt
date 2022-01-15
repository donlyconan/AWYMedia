package com.donly.sradatn.app

import android.app.Application
import android.util.Log

class AwyMediaApplication: Application() {
    companion object {
        val TAG = AwyMediaApplication.javaClass.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
    }


    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "onTerminate: ")
    }
    
}