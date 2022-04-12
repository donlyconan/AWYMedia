package com.utc.donlyconan.media.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.utc.donlyconan.media.IGoogleDrive
import com.utc.donlyconan.media.IGoogleDriveListener
import com.utc.donlyconan.media.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*
import kotlin.collections.ArrayList


class GoogleDriveService : Service() {

    companion object {
        val TAG = GoogleDriveService::class.java.simpleName
        const val FOLDER_NAME = "Awy-Media"
    }

    private val binder = object : IGoogleDrive.Stub() {

        private fun createFolderIfNotExists() {
            Log.d(TAG, "createFolderIfNotExists() called")
            service.files().list().contains(FOLDER_NAME)
            val fileMetaData = File()
            fileMetaData.name = FOLDER_NAME
            fileMetaData.mimeType = "application/vnd.google-apps.folders"
            val file = service.files().create(fileMetaData)
                .setFields("id")
                .execute()
            Log.d(TAG, "createFolderIfNotExists: folderId=${file.id}")
        }

        override fun removeFromQueue(path: String) {
            Log.d(TAG, "removeFromQueue() called with: path = $path")
            fileUris.remove(path)
        }

        override fun downloadFile(path: String) {
            Log.d(TAG, "downloadFile() called with: path = $path")

        }

        override fun downloadAllFile(path: MutableList<String>) {
            Log.d(TAG, "downloadAllFile() called with: path = $path")
        }

        override fun pushOnQueue(path: String) {
            Log.d(TAG, "pushOnQueue() called with: path = $path")
            fileUris.push(path)
        }

        override fun pushAllOnQueue(paths: MutableList<String>) {
            Log.d(TAG, "pushAllOnQueue() called with: paths = $paths")
            fileUris.addAll(paths)
        }

        override fun cancel() {
            Log.d(TAG, "cancel() called")
        }

        override fun upload() {
            Log.d(TAG, "upload() called")
            while(fileUris.isNotEmpty()) {
                val folderId = "0BwwA4oUTeiV1TGRPeTVjaWRDY1E";
                val fileMetadata = File()
                fileMetadata.name = "photo.jpg"
                fileMetadata.parents = Collections.singletonList(folderId)
                val filePath = java.io.File("files/photo.jpg")
                val mediaContent = FileContent("image/jpeg", filePath)
                val file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute()
                Log.d(TAG, "upload: File ID=${file.id}")
            }
        }

        override fun registerGoogleDriveListener(listener: IGoogleDriveListener) {
            Log.d(TAG, "registerGoogleDriveListener() called with: listener = $listener")
            listeners.add(listener)
        }

        override fun unregisterGoogleDriveListener(listener: IGoogleDriveListener) {
            Log.d(TAG, "unregisterGoogleDriveListener() called with: listener = $listener")
            listeners.remove(listener)
        }
    }

    private lateinit var service: Drive
    private lateinit var credential: GoogleAccountCredential
    private lateinit var listeners: ArrayList<IGoogleDriveListener>
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var fileUris: LinkedList<String>

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        service = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        ).setApplicationName(getString(R.string.app_name))
            .build()
        fileUris = LinkedList()
        listeners = ArrayList()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder.asBinder()
    }

}