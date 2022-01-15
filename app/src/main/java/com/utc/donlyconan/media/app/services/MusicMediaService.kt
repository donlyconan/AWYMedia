package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

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
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}