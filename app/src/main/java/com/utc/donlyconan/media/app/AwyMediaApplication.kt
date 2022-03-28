package com.utc.donlyconan.media.app

import android.app.Application
import android.util.Log
import com.utc.donlyconan.media.data.db.AwyMediaDatabase

class AwyMediaApplication: Application() {

    val database by lazy { AwyMediaDatabase.getInstance(this) }
    val listVideoDao by lazy { database.getListVideoDao() }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "onTerminate() called")
    }

    companion object {
        val TAG = AwyMediaApplication.javaClass.simpleName
    }
}