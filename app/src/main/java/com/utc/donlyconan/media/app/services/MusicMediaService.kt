package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MusicMediaService: Service() {
    companion object {
        val TAG = MusicMediaService.javaClass.simpleName
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind() called with: intent = $intent")
        // TODO implemented
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}