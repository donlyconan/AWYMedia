package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.utc.donlyconan.media.extension.components.FOLDERS


class FileService : Service() {

    private val binder by lazy { LocalBinder() }
    private val fileObserver = @RequiresApi(Build.VERSION_CODES.Q)
    object : FileObserver(FOLDERS, FileObserver.ALL_EVENTS) {

        override fun onEvent(event: Int, path: String?) {
            Log.d(TAG, "onEvent() called with: event = $event, path = $path")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")
        fileObserver.startWatching()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        fileObserver.stopWatching()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): FileService = this@FileService
    }

}