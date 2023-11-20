package com.utc.donlyconan.media.app

import android.app.Application
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.utc.donlyconan.media.app.manager.TrashRemovalWorker
import com.utc.donlyconan.media.app.services.AudioService
import com.utc.donlyconan.media.app.services.FileService
import com.utc.donlyconan.media.dagger.components.ApplicationComponent
import com.utc.donlyconan.media.dagger.components.DaggerApplicationComponent
import com.utc.donlyconan.media.dagger.modules.ApplicationModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Represent a application of AWYMedia that provides all app's dependencies
 */
class EGMApplication: Application() {
    private lateinit var appComponent: ApplicationComponent
    private var audioService: AudioService? = null
    private var fileService: FileService? = null
    private var listener: OnInitializedService? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")

//      Create dagger component
        appComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

        // Start musical service
        val intent = Intent(this, AudioService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        // Register work manager
        var periodicWorkRequest =  PeriodicWorkRequestBuilder<TrashRemovalWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest)


        connectToFileService()
    }



    private val fileServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            Log.d(TAG, "onServiceConnected() called with: name = $name, binder = $binder")
            fileService = (binder as? FileService.LocalBinder)?.getService()
            listener?.onServiceConnected(fileService)
    }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected() called with: name = $name")
            connectToFileService()
            listener?.onServiceDisconnected(fileService)
        }
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            Log.d(TAG, "onServiceConnected() called with: name = $name, binder = $binder")
            val egmBinder = binder as? AudioService.EGMBinder
            audioService = egmBinder?.getService()
            listener?.onServiceConnected(audioService)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected() called with: name = $name")
            listener?.onServiceDisconnected(audioService)
        }
    }

    private fun connectToFileService() {
        Log.d(TAG, "connectToFileService() called")
        val fileServiceIntent = Intent(this, FileService::class.java)
        bindService(fileServiceIntent, fileServiceConnection, Context.BIND_AUTO_CREATE)
    }

    fun getFileService(): FileService? {
        return fileService
    }

    fun getAudioService(): AudioService? {
        return audioService
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

    fun setOnInitializedService(onInitializedService: OnInitializedService) {
        Log.d(TAG, "setOnInitializedService() called with: onInitializedService = $onInitializedService")
        listener = onInitializedService
    }

    companion object {
        val TAG: String = EGMApplication.javaClass.simpleName

        const val WORK_TAG = "CLEAN_YOUR_TRASH"
        const val REPEATED_INTERVAL_TIME = 15L

    }

    interface OnInitializedService {
        fun onServiceConnected(service: Service?) {}
        fun onServiceDisconnected(service: Service?) {}
    }
}