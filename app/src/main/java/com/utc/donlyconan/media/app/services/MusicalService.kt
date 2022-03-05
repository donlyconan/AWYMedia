package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MusicalService: Service() {

    companion object {
        val TAG = MusicalService.javaClass.simpleName
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}