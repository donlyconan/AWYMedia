package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.utc.donlyconan.media.IGoogleDrive
import com.utc.donlyconan.media.IGoogleDriveListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.sql.DriverManager

class GoogleDriveService : Service() {

    private val binder = object : IGoogleDrive.Stub() {
        override fun removeFromQueue(path: String?) {

        }

        override fun downloadFile(path: String?) {

        }

        override fun downloadAllFile(path: MutableList<String>?) {

        }

        override fun pushOnQueue(path: String?) {

        }

        override fun pushAllOnQueue(paths: MutableList<String>?) {

        }

        override fun cancel() {
            Log.d(TAG, "cancel() called")

        }

        override fun registerGoogleDriveListener(listener: IGoogleDriveListener) {
            Log.d(TAG, "registerGoogleDriveListener() called with: listener = $listener")
            listeners.add(listener)
        }

        override fun unregisterGoogleDriveListener(listener: IGoogleDriveListener) {
            Log.d(TAG, "unregisterGoogleDriveListener() called with: listener = $listener")
            listeners.remove(listener)
        }

    }

    private lateinit var driverManager: DriverManager
    private lateinit var listeners: ArrayList<IGoogleDriveListener>
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        listeners = ArrayList()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder.asBinder()
    }


    companion object {
        val TAG = GoogleDriveService::class.java.simpleName
    }

}