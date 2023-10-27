package com.utc.donlyconan.media.views

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.services.FileService
import java.util.*


class MainActivity : BaseActivity() {

    private var fileService: FileService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fileServiceIntent = Intent(this, FileService::class.java)
        bindService(fileServiceIntent, fileServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private val fileServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            Log.d(EGMApplication.TAG, "onServiceConnected() called with: name = $name, binder = $binder")
            fileService = (binder as? FileService.LocalBinder)?.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(EGMApplication.TAG, "onServiceDisconnected() called with: name = $name")
        }
    }

    companion object {
        val TAG: String = MainActivity.javaClass.simpleName
    }

}