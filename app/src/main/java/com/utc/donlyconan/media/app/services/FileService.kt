package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.Video.Media
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.utc.donlyconan.media.R
import com.utc.donlyconan.media.app.EGMApplication
import com.utc.donlyconan.media.app.utils.Logs
import com.utc.donlyconan.media.app.utils.androidFile
import com.utc.donlyconan.media.data.repo.TrashRepository
import com.utc.donlyconan.media.data.repo.VideoRepository
import com.utc.donlyconan.media.extension.components.getMediaUri
import com.utc.donlyconan.media.extension.widgets.showMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject


class FileService : Service() {

    companion object {
        const val TAG = "FileService"
        const val DELAY_BEFORE_DELETING = 5000L
    }

    private val binder by lazy { LocalBinder() }
    private val context by lazy { application }
    @Inject lateinit var videoRepository: VideoRepository
    @Inject lateinit var trashRepository: TrashRepository
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }
    private var syncJob: Job? = null
    private lateinit var listUris: MutableSet<Uri>
    private val listeners by lazy { mutableListOf<OnFileServiceListener>() }
    private var deletingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        contentResolver.registerContentObserver(Media.EXTERNAL_CONTENT_URI, false, mediaObserver)
        (application as EGMApplication)
            .applicationComponent()
            .inject(this)
        listUris = mutableSetOf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        contentResolver.unregisterContentObserver(mediaObserver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): FileService = this@FileService
    }

//    private var localFileObserver = object : FileObserver(context.filesDir, CREATE or DELETE) {
//        override fun onEvent(event: Int, path: String?) {
//            Log.d(TAG, "onEvent() called with: event = $event, path = $path")
//            when (event) {
//                CREATE -> {
//
//                }
//                DELETE -> {
//
//                }
//                else -> {
//                    Log.d(TAG, "onEvent: $event not found.")
//                }
//
//            }
//        }
//    }


    private val mediaObserver =  object : ContentObserver(Handler(Looper.myLooper()!!)){

        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
            super.onChange(selfChange, uri, flags)
            Log.d(TAG, "onChange() called with: selfChange = $selfChange, uri = $uri, flags = $flags")
        }

        override fun onChange(selfChange: Boolean, uris: MutableCollection<Uri>, flags: Int) {
            super.onChange(selfChange, uris, flags)
            Log.d(TAG, "onChange() called with: selfChange = $selfChange, uris = $uris, flags = $flags")
            // sync data from external into local data
            syncJob?.cancel()
            syncJob = coroutineScope.launch(Dispatchers.IO) {
                deletingJob?.join()
                videoRepository.sync()
            }
        }
    }
    
    private fun generateExceptionHandler() = CoroutineExceptionHandler { v, e ->
        listeners.forEach { listener -> listener.onError(e) }
        Log.d(TAG, "generateExceptionHandler() called with: v = $v, e = $e")
        coroutineScope.launch(Dispatchers.Main) {
            context.showMessage(R.string.toast_when_failed_user_action, duration = Toast.LENGTH_SHORT)
        }
    }

    @Throws(IOException::class)
    suspend fun saveIntoInternal(uri: Uri, filename: String): Pair<String, Uri> {
        Logs.d( "save() called with: uri = $uri, filename = $filename")
        val file = File(filename)
        val newFilename = handleFilename(file)
        val outputStream = context.openFileOutput(newFilename, Context.MODE_PRIVATE)
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.copyTo(outputStream)
            outputStream.flush()
            outputStream.close()
        }
        val newUri = context.getFileStreamPath(newFilename).toUri()
        return Pair(newFilename, newUri)
    }

    @Throws(IOException::class)
    suspend fun saveIntoExternal(filename: String, finish: (result: Boolean, file: File?, uri: Uri) -> Unit){
        Logs.d("removeFromInternal() called with: filename = $filename")
        val rootFolder = androidFile(Environment.DIRECTORY_DOWNLOADS)
        val file = File(rootFolder, filename)
        val newFilename = handleFilename(file)
        val newFile = File(rootFolder, newFilename)
        if (!newFile.exists()) {
            newFile.createNewFile()
        }
        val outputStream = newFile.outputStream()
        context.getFileStreamPath(filename).inputStream().use { stream ->
            stream.copyTo(outputStream)
        }
        val result = context.deleteFile(filename)
        newFile.getMediaUri(context) {
            finish(result, newFile, it)
        }
    }

    private fun handleFilename(file: File): String {
        Logs.d("handleFilename() called with: file = $file")
        var index = 1
        var filename = file.name.substringBeforeLast('.')
        var ext = file.name.substringAfterLast('.')
        var newFile = file
        while (newFile.exists()) {
            var newName = "$filename($index)"
            index++
            newFile = File("$newName.$ext")
        }
        return newFile.name
    }

    @Throws(SecurityException::class)
    private suspend fun deleteFileFromExternalStorage(vararg uris: Uri): Int {
        Log.d(TAG, "deleteFileFromExternalStorage() called with: uris = $uris")
        val operations = uris.map { uri -> ContentProviderOperation.newDelete(uri).build() }
            .toMutableList()
       return contentResolver.applyBatch(MediaStore.AUTHORITY, ArrayList(operations)).size
    }
    
    suspend fun deleteFileFromLocalData(vararg names: String): Int {
        Log.d(TAG, "deleteFileFromLocalData() called with: filenames = $names")
        return names.count { name -> context.deleteFile(name) }
    }

    /**
     * Allow to sync video between the app and android systems
     */
    fun sync() = runIO {
        if (deletingJob?.isActive == false) {
            videoRepository.sync()
        }
    }

    fun syncRecycleBin() = runIO {
        Log.d(TAG, "syncRecycleBin() called")
        if(deletingJob?.isActive == false) {
            trashRepository.sync()
        }
    }


    private fun createRequestDelete() = coroutineScope.launch(Dispatchers.IO) {
        delay(DELAY_BEFORE_DELETING)
        Log.d(TAG, "createRequestDelete: deleting files = $listUris")
        try {
            deleteFileFromExternalStorage(*listUris.toTypedArray())
            Log.d(TAG, "createRequestDelete() cleared ${listUris.size} files")
            listUris.clear()
        } catch (e: Exception) {
            Log.e(TAG, "createRequestDelete:", e)
            listeners.forEach { it.onDeletedFileError(listUris.toList(), e) }
        }
    }

    fun requestDeletingFile(uri: Uri) {
        Log.d(TAG, "requestDelete() called with: uri = $uri")
        if (deletingJob?.isActive == true) {
            deletingJob?.cancel()
        }
        listUris.add(uri)
        deletingJob = createRequestDelete()
    }

    fun registerOnFileServiceListener(listener: OnFileServiceListener) {
        listeners.add(listener)
    }

    fun unregisterOnFileServiceListener(listener: OnFileServiceListener) {
        listeners.remove(listener)
    }

    /**
     * Push task into IO thread and run
     */
    fun runIO(runnable: suspend FileService.() -> Unit) = coroutineScope.launch(Dispatchers.IO + generateExceptionHandler()) {
        runnable.invoke(this@FileService)
    }


    interface OnFileServiceListener {

        fun onDiskSizeNotEnough() {}

        fun onDeletedFileError(uris: List<Uri>,e: Exception) {}
        fun onError(e: Throwable?) {}

    }
}