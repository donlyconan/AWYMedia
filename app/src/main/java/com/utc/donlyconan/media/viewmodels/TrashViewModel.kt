package com.utc.donlyconan.media.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.utc.donlyconan.media.data.models.Trash
import com.utc.donlyconan.media.views.fragments.TrashFragment.Companion.TAG
import kotlinx.coroutines.launch
import java.io.File

class TrashViewModel(val app: Application) : BaseAndroidViewModel(app) {
    private val trashRepo = myApp.applicationComponent().getTrashRepo()
    private val videoRepo = myApp.applicationComponent().getVideoRepo()
    private val playlistRepo = myApp.applicationComponent().getPlaylistRepo()
    private val settings = myApp.applicationComponent().getSettings()
    private val contentResolver = app.contentResolver
    val videoList = trashRepo.getTrashes()

    fun restore(trash: Trash) {
        Log.d(TAG, "restore() called with: trash = $trash")
        val video = trash.toVideo()
        videoRepo.insert(video)
        trashRepo.delete(trash)
    }

    fun delete(trash: Trash) {
        Log.d(TAG, "delete() called with: trash = $trash")
        trashRepo.delete(trash)
        viewModelScope.launch {
            playlistRepo.removeVideoFromPlaylist(trash.videoId)
            Log.d(TAG, "clearAll: deleteFromStorage=" + settings.deleteFromStorage)
            if(settings.deleteFromStorage) {
                contentResolver.delete(Uri.parse(trash.path), null, null)
            }
            Log.d(TAG, "clearAll: Done!")
        }
    }

    fun clearAll(trashes: List<Trash>) {
        Log.d(TAG, "clearAll() called size=${trashes.size}")
        viewModelScope.launch {
            trashes.forEach { trash ->
                playlistRepo.removeVideoFromPlaylist(trash.videoId)
                Log.d(TAG, "clearAll: deleteFromStorage=" + settings.deleteFromStorage)
                if(settings.deleteFromStorage) {
                    contentResolver.delete(Uri.parse(trash.path), null, null)
                }
            }
            trashRepo.removeAll()
            Log.d(TAG, "clearAll: Done!")
        }
    }
}