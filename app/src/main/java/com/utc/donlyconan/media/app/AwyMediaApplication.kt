package com.utc.donlyconan.media.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.utc.donlyconan.media.IMusicService
import com.utc.donlyconan.media.app.services.MusicService
import com.utc.donlyconan.media.dagger.components.ApplicationComponent
import com.utc.donlyconan.media.dagger.components.DaggerApplicationComponent
import com.utc.donlyconan.media.dagger.modules.ApplicationModule

/**
 * Represent a application of AWYMedia that provides all app's dependencies
 */
class AwyMediaApplication: Application() {
    private lateinit var appComponent: ApplicationComponent
    private var iMusicalService: IMusicService? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")

//         Create dagger component
        appComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

        // Start musical service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            Log.d(TAG, "onServiceConnected() called with: name = $name, service = $service")
            iMusicalService = IMusicService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected() called with: name = $name")
        }
    }

    /**
     * Provide Application Component
     */
    fun applicationComponent(): ApplicationComponent {
        return appComponent
    }

    fun iMusicalService(): IMusicService? {
        return iMusicalService
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "onTerminate() called")
    }

    companion object {
        val TAG: String = AwyMediaApplication.javaClass.simpleName

        private lateinit var instance: AwyMediaApplication

        // Save instance of Application
        fun getInstance() = instance
    }
}