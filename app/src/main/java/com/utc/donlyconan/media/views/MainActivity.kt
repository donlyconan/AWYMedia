package com.utc.donlyconan.media.views

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.utc.donlyconan.media.R


class MainActivity : AppCompatActivity() {

    private lateinit var listActivityEventHandler: ArrayList<ActivityEventHandler>
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listActivityEventHandler = ArrayList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        Log.d(TAG, "onPictureInPictureModeChanged() called with: isInPictureInPictureMode " +
                "= $isInPictureInPictureMode, newConfig = $newConfig")
//        Log.d(TAG, "onPictureInPictureModeChanged() called with: isInPictureInPictureMode = " +
//                "$isInPictureInPictureMode, newConfig = $newConfig")
//        if(lifecycle.currentState == Lifecycle.State.CREATED) {
//            Log.d(TAG, "onPictureInPictureModeChanged: Lifecycle.State.CREATED")
//            finish()
//        }
        for (listener in listActivityEventHandler) {
            listener.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed() called")
        if (listActivityEventHandler.isEmpty()) {
            super.onBackPressed()
        } else {
            for (listener in listActivityEventHandler) {
                if (listener.onBackPressed()) {
                    super.onBackPressed()
                }
            }
        }
    }

    fun registerActivityEventHandler(listener: ActivityEventHandler) {
        Log.d(TAG, "registerActivityEventHandler() called with: listener = $listener")
        listActivityEventHandler.add(listener)
    }

    fun unregisterActivityEventHandler(listener: ActivityEventHandler) {
        Log.d(TAG, "unregisterActivityEventHandler() called with: listener = $listener")
        listActivityEventHandler.remove(listener)
    }

    interface ActivityEventHandler {
        fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration)
        fun onBackPressed(): Boolean = true
    }

    companion object {
        val TAG: String = MainActivity.javaClass.simpleName
        const val REQUEST_READ_WRITE = 1
    }

}