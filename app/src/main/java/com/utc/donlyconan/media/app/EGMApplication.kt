package com.utc.donlyconan.media.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.utc.donlyconan.media.app.services.EGMService
import com.utc.donlyconan.media.app.workmanager.TrashRemovalWorker
import com.utc.donlyconan.media.dagger.components.ApplicationComponent
import com.utc.donlyconan.media.dagger.components.DaggerApplicationComponent
import com.utc.donlyconan.media.dagger.modules.ApplicationModule
import java.util.concurrent.TimeUnit

/**
 * Represent a application of AWYMedia that provides all app's dependencies
 */
class EGMApplication: Application() {
    private lateinit var appComponent: ApplicationComponent
    private var service: EGMService? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")

//         Create dagger component
        appComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

        // Start musical service
        val intent = Intent(this, EGMService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        var myWorker =  PeriodicWorkRequestBuilder<TrashRemovalWorker>(1, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("CLEAN", ExistingPeriodicWorkPolicy.KEEP, myWorker)
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            Log.d(TAG, "onServiceConnected() called with: name = $name, binder = $binder")
            val egmBinder = binder as? EGMService.EGMBinder
            service = egmBinder?.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected() called with: name = $name")
        }
    }

    fun getEgmService(): EGMService? {
        return service
    }

    /**
     * Provide Application Component
     */
    fun applicationComponent(): ApplicationComponent {
        return appComponent
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "onTerminate() called")
    }

    companion object {
        val TAG: String = EGMApplication.javaClass.simpleName

        private lateinit var instance: EGMApplication

        // Save instance of Application
        fun getInstance() = instance
    }
}