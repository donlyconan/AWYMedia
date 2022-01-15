package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class VideoMediaService: Service() {

    companion object {
        val TAG = VideoMediaService.javaClass.simpleName
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}