package com.utc.donlyconan.media.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.utc.donlyconan.media.IMusicalService
import com.utc.donlyconan.media.app.services.MusicalService
import com.utc.donlyconan.media.app.settings.Settings
import com.utc.donlyconan.media.data.dao.VideoDao
import com.utc.donlyconan.media.data.db.AwyMediaDatabase
import com.utc.donlyconan.media.data.repo.ListVideoRepositoryImpl
import com.utc.donlyconan.media.data.repo.VideoRepositoryImpl

class AwyMediaApplication: Application() {

    val database by lazy { AwyMediaDatabase.getInstance(this) }
    val listVideoDao by lazy { database.listVideoDao() }
    val videoDao by lazy { database.videoDao() }
    val videoRepo by lazy { VideoRepositoryImpl(videoDao) }
    val lstVideoRepo by lazy { ListVideoRepositoryImpl(this, listVideoDao) }

    var iMusicalService: IMusicalService? = null
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        Settings.getInstance(this)
        instance = this

        // Start musical service
        val intent = Intent(this, MusicalService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            Log.d(TAG, "onServiceConnected() called with: name = $name, service = $service")
            iMusicalService = IMusicalService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected() called with: name = $name")
        }

    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "onTerminate() called")
    }

    companion object {
        val TAG = AwyMediaApplication.javaClass.simpleName

        private lateinit var instance: AwyMediaApplication

        // Save instance of Application
        fun getInstance() = instance
    }
}